package com.geeetech.administrator.easyprint.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geeetech.administrator.easyprint.Internet.SocketItem;
import com.geeetech.administrator.easyprint.Internet.TCPSocket;
import com.geeetech.administrator.easyprint.Internet.WSItem;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.rabtman.wsmanager.WsManager;
import com.rabtman.wsmanager.listener.WsStatusListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okio.ByteString;

/**
 * Created by Administrator on 2018-09-18.
 */

public class Myminifactory extends BaseActivity {

    private static final int FILE_SELECT_CODE = 0;

    private WebView mTest;    //webview测试
    private TextView mText;
    EditText mIpConfig;
    EditText mIpPort;
    EditText mMessage;
    Button mSend;
    Button mSendByte;
    Button mConnect;
    Button mChooseFile;
    Button mUpload;
    TextView mFileName;
    boolean isConnect=true;//连接还是断开
    TextView mShowMessage;
    TextView mShowTime;
    public static Socket socket = null;//定义socket
    public static WsManager ws = null;
    private OutputStream outputStream = null;//定义输出流
    private InputStream inputStream = null;//定义输入流
    public boolean mState = true;
    //上传的位置
    private int fileSendLocation = -1;
    private byte[] allBuffer = new byte[0];
    private byte[] nextBuffer = new byte[0];
    private byte[] currentBuffer = new byte[0];
    //选中的文件
    private String needFile = "";

