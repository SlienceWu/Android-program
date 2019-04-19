package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-03-16.
 */

public class Level extends BaseActivity {
    private Button mTopLeft,mTopRight,mCenter,mBottomLeft,mBottomRight;
    private Button mUpZ,mDownZ,mSave;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_level);

        mTopLeft = findViewById(R.id.top_left);
        mTopRight = findViewById(R.id.top_right);
        mCenter = findViewById(R.id.center);
        mBottomLeft = findViewById(R.id.bottom_left);
        mBottomRight = findViewById(R.id.bottom_right);
        mUpZ = findViewById(R.id.move_up_z);
        mDownZ = findViewById(R.id.move_down_z);
        mSave = findViewById(R.id.move_save);

        mTopLeft.setOnClickListener(clickListener);
        mTopRight.setOnClickListener(clickListener);
        mCenter.setOnClickListener(clickListener);
        mBottomLeft.setOnClickListener(clickListener);
        mBottomRight.setOnClickListener(clickListener);
        mUpZ.setOnClickListener(clickListener);
        mDownZ.setOnClickListener(clickListener);
        mSave.setOnClickListener(clickListener);

        if ( getState().equals("login") && MainActivity.isNetWork == 1){
            if ( MainActivity.printerState.equals("2")){
                setHome();
            }
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ( getState().equals("logout")){
                getQMUITipShow("please log in",0);
                return;
            }
            if ( MainActivity.isNetWork == 2){
                getQMUITipShow("Server fail",0);
                return;
            }else if( MainActivity.isNetWork == 0){
                getQMUITipShow("Network connection fail",0);
                return;
            }
            if ( !MainActivity.printerState.equals("2")){
                switch (MainActivity.printerState) {
                    case "0":
                        getQMUITipShow("Printer is offline.", 0);
                        break;
                    case "1":
                        getQMUITipShow("Printer is logout.", 0);
                        break;
                    case "3":
                    case "4":
                        getQMUITipShow("Printer is printing.", 0);
                        break;
                }
                return;
            }
            switch (view.getId()) {
                case R.id.top_left:
                    setLevel("move","1");
                    break;
                case R.id.top_right:
                    setLevel("move","2");
                    break;
                case R.id.center:
                    setLevel("move","5");
                    break;
                case R.id.bottom_left:
                    setLevel("move","4");
                    break;
                case R.id.bottom_right:
                    setLevel("move","3");
                    break;
                case R.id.move_up_z:
                    setLevel("up","0.5");
                    break;
                case R.id.move_down_z:
                    setLevel("down","0.5");
                    break;
                case R.id.move_save:
                    setLevel("save","0.5");
                    break;
            }
        }
    };

    //调平
    public void setLevel(final String move,final String where){
        //getQMUITipShow("test1 " + move + " " + where,0);
        String encryStr = getUserID("",0);
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        try {
            OkGo.<String>post(url)
                    .tag(this)
                    .upJson(getParams(move,where))
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            int code = response.code();
                            //getQMUITipShow("test2 " + move + " " + where,0);
                            if ( code == 200){
                                //getQMUITipShow("test3 " + move + " " + where,0);
                            }
                        }
                        @Override
                        public void onError(Response<String> response){
                            getErrorRespone(response);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            getQMUITipShow("Set fail",0);
        }
    }
    public JSONObject getParams(String move,String where) throws JSONException{
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        js.put("action","set");
        js.put("target","printer_leveling");
        js.put("token",getToken());

        obj.put("do",move);
        obj.put("value",where);

        js.put("object",obj);
        return js;
    }
    //调平归位
    public void setHome(){
        JSONObject js=new JSONObject();
        try {
            js.put("action","reset");
            js.put("target","printer_leveling");
            js.put("token",getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String encryStr = getUserID("",0);
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
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
    //退出调平归位
    public void getBack(){
        String encryStr = getUserID("",0);
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum;
        try {
            OkGo.<String>post(url)
                    .tag(this)
                    .upJson(getParamsHome("all"))
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
        } catch (JSONException e) {
            e.printStackTrace();
            getQMUITipShow("Move fail",0);
        }
    }
    public JSONObject getParamsHome(String where) throws JSONException {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        js.put("action","reset");
        js.put("target","printer_motor");
        js.put("token",getToken());
        obj.put("axis",where);

        js.put("object",obj);
        return js;
    }

    @Override
    public void addLeftBackTopBar() throws Exception{
        QMUITopBar mTopBar = (QMUITopBar) findViewById(R.id.topbar);
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( getState().equals("login") && MainActivity.isNetWork == 1){
                    if ( MainActivity.printerState.equals("2")){
                        getBack();
                    }
                }
                onBackPressed();
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if ( getState().equals("login") && MainActivity.isNetWork == 1){
                if ( MainActivity.printerState.equals("2")){
                    getBack();
                }
            }
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
