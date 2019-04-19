package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-03-16.
 */

public class Gcode extends BaseActivity {
    private EditText mGcodeReq;
    private TextView mGcodeRes;
    private Button mSend,mClear;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_gcode);

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

                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
}