    private File mFile;
    //文件路径及名称
    String path = "";
    String name = "";
    //多线程
    //默认上传长度
    int fileSendLen = 4096;
    public static List<SocketItem> socketGroup = new ArrayList<>();
    public static List<WSItem> wsSocketGroup = new ArrayList<>();
    //ws
    public WsManager wsManager;
    public static int isReceive = -1; //连接状态 0：未连接 1：连接超时 -1：未连接
    public int reconnectTimes = 0; //重连次数
    public boolean isClickConnect = false;
    public static Context isContext;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minifactory);
        isContext = getBaseContext();
        mTest = findViewById(R.id.stl_webview);
        mText = findViewById(R.id.text_view);

        mIpConfig = findViewById(R.id.ip_config);
        mIpPort = findViewById(R.id.ip_port);
        mMessage = findViewById(R.id.txt_message);
        mSend = findViewById(R.id.send_message);
        mSendByte = findViewById(R.id.send_byte);
        mConnect = findViewById(R.id.connect);
        mShowMessage = findViewById(R.id.show_message);
        mShowTime = findViewById(R.id.show_time);
        mChooseFile = findViewById(R.id.choose_file);
        mFileName = findViewById(R.id.file_name);
        mUpload = findViewById(R.id.upload_file);

        mTest.loadUrl("https://auth.myminifactory.com/web/authorize?client_id=easyprint_3d&redirect_uri=geeetech://mymini.app/callback&response_type=code&state=kjfgierwgn");
        //mTest.loadUrl("https://auth.myminifactory.com/web/login?client_id=easyprint_3d&login_redirect_uri=/web/authorize&redirect_uri='geeetech://mymini.app/callback'&response_type=code&state=123");
        WebSettings settings = mTest.getSettings();
        //打开js和安卓通信
        settings.setJavaScriptEnabled(true);
        mTest.setWebViewClient(new WebViewClient() {
            /*@Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ToastUtil.showToast(getBaseContext(),url);
                if (url.startsWith("http") || url.startsWith("https")){
                    if (url.contains("code")){
                        mText.setVisibility(View.VISIBLE);
                        mText.setText(url);
                    }
                }else{
                    Intent mymini = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (mymini.resolveActivity(getPackageManager()) != null){
                        //mymini.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        startActivity(mymini);
                    }
                }
            }*/
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 是否使用第三方浏览器 返回true不调用 返回false调用
                //view.loadUrl("https://auth.myminifactory.com/web/login?client_id=easyprint_3d&login_redirect_uri=/web/authorize&redirect_uri='http://47.254.41.125:5000/v1/app/heartbeat'&response_type=code&state=123");
                //ToastUtil.showToast(getBaseContext(),url);
                if (url.startsWith("http") || url.startsWith("https")){
                    return false;
                } else if (url.contains("code=")){
                    mTest.loadUrl("https://auth.myminifactory.com/web/authorize?client_id=easyprint_3d&redirect_uri=geeetech://mymini.app/callback&response_type=token&state=kjfgierwgn");
                    return true;
                } else{
                    Intent mymini = new Intent(Intent.ACTION_VIEW, Uri.parse("geeetech://mymini.app/callback"));
                    if (mymini.resolveActivity(getPackageManager()) != null){
                        //mymini.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        mymini.putExtra("url",url);
                        startActivity(mymini);
                    }
                    return true;
                }
            }
            /*@Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return null;
            }*/
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socketGroup.isEmpty()){
                    ToastUtil.showToast(getBaseContext(),"no socket connect");
                    return;
                }
                /*String message = "";
                mFileName.setText(message);
                for (int i=0;i<socketGroup.size();i++){
                    message = i + ": " + socketGroup.get(i).getMessage();
                    String a = "message:" + socketGroup.get(i).getMessage() + "state:" + socketGroup.get(i).getState() + "\n" + "fileName:" + socketGroup.get(i).getFileName() + "\n" + "present:" + socketGroup.get(i).getPrecent();
                    mFileName.setText(a + "\n" + "extruderTemp:" + socketGroup.get(i).getcExtruderTemp() + "\n" +"bedTemp:" + socketGroup.get(i).getcBedTemp());
                }*/
                String need = mMessage.getText().toString();
                if (socket == null){
                    ToastUtil.showToast(getBaseContext(),"please connect a socket");
                    return;
                }
                send(need);
            }
        });
        mSendByte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int need = 4096;
                try{
                    need = Integer.valueOf(mMessage.getText().toString());
                }catch (Exception e){
                    e.printStackTrace();
                    mMessage.setText("4096");
                }
                fileSendLen = need;
                //sendByte(need);
            }
        });
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClickConnect){
                    ToastUtil.showToast(getBaseContext(),"is connecting...");
                    return;
                }
                isClickConnect = true;
                try{
                    String ipAddress = mIpConfig.getText().toString();
                    int port =Integer.valueOf(mIpPort.getText().toString());
                    //ToastUtil.showToast(getBaseContext(), "connect size:" + socketGroup.size());
                    if(socketGroup.size() >= 5 ){
                        ToastUtil.showToast(getBaseContext(),"too many socket " + socketGroup.get(4).getSocket());
                        return;
                    }
                    for (int i=0;i<Myminifactory.socketGroup.size();i++){
                        if (Myminifactory.socketGroup.get(i).getIp().equals(ipAddress) ){
                            //socket = socketGroup.get(i).getSocket();
                            ToastUtil.showToast(getBaseContext(),"socket is exist..." + socketGroup.get(i).getConnectState());
                            if (!socketGroup.get(i).getConnectState()){
                                TCPSocket socket1 = new TCPSocket(getBaseContext(),ipAddress,port);
                                if (isReceive == -1){
                                    receive(ipAddress,port);
                                }
                                ToastUtil.showToast(getBaseContext(),"socket is reconnecting...");
                            }
                            return;
                        }
                    }
                    TCPSocket socket1 = new TCPSocket(getBaseContext(),ipAddress,port);
                    if (isReceive == -1){
                        //ToastUtil.showToast(getBaseContext(),"receive start...");
                        receive(ipAddress,port);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(getBaseContext(),"connect fail");
                }
                /*mState = true;
                socket = null;
                conn(mState);*/
            }
        });

        mChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType(“image/*”);//选择图片
                //intent.setType(“audio/*”); //选择音频
                //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType(“video/*;image/*”);//同时选择视频和图片
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getBaseContext(), "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
                }
                //startActivityForResult(intent, 1);
            }
        });
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    fileSendLocation = -1;
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
                        if (ActivityCompat.shouldShowRequestPermissionRationale(Myminifactory.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            ToastUtil.showToast(getBaseContext(),"Please open the related permission, otherwise the application can not be used normally! ");
                        } else {
                            ActivityCompat.requestPermissions(Myminifactory.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                        return;
                    }
                    upload();
                }catch (Exception e){
                    e.printStackTrace();
                    mShowMessage.setText("file upload fail.");
                }
            }
        });
    }
    /**
     * 发送消息
     */
    //普通文本
    public void send(final String need) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                    int len = need.getBytes().length;
                    byte[] list = need.getBytes();
                    byte[] needBuffer = new byte[len+3];
                    byte[] filelen = IntToByte(len);
                    byte end = Integer.valueOf("82", 16).byteValue();
                    if (need.equals("hi new user")||need.equals("reconnect user")){
                        end = Integer.valueOf("83", 16).byteValue();
                    }
                    needBuffer[0] = end;
                    needBuffer[1] = filelen[2];
                    needBuffer[2] = filelen[3];
                    for (int i=0;i<len;i++){
                        needBuffer[i+3] = list[i];
                    }
                    writer.write(needBuffer);
                    writer.flush();
                    if (need.equals("hi new user")||need.equals("reconnect user")){

                    }else{
                        mShowMessage.setText("Array:" + Arrays.toString(needBuffer) + " send:" + need);
                    }
                    System.out.println("发送消息:" + need);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(need + " send fail\n");
                    //mShowMessage.setText(need + " send fail\n");
                }
            }
        }.start();
    }
    //文件数据流
    public void sendByte(final byte[] need) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                    byte[] slipt = new byte[1460];
                    byte[] filelen = IntToByte(1457);
                    byte start = Integer.valueOf("80", 16).byteValue();
                    slipt[0] = start;
                    slipt[1] = filelen[2];
                    slipt[2] = filelen[3];
                    int size = need.length/1457;
                    for (int i=0;i<=size;i++){
                        if (i == size){
                            int lenth = need.length-1457*i;
                            if(lenth != 0){
                                slipt = new byte[lenth+3];
                                byte[] fileEnd = IntToByte(lenth);
                                byte end = Integer.valueOf("81", 16).byteValue();
                                slipt[0] = end;
                                slipt[1] = fileEnd[2];
                                slipt[2] = fileEnd[3];
                                System.arraycopy(need, i*1457, slipt,3, lenth);
                            }
                        }else{
                            System.arraycopy(need, i*1457, slipt,3, 1457);
                        }
                        writer.write(slipt);
                    }
                    //writer.write();
                    writer.flush();
                    mShowMessage.setText("偏移地址：" + fileSendLen*fileSendLocation + "send:" + Arrays.toString(slipt));
                    System.out.println("发送上传文件");
                } catch (Exception e) {
                    e.printStackTrace();
                    mShowMessage.setText(mShowMessage.getText() + " send byte fail\n");
                }
            }
        }.start();
    }
    //int 转 byte[]
    public static byte[] IntToByte(int num){
        byte[]bytes=new byte[4];
        bytes[0]=(byte) ((num>>24)&0xff);
        bytes[1]=(byte) ((num>>16)&0xff);
        bytes[2]=(byte) ((num>>8)&0xff);
        bytes[3]=(byte) (num&0xff);
        return bytes;
    }
    public void sendBuffer(final byte[] need) {
        //new Thread() {
            //@Override
            //public void run() {
                try {
                    //DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                    int len = need.length;
                    if (fileSendLocation == -1){
                        String message = "M2110 start";
                        send(message);
                        //writer.write(need.getBytes());
                        //timeTest = true;
                        mShowTime.setText(new Date() + "");
                    }else{
                        if (fileSendLen*fileSendLocation >= len){
                            fileSendLocation = -1;
                            needFile = "";
                            nextBuffer = new byte[0];
                            currentBuffer = new byte[0];
                            String message = "M2110 send end";
                            send(message);
                            //writer.write(need.getBytes());
                            //timeTest = false;
                            mShowTime.setText(mShowTime.getText() + "***" + new Date() + "" + "\n" + fileSendLen*fileSendLocation + " file len: " + len);
                        }else{
                            if (nextBuffer.length != 0){
                                sendByte(nextBuffer);
                                //writer.write(nextBuffer);
                                nextBuffer = getBuffer(len,need,"next");
                                return;
                            }
                            currentBuffer = getBuffer(len,need,"");
                            sendByte(currentBuffer);
                            //writer.write(currentBuffer);
                            nextBuffer = getBuffer(len,need,"next");
                        }
                    }
                    //writer.flush();
                    //mShowMessage.setText(mMessage.getText().toString() + " send");
                    System.out.println("发送消息");
                } catch (Exception e) {
                    e.printStackTrace();
                    //mShowMessage.setText(mShowMessage.getText() + "" + mMessage.getText().toString() + " send fail\n");
                }
        //    }
        //}.start();
    }
    public byte[] getBuffer(int len, byte[] need, String location){
        byte[] nullBuffer = new byte[0];
        int nextLocation = fileSendLocation;
        if (location.equals("next")){
            nextLocation = fileSendLocation + 1;
        }
        if (fileSendLen*(nextLocation+1)>len){
            try{
                byte[] fileBuffer = new byte[len-fileSendLen*nextLocation];
                System.arraycopy(need, fileSendLen*nextLocation, fileBuffer,0, len-fileSendLen*nextLocation);
                String fileString = new String(fileBuffer);
                String needString = "M2110  SD0 " + name + ";" + fileSendLen*nextLocation + ";" + (len-fileSendLen*nextLocation) + ";" + fileString;
                byte buffer[] = needString.getBytes();
                int crc = crc16(buffer);

                int len1 = buffer.length;
                int crc1 = (crc >> 8) & 0xff;
                int crc2 = crc & 0xff;
                byte[] needBuffer = new byte[len1+3];
                try{
                    for (int i=0;i<len1;i++){
                        needBuffer[i] = buffer[i];
                    }
                    needBuffer[len1] = (byte)crc1;
                    needBuffer[len1+1] = (byte)crc2;
                    byte end = Integer.valueOf("0A", 16).byteValue();
                    needBuffer[len1+2] = end;
                }catch(Exception e){
                    mFileName.setText("buffer length error");
                }
                return needBuffer;
            }catch (Exception e){
                return nullBuffer;
            }
        }else {
            try{
                byte[] fileBuffer = new byte[fileSendLen];
                System.arraycopy(need, fileSendLen*nextLocation, fileBuffer,0, fileSendLen);
                String fileString = new String(fileBuffer);
                String needString = "M2110  SD0 " + name + ";" + fileSendLen*nextLocation + ";" + fileSendLen + ";" + fileString;
                byte buffer[] = needString.getBytes();
                int crc = crc16(buffer);
                int len1 = buffer.length;
                int crc1 = (crc >> 8) & 0xff;
                int crc2 = crc & 0xff;
                byte[] needBuffer = new byte[len1+3];
                try{
                    for (int i=0;i<len1;i++){
                        needBuffer[i] = buffer[i];
                    }
                    needBuffer[len1] = (byte)crc1;
                    needBuffer[len1+1] = (byte)crc2;
                    byte end = Integer.valueOf("0A", 16).byteValue();
                    needBuffer[len1+2] = end;
                }catch(Exception e){
                    mFileName.setText("buffer length error");
                }
                return needBuffer;
            }catch (Exception e){
                return nullBuffer;
            }
        }
    }
    /**
     * 接受消息
     */
    public void receive(final String ip,final int port) {
        new Thread() {
            @Override
            public void run() {
                try
                {
                    while (true)
                    {
                        System.out.println("isReceive：" + isReceive);
                        if (isReceive == 1) {
                            isReceive = -1;
                            isClickConnect = false;
                            System.out.println("connect time out.");
                            ToastUtil.showToast(getBaseContext(),"connect time out.");
                            return;
                        }else if (isReceive == 0){
                            int len = 0;
                            byte[] buffer = new byte[1024];
                            try{
                                //final byte[] buffer = new byte[1024];//创建接收缓冲区
                                //inputStream = socket.getInputStream();
                                socket = socketGroup.get(0).getSocket();
                                socket.setSoTimeout(3000);
                                inputStream = new DataInputStream( socket.getInputStream());
                                len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                reconnectTimes = 0;
                            }catch (Exception e){
                                reconnectTimes ++;
                                e.printStackTrace();
                            }
                            System.out.println("数据长度：" + len);
                            if (len>0){
                                try{
                                    String res = new String(buffer,0,len);
                                    if (!res.contains("live")){
                                        mFileName.setText(res);
                                    }
                                    Myminifactory.socketGroup.get(0).setConnectState(true);
                                    Myminifactory.socketGroup.get(0).setMessage(res);
                                    send("hi new user");

                                    if (res.contains("M2110")&&res.contains("start")){
                                        fileSendLocation = 0;
                                        nextBuffer = new byte[0];
                                        sendBuffer(allBuffer);
                                    } else if (res.contains("M2110")&&res.contains("ok")&&fileSendLocation >= 0){
                                        String[] a = res.split(" ");
                                        int b = Integer.valueOf(a[1]);
                                        if ( b != fileSendLen*fileSendLocation){
                                            ToastUtil.showToast(getBaseContext(),"偏移地址错误" + fileSendLen);
                                        }else{
                                            fileSendLocation ++;
                                            sendBuffer(allBuffer);
                                        }
                                    } else if(res.contains("M2110")&&res.contains("fail")){
                                        nextBuffer = new byte[0];
                                        //currentBuffer = new byte[0];
                                        sendBuffer(allBuffer);
                                        mFileName.setText(res);
                                    } else if (res.contains("M2110")&&res.contains("stop")){
                                        nextBuffer = new byte[0];
                                        currentBuffer = new byte[0];
                                        try{
                                            ToastUtil.showToast(getBaseContext(),"upload fail.");
                                        }catch (Exception e){
                                            mFileName.setText("upload fail.");
                                        }
                                    } else if (res.contains("M2110")&&res.contains("Err")){
                                        nextBuffer = new byte[0];
                                        currentBuffer = new byte[0];
                                        try{
                                            ToastUtil.showToast(getBaseContext(),"upload fail.");
                                        }catch (Exception e){
                                            mFileName.setText("upload fail.");
                                        }
                                    }
                                    //mShowMessage.setText(fileSendLocation + "");
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                //System.out.println(new String(buffer,0,len));
                            }else{
                                if (reconnectTimes > 3){
                                    reconnectTimes = 0;
                                    System.out.println("socket is disconnected");
                                    InetAddress ipAddress = InetAddress.getByName(ip);
                                    try{
                                        Myminifactory.socketGroup.get(0).setConnectState(false);
                                        socket = new Socket();
                                        SocketAddress socAddress = new InetSocketAddress(ipAddress, port);
                                        socket.connect(socAddress, 3000);//超时3秒
                                        isReceive = 0;
                                        for (int i=0;i<Myminifactory.socketGroup.size();i++){
                                            if (Myminifactory.socketGroup.get(i).getIp().equals(ip) ){
                                                Myminifactory.socketGroup.get(i).setSocket(socket);
                                                send("reconnect user");
                                                break;
                                            }
                                        }
                                    }catch (Exception e1){
                                        isReceive = 1;
                                        Myminifactory.socketGroup.get(0).setConnectState(false);
                                        ToastUtil.showToast(getBaseContext(),"reconnect fail.");
                                        System.out.println("reconnect fail.");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isReceive = -1;
                    isClickConnect = false;
                    ToastUtil.showToast(getBaseContext(),"socket:" + socket);
                }
            }
        }.start();
    }

    public void upload(){
        try{
            if (path.equals("")){
                ToastUtil.showToast(getBaseContext(),"please choose a file.");
                return;
            }
            allBuffer = readFileToBuffer(path);
            sendBuffer(allBuffer);
        }catch (Exception e){
            e.printStackTrace();
            mShowMessage.setText("send error");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                path = uri.getPath();
                String[] keyValue = path.split("/");
                name = keyValue[keyValue.length - 1];
                mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + path, name);
                mFileName.setText(path + "  " + name);
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                try{
                    path = getPath(this, uri);
                }catch (Exception e){
                    mFileName.setText("get file error");
                    return;
                }
                String[] keyValue = path.split("/");
                name = keyValue[keyValue.length - 1];
                mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + path, name);
                mFileName.setText(path + "  " + name);
            } else {//4.4以下下系统调用方法
                try{

                }catch (Exception e){
                    mFileName.setText("get file error1");
                }
                path = getRealPathFromURI(uri);
                String[] keyValue = path.split("/");
                name = keyValue[keyValue.length - 1];
                mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + path, name);
                mFileName.setText(path + "  " + name);
            }
        }
    }

    public String readFileSdcardFile(String fileName) throws IOException {
        String res = "";
        try{
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer, "UTF-8");
            fin.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    public byte[] readFileToBuffer(String fileName) throws IOException {
        byte[] res = new byte[0];
        try{
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = buffer;
            fin.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null != cursor && cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) throws Exception {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /*public int crc16( byte[] d) throws Exception
    {
        int  b  =  0 ;
        int  crc  =   0xffff ;
        int  i, j;
        for (i = 0 ; i < d.length; i ++ )
        {
            for (j = 0 ; j < 8 ; j ++ )
            {
                b =  (((d[i] << j) & 0x80) ^ ((crc & 0x8000) >> 8))&0xff;
                crc <<= 1;
                if (b != 0 )
                    crc ^= 0x1021 ;
            }
        }
        // crc = (ushort)~crc;
        crc =  ~(crc)&0xffff;
        return crc;
    };*/

    //ws连接
    public void connectWs(String ip,String port){
        /*String address = ip + ":" + port;
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .pingInterval(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        wsManager = new WsManager.Builder(this)
                .wsUrl("ws://" + address + "/")
                .client(okHttpClient)
                .needReconnect(false)
                .build();

        ws.startConnect();*/
        ws.setWsStatusListener(new WsStatusListener() {
            /*@Override
            public void onOpen(okhttp3.Response response) {
                super.onOpen(response);
                String need = "test";
                ws.sendMessage(need);
            }*/

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
                try{
                    String res = text;
                    if (res.contains("M2110")&&res.contains("start")){
                        fileSendLocation = 0;
                        nextBuffer = new byte[0];
                        sendBuffer(allBuffer);
                    } else if (res.contains("M2110")&&res.contains("ok")&&fileSendLocation >= 0){
                        String[] a = res.split(" ");
                        int b = Integer.valueOf(a[1]);
                        if ( b != fileSendLen*fileSendLocation){
                            ToastUtil.showToast(getBaseContext(),"偏移地址错误" + fileSendLen);
                        }else{
                            fileSendLocation ++;
                            sendBuffer(allBuffer);
                        }
                    } else if(res.contains("M2110")&&res.contains("fail")){
                        nextBuffer = new byte[0];
                        sendBuffer(allBuffer);
                        mFileName.setText(res);
                    } else if (res.contains("M2110")&&res.contains("stop")){
                        nextBuffer = new byte[0];
                        currentBuffer = new byte[0];
                        try{
                            ToastUtil.showToast(getBaseContext(),"upload fail.");
                        }catch (Exception e){
                            mFileName.setText("upload fail.");
                        }
                    } else if (res.contains("M2110")&&res.contains("Err")){
                        nextBuffer = new byte[0];
                        currentBuffer = new byte[0];
                        try{
                            ToastUtil.showToast(getBaseContext(),"upload fail.");
                        }catch (Exception e){
                            mFileName.setText("upload fail.");
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
            }

            @Override
            public void onReconnect() {
                super.onReconnect();
            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);

            }

            @Override
            public void onFailure(Throwable t, okhttp3.Response response) {
                super.onFailure(t, response);

            }
        });
    }
}