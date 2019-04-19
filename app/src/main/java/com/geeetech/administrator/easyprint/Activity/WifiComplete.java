package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import com.geeetech.administrator.easyprint.Internet.AesECBUtils;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.geeetech.administrator.easyprint.MainActivity.mCurrentPrinter;

/**
 * Created by Administrator on 2018-06-02.
 */

public class WifiComplete extends BaseActivity{
    private Runnable runnable;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_complete);

        runnable = new Runnable() {
            @Override
            public void run() {
                if ( MainActivity.isNetWork == 1){
                    getPrinterState();
                }
                handler.postDelayed(this,1000);
            }
        };
    }
    //获取打印机状态
    public void getPrinterState(){
        String serialNumber = mCurrentPrinter;
        if ( mCurrentPrinter.equals("")||getState().equals("logout")){
            return;
        }
        Context context = getBaseContext();
        List FirstUser = SpUtil.getList(context,"FirstUser");
        String email = FirstUser.get(0).toString();
        String token = FirstUser.get(1).toString();
        String state = FirstUser.get(3).toString();
        String encryStr = "";
        String encryStrId = "";
        //生成key
        String secretKey = "qstring1871salt";
        try {
            encryStr = AesECBUtils.encrypt(email,secretKey);
            encryStrId = AesECBUtils.encrypt(serialNumber,secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"?token=" + token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( WifiComplete.this.isFinishing()) {
                            return;
                        }
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                String bedSetTemp,bedNowTemp,exSetTemp,exNowTemp;
                                if ( state.equals("0")||state.equals("1")){

                                }else{
                                    Intent intent = new Intent();
                                    intent.setClass(getBaseContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( WifiComplete.this.isFinishing()) {
                            return;
                        }
                        //getErrorRespone(response);
                    }
                });
    }
    @Override
    public void onResume(){
        super.onResume();
        handler.postDelayed(runnable,1000);
    }
    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }
    //返回键按钮
    @Override
    public void addLeftBackTopBar() throws Exception{
        QMUITopBar mTopBar = (QMUITopBar) findViewById(R.id.topbar);
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                MainActivity.mViewPager.setCurrentItem(0,false);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            MainActivity.mViewPager.setCurrentItem(0,false);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
