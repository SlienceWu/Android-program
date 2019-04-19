package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Activity.Fragment.SdCard;
import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.SpinnerAdapter;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.rabtman.wsmanager.WsManager;
import com.rabtman.wsmanager.listener.WsStatusListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okio.ByteString;

/**
 * Created by Administrator on 2018-03-26.
 */

public class ModelDetail extends BaseActivity {
    private String mModelName,mModelUrl,mModelStl;
    private String mModelNum;
    private String mModelWidth = "0",mModelDepth = "0",mModelHeight = "0";
    private ImageView mModelImg;
    private TextView mWidth,mDepth,mHeight;
    private Button mButtonPrint;
    private Spinner mSpinnerPrints,mSpinnerPrinters,mSpinnerMaterials;
    private EditText mModelScale;
    private TextView mBind,mAddPrinter,mAddMaterial,mAddSet;
    private ImageView mHideBar;

    private List<ListItem> listTypePrints = new ArrayList<>();
    private List<ListItem> listTypePrinters = new ArrayList<>();
    private List<ListItem> listTypeMaterials = new ArrayList<>();
    //详情id
    public static String mPrintId;
    private String mPrinterId,mMaterialId;
    private int mPrintNum=0,mPrinterNum=0,mMaterialNum=0;
    private TextView mSliceShow,mActionShow;
    private ProgressBar mSliceBar;
    //模型参数
    private String machineWidth = "0",
            machineDepth = "0",
            machineHeight = "0",
            machineRadius = "0",
            machineShape = "0",
            materialDiameter  = "0.4",
            extruderTemp = "175",
            bedTemp = "60";
    final String[] radioChecked = {"normal"};
    //ws 状态
    private boolean isExistWs = false;
    private RelativeLayout mShowLayout;
    //定时查询上传进度
    private Handler handler = new Handler();
    private Runnable runnable;
    //切片超时
    private Runnable cureRunnable;
    private int cureCheck = 0;
    //下载状态
    public int loadTaskState = 0;
    //下载链接
    public String mLoadUrl = "";
    //第二个等待框
    @BindView(R.id.show_progressBar_two) RelativeLayout mRelativeB;
    //第三个等待框
    @BindView(R.id.show_progressBar_three) RelativeLayout mRelativeC;
    @BindView(R.id.hide_progressBar_three) ImageView mHideBarC;
    @BindView(R.id.slice_progress_show_three) TextView mSliceShowC;
    @BindView(R.id.rect_progressBar_three) ProgressBar mSliceBarC;
    //stl显示
    @BindView(R.id.stl_webview)
    WebView mWebView;
    @BindView(R.id.btn_change) Button mBtnChange;
    @BindView(R.id.scrollView)
    ScrollView mScrollView;

    private QMUIDialog.MessageDialogBuilder qmuiDialog,getQmuiDialog;
    private boolean isExistDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_model_detail);
        ButterKnife.bind(this);

        mWebView.loadUrl("file:///android_asset/www/scan_wifi.html");//http://www.geeetech.com/3d_models/public/uploads/20171101/AA_porta_pegamento_en_barra_473658.stl
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);//打开js和安卓通信
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    //当页面加载完成后，调用js方法
//                mWebview.loadUrl("javascript:方法名(参数)");
                    JSONObject json = new JSONObject();
                    json.put("url", mModelStl);
                    mWebView.loadUrl("javascript:showMessage("+json.toString()+")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
        mBtnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWebView.getVisibility() == View.VISIBLE){
                    mWebView.setVisibility(View.INVISIBLE);
                } else{
                    mWebView.setVisibility(View.VISIBLE);
                }
            }
        });
        //触摸stl文件显示时，ScrollView禁止滑动
        mWebView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    mScrollView.requestDisallowInterceptTouchEvent(false);
                }else{
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        mModelName = getIntent().getStringExtra("modelName");
        mModelNum = getIntent().getStringExtra("modelNum");
        mModelUrl = getIntent().getStringExtra("modelUrl");
        mModelStl = getIntent().getStringExtra("modelStl");

        mShowLayout = findViewById(R.id.show_progressBar);
        mHideBar = findViewById(R.id.hide_progressBar);
        mSliceShow = findViewById(R.id.slice_progress_show);
        mSliceBar = findViewById(R.id.rect_progressBar);
        mActionShow = findViewById(R.id.action_show);
        mShowLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });
        mHideBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QMUIDialog.MessageDialogBuilder(ModelDetail.this)
                        .setMessage("Are you sure to cancel the slice?")
                        .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("OK", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                loadTaskState = 0;
                                chooseLayout(0);
                                handler.removeCallbacks(runnable);
                                handler.removeCallbacks(cureRunnable);

                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        mHideBarC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExistDialog = true;
                new QMUIDialog.MessageDialogBuilder(ModelDetail.this)
                        .setMessage("Are you sure to cancel the upload?")
                        .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        isExistDialog = false;
                                    }
                                });
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.dismiss();
                            }
                        })
                        .addAction("OK", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                loadTaskState = 0;
                                chooseLayout(0);
                                handler.removeCallbacks(runnable);
                                try {
                                    giveUp();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        isExistDialog = false;
                                    }
                                });
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        final RadioButton radioDraft = findViewById(R.id.radio_draft);
        final RadioButton radioNormal = findViewById(R.id.radio_normal);
        final RadioButton radioBest = findViewById(R.id.radio_best);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (radioDraft.getId() == checkedId) {
                    radioChecked[0] = "draft";
                }
                if (radioNormal.getId() == checkedId) {
                    radioChecked[0] = "normal";
                }
                if (radioBest.getId() == checkedId) {
                    radioChecked[0] = "best";
                }
            }
        });

        mWidth = findViewById(R.id.model_width);
        mDepth = findViewById(R.id.model_depth);
        mHeight = findViewById(R.id.model_height);
        mModelScale = findViewById(R.id.model_scale);
        mModelScale.setText("100");

        mModelScale.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            //一般我们都是在这个里面进行我们文本框的输入的判断，上面两个方法用到的很少
            @Override
            public void afterTextChanged(Editable s) {
                String scale = mModelScale.getText().toString();
                if ( scale.equals("")){
                    mWidth.setText(mModelWidth);
                    mDepth.setText(mModelDepth);
                    mHeight.setText(mModelHeight);
                    return;
                }
                int intScale = Integer.parseInt(scale);
                Float width,depth,height;
                width = Float.parseFloat(mModelWidth);
                depth = Float.parseFloat(mModelDepth);
                height = Float.parseFloat(mModelHeight);
                mWidth.setText(width*intScale/100+"");
                mDepth.setText(depth*intScale/100+"");
                mHeight.setText(height*intScale/100+"");
                //Pattern p = Pattern.compile("[0-9]*");
                //Matcher m = p.matcher(money);
                /*if (m.matches()) {
                } else {
                }*/
            }
        });
        mModelImg = findViewById(R.id.model_detail_img);
        Picasso.with(getBaseContext()).load(mModelUrl).into(mModelImg);//resize(80, 80).centerCrop().
        getModelDetail();
        //getQMUITipShow(mModelName,0);

        mSpinnerPrints = findViewById(R.id.spinner_bind);
        mSpinnerPrinters = findViewById(R.id.spinner_add_1);
        mSpinnerMaterials = findViewById(R.id.spinner_add_2);

        mButtonPrint = findViewById(R.id.button_print);
        mButtonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }*/
                if (listTypePrints.isEmpty()||listTypePrinters.isEmpty()||listTypeMaterials.isEmpty()){
                    getQMUITipShow("Params are wrong.",0);
                    return;
                }
                String scale = mModelScale.getText().toString();
                if ( scale.equals("")){
                    getQMUITipShow("please write the scale.",0);
                    return;
                }
                mPrintId = listTypePrints.get(mPrintNum).getNumber();
                mPrinterId = listTypePrinters.get(mPrinterNum).getNumber();
                mMaterialId = listTypeMaterials.get(mMaterialNum).getNumber();
                if ( mPrintId.isEmpty()||mPrinterId.isEmpty()||mMaterialId.isEmpty()){
                    getQMUITipShow("Params are wrong.",0);
                    return;
                }
                getPrinterState();
            }
        });
        //Spinner添加监听
        mSpinnerPrints.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mPrintNum = arg2;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mSpinnerPrinters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mPrinterNum = arg2;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mSpinnerMaterials.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mMaterialNum = arg2;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        mBind = findViewById(R.id.bind_printer);
        mAddPrinter = findViewById(R.id.add_printer);
        mAddMaterial = findViewById(R.id.add_material);
        mAddSet = findViewById(R.id.add_setting);
        mBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), BindPrinter.class);
                startActivity(intent);
            }
        });
        mAddPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("action","add");
                intent.setClass(getBaseContext(), AddPrinterProfile.class);
                startActivity(intent);
            }
        });
        mAddMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("action","add");
                intent.setClass(getBaseContext(), AddMaterialProfile.class);
                startActivity(intent);
            }
        });
        mAddSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout advance = findViewById(R.id.advance_more);
                if ( advance.getVisibility() == View.GONE){
                    advance.setVisibility(View.VISIBLE);
                }else{
                    advance.setVisibility(View.GONE);
                }
            }
        });
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getLoadState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this,1000);
            }
        };
    }
    //获取模型详情
    private void getModelDetail(){
        String url = Urls.SERVER + "/v1/modelbase/models/" + mModelNum +"/submodels/" + mModelName;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if( response.code() == 200){
                            try {
                                JSONObject obj = new JSONObject(response.body());
                                mModelWidth = obj.getString("submodel_width");
                                mModelDepth = obj.getString("submodel_depth");
                                mModelHeight = obj.getString("submodel_height");
                                mWidth.setText(mModelWidth);
                                mDepth.setText(mModelDepth);
                                mHeight.setText(mModelHeight);
                            }catch (Exception e){

                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response);
                    }
                });
    }
    //获取打印机列表
    public void getList() throws Exception {
        String encryStr = getUserID("",0);
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printers?token="+getToken();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try{
                                listTypePrints.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    ListItem item = new ListItem("null","");
                                    listTypePrints.add(item);
                                    initSpinner("prints");
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("serial_num");
                                    String mPrinterName = jsonObject.getString("name");
                                    ListItem item = new ListItem(mPrinterName,mSerialNumber);
                                    listTypePrints.add(item);
                                }
                                initSpinner("prints");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //获取打印机配置文件列表
    public void getProfileList() throws Exception{
        String encryStr = getUserID("",0);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try{
                                listTypePrinters.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    ListItem item = new ListItem("null","");
                                    listTypePrinters.add(item);
                                    initSpinner("printers");
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String id = jsonObject.getString("id");
                                    String name = jsonObject.getString("name");
                                    ListItem item = new ListItem(name,id);
                                    listTypePrinters.add(item);
                                }
                                initSpinner("printers");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //获取材料配置文件列表
    public void getMaterialList() throws Exception{
        String encryStr = getUserID("",0);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/material_profiles?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try{
                                listTypeMaterials.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    ListItem item = new ListItem("null","");
                                    listTypeMaterials.add(item);
                                    initSpinner("materials");
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String id = jsonObject.getString("id");
                                    String name = jsonObject.getString("name");
                                    ListItem item = new ListItem(name,id);
                                    listTypeMaterials.add(item);
                                }
                                initSpinner("materials");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }

    //初始化spinner
    private void initSpinner(String type){
        if (type.equals("prints")){
            Spinner spinner = findViewById(R.id.spinner_bind);
            SpinnerAdapter spinnerAdapter = new SpinnerAdapter(getBaseContext(),R.layout.model_spinner_item,listTypePrints);
            spinner.setAdapter(spinnerAdapter);
        }else if(type.equals("printers")){
            Spinner spinner = findViewById(R.id.spinner_add_1);
            SpinnerAdapter spinnerAdapter = new SpinnerAdapter(getBaseContext(),R.layout.model_spinner_item,listTypePrinters);
            spinner.setAdapter(spinnerAdapter);
        }else if(type.equals("materials")){
            Spinner spinner = findViewById(R.id.spinner_add_2);
            SpinnerAdapter spinnerAdapter = new SpinnerAdapter(getBaseContext(),R.layout.model_spinner_item,listTypeMaterials);
            spinner.setAdapter(spinnerAdapter);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if ( getState().equals("logout") && MainActivity.isNetWork != 1){
            //getQMUITipShow("please log in",0);
            return;
        }
        if ( mShowLayout.getVisibility() == View.VISIBLE||mRelativeB.getVisibility() == View.VISIBLE||mRelativeC.getVisibility() == View.VISIBLE){
            if( mShowLayout.getVisibility() == View.VISIBLE){
                try{
                    cureCheck = 0;
                    handler.postDelayed(cureRunnable,1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return;
            }
            try {
                handler.postDelayed(runnable,0);
                //getLoadState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try{
            getList();
            getProfileList();
            getMaterialList();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //获取选中打印机状态
    public void getPrinterState(){
        String serialNumber = "";
        serialNumber = mPrintId;
        Context context = getBaseContext();
        String encryStr = getUserID("",0);
        String encryStrId = getUserID(serialNumber,1);;
        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"?token=" + getToken();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                String bedSetTemp,bedNowTemp,exSetTemp,exNowTemp;
                                if ( state.equals("2")){
                                    getProfileDetailList(mPrinterId);
                                }else if ( state.equals("0")){
                                    getQMUITipShow("The printer is offline.",0);
                                }else if ( state.equals("1")){
                                    getQMUITipShow("The printer is logout.",0);
                                }else if ( state.equals("3")|| state.equals("4")){
                                    getQMUITipShow("The printer is printing.",0);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //获取选中打印机配置文件
    public void getProfileDetailList(String serialNumber){
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles/"+ encryStrNum +"?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            String scale = mModelScale.getText().toString();
                            //解析json
                            CommonJSONParser commonJSONParser = new CommonJSONParser();
                            Map<String, Object> result = commonJSONParser.parse(res);
                            String shape = result.get("shape").toString();
                            machineShape = shape;
                            String heatbed = result.get("heatbed_exist").toString();
                            if ( shape.equals("0")){
                                String width = result.get("width").toString();
                                String depth = result.get("depth").toString();
                                String height = result.get("height").toString();
                                machineWidth = width;
                                machineDepth = depth;
                                machineHeight = height;
                                float a,b,c;
                                a = Float.parseFloat(mWidth.getText().toString())/Integer.parseInt(width);
                                b = Float.parseFloat(mDepth.getText().toString())/Integer.parseInt(depth);
                                c = Float.parseFloat(mHeight.getText().toString())/Integer.parseInt(height);
                                if ( a>1 || b>1 || c>1){
                                    getQMUITipShow("The model size is too large for the selected printer. Please resize.",0);
                                    return;
                                }
                                getMaterialDetailList(mMaterialId);
                            }else{
                                String radius = result.get("radius").toString();
                                String height = result.get("height").toString();
                                machineRadius = radius;
                                machineHeight = height;
                                double a,b,c;
                                a = Float.parseFloat(mWidth.getText().toString())/Integer.parseInt(radius)*1.4;
                                b = Float.parseFloat(mDepth.getText().toString())/Integer.parseInt(radius)*1.4;
                                c = Float.parseFloat(mHeight.getText().toString())/Integer.parseInt(height);
                                if ( a>1 || b>1 || c>1){
                                    getQMUITipShow("The model size is too large for the selected printer. Please resize.",0);
                                    return;
                                }
                                getMaterialDetailList(mMaterialId);
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //获取选中材料配置文件
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
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            //解析json
                            //CommonJSONParser commonJSONParser = new CommonJSONParser();
                            //Map<String, Object> result = commonJSONParser.parse(res);
                            try {
                                JSONObject result = new JSONObject(res);
                                String diameter = null,extruder_temp = null,bed_temp = null;
                                diameter = result.get("diameter").toString();
                                bed_temp = result.get("bed_temp").toString();
                                extruder_temp = result.get("extruder_temp").toString();
                                materialDiameter = diameter;
                                extruderTemp = extruder_temp;
                                bedTemp = bed_temp;
                                startCure();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //切片
    //private final MockWebServer mockWebServer = new MockWebServer();
    //private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    private void startCure(){
        if ( isExistWs ){
            getQMUITipShow("Cura engine is running",0);
            return;
        }
        //mShowLayout.setVisibility(View.VISIBLE);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .pingInterval(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        final WsManager wsManager = new WsManager.Builder(this)
                .wsUrl("ws://47.88.84.109:3389/")
                .client(okHttpClient)
                .needReconnect(false)
                .build();
        wsManager.startConnect();
        wsManager.setWsStatusListener(new WsStatusListener() {
            @Override
            public void onOpen(okhttp3.Response response) {
                super.onOpen(response);
                chooseLayout(1);
                isExistWs = true;
                cureCheck = 0;
                cureRunnable = new Runnable() {
                    @Override
                    public void run() {
                        cureCheck = cureCheck + 1;
                        if ( cureCheck > 120){
                            wsManager.stopConnect();
                            isExistWs = false;
                            mShowLayout.setVisibility(View.GONE);
                            handler.removeCallbacks(this);
                            getQMUITipShow("Time out for slicing.",0);
                        }
                        handler.postDelayed(this,1000);
                    }
                };
                handler.postDelayed(cureRunnable,1000);
                String need = getCureParams().toString();
                wsManager.sendMessage(need);
            }

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
                try {
                    JSONObject data = new JSONObject(text);
                    JSONObject insParam = new JSONObject(data.get("insParam").toString());
                    try{
                        if ( insParam.getString("state").equals("2")){
                            //开始切片
                        }else if ( insParam.getString("state").equals("3")){
                            cureCheck = 0;
                            String name = insParam.getString("file");
                            String progress = insParam.getString("prog");
                            try{
                                Double a = Double.valueOf(progress)*100;
                                int b = a.intValue();
                                mSliceBar.setProgress(b);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try{
                                Double a = Double.valueOf(progress)*100;
                                String b = a.intValue()+"%";
                                mSliceShow.setText(b);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if ( insParam.getString("state").equals("4")){
                            isExistWs = false;
                            chooseLayout(0);
                            //切片完成
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                        if ( insParam.getString("modelUrl").indexOf("user") > 0){
                            chooseLayout(2);
                            try{
                                handler.removeCallbacks(cureRunnable);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            mLoadUrl = insParam.getString("modelUrl");
                            upLoad(insParam.getString("modelUrl"));
                            wsManager.stopConnect();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //isExistWs = false;
                    //mShowLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onMessage(ByteString bytes) {
                super.onMessage(bytes);
            }

            @Override
            public void onReconnect() {
                super.onReconnect();
            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);
                isExistWs = false;
                mShowLayout.setVisibility(View.GONE);
                try{
                    handler.removeCallbacks(cureRunnable);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t, okhttp3.Response response) {
                super.onFailure(t, response);
                isExistWs = false;
                try{
                    handler.removeCallbacks(cureRunnable);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //wsManager.stopConnect();
    }
    //切片参数
    public JSONObject getCureParams(){
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
        String email = FirstUser.get(0).toString();
        try {
            obj.put("heatbedExist","1");
            obj.put("modelUrl",mModelStl);
            obj.put("modelName",mModelName);
            obj.put("modelNewName","");
            EditText model_scale = findViewById(R.id.model_scale);
            int scaleInt = Integer.valueOf(model_scale.getText().toString());
            Double modelScale = scaleInt*0.01;
            obj.put("modelScale",modelScale);
            obj.put("nozzleDiameter",0.4);
            obj.put("materialDiameter",Float.parseFloat(materialDiameter));
            obj.put("extruderTemp",Float.parseFloat(extruderTemp));
            obj.put("bedTemp",Float.parseFloat(bedTemp));
            obj.put("machineWidth",Float.parseFloat(machineWidth));
            obj.put("machineDepth",Float.parseFloat(machineDepth));
            obj.put("machineHeight",Float.parseFloat(machineHeight));
            obj.put("machineRadius",Float.parseFloat(machineRadius));
            obj.put("machineShape",Float.parseFloat(machineShape));

            if ( radioChecked[0].equals("draft")){
                obj.put("layerHeight",0.2);
                obj.put("wallThickness",0.4);
                obj.put("infillRate",10);
                obj.put("outlineSpeed",60);
            }else if( radioChecked[0].equals("best")){
                obj.put("layerHeight",0.2);
                obj.put("wallThickness",0.8);
                obj.put("infillRate",20);
                obj.put("outlineSpeed",60);
            }else{
                obj.put("layerHeight",0.1);
                obj.put("wallThickness",0.8);
                obj.put("infillRate",20);
                obj.put("outlineSpeed",30);
            }
            obj.put("handler",0);
            CheckBox supportBox = findViewById(R.id.support_box);
            if (supportBox.isChecked()){
                obj.put("modelSupport",1);
            }else{
                obj.put("modelSupport",0);
            }
            EditText materialFlow = findViewById(R.id.material_flow);
            int flowInt = Integer.valueOf(materialFlow.getText().toString());
            obj.put("materialFlow",flowInt);
        } catch (JSONException e) {
            return js = null;
        }
        try {
            js.put("reqDev","Android");
            js.put("reqAcc",email);
            js.put("insType",0);
            js.put("insParam",obj);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }

    //切片完成后上传
    public void upLoad(String url) throws Exception{
        String server = Urls.USER_COMMON() +getUserID("",0)+"/printers/"+getUserID(mPrintId,1)+"?token=" + getToken();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action","upload");
        jsonObject.put("value",url);

        OkGo.<String>post(server)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( response.code() == 200){

                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( response.code() == 504){
                            return;
                        }
                        handler.removeCallbacks(runnable);
                        chooseLayout(0);

                        if ( response.code() == 500){
                            getQMUITipShow("Download error.",0);
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
        handler.postDelayed(runnable,1000);
    }
    //获取上传进度
    public void getLoadState() throws Exception{
        String serialNumber = "";
        try{
            serialNumber = mPrintId;
        }catch (Exception e){
            e.printStackTrace();
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
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                String taskFile;
                                int taskState = result.getInt("task_state");
                                RelativeLayout progressAll = findViewById(R.id.progress_all);
                                if ( taskState == 0){
                                    if ( mRelativeB.getVisibility() == View.GONE){
                                        chooseLayout(0);
                                    }

                                    if ( loadTaskState == 2){
                                        loadTaskState = 0;
                                        chooseLayout(0);
                                        handler.removeCallbacks(runnable);
                                        getQMUITipShow("Skip to print.",1);
                                    }
                                    return;
                                }else if ( taskState == 4){
                                    loadTaskState = 0;
                                    chooseLayout(0);

                                    getQMUITipShow("SD card exception",0);
                                    handler.removeCallbacks(runnable);
                                    return;
                                }else if ( taskState == 1){
                                    chooseLayout(2);
                                    return;
                                }else if ( taskState == 2){
                                    try {
                                        chooseLayout(3);
                                        loadTaskState = 2;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }else if ( taskState == 3){
                                    loadTaskState = 0;
                                    chooseLayout(0);
                                    handler.removeCallbacks(runnable);
                                    getQMUITipShow("Upload fail.",0);
                                    return;
                                }

                                int taskFileLength,taskFileUpload;
                                taskFile = result.getString("task_file");
                                taskFileLength = result.getInt("task_file_length");
                                taskFileUpload = result.getInt("task_file_uploaded");
                                SdCard.mPrintFileName = taskFile;
                                if ( taskFileLength == 0){
                                    return;
                                }
                                try{
                                    int a = taskFileUpload*100/taskFileLength;
                                    mSliceBarC.setProgress(a);
                                    String b = a +"%";
                                    mSliceShowC.setText(b);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( ModelDetail.this.isFinishing()) {
                            return;
                        }
                        if ( response.code() == 504){
                            return;
                        }
                        loadTaskState = 0;
                        chooseLayout(0);
                        handler.removeCallbacks(runnable);
                        getErrorRespone(response);
                    }
                });
    }

    //取消上传
    public void giveUp() throws JSONException {
        String serialNumber = "";
        try{
            serialNumber = mPrintId;
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID( serialNumber,1);
        String url = Urls.USER_COMMON() + encryStr +"/printers/" + encryStrNum +"?token=" +getToken();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action","stop_upload");
        jsonObject.put("value",mLoadUrl);

        OkGo.<String>post(url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        loadTaskState = 0;
                        chooseLayout(0);
                        ToastUtil.showToast(getBaseContext(),"Cancel success.");
                    }
                    @Override
                    public void onError(Response<String> response) {
                        ToastUtil.showToast(getBaseContext(),"An unkonwn error.");
                    }
                });
    }
    //弹窗选择
    private void chooseLayout(int num){
        switch (num){
            case 0:
                mShowLayout.setVisibility(View.GONE);
                mRelativeB.setVisibility(View.GONE);
                mRelativeC.setVisibility(View.GONE);
                initLayout();
                break;
            case 1:
                mShowLayout.setVisibility(View.VISIBLE);
                mRelativeB.setVisibility(View.GONE);
                mRelativeC.setVisibility(View.GONE);
                break;
            case 2:
                mShowLayout.setVisibility(View.GONE);
                mRelativeB.setVisibility(View.VISIBLE);
                mRelativeC.setVisibility(View.GONE);
                break;
            case 3:
                mShowLayout.setVisibility(View.GONE);
                mRelativeB.setVisibility(View.GONE);
                mRelativeC.setVisibility(View.VISIBLE);
                break;
        }
    }
    //弹窗初始化
    private void initLayout(){
        mSliceBar.setProgress(0);
        mSliceShow.setText("0%");
        mSliceBarC.setProgress(0);
        mSliceShowC.setText("0%");
    }

    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
        try{
            handler.removeCallbacks(cureRunnable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (mRelativeC.getVisibility() == View.VISIBLE && !isExistDialog) {
                new QMUIDialog.MessageDialogBuilder(ModelDetail.this)
                        .setMessage("Are you sure to cancel the upload?")
                        .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        isExistDialog = false;
                                    }
                                });
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.dismiss();
                            }
                        })
                        .addAction("OK", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                loadTaskState = 0;
                                chooseLayout(0);
                                handler.removeCallbacks(runnable);
                                try {
                                    giveUp();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        isExistDialog = false;
                                    }
                                });
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.dismiss();
                            }
                        })
                        .show();
                //ToastUtil.showToast(getBaseContext(), "Please wait while uploading.");
            } else{
                isExistDialog = false;
                onBackPressed();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void addLeftBackTopBar() throws Exception{
        mTopBar = (QMUITopBar) findViewById(R.id.topbar);
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRelativeC.getVisibility() == View.VISIBLE && !isExistDialog) {
                    new QMUIDialog.MessageDialogBuilder(ModelDetail.this)
                            .setMessage("Are you sure to cancel the upload?")
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            isExistDialog = false;
                                        }
                                    });
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.dismiss();
                                }
                            })
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    loadTaskState = 0;
                                    chooseLayout(0);
                                    handler.removeCallbacks(runnable);
                                    try {
                                        giveUp();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            isExistDialog = false;
                                        }
                                    });
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    //ToastUtil.showToast(getBaseContext(), "Please wait while uploading.");
                    return;
                } else{
                    isExistDialog = false;
                    onBackPressed();
                }
                finish();
            }
        });
    }
}
/*
下载超时
num = 0;
if (loadTaskState == 0){
    loadRunnable = new Runnable() {
        @Override
        public void run() {
            num = num + 1;
            if ( num > 120){
                chooseLayout(0);
                handler.removeCallbacks(this);
                handler.removeCallbacks(runnable);
                getQMUITipShow("Time out for uploading.",0);
            }
            handler.postDelayed(this,1000);
        }
    };
    handler.postDelayed(loadRunnable,1000);
}
*/