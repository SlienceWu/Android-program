package com.geeetech.administrator.easyprint;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.geeetech.administrator.easyprint.AdDialog.AdConstant;
import com.geeetech.administrator.easyprint.AdDialog.AdManager;
import com.geeetech.administrator.easyprint.AdDialog.bean.AdInfo;
import com.geeetech.administrator.easyprint.AdDialog.transformer.DepthPageTransformer;
import com.geeetech.administrator.easyprint.AdDialog.utils.DisplayUtil;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-02-27.
 */

public class LaunchActivity extends Activity{
    private List mAdvList = new ArrayList<>();;
    private Handler mHandler = new Handler();
    private Runnable gotoAdvert = null;
    private Runnable gotoMain = null;
    AdInfo adInfo = new AdInfo();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //后台过后进入程序保留之前页面
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        initDisplayOpinion();
        Fresco.initialize(this);
        super.onCreate(savedInstanceState);

        //加载启动界面
        setContentView(R.layout.activity_launch);

        //保存默认ip
        try{
            List currentIp = SpUtil.getList(getBaseContext(),"CurrentIp");
            String ip = currentIp.get(0).toString();
            if (ip.equals("1")){
                Urls.setServer("47.254.41.125:5000");
            }else{
                Urls.setServer("47.254.41.125:8000");
            }
        }catch (Exception e){
            Urls.setServer("47.254.41.125:5000");
        }
        Integer time = 1000;    //设置等待时间，单位为毫秒
        Integer timeAfter = 9000;
        //当计时结束时，跳转至广告
        gotoAdvert = new Runnable() {
            @Override
            public void run() {
                String url = Urls.USER_AD();
                OkGo.<String>get(url)
                        .tag(this)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                try {
                                    JSONArray jsonArray = new JSONArray(response.body());
                                    if ( jsonArray.length() == 0){
                                        mHandler.removeCallbacks(gotoMain);
                                        startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                                        LaunchActivity.this.finish();
                                        return;
                                    }
                                    mAdvList.clear();
                                    for (int i=0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String path = jsonObject.getString("image_path");
                                        adInfo = new AdInfo();
                                        adInfo.setActivityImg(path);
                                        mAdvList.add(adInfo);
                                    }
                                    initData();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onError(Response<String> response) {
                                mHandler.removeCallbacks(gotoMain);
                                initData();
                                if ( LaunchActivity.this.isFinishing() ){
                                    return;
                                }
                                startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                                LaunchActivity.this.finish();
                            }
                        });
            }
        };
        mHandler.postDelayed(gotoAdvert, time);

        gotoMain = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(LaunchActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                //startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                LaunchActivity.this.finish();
            }
        };
        mHandler.postDelayed(gotoMain, timeAfter);
    }
    /**
     * 初始化数据
     */
    private void initData() {
        /*AdInfo adInfo = new AdInfo();
        adInfo.setActivityImg("https://raw.githubusercontent.com/yipianfengye/android-adDialog/master/images/testImage1.png");
        mAdvList.add(adInfo);

        adInfo = new AdInfo();
        adInfo.setActivityImg("https://raw.githubusercontent.com/yipianfengye/android-adDialog/master/images/testImage2.png");
        mAdvList.add(adInfo);*/

        AdManager adManager = new AdManager(LaunchActivity.this, mAdvList);
        adManager.setOnImageClickListener(new AdManager.OnImageClickListener() {
            @Override
            public void onImageClick(View view, AdInfo advInfo) {
                Intent intent = new Intent();
                intent.setClass(LaunchActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                LaunchActivity.this.finish();
                openDefaultBrowser("http://www.giantarm.com/featured_product/d200-3dprinter.html");
                mHandler.removeCallbacks(gotoMain);
            }
        })
                .setOnCloseClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mHandler.removeCallbacks(gotoMain);
                        Intent intent = new Intent();
                        intent.setClass(LaunchActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        LaunchActivity.this.finish();
                    }
                })
                .setOverScreen(false)
                .setPageTransformer(new DepthPageTransformer())
                .showAdDialog(AdConstant.ANIM_STOP_DEFAULT);
    }
    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
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
}
