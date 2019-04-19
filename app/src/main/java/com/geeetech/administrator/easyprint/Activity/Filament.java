package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-03-16.
 */

public class Filament extends BaseActivity {
    private LinearLayout mLoad,mLoadOut;
    //挤出头当前状态
    private int mState = 0;
    private ProgressBar mProgress;
    private TextView mTextShowProgress,mSetTemp;
    private Button mButton;
    private Handler handler = new Handler();
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_filament);

        mLoad = findViewById(R.id.load);
        mLoadOut = findViewById(R.id.load_out);
        mProgress = findViewById(R.id.rect_progressBar);
        mTextShowProgress = findViewById(R.id.progress_show);
        mSetTemp = findViewById(R.id.set_temp);
        mButton = findViewById(R.id.button_set);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(Filament.this);
                builder.setTitle("Set printer extruder temp")
                        .setPlaceholder("0-250")
                        .setInputType(InputType.TYPE_CLASS_NUMBER)
                        .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("Sure", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                CharSequence text = builder.getEditText().getText();
                                if (text != null && text.length() >0) {
                                    String a = text.toString();
                                    int b = Integer.valueOf(a);
                                    if ( b < 251){
                                        //mProgress.setProgress(b);
                                        //mTextShowProgress.setText(text);
                                    }else{
                                        ToastUtil.showToast(Filament.this, "Params are too large.");
                                        return;
                                    }
                                    setTemp(text);
                                    dialog.dismiss();
                                } else {
                                    ToastUtil.showToast(Filament.this, "Please write something");
                                }
                            }
                        })
                        .show();
            }
        });
        mLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                if ( mState == 0){
                    mState = 1;
                    setExtruder("step_in");
                }else{
                    getQMUITipShow("please wait.",0);
                }
            }
        });
        mLoadOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                if ( mState == 0){
                    mState = 2;
                    setExtruder("step_out");
                }else{
                    getQMUITipShow("please wait.",0);
                }
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!getState().equals("logout")){
                        getTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this,1000);
            }
        };
    }

    //挤出头操作
    public void setExtruder(String step){
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            mState = 0;
            return;
        }

        String encryStr = getUserID("",0);

        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        try {
            OkGo.<String>post(url)
                    .tag(this)
                    .upJson(getExtruderParams(step))
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            int code = response.code();
                            if ( code == 200){
                                mState = 0;
                            }
                        }
                        @Override
                        public void onError(Response<String> response){
                            mState = 0;
                            getErrorRespone(response);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            mState = 0;
        }
    }
    public JSONObject getExtruderParams(String step) throws JSONException{
        JSONObject js = new JSONObject();
        JSONObject obj = new JSONObject();
        obj.put("extruder_num",0);
        obj.put("direction",step);
        obj.put("value",20);
        js.put("action","set");
        js.put("target","printer_extruder_feed");
        js.put("object",obj);
        js.put("token",getToken());
        return js;
    }

    //设置挤出头温度
    public void setTemp(CharSequence temp){
        String encryStr = getUserID("",0);
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        JSONObject js;
        try {
            js = getParams(temp);
            if ( getParams(temp) == null ){
                getQMUITipShow("Params is wrong.",0);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
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
    //请求参数
    public JSONObject getParams(CharSequence temp) throws JSONException {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        js.put("action","set");
        js.put("target","printer_extruder_temp");
        js.put("token",getToken());

        String a = temp.toString();
        int tempNeed = Integer.valueOf(a);

        obj.put("extruder_num",0);
        obj.put("value",tempNeed);

        js.put("object",obj);
        return js;
    }

    //获取温度
    public void getTemp() throws Exception{
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStr = getUserID("",0);
        String encryStrId = getUserID(serialNumber,1);
        String token = getToken();
        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"?token=" + token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( Filament.this.isFinishing()) {
                            return;
                        }
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                String bedSetTemp,bedNowTemp,exSetTemp,exNowTemp;
                                if ( state.equals("2") || state.equals("3")||state.equals("4")){
                                    bedSetTemp = result.getString("bed_setting_temp");
                                    exSetTemp = result.getString("extruder_setting_temp")+"℃";
                                    bedNowTemp = result.getString("bed_current_temp")+"℃";
                                    exNowTemp = result.getString("extruder_current_temp")+"℃";

                                    mSetTemp.setText(exSetTemp);
                                    mTextShowProgress.setText(exNowTemp);
                                    Double a = Double.valueOf(result.getString("extruder_current_temp"));
                                    int b = a.intValue();
                                    mProgress.setProgress(b);
                                }else if ( state.equals("0")){

                                }else if ( state.equals("1")){

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( Filament.this.isFinishing()) {
                            return;
                        }
                        handler.removeCallbacks(runnable);
                        getErrorRespone(response);
                    }
                });
    }

    @Override
    public void onResume(){
        handler.postDelayed(runnable,1000);
        super.onResume();
    }
    @Override
    public void onPause(){
        handler.removeCallbacks(runnable);
        super.onPause();
    }
}
