package com.geeetech.administrator.easyprint.AdDialog;

import android.app.Application;
import android.util.DisplayMetrics;

import com.geeetech.administrator.easyprint.AdDialog.utils.DisplayUtil;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by aaron on 16/8/2.
 * 自定义Application
 */
public class LApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initDisplayOpinion();

        Fresco.initialize(this);
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
}
