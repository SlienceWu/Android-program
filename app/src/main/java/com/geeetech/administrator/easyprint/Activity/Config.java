package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-03-16.
 */

public class Config extends BaseActivity {
    private Button mButton_set;
    private Spinner mSpinner;

    private EditText mGcodeReq;
    private TextView mGcodeRes;
    private Button mSend,mClear;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_config);

        mButton_set = findViewById(R.id.button_set);
        mSpinner = findViewById(R.id.spinner);
        mButton_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                }else{
                    if (MainActivity.printerState.equals("0")||MainActivity.printerState.equals("1")){
                        ToastUtil.showToast(getBaseContext(),"Printer is offline.");
                        return;
                    }
                    setBaudRate();
                }
            }
        });

        mGcodeReq = findViewById(R.id.gcode_req);
        mGcodeRes = findViewById(R.id.gcode_res);
        mSend = findViewById(R.id.send);
        mClear = findViewById(R.id.clear);
        //发送命令
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                sendMessage();
            }
        });
        //清空
        mClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mGcodeRes.setText("");
            }
        });
    }

    //波特率设置
    public  void setBaudRate(){
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        OkGo.<String>post(url)
                .tag(this)
                .upJson(getParams())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if( response.code() == 200){
                            getQMUITipShow("Set success.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
    //请求参数
    public JSONObject getParams(){
        JSONObject js = new JSONObject();
        String value = mSpinner.getSelectedItem().toString();
        try {
            js.put("action","set");
            js.put("target","printer_printing_com_baudrate");
            js.put("value",value);
            js.put("token",getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }

    //自定义请求命令
    public void sendMessage(){
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        JSONObject js = new JSONObject();
        String value = mGcodeReq.getText().toString();
        try {
            js.put("action","send");
            js.put("value",value);
            js.put("token",getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;

        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        int code = response.code();
                        if ( code == 200){
                            getQMUITipShow("Send success.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
    //获取当前波特率
    public void getBaudRate() throws JSONException{
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action","get");
        jsonObject.put("target","printer_printing_com_baudrate");
        jsonObject.put("token",getToken());
        OkGo.<String>post(url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = null;
                        res = response.body();
                        try{
                            JSONObject jsonObject = new JSONObject(res);
                            String baudrate = jsonObject.getString("baudrate");
                            if ( baudrate.equals("256000")){
                                mSpinner.setSelection(2,true);
                            }else if ( baudrate.equals("250000")){
                                mSpinner.setSelection(1,true);
                            }else{
                                mSpinner.setSelection(0,true);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
    @Override
    public void onResume(){
        super.onResume();
        try {
            if (getState().equals("login") && MainActivity.isNetWork == 1){
                if (MainActivity.printerState.equals("0")||MainActivity.printerState.equals("1")){
                    return;
                }
                getBaudRate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
