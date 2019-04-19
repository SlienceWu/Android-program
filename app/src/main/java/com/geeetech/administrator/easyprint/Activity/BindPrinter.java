package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-03-08.
 */

public class BindPrinter extends BaseActivity{
    private EditText mPrintSign;
    private Button mButton;
    private RelativeLayout mLayerWait;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_bind);

        mPrintSign = findViewById(R.id.printer_sign);
        mButton = findViewById(R.id.button_add_print);
        mLayerWait = findViewById(R.id.layer_wait);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mLayer = mLayerWait.getVisibility();
                if ( mLayer == 0){
                    return;
                }
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                String serialNum = mPrintSign.getText().toString();
                if(serialNum.equals("")){
                    getQMUITipShow("Can't be empty.",0);
                    return;
                }
                isClickAfter();
                String token = getToken();
                String encryStr = getUserID("",0);

                String url = Urls.USER_BIND_PRINTER() + encryStr;

                Map<String ,String> params = new HashMap<>();
                params.put("action","add");
                params.put("target","user_printer");
                params.put("serial_num",serialNum);
                params.put("token",token);
                JSONObject jsonObject = new JSONObject(params);
                OkGo.<String>post(url)
                        .tag(this)
                        .upJson(jsonObject)
                        .execute(new StringCallback(){
                            @Override
                            public void onSuccess(Response<String> response){
                                if ( BindPrinter.this.isFinishing()) {
                                    return;
                                }
                                isClickBefore();
                                String res = null;
                                int responseCode = response.code();
                                res = response.body();
                                if ( responseCode == 200){
                                    try {
                                        getList();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    onBackPressed();
                                    ToastUtil.showToast(getBaseContext(),"Successfully bind.");
                                }
                                else if ( responseCode == 400){
                                    getQMUITipShow("Invalid params.",0);
                                }
                                else if ( responseCode == 410){
                                    getQMUITipShow("The serial number does not exist,please check it.",0);
                                }
                                else if ( responseCode == 409){
                                    getQMUITipShow("The printer has been bound with another device, please check it.",0);
                                }
                            }
                            @Override
                            public void onError(Response<String> response){
                                if ( BindPrinter.this.isFinishing()) {
                                    return;
                                }
                                isClickBefore();
                                getErrorRespone(response);
                            }
                        });
            }
        });
    }

    //配置过程中控件变化
    private void isClickAfter(){
        mLayerWait.setVisibility(View.VISIBLE);
        mPrintSign.setEnabled(false);
    }
    private void isClickBefore(){
        mLayerWait.setVisibility(View.GONE);
        mPrintSign.setEnabled(true);
    }
}
