package com.geeetech.administrator.easyprint.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.geeetech.administrator.easyprint.Activity.Fragment.GallaryFragment;
import com.geeetech.administrator.easyprint.Activity.Fragment.MyFragment;
import com.geeetech.administrator.easyprint.Internet.AesECBUtils;
import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.LoginActivity;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.PrintFragment;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.Reserve;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-24.
 */

public class BaseActivity extends Activity{
    QMUITopBar mTopBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // TODO Auto-generated method stub
        super.setContentView(layoutResID);
        try {
            addLeftBackTopBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //添加返回键按钮
    public void addLeftBackTopBar() throws Exception{
        mTopBar = (QMUITopBar) findViewById(R.id.topbar);
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }
    //获取加密后ID
    public String getUserID(String need,int num){
        String encryStr = "";

        List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
        String email = FirstUser.get(0).toString();
        //生成key
        String secretKey = "qstring1871salt";
        if ( num == 0){
            //AES加密
            try {
                encryStr = AesECBUtils.encrypt(email,secretKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if ( num == 1){
            try {
                encryStr = AesECBUtils.encrypt(need,secretKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encryStr;
    }
    //获取token值
    public String getToken(){
        List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
        String token = FirstUser.get(1).toString();
        return token;
    }
    //获取email
    public String getEmail(){
        String email = "";
        try{
            List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
            email = FirstUser.get(0).toString();
        }catch (Exception e){
            email = "";
        }
        return email;
    }
    //获取登录状态state
    public String getState(){
        String state = "";
        try{
            List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
            state = FirstUser.get(3).toString();
        }catch (Exception e){
            state = "logout";
        }
        return state;
    }

    //QMUI提示框
    public void getQMUITipShow(final String message, int type){
        if ( type == 0){
            new QMUIDialog.MessageDialogBuilder(BaseActivity.this)
                    .setMessage(message)
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            if ( message.equals("please log in")){
                                ImgList.instance.finish();
                                ModelView.instance.finish();
                                Intent intent = new Intent();
                                intent.setClass(getBaseContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }
                        }
                    })
                    .show();
        }else if (type == 1){
            new QMUIDialog.MessageDialogBuilder(BaseActivity.this)
                    .setMessage(message)
                    .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            Intent intent = new Intent();
                            intent.setClass(getBaseContext(),MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            if ( message.equals("Skip to print.")){
                                ImgList.instance.finish();
                                ModelView.instance.finish();
                                MainActivity.mViewPager.setCurrentItem(1,false);
                                try{
                                    GallaryFragment.mContentVp.setCurrentItem(2, false);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }
    //更换token值
    public void changeToken(){
        String token = getToken();
        String encryStr = getUserID("",0);

        String url = Urls.USER_CHANGE() + encryStr;
        //参数
        Map<String ,String> parmas = new HashMap<>();
        parmas.put("action","update");
        parmas.put("target","user_token");
        parmas.put("token",token);
        JSONObject jsonObject = new JSONObject(parmas);

        OkGo.<String>patch( url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        if ( BaseActivity.this.isFinishing()) {
                            return;
                        }
                        Context context = getBaseContext();
                        String res = response.body();
                        int code = response.code();
                        if ( code == 200){
                            //解析json
                            CommonJSONParser commonJSONParser = new CommonJSONParser();
                            Map<String, Object> result = commonJSONParser.parse(res);

                            String email = result.get("email").toString();
                            String newtoken = result.get("new_token").toString();
                            String tokenExpire = result.get("token_expire").toString();
                            //获取当前登录状态
                            List FirstUser = SpUtil.getList(context,"FirstUser");
                            String state = FirstUser.get(3).toString();
                            //储存数据
                            try {
                                Reserve.reserveList(context,email,newtoken,tokenExpire,state);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Data.setResCode(0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( BaseActivity.this.isFinishing()) {
                            return;
                        }
                        Data.setResCode(0);
                        getErrorRespone(response);
                    }
                });
    }

    //请求错误调用
    public void getErrorRespone(Response<String> response){
        int responseCode = response.code();
        if ( responseCode == 500){
            ToastUtil.showToast(getBaseContext(),"The server failed to handle the request for some unknown reasons.");
        }else if ( responseCode == 503){
            getQMUITipShow("The server is too overloaded to complete the request.",0);
        }else if ( responseCode == 996){
            getQMUITipShow("Wrong parameters in the requested address.",0);
        }else if ( responseCode == 997){
            getQMUITipShow("Token null.",0);
        }else if ( responseCode == 998){
            if ( Data.getResCode() == 999 || Data.getResCode() == 998){
                return;
            }
            Data.setResCode(998);
            try{
                MainActivity.mCurrentPrinter = "";
                PrintFragment.mPrinterState.setText(" ");
                MainActivity.mCurrentName = "";
                //PrintFragment.mCircleImg.setVisibility(View.GONE);
                Picasso.with(getBaseContext()).load(R.drawable.icon_printer_off).into(PrintFragment.mCircleImg);
                //PrintFragment.mCircleProgress.setBackgroundResource(R.drawable.icon_printer_off);
                PrintFragment.mGcodeName.setText("");
                PrintFragment.mCircleProgress.setProgress(0);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
                String email = FirstUser.get(0).toString();
                String token = FirstUser.get(1).toString();
                String tokenExpire = FirstUser.get(2).toString();
                String state = "logout";
                Reserve.reserveList(getBaseContext(),email,token,tokenExpire,state);
                MyFragment.mBtnLogout.setVisibility(View.GONE);
            }catch(Exception e){
                e.printStackTrace();
            }
            ToastUtil.showToast(getBaseContext(),"Login validation has expired");        //Has login by other user
            Intent intent = new Intent();
            intent.setClass(BaseActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }else if ( responseCode == 999){
            changeToken();
            Data.setResCode(999);
        }else if ( responseCode == -1 || responseCode == 502){
            MainActivity.tipShow("server");
        }/*else{
            String error = responseCode + "";
            String head = response.headers().toString();
            //getQMUITipShow(error +"  "+head,0);
        }*/
    }

    //获取用户绑定的打印机
    public void getList() throws Exception {
        Context context = getBaseContext();
        List FirstUser = SpUtil.getList(context,"FirstUser");
        String email = FirstUser.get(0).toString();
        String token = FirstUser.get(1).toString();
        String state = FirstUser.get(3).toString();
        String encryStr = "";

        //生成key
        String secretKey = "qstring1871salt";
        try {
            encryStr = AesECBUtils.encrypt(email,secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printers?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( BaseActivity.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try{
                                MainActivity.printerList.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("serial_num");
                                    String mPrinterName = jsonObject.getString("name");
                                    initList(mPrinterName,mSerialNumber);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( BaseActivity.this.isFinishing()) {
                            return;
                        }
                    }
                });
    }
    private void initList(String name,String number){
        ListItem item = new ListItem(name, number);
        MainActivity.printerList.add(item);
        if ( MainActivity.mCurrentPrinter.equals("")){
            MainActivity.mCurrentPrinter =  MainActivity.printerList.get(0).getNumber();
        }
    }

    public void openDefaultBrowser(String load_url)
    {
        /*try{
            //系统默认浏览器打开网页
            Intent intent = new Intent();
            intent.setAction("Android.intent.action.VIEW");
            Uri content_url = Uri.parse(load_url);
            intent.setData(content_url);
            intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
            startActivity(intent);
        }catch (Exception e){*/
            //选择浏览器打开网页
            Uri uri = Uri.parse(load_url);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        //}
    }
    //截取url中的参数值
    public static String getValueByName(String url, String name) {
        String result = "";
        if (url.contains("#")){
            int index = url.indexOf("#");
            String temp = url.substring(index + 1);
            String[] keyValue = temp.split("&");
            for (String str : keyValue) {
                if (str.contains(name)) {
                    result = str.replace(name + "=", "");
                    break;
                }
            }
        }else{
            int index = url.indexOf("?");
            String temp = url.substring(index + 1);
            String[] keyValue = temp.split("&");
            for (String str : keyValue) {
                if (str.contains(name)) {
                    result = str.replace(name + "=", "");
                    break;
                }
            }
        }
        return result;
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
    //crc校验
    public static int crc16( byte[] d) throws Exception
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
    };
    //异或校验
    public static String xor(String need){
        byte[] b = need.getBytes();
        int key = 0;
        for (int i=0;i<b.length;i++){
            key = key^b[i];
        }
        return need + "*" + key + "\r\n";
    }
}