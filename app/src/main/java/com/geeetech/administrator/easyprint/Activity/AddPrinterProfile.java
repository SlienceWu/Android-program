package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2018-03-10.
 */

public class AddPrinterProfile extends BaseActivity {
    private EditText mName;
    private ImageView mBedShape;
    private EditText mWidth;
    private EditText mDepth;
    private EditText mHeight;
    private EditText mHeightOne;
    private EditText mRaidus;
    private TextView mHeatBed;
    private Button mButton;
    private int mShape = 0;
    private LinearLayout mShapeRectangle;
    private LinearLayout mShapeCircular;
    private ImageView mHeatBedImage;

    private String mProfileName;
    private String mProfileID;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_printer_profile);

        mName = findViewById(R.id.name);
        mWidth = findViewById(R.id.width);
        mDepth = findViewById(R.id.depth);
        mHeight = findViewById(R.id.height);
        mHeightOne = findViewById(R.id.height_1);
        mRaidus = findViewById(R.id.radius);
        mHeatBed = findViewById(R.id.heatbed_exist);
        mButton = findViewById(R.id.button_add);
        mBedShape = findViewById(R.id.bed_shape);
        mShapeRectangle = findViewById(R.id.bed_shape_rectangle);
        mShapeCircular = findViewById(R.id.bed_shape_circular);
        mHeatBedImage = findViewById(R.id.heatbed_img);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                if (TextUtils.isEmpty(mName.getText().toString())){
                    getQMUITipShow("Name null",0);
                    return;
                }
                if ( mShape == 1){
                    int height=0,radius=0;
                    try{
                        height = Integer.parseInt(mHeightOne.getText().toString());
                        radius = Integer.parseInt(mRaidus.getText().toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if ( height<1000&&height>0&&radius<1000&&radius>0){
                        addPrinterProfile();
                    }else{
                        getQMUITipShow("Params are not in the valid range.",0);
                    }
                }else{
                    int width=0,depth=0,height=0;
                    try{
                        width = Integer.parseInt(mWidth.getText().toString());
                        depth = Integer.parseInt(mDepth.getText().toString());
                        height = Integer.parseInt(mHeight.getText().toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if ( height<1000&&height>0&&depth<1000&&depth>0&&width<1000&&width>0){
                        addPrinterProfile();
                    }else{
                        getQMUITipShow("Params are not in the valid range.",0);
                    }
                }
                //addPrinterProfile();
            }
        });
        // 初始化控件
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // 建立数据源
        String[] mItems = getResources().getStringArray(R.array.choose_shape);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner .setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String[] languages = getResources().getStringArray(R.array.choose_shape);
                if ( languages[pos].equals("Circular")){
                    mShape = 1;
                    mBedShape.setImageResource(R.drawable.icon_shape_circular);
                    mShapeCircular.setVisibility(View.VISIBLE);
                    mShapeRectangle.setVisibility(View.GONE);
                }else{
                    mShape = 0;
                    mBedShape.setImageResource(R.drawable.icon_shape_rectangle);
                    mShapeCircular.setVisibility(View.GONE);
                    mShapeRectangle.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        mAction = getIntent().getStringExtra("action");
        if ( mAction.equals("update")){
            mProfileName = getIntent().getStringExtra("name");
            mProfileID = getIntent().getStringExtra("id");
            getProfileDetailList(mProfileID);
            String text = "change";
            mButton.setText(text);
        }
        mHeatBedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mHeatBed.getText().equals("1")){
                    mHeatBed.setText("0");
                    mHeatBedImage.setImageResource(R.drawable.icon_heatbed_no);
                }else{
                    mHeatBed.setText("1");
                    mHeatBedImage.setImageResource(R.drawable.icon_heatbed_yes);
                }
            }
        });
    }
    //修改打印机配置文件
    //1.获取某个打印机配置文件
    public void getProfileDetailList(String serialNumber){
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles/"+ encryStrNum +"?token="+token;
        //mName.setText(url);
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( AddPrinterProfile.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            //解析json
                            CommonJSONParser commonJSONParser = new CommonJSONParser();
                            Map<String, Object> result = commonJSONParser.parse(res);
                            String shape = result.get("shape").toString();
                            String heatbed = result.get("heatbed_exist").toString();
                            if ( shape.equals("0")){
                                String width = result.get("width").toString();
                                String depth = result.get("depth").toString();
                                String height = result.get("height").toString();
                                initParams(shape,width,depth,"",height,heatbed);
                            }else{
                                String radius = result.get("radius").toString();
                                String height = result.get("height").toString();
                                initParams(shape,"","",radius,height,heatbed);
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (AddPrinterProfile.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //2.初始化参数
    public void initParams(String shape,String width,String depth,String radius,String height,String heatbed){
        mName.setText(mProfileName);
        mHeatBed.setText(heatbed);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if ( shape.equals("0")){
            spinner.setSelection(0,true);
            mBedShape.setImageResource(R.drawable.icon_shape_rectangle);
            mShapeCircular.setVisibility(View.GONE);
            mShapeRectangle.setVisibility(View.VISIBLE);
            mWidth.setText(width);
            mDepth.setText(depth);
            mHeight.setText(height);
        }else{
            spinner.setSelection(1,true);
            mBedShape.setImageResource(R.drawable.icon_shape_circular);
            mShapeCircular.setVisibility(View.VISIBLE);
            mShapeRectangle.setVisibility(View.GONE);
            mHeightOne.setText(height);
            mRaidus.setText(radius);
        }
    }
    //添加及更新打印机配置文件
    public void addPrinterProfile(){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String encryStr = getUserID("",0);
        String encryStr1 = getUserID(mProfileID,1);
        String url = Urls.GET_PRINTER_LIST() + encryStr;
        JSONObject data = getPrinterParams();
        if (data == null){
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
                            int responseCode = response.code();
                            String res = response.body();
                            if ( responseCode == 201){
                                ToastUtil.showToast(getBaseContext(),"Add Success.");
                                onBackPressed();
                            }
                        }
                        @Override
                        public void onError(Response<String> response){
                            getErrorRespone(response);
                        }
                    });
        }else{
            url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles/"+ encryStr1;
            OkGo.<String>patch(url)
                    .tag(this)
                    .upJson(data)
                    .execute(new StringCallback(){
                        @Override
                        public void onSuccess(Response<String> response){
                            int responseCode = response.code();
                            String res = response.body();
                            if ( responseCode == 200){
                                ToastUtil.showToast(getBaseContext(),"Change Success.");
                                onBackPressed();
                            }
                        }
                        @Override
                        public void onError(Response<String> response){
                            getErrorRespone(response);
                        }
                    });
        }
    }
    //请求参数
    public JSONObject getPrinterParams(){
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        String name = mName.getText().toString();
        int shape = mShape;
        int width = 0;
        int depth = 0;
        int radius = 0;
        int heatbed = 0;
        int height = 0;
        try{
            if (shape == 1){
                height = Integer.parseInt(mHeightOne.getText().toString());
                radius = Integer.parseInt(mRaidus.getText().toString());
            }else{
                width = Integer.parseInt(mWidth.getText().toString());
                depth = Integer.parseInt(mDepth.getText().toString());
                height = Integer.parseInt(mHeight.getText().toString());
            }
            heatbed = Integer.parseInt(mHeatBed.getText().toString());
        }catch (Exception e){
            js = null;
            return js;
        }
        String token = getToken();
        try {
            obj.put("name",name);
            if (shape == 1){
                obj.put("shape",shape);
                obj.put("height",height);
                obj.put("radius",radius);
            }else{
                shape = 0;
                obj.put("shape",shape);
                obj.put("width",width);
                obj.put("height",height);
                obj.put("depth",depth);
            }
            obj.put("heatbed_exist",heatbed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (mAction.equals("add")){
                js.put("action","add");
            }else{
                js.put("action","update");
            }
            js.put("target","user_pprofile");
            js.put("object",obj);
            js.put("token",token);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }
}
