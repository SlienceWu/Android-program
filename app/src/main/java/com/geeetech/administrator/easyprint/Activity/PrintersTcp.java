package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.geeetech.administrator.easyprint.Internet.SocketItem;
import com.geeetech.administrator.easyprint.Internet.TCPSocket;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.ListTcpAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListTcpAdapter.Callback;
import com.geeetech.administrator.easyprint.StoreData.ListTcpItem;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018-12-03.
 */

public class PrintersTcp extends BaseActivity implements OnItemClickListener,
        Callback {
    public static List<SocketItem> socketGroup = new ArrayList<>(); //定义tcp打印机连接列表
    public static Socket socket = null;//定义socket
    public static int isReceive = -1; //连接状态 0：未连接 1：连接超时 -1：未连接
    public static int mCurrentPrinter = 0; //默认列表内第一个为当前打印机
    public static int sendTimes = 0;   //发送命令次数
    public static boolean isWhile = true;   //线程循环接受消息
    public static Context isContext;
    //选中的文件
    public static String path = "";
    public static String name = "";

    public int reconnectTimes = 0; //重连次数
    private InputStream inputStream = null;//定义输入流
    //上传的位置
    public static int fileSendLocation = -1;
    public static byte[] allBuffer = new byte[0];
    public static byte[] nextBuffer = new byte[0];
    public static byte[] currentBuffer = new byte[0];
    //默认上传长度
    public static int fileSendLen = 4096;

    public EditText mIp;
    public EditText mPort;
    public Button mConnect;
    //定时查询打印机列表及状态
    public Runnable runnable;
    public static Handler handler = new Handler();
    private List<ListTcpItem> listState = new ArrayList<>();
    private static Handler handlerListen;
    public Date startTime;
    public static Date endTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_connect);
        isContext = getBaseContext();
        mIp = findViewById(R.id.ip);
        mPort = findViewById(R.id.port);
        mConnect = findViewById(R.id.button_connect);
        runnable = new Runnable() {
            @Override
            public void run() {
                getListState();
                handler.postDelayed(this,1000);
            }
        };
        handlerListen = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        //在这里可以进行UI操作
                        //mText.setText("HelloJohnnyZhou");
                        int progress = fileSendLen*fileSendLocation/allBuffer.length*100;
                        Log.d("PrintersTcp","*********************** progress:" + progress);
                        PrintersControll.mProgressBar.setProgress(progress);
                        break;
                    default:
                        break;
                }
            }
        };
        //开始连接
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIp.getText().toString().equals("")||mPort.getText().toString().equals("")){
                    ToastUtil.showToast(getBaseContext(),"Please write something.");
                    return;
                }
                String ipAddress = mIp.getText().toString();
                int port =Integer.valueOf(mPort.getText().toString());
                try{
                    for (int i=0;i<socketGroup.size();i++){
                        if(i >= 5 ){
                            ToastUtil.showToast(getBaseContext(),"too many socket " + socketGroup.get(4).getSocket());
                            return;
                        }
                        //验证是否存在列表中
                        if (socketGroup.get(i).getIp().equals(ipAddress)){
                            //验证是否连接
                            if (!socketGroup.get(i).getConnectState()){
                                TCPSocket socket1 = new TCPSocket(getBaseContext(),ipAddress,port);
                                if (isReceive == -1){
                                    isWhile = true;
                                    receive(ipAddress,port);
                                }
                                return;
                            }
                            ToastUtil.showToast(getBaseContext(),"is connecting...");
                            return;
                        }
                    }
                    TCPSocket socket1 = new TCPSocket(getBaseContext(),ipAddress,port);
                    if (isReceive == -1){
                        isWhile = true;
                        receive(ipAddress,port);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(getBaseContext(),"connect fail");
                }
            }
        });
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
                    while (isWhile)
                    {
                        String newIp = ip;
                        int newPort = port;
                        if (socketGroup.size()>0){
                            newIp = socketGroup.get(mCurrentPrinter).getIp();
                            newPort = socketGroup.get(mCurrentPrinter).getPort();
                        }
                        //System.out.println("isReceive：" + isReceive);
                        if (isReceive == 1) {
                            isReceive = -1;
                            if (socketGroup.size()>0){
                                socketGroup.get(mCurrentPrinter).setConnectState(false);
                            }
                            Log.d("PrintersTcp","connect time out.");
                            ToastUtil.showToast(getBaseContext(),"connect time out.");
                            return;
                        }else if (isReceive == 0){
                            int len = 0;
                            byte[] buffer = new byte[1024];
                            try{
                                socket = socketGroup.get(mCurrentPrinter).getSocket();
                                socket.setSoTimeout(3000);
                                inputStream = new DataInputStream( socket.getInputStream());
                                len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                reconnectTimes = 0;
                            }catch (Exception e){
                                reconnectTimes ++;
                                e.printStackTrace();
                            }
                            //System.out.println("数据长度：" + len);
                            if (len>0){
                                reconnectTimes ++;
                                try{
                                    String res = new String(buffer,0,len);
                                    send(getBaseContext(),"hi new user");
                                    //System.out.println("******************************current socket:" + socketGroup.get(mCurrentPrinter).getSocket());
                                    if (!res.equals("live")){
                                        socketGroup.get(mCurrentPrinter).setConnectState(true);
                                        socketGroup.get(mCurrentPrinter).setMessage(res);
                                        if (res.contains("M2110")&&res.contains("start")){
                                            fileSendLocation = 0;
                                            nextBuffer = new byte[0];
                                            sendBuffer(getBaseContext(),allBuffer);
                                            Log.d("PrintersTcp","--------------------+++++");
                                            startTime =  new Date();
                                            Log.d("PrintersTcp","start time:" + new Date());
                                            Log.d("PrintersTcp","--------------------+++++");
                                        } else if (res.contains("M2110")&&res.contains("ok")&&fileSendLocation >= 0){
                                            String[] a = res.split(" ");
                                            int b = Integer.valueOf(a[1]);
                                            if ( b != fileSendLen*fileSendLocation){
                                                Log.d("PrintersTcp","偏移地址错误" + fileSendLen);
                                                //ToastUtil.showToast(getBaseContext(),"偏移地址错误" + fileSendLen);
                                            }else{
                                                fileSendLocation ++;
                                                sendBuffer(getBaseContext(),allBuffer);
                                            }
                                        } else if(res.contains("M2110")&&res.contains("fail")){
                                            nextBuffer = new byte[0];
                                            sendBuffer(getBaseContext(),allBuffer);
                                        } else if(res.contains("M2110 ContinueStart")){
                                            nextBuffer = new byte[0];
                                            sendBuffer(getBaseContext(),allBuffer);
                                        } else if (res.contains("M2110")&&res.contains("stop")){
                                            nextBuffer = new byte[0];
                                            currentBuffer = new byte[0];
                                            ToastUtil.showToast(getBaseContext(),"upload fail.");
                                        } else if (res.contains("M2110")&&res.contains("Err")){
                                            nextBuffer = new byte[0];
                                            currentBuffer = new byte[0];
                                            ToastUtil.showToast(getBaseContext(),"upload fail.");
                                        } else if (res.contains("M2110 send completion")){
                                            Log.d("PrintersTcp","--------------------+++++");
                                            Log.d("PrintersTcp","start time:" + startTime);
                                            Log.d("PrintersTcp","end time:" + endTime);
                                            endTime = null;
                                            Log.d("PrintersTcp","--------------------+++++");
                                        }
                                        //打印机文件处理
                                        if ( res.contains("M20;") || res.contains("M20 3DWF;")){
                                            send(getBaseContext(),xor("N-0 M20 ok"));
                                        }
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                    Log.e("PrintersTcp","get message error");
                                }
                            }else{
                                if (reconnectTimes > 3){
                                    reconnectTimes = 0;
                                    Log.d("PrintersTcp","socket is disconnected");
                                    InetAddress ipAddress = InetAddress.getByName(newIp);
                                    try{
                                        socketGroup.get(mCurrentPrinter).setConnectState(false);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    try{
                                        socket = new Socket();
                                        SocketAddress socAddress = new InetSocketAddress(ipAddress, port);
                                        socket.connect(socAddress, 3000);//超时3秒
                                        isReceive = 0;
                                        for (int i=0;i<socketGroup.size();i++){
                                            if (socketGroup.get(i).getIp().equals(newIp) ){
                                                socketGroup.get(i).setSocket(socket);
                                                send(getBaseContext(),"reconnect user");
                                                break;
                                            }
                                        }
                                    }catch (Exception e1){
                                        isReceive = 1;
                                        socketGroup.get(mCurrentPrinter).setConnectState(false);
                                        ToastUtil.showToast(getBaseContext(),"reconnect fail.");
                                        Log.e("PrintersTcp","reconnect fail.");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isReceive = -1;
                    ToastUtil.showToast(getBaseContext(),"socket:" + socket);
                }
            }
        }.start();
    }
    /**
     * 发送消息
     * 上传文件内容开头加"80"
     * 上传文件内容结束加"81"
     * 发送命令加"82"
     * 无关消息加"83"
     */
    //普通文本
    public static void send(final Context context,final String need) {
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
                    sendTimes = 0;
                    Log.d("PrintersTcp","当前选中打印机发送消息:" + need);
                } catch (Exception e) {
                    sendTimes++;
                    if (sendTimes > 5){
                        isReceive = -1;
                        if (socketGroup.size() > 0){
                            socketGroup.get(mCurrentPrinter).setConnectState(false);
                        }
                    }
                    e.printStackTrace();
                    ToastUtil.showToast(context,"send fail.");
                    Log.e("PrintersTcp",need + " send fail\n");
                }
            }
        }.start();
    }
    //文件数据流
    public static void sendByte(final byte[] need) {
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
                    writer.flush();
                    sendMessage(0);
                    Log.d("PrintersTcp","偏移地址：" + fileSendLen*fileSendLocation + "send:" + Arrays.toString(slipt));
                    //System.out.println("偏移地址：" + fileSendLen*fileSendLocation + "send:" + Arrays.toString(slipt));
                    //System.out.println("发送上传文件");
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("send byte fail\n");
                    Log.e("PrintersTcp","send byte fail\n");
                }
            }
        }.start();
    }
    //文件上传
    public static void sendBuffer(final Context context,final byte[] need) {
        try {
            int len = need.length;
            if (fileSendLocation == -1){
                String message = "M2110 start";
                send(context,message);
            }else{
                if (fileSendLen*fileSendLocation >= len){
                    fileSendLocation = -1;
                    //needFile = "";
                    nextBuffer = new byte[0];
                    currentBuffer = new byte[0];
                    endTime = new Date();
                    String message = "M2110 send end";
                    send(context,message);
                }else{
                    if (nextBuffer.length != 0){
                        sendByte(nextBuffer);
                        nextBuffer = getBuffer(len,need,"next");
                        return;
                    }
                    currentBuffer = getBuffer(len,need,"");
                    sendByte(currentBuffer);
                    nextBuffer = getBuffer(len,need,"next");
                }
            }
            Log.d("PrintersTcp","发送消息");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] getBuffer(int len, byte[] need, String location){
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
                    Log.e("PrintersTcp","buffer length error");
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
                    Log.d("PrintersTcp","buffer length error");
                }
                return needBuffer;
            }catch (Exception e){
                return nullBuffer;
            }
        }
    }


    //初始化打印机列表及状态
    public void getListState(){
        listState.clear();
        List<ListTcpItem> a = new ArrayList<ListTcpItem>();
        List<ListTcpItem> b = new ArrayList<ListTcpItem>();
        List<ListTcpItem> c = new ArrayList<ListTcpItem>();
        a.clear();
        b.clear();
        c.clear();
        /*System.out.println("+++++++++++++++++++++++++++++++");
        System.out.println(socketGroup);*/
        for(int i=0;i<socketGroup.size();i++){
            boolean state = socketGroup.get(i).getConnectState();
            String number = socketGroup.get(i).getmSerialNumber();
            String name = socketGroup.get(i).getmSerialNumber();
            if ( state){
                ListTcpItem item = new ListTcpItem(name, number,state);
                a.add(item);
            }else{
                ListTcpItem item = new ListTcpItem(name, number,state);
                b.add(item);
            }
        }
        c = Data.getListTcp(a,b);
        listState.addAll(c);
        initListState();
    }
    public void initListState(){
        ListTcpAdapter listTcpAdapter = new ListTcpAdapter(PrintersTcp.this, R.layout.tcp_list_item, listState, this);
        ListView listView = (ListView) findViewById(R.id.printers_list);
        listView.setAdapter(listTcpAdapter);
        listView.setOnItemClickListener(this);
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mPrinterName = listState.get(position).getName();
                String mSerialNumber = listState.get(position).getNumber();

                for(int j=0;j<socketGroup.size();j++){
                    if (mSerialNumber == socketGroup.get(j).getmSerialNumber()){
                        mCurrentPrinter = j;
                        System.out.println("*************************position:" + j);
                        socketGroup.get(mCurrentPrinter).setCurrent(true);
                    }else{
                        socketGroup.get(j).setCurrent(false);
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("ip",socketGroup.get(mCurrentPrinter).getIp());
                intent.putExtra("port",socketGroup.get(mCurrentPrinter).getPort());
                intent.putExtra("serial_number",mSerialNumber);
                intent.setClass(PrintersTcp.this, PrintersControll.class);
                startActivity(intent);
            }
        });*/
    }
    /**
     * 响应ListView中item的点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        String mPrinterName = listState.get(position).getName();
        String mSerialNumber = listState.get(position).getNumber();

        for(int j=0;j<socketGroup.size();j++){
            if (mSerialNumber == socketGroup.get(j).getmSerialNumber()){
                mCurrentPrinter = j;
                Log.d("PrintersTcp","*************************position:" + j);
                socketGroup.get(mCurrentPrinter).setCurrent(true);
            }else{
                socketGroup.get(j).setCurrent(false);
            }
        }
        Intent intent = new Intent();
        intent.putExtra("ip",socketGroup.get(mCurrentPrinter).getIp());
        intent.putExtra("port",socketGroup.get(mCurrentPrinter).getPort());
        intent.putExtra("serial_number",mSerialNumber);
        intent.setClass(PrintersTcp.this, PrintersControll.class);
        startActivity(intent);
    }

    /**
     * 接口方法，响应ListView按钮点击事件
     */
    @Override
    public void click(View v) {
        int position = (Integer)v.getTag();
        String mPrinterName = listState.get(position).getName();
        final String mSerialNumber = listState.get(position).getNumber();

        if (v.getId() == R.id.button_connect){
            ToastUtil.showToast(getBaseContext(),"" + mSerialNumber);

            for(int j=0;j<socketGroup.size();j++){
                if(mSerialNumber == socketGroup.get(j).getmSerialNumber()){
                    if (!socketGroup.get(j).getConnectState()){
                        if (j == mCurrentPrinter){
                            isWhile = true;
                            isReceive = -1;
                        }
                        try{
                            String ip = socketGroup.get(j).getIp();
                            int port = socketGroup.get(j).getPort();
                            TCPSocket socket1 = new TCPSocket(getBaseContext(),ip,port);
                            if (isReceive == -1){
                                receive(ip,port);
                            }
                            /*socketGroup.get(j).setConnectState(false);
                            Socket socket = new Socket();
                            SocketAddress socAddress = new InetSocketAddress(ip, port);
                            socket.connect(socAddress, 3000);//超时3秒
                            if (position == mCurrentPrinter){
                                isReceive = 0;
                            }
                            for (int i=0;i<socketGroup.size();i++){
                                if (socketGroup.get(i).getIp().equals(ip) ){
                                    socketGroup.get(i).setSocket(socket);
                                    send(getBaseContext(),"reconnect user");
                                    break;
                                }
                            }*/
                        }catch (Exception e1){
                            if (j == mCurrentPrinter){
                                isReceive = 1;
                            }
                            socketGroup.get(j).setConnectState(false);
                            ToastUtil.showToast(getBaseContext(),"reconnect fail.");
                            Log.e("PrintersTcp","reconnect fail.");
                        }
                    }else{
                        try{
                            if (j == mCurrentPrinter){
                                isWhile = false;
                            }
                            socketGroup.get(j).getSocket().close();
                            socketGroup.get(j).setConnectState(false);
                            Log.d("PrintersTcp","********** socket close success ***********");
                        }catch (Exception e){
                            Log.e("PrintersTcp","********** socket close fail ***********");
                        }
                    }
                    break;
                }
            }
            return;
        }
        if (mSerialNumber.equals(socketGroup.get(mCurrentPrinter).getmSerialNumber())){
            Intent intent = new Intent();
            intent.putExtra("ip",socketGroup.get(mCurrentPrinter).getIp());
            intent.putExtra("port",socketGroup.get(mCurrentPrinter).getPort());
            intent.putExtra("serial_number",mSerialNumber);
            intent.setClass(PrintersTcp.this, PrintersControll.class);
            startActivity(intent);
            return;
        }
        new QMUIDialog.MessageDialogBuilder(PrintersTcp.this)
                .setMessage("Do you want to switch the current printer?")
                .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("OK", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        PrintersTcp.path = "";
                        PrintersTcp.name = "";
                        PrintersTcp.fileSendLocation = -1;
                        PrintersTcp.allBuffer = new byte[0];
                        PrintersTcp.nextBuffer = new byte[0];
                        PrintersTcp.currentBuffer = new byte[0];

                        for(int j=0;j<socketGroup.size();j++){
                            if (mSerialNumber == socketGroup.get(j).getmSerialNumber()){
                                mCurrentPrinter = j;
                                Log.d("PrintersTcp","*************************position:" + j);
                                socketGroup.get(mCurrentPrinter).setCurrent(true);
                            }else{
                                socketGroup.get(j).setCurrent(false);
                            }
                        }
                        Intent intent = new Intent();
                        intent.putExtra("ip",socketGroup.get(mCurrentPrinter).getIp());
                        intent.putExtra("port",socketGroup.get(mCurrentPrinter).getPort());
                        intent.putExtra("serial_number",mSerialNumber);
                        intent.setClass(PrintersTcp.this, PrintersControll.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .show();
        /*for(int j=0;j<socketGroup.size();j++){
            if (mSerialNumber == socketGroup.get(j).getmSerialNumber()){
                mCurrentPrinter = j;
                Log.d("PrintersTcp","*************************position:" + j);
                socketGroup.get(mCurrentPrinter).setCurrent(true);
            }else{
                socketGroup.get(j).setCurrent(false);
            }
        }
        Intent intent = new Intent();
        intent.putExtra("ip",socketGroup.get(mCurrentPrinter).getIp());
        intent.putExtra("port",socketGroup.get(mCurrentPrinter).getPort());
        intent.putExtra("serial_number",mSerialNumber);
        intent.setClass(PrintersTcp.this, PrintersControll.class);
        startActivity(intent);*/
    }

    //进入页面定时查询
    @Override
    public void onResume () {
        super.onResume();
        handler.postDelayed(runnable,0);
    }
    //退出页面取消查询
    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }
    public static void sendMessage(int what){
        //新建一个Message对象，存储需要发送的消息
        Message message = new Message();
        message.what = what;
        //然后将消息发送出去
        handler.sendMessage(message);
    }
}
