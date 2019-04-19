package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018-03-09.
 */

public class Temp extends BaseActivity{
    protected static final int STOP = 0x10000;
    protected static final int NEXT = 0x10001;
    int count;
    boolean mExist = true;
    @BindView(R.id.rect_progressBar) ProgressBar mRectProgressBar;
    @BindView(R.id.rect_progressBar2) ProgressBar mRectProgressBarA;
    //@BindView(R.id.button_start) Button mStartBtn;
    @BindView(R.id.printer_progress_show) TextView mProgressExtruderTemp;
    @BindView(R.id.printer_progress_show2) TextView mProgressHotBedTemp;
    @BindView(R.id.hotbed_temp) TextView mHotBedTemp;
    private TextView mExtruderTemp;
    private ImageView mExtruderSwitch;
    private ImageView mHotbedSwitch;
    private boolean SwitchOne = true,SwitchTwo = true;

    //定时查询打印机温度
    final Handler handler = new Handler();
    Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_temp);
        ButterKnife.bind(this);

        mExtruderSwitch = findViewById(R.id.extruder_switch);
        mHotbedSwitch = findViewById(R.id.hotbed_switch);
        mExtruderSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                if ( SwitchOne){
                    SwitchOne = false;
                    mExtruderSwitch.setImageResource(R.drawable.icon_off);
                    setTemp("extruder_off");
                }else{
                    SwitchOne = true;
                    mExtruderSwitch.setImageResource(R.mipmap.icon_on);
                    setTemp("extruder");
                }
            }
        });
        mHotbedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                if ( SwitchTwo){
                    SwitchTwo = false;
                    mHotbedSwitch.setImageResource(R.drawable.icon_off);
                    setTemp("hotbed_off");
                }else{
                    SwitchTwo = true;
                    mHotbedSwitch.setImageResource(R.mipmap.icon_on);
                    setTemp("hotbed");
                }
            }
        });

        mExtruderTemp = findViewById(R.id.extruder_temp);
        mExtruderTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                extruderTemp();
            }
        });
        mHotBedTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                hotbedTemp();
            }
        });
        /*try{
                    int temp = Integer.parseInt(mExtruderTemp.getText().toString());
                    mRectProgressBar.setProgress(temp);
                    String param = temp+"℃";
                    mProgressExtruderTemp.setText(param);
                }catch (Exception e){

                }*/
        /*mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }else if(mExtruderTemp.getText().toString().length() == 0){
                    getQMUITipShow("please write extruder temp.",0);
                    return;
                }else if (mHotBedTemp.getText().toString().length() == 0){
                    getQMUITipShow("please write hotbed temp.",0);
                    return;
                }
                int extruder = Integer.parseInt(mExtruderTemp.getText().toString());
                int hotbed = Integer.parseInt(mHotBedTemp.getText().toString());
                if (extruder>250||hotbed>120){
                    getQMUITipShow("Params are too large.",0);
                    return;
                }
                setTemp("extruder");
                setTemp("hotbed");
            }
        });*/
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if ( getState().equals("login")){
                        getTemp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this,1000);
            }
        };
    }

    //温度设置
    private void extruderTemp(){
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(Temp.this);
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
                                mExtruderTemp.setText(text);
                            }else{
                                ToastUtil.showToast(Temp.this, "Params are too large.");
                                return;
                            }
                            setTemp("extruder");
                            dialog.dismiss();
                        } else {
                            ToastUtil.showToast(Temp.this, "Please write something");
                        }
                    }
                })
                .show();
    }
    private void hotbedTemp(){
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(Temp.this);
        builder.setTitle("Set printer hotbed temp")
                .setPlaceholder("0-120")
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
                            if ( b < 121){
                                mHotBedTemp.setText(text);
                            }else{
                                ToastUtil.showToast(Temp.this, "Params are too large.");
                                return;
                            }
                            setTemp("hotbed");
                            dialog.dismiss();
                        } else {
                            ToastUtil.showToast(Temp.this, "Please write something");
                        }
                    }
                })
                .show();
    }
    private void setTemp(String target){
        String encryStr = getUserID("",0);
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        JSONObject js;
        if (target.equals("extruder")){
            try {
                js = getExtruderParams();
                if ( getExtruderParams() == null ){
                    getQMUITipShow("Params is wrong.",0);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }else if (target.equals("extruder_off")){
            try {
                js = getSwitchParams("one");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }else if (target.equals("hotbed_off")){
            try {
                js = getSwitchParams("two");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }else{
            try {
                js = getBedTempParams();
                if ( getBedTempParams() == null ){
                    getQMUITipShow("Params is wrong.",0);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }

        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( Temp.this.isFinishing()){
                            return;
                        }
                        int code = response.code();
                        if ( code == 200){

                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( Temp.this.isFinishing()){
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //请求参数
    public JSONObject getExtruderParams() throws JSONException {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        js.put("action","set");
        js.put("target","printer_extruder_temp");
        js.put("token",getToken());
        int temp = 0;
        try{
            temp = Integer.parseInt(mExtruderTemp.getText().toString());
        }catch (Exception e){
            js = null;
            return js;
        }
        obj.put("extruder_num",0);
        obj.put("value",temp);

        js.put("object",obj);
        return js;
    }
    public JSONObject getBedTempParams() throws JSONException {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        js.put("action","set");
        js.put("target","printer_bed_temp");
        js.put("token",getToken());
        int temp = 0;
        try{
            temp = Integer.parseInt(mHotBedTemp.getText().toString());
        }catch (Exception e){
            js = null;
            return js;
        }
        js.put("value",temp);

        return js;
    }
    public JSONObject getSwitchParams(String type) throws JSONException {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        if ( type.equals("one")){
            js.put("action","set");
            js.put("target","printer_extruder_temp");
            js.put("token",getToken());
            obj.put("extruder_num",0);
            obj.put("value",0);
            js.put("object",obj);
        }else if( type.equals("two")){
            js.put("action","set");
            js.put("target","printer_bed_temp");
            js.put("token",getToken());
            js.put("value",0);
        }
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
                        if ( Temp.this.isFinishing()) {
                            return;
                        }
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                Double bedSetTemp,bedNowTemp,exSetTemp,exNowTemp;
                                if ( state.equals("2") || state.equals("3") || state.equals("4")){
                                    bedSetTemp = result.getDouble("bed_setting_temp");
                                    exSetTemp = result.getDouble("extruder_setting_temp");
                                    bedNowTemp = result.getDouble("bed_current_temp");
                                    exNowTemp = result.getDouble("extruder_current_temp");

                                    int a = exNowTemp.intValue();
                                    int b = bedNowTemp.intValue();
                                    int c = exSetTemp.intValue();
                                    int d = bedSetTemp.intValue();

                                    mRectProgressBar.setProgress(a);
                                    String tempShow = exNowTemp+"℃";
                                    mProgressExtruderTemp.setText(tempShow);
                                    String cc = c+"";
                                    mExtruderTemp.setText(cc);

                                    mRectProgressBarA.setProgress(b);
                                    String bedTempShow = bedNowTemp+"℃";
                                    mProgressHotBedTemp.setText(bedTempShow);
                                    String dd = d+"";
                                    mHotBedTemp.setText(dd);
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
                        if ( Temp.this.isFinishing()) {
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
