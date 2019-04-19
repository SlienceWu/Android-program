package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018-02-23.
 */

public class FeedbackActivity extends BaseActivity {
    private Button mSubmit;
    private EditText mMessage;
    private EditText mSerialNum;
    private EditText mBaudRate;
    private EditText mDeviceName;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfragment_feedback);

        mSubmit = findViewById(R.id.button_submit);
        mMessage = findViewById(R.id.suggest_message);
        mSerialNum = findViewById(R.id.serial_number);
        mBaudRate = findViewById(R.id.baud_rate);
        mDeviceName = findViewById(R.id.device_name);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                }else{
                    try {
                        sendMessage();
                    } catch (Exception e) {
                        getQMUITipShow("Send fail",0);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendMessage() throws Exception {
        String url = Urls.SERVER + "/v1/app/feedback";
        String needNum = mSerialNum.getText().toString();
        String needBaud = mBaudRate.getText().toString();
        String needDevice = mDeviceName.getText().toString();
        String need = mMessage.getText().toString();
        if ( need.isEmpty()){
            getQMUITipShow("please enter something",0);
            return;
        }else if (needNum.isEmpty()){
            getQMUITipShow("please enter serial number",0);
            return;
        }else if (needBaud.isEmpty()){
            getQMUITipShow("please enter baud rate",0);
            return;
        }else if (needDevice.isEmpty()){
            getQMUITipShow("please enter device name",0);
            return;
        }
        String details = "serial number:" + needNum + "\n baud rate:" + needBaud + "\n device name:" + needDevice + "\n feedback:" + need;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action","feedback");
        jsonObject.put("email",getEmail());
        jsonObject.put("value",details);
        jsonObject.put("token",getToken());
        OkGo.<String>post(url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        getQMUITipShow("Send message success.",0);
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
}
