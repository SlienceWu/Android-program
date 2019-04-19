package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;

/**
 * Created by Administrator on 2018-03-06.
 */

public class WifiSetting extends BaseActivity {
    private EditText mWifiName;
    private EditText mWifiPassword;
    private Button mButton;
    private RelativeLayout mLayerWait;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);

        mWifiName = findViewById(R.id.wifi_name);
        mWifiPassword = findViewById(R.id.wifi_password);
        mButton = findViewById(R.id.button_config);

        mLayerWait = findViewById(R.id.layer_wait);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mLayer = mLayerWait.getVisibility();
                if ( mLayer == 0){
                    return;
                }
                try {
                    isClickAfter();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                String wifiName = mWifiName.getText().toString();
                String wifiPassword = mWifiPassword.getText().toString();
                String url = "http://192.168.4.1:80/?";//"http://192.168.1.247:8888/?";//
                String data = "ssid:" + wifiName + ";password:" + wifiPassword + ";server:" + Urls.getServerWifi()+ ";\n";
                HttpHeaders headers = new HttpHeaders();
                headers.put(HttpHeaders.HEAD_KEY_USER_AGENT, getUserAgent(getBaseContext()));
                String Url = url + data;
                OkGo.<String>get( Url )
                        .tag(this)
                        .headers(headers)
                        .execute(new StringCallback(){
                            @Override
                            public void onSuccess(Response<String> response){
                                if (WifiSetting.this == null || WifiSetting.this.isFinishing()) {
                                    return;
                                }
                                int responseCode = response.code();
                                String res = null;
                                res = response.body();
                                try {
                                    isClickBefore();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                if ( res.equals("config ok")){
                                    ToastUtil.showToast(getBaseContext(),"Wi-Fi config is completed");
                                    Intent intent = new Intent();
                                    intent.setClass(getBaseContext(), MainActivity.class);
                                    //intent.setClass(getBaseContext(), WifiComplete.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    //MainActivity.mViewPager.setCurrentItem(0,false);
                                    startActivity(intent);
                                    finish();
                                    //onBackPressed();
                                }else{
                                    getQMUITipShow("Wi-Fi config is fail",0);
                                }
                            }
                            @Override
                            public void onError(Response<String> response){
                                if ( WifiSetting.this.isFinishing()) {
                                    return;
                                }
                                try {
                                    isClickBefore();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                getQMUITipShow("Wi-Fi config is fail",0);
                            }
                        });
            }
        });
    }

    //配置过程中控件变化
    private void isClickAfter() throws Exception{
        mLayerWait.setVisibility(View.VISIBLE);
        mWifiName.setEnabled(false);
        mWifiPassword.setEnabled(false);
    }
    private void isClickBefore() throws Exception{
        mLayerWait.setVisibility(View.GONE);
        mWifiName.setEnabled(true);
        mWifiPassword.setEnabled(true);
    }
    //获取user-agent, 解决Android 9.0 配置WiFi失败问题
    private static String getUserAgent(Context context) {
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
