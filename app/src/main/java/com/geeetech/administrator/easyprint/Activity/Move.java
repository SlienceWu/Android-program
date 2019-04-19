package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-03-16.
 */

public class Move extends BaseActivity {
    private TextView mTop,mBottom,mLeft,mRight,
    mTopZ,mBottomZ;
    private ImageView mHome;
    private LinearLayout mHomeX,mHomeY,mHomeZ;
    private Spinner mSize;

    private Thread newThread;//声明一个子线程
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll_move);

        mTop = findViewById(R.id.move_top);
        mBottom = findViewById(R.id.move_bottom);
        mLeft = findViewById(R.id.move_left);
        mRight = findViewById(R.id.move_right);
        mHome = findViewById(R.id.move_home);
        mHomeX = findViewById(R.id.move_home_x);
        mHomeY = findViewById(R.id.move_home_y);
        mHomeZ = findViewById(R.id.move_home_z);
        mTopZ = findViewById(R.id.move_z_top);
        mBottomZ = findViewById(R.id.move_z_bottom);
        mSize = findViewById(R.id.spinner);

        mTop.setOnClickListener(clickListener);
        mBottom.setOnClickListener(clickListener);
        mLeft.setOnClickListener(clickListener);
        mRight.setOnClickListener(clickListener);
        mHome.setOnClickListener(clickListener);
        mHomeX.setOnClickListener(clickListener);
        mHomeY.setOnClickListener(clickListener);
        mHomeZ.setOnClickListener(clickListener);
        mTopZ.setOnClickListener(clickListener);
        mBottomZ.setOnClickListener(clickListener);

        /*newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作
            }
        });
        newThread.start(); //启动线程*/
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ( getState().equals("logout")){
                getQMUITipShow("please log in",0);
                return;
            }
            String state = MainActivity.printerState;
            if ( state.equals("2")){
                switch (view.getId()){
                    case R.id.move_top:
                        setModol("y",0);
                        break;
                    case R.id.move_bottom:
                        setModol("y",1);
                        break;
                    case R.id.move_left:
                        setModol("x",1);
                        break;
                    case R.id.move_right:
                        setModol("x",0);
                        break;
                    case R.id.move_home:
                        setModelHome("all");
                        break;
                    case R.id.move_home_x:
                        setModelHome("x");
                        break;
                    case R.id.move_home_y:
                        setModelHome("y");
                        break;
                    case R.id.move_home_z:
                        setModelHome("z");
                        break;
                    case R.id.move_z_top:
                        setModol("z",0);
                        break;
                    case R.id.move_z_bottom:
                        setModol("z",1);
                        break;
                }
            }else if ( state.equals("0")){
                getQMUITipShow("Printer is offline.",0);
            }else if ( state.equals("1")){
                getQMUITipShow("Printer is logout.",0);
            }else if ( state.equals("3")||state.equals("4")){
                getQMUITipShow("Printer is printing.",0);
            }
            //getPrinterState(view);
        }
    };
    //获取打印机状态
    public void getPrinterState(final View view){
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        String encryStr = getUserID("",0);
        String encryStrId = getUserID(serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"?token=" + getToken();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                if ( state.equals("2")){

                                }else if ( state.equals("0")){
                                    getQMUITipShow("Printer is offline.",0);
                                }else if ( state.equals("1")){
                                    getQMUITipShow("Printer is logout.",0);
                                }else if ( state.equals("3")||state.equals("4")){
                                    getQMUITipShow("Printer is printing.",0);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {

                    }
                });
    }

    //移动操作
    public void setModol(String where,int state){
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
                    .upJson(getParams(where,state,"relative"))
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
    //请求参数
    public JSONObject getParams(String where,int state,String lock) throws JSONException {
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        js.put("action","set");
        js.put("target","printer_motor");
        js.put("token",getToken());

        obj.put("axis",where);
        obj.put("position","relative");
        if ( state == 0){
            int value = Integer.parseInt(mSize.getSelectedItem().toString());
            obj.put("value",value);
        }else{
            String need = "-"+mSize.getSelectedItem().toString();
            int value = Integer.parseInt(need);
            obj.put("value",value);
        }
        js.put("object",obj);
        return js;
    }
    //归位
    public void setModelHome(String where){
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
                    .upJson(getParamsHome(where))
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

    //挤出头操作
    public void setExtruder(String step){
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
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
}
