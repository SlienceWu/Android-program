package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-03-10.
 */

public class AddMaterialProfile extends BaseActivity {
    private EditText mName;
    private Spinner mDiameter;
    private EditText mExtruderTemp;
    private EditText mBedTemp;
    private Button mButton;

    private String mMaterialName;
    private String mMaterialID;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_material_profile);

        mName = findViewById(R.id.name);
        mDiameter = findViewById(R.id.spinner);
        mExtruderTemp =  findViewById(R.id.extruder_temp);
        mBedTemp = findViewById(R.id.bed_temp);
        mButton = findViewById(R.id.button_add);

        mAction = getIntent().getStringExtra("action");
        if ( mAction.equals("update")){
            mMaterialName = getIntent().getStringExtra("name");
            mMaterialID = getIntent().getStringExtra("id");
            getMaterialDetailList(mMaterialID);
            String text = "change";
            mButton.setText(text);
        }

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mName.getText().toString())){
                    getQMUITipShow("Name null",0);
                    return;
                }
                int extruderTemp = 0,bedTemp = 0;
                try{
                    extruderTemp = Integer.parseInt(mExtruderTemp.getText().toString());
                    bedTemp = Integer.parseInt(mBedTemp.getText().toString());
                }catch(Exception e){
                    e.printStackTrace();
                }
                if (extruderTemp>0&&extruderTemp<251&&bedTemp>0&&bedTemp<121){
                    addMaterialProfile();
                }else{
                    getQMUITipShow("Params are not in the valid range.",0);
                }
            }
        });
    }
    //修改材料配置文件
    //1.获取某个材料配置文件
    public void getMaterialDetailList(String serialNumber){
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/material_profiles/"+ encryStrNum +"?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( AddMaterialProfile.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try {
                                JSONObject result = new JSONObject(res);
                                String diameter = null,extruder_temp = null,bed_temp = null;
                                diameter = result.get("diameter").toString();
                                bed_temp = result.get("bed_temp").toString();
                                extruder_temp = result.get("extruder_temp").toString();
                                initParams(diameter,extruder_temp,bed_temp);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( AddMaterialProfile.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //2.初始化参数
    public void initParams(String diameter,String extruder_temp,String bed_temp){
        mName.setText(mMaterialName);
        if ( diameter.equals("1.75")){
            mDiameter.setSelection(0,true);
        }else{
            mDiameter.setSelection(1,true);
        }
        mExtruderTemp.setText(extruder_temp);
        mBedTemp.setText(bed_temp);
    }
    //添加及更新材料配置文件
    public void addMaterialProfile(){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String encryStr = getUserID("",0);
        String encryStr1 = getUserID(mMaterialID,1);
        String url = Urls.GET_PRINTER_LIST() + encryStr;
        JSONObject data = getMaterialParams();
        if (data == null ){
            getQMUITipShow("Invalid params",0);
            return;
        }
        if ( mAction.equals("add")){
            url = Urls.GET_PRINTER_LIST() + encryStr;
            OkGo.<String>post(url)
                    .tag(this)
                    .upJson(data)
                    .execute(new StringCallback(){
                        @Override
                        public void onSuccess(Response<String> response){
                            if( AddMaterialProfile.this.isFinishing()){
                                return;
                            }
                            int responseCode = response.code();
                            String res = response.body();
                            if ( responseCode == 201){
                                ToastUtil.showToast(getBaseContext(),"Add Success.");
                                onBackPressed();
                            }
                        }
                        @Override
                        public void onError(Response<String> response){
                            if( AddMaterialProfile.this.isFinishing()){
                                return;
                            }
                            getErrorRespone(response);
                        }
                    });
        }else{
            url = Urls.GET_PRINTER_LIST() + encryStr +"/material_profiles/"+ encryStr1;
            OkGo.<String>patch(url)
                    .tag(this)
                    .upJson(data)
                    .execute(new StringCallback(){
                        @Override
                        public void onSuccess(Response<String> response){
                            if( AddMaterialProfile.this.isFinishing()){
                                return;
                            }
                            int responseCode = response.code();
                            String res = response.body();
                            if ( responseCode == 200){
                                ToastUtil.showToast(getBaseContext(),"Change Success.");
                                onBackPressed();
                            }
                        }
                        @Override
                        public void onError(Response<String> response){
                            if( AddMaterialProfile.this.isFinishing()){
                                return;
                            }
                            getErrorRespone(response);
                        }
                    });
        }
    }
    //请求参数
    public JSONObject getMaterialParams(){
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        String name = mName.getText().toString();
        float diameter = 0;
        int extruder_temp = 0;
        int bed_temp = 0;
        try{
            diameter = Float.parseFloat(mDiameter.getSelectedItem().toString());
            extruder_temp = Integer.parseInt(mExtruderTemp.getText().toString());
            bed_temp = Integer.parseInt(mBedTemp.getText().toString());
        }catch (Exception e){
            js = null;
            return js;
        }
        try {
            obj.put("name",name);
            obj.put("diameter",diameter);
            obj.put("extruder_temp",extruder_temp);
            obj.put("bed_temp",bed_temp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if ( mAction.equals("add")){
                js.put("action","add");
            }else{
                js.put("action","update");
            }
            js.put("target","user_mprofile");
            js.put("object",obj);
            js.put("token",getToken());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }
}