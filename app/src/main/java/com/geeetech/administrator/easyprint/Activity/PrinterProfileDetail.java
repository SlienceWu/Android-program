package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.geeetech.administrator.easyprint.Internet.AesECBUtils;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-06.
 */

public class PrinterProfileDetail extends BaseActivity {
    public static EditText mName;
    public EditText mDiameter;
    public EditText mExtruder;
    public Button mButton;
    String mJsonData;
    String mTestName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_profile_detail);

        mName = findViewById(R.id.printer_name);
        mDiameter = findViewById(R.id.diameter_num);
        mExtruder = findViewById(R.id.extruder_num);
        mButton = findViewById(R.id.button_add_print);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJsonData = mDiameter.getText().toString();
                mTestName = mExtruder.getText().toString();
                //生成key
                String secretKey = "qstring1871salt"; //192--token1781salt
                //AES加密
                String encryStr = null;
                try {
                    encryStr = AesECBUtils.encrypt(mJsonData,secretKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //AES解密
                String decryStr = null;
                try {
                    decryStr = AesECBUtils.decrypt(encryStr, secretKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                changePrinterProfile();
            }
        });
        initTest();
    }

    private void initTest() {
        Context context = getBaseContext();
        int email= SpUtil.getInt(context,"List", 0);
        List list = SpUtil.getList(context,"FirstUser");
        EditText Res = findViewById(R.id.response);
        Res.setText(email+";"+list);
    }

    //okgo
    public void commonRequest(View view,String email,String name) {
        String url = Urls.USER_LOGIN();
        //url = "http://www.baidu.com";
        Context context = getBaseContext();
        Map<String ,String> params = new HashMap<>();
        params.put("action","add");
        params.put("target","user");
        params.put("email",email);
        params.put("password","1234567");
        params.put("name",name);
        params.put("area","China");
        JSONObject jsonObject = new JSONObject(params);
        OkGo.<String>post( url )//
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        Context context = getBaseContext();
                        ToastUtil.showToast(context, "success");
                        int body = response.code();
                        Boolean successful = response.isSuccessful();

                        EditText Res = findViewById(R.id.response);
                        Res.setText(body+"");
                    }
                    @Override
                    public void onError(Response<String> response){
                        Context context = getBaseContext();
                        ToastUtil.showToast(context, "fail");

                        int body = response.code();
                        Boolean successful = response.isSuccessful();
                        //mName.setText(body);
                        EditText Res = findViewById(R.id.response);
                        Res.setText(body+"");
                    }
                });
    }

    //修改打印机配置文件
    public void changePrinterProfile(){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }

        String encryStr = getUserID("",0);
        String encryStr1 = getUserID(mJsonData,0);
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles/" + encryStr1;

        OkGo.<String>patch(url)
                .tag(this)
                .upJson(getChangeParams())
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        int responseCode = response.code();
                        String res = response.body();
                        if ( responseCode == 200){
                            getQMUITipShow("Change Success.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }

    //请求参数
    public JSONObject getChangeParams(){
        JSONObject js = new JSONObject();
        JSONObject obj = new JSONObject();
        String name = mName.getText().toString();
        String token = getToken();

        try {
            js.put("action","update");
            js.put("target","user_pprofile");
            js.put("token",token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            obj.put("name",name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }
}
