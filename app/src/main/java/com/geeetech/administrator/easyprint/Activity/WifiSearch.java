package com.geeetech.administrator.easyprint.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.geeetech.administrator.easyprint.R;

/**
 * Created by Administrator on 2018-03-06.
 */

public class WifiSearch extends BaseActivity {
    private Button mButton;
    private String mWifiName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_search);

        mButton = findViewById(R.id.button_search);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // 跳转到系统的网络设置界面
                Intent intent = null;
                startActivity(new Intent(WifiSearch.this, WifiSetting.class));
                //WifiSearch.this.finish();

                /*WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                mWifiName = ssid;

                if ( mWifiName.contains("GT_printer")){
                    startActivity(new Intent(WifiSearch.this, WifiSetting.class));
                }else{
                    ToastUtil.showToast(getBaseContext(),"No connection to printer WiFi");
                    //ToastUtil.showToast(getBaseContext(),mWifiName);
                }*/
                // 先判断当前系统版本
                /*if(android.os.Build.VERSION.SDK_INT > 10){  // 3.0以上
                    intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);//ACTION_WIRELESS_SETTINGS  ACTION_AIRPLANE_MODE_SETTINGS
                }else{
                    intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
                }
                startActivity(intent);*/
            }
        });
    }
}
