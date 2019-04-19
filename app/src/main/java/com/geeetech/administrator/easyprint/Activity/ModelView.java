package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.GlideImageLoader;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.youth.banner.Banner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018-03-26.
 */

public class ModelView extends BaseActivity{
    public static ModelView instance = null;
    List<?> mImages=new ArrayList<>();
    private ArrayList<String> list_path;
    public static String modelName,modelId,modelPath,modelWidth,modelDepth,modelHeight,modelThumb,modelResource,modelDescription;
    //public String mModelDescription;
    private String mModelNum,mModelStl;
    private TextView mModelInfo,mModelInfoButton,mModelFilesButton;
    private ListView mModelFiles;
    private ScrollView mMainScroll;
    //private Button mPrint;
    private List<ListItem> list = new ArrayList<>();
    private ArrayList<String> list_stl= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_model_view);
        instance = this;
        mModelNum = getIntent().getStringExtra("modelNum");
        //放图片地址的集合
        list_path = new ArrayList<>();

        mMainScroll = findViewById(R.id.main_scroll);
        mModelInfo = findViewById(R.id.model_info);
        mModelFiles = findViewById(R.id.model_files);
        mModelInfoButton = findViewById(R.id.model_info_button);
        mModelFilesButton = findViewById(R.id.model_files_button);
        /*mPrint = findViewById(R.id.button_print);
        mPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(),ModelDetail.class);
                startActivity(intent);
            }
        });*/
        mModelInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModelInfo.setVisibility(View.VISIBLE);
                mModelFiles.setVisibility(View.GONE);
                mModelInfoButton.setTextColor(getResources().getColor(R.color.app_main_show_blue));
                mModelInfoButton.setBackgroundColor(getResources().getColor(R.color.app_main_white));
                mModelFilesButton.setTextColor(getResources().getColor(R.color.app_main_white));
                mModelFilesButton.setBackgroundColor(getResources().getColor(R.color.app_main_show_blue));
            }
        });
        mModelFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mModelFiles.setVisibility(View.VISIBLE);
                mModelInfo.setVisibility(View.GONE);
                mModelFilesButton.setTextColor(getResources().getColor(R.color.app_main_show_blue));
                mModelFilesButton.setBackgroundColor(getResources().getColor(R.color.app_main_white));
                mModelInfoButton.setTextColor(getResources().getColor(R.color.app_main_white));
                mModelInfoButton.setBackgroundColor(getResources().getColor(R.color.app_main_show_blue));
            }
        });
        mModelFiles.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    mMainScroll.requestDisallowInterceptTouchEvent(false);
                }else{
                    mMainScroll.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });
    }

    @Override
    public void onResume(){
        try {
            if ( mModelNum!= null){
                initModelDetail();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }
    //初始化模型详情
    public void initModelDetail() throws Exception{
        String url = Urls.SERVER + "/v1/modelbase/models/" + mModelNum;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        int code = response.code();
                        if (response.code() == 200){
                            try {
                                JSONObject obj = new JSONObject(response.body());
                                modelId = obj.getString("model_id");
                                modelPath = obj.getString("model_path");
                                modelThumb = obj.getString("model_thumb");
                                modelResource = obj.getString("model_resource");
                                modelDescription = obj.getString("model_description");
                                JSONArray jsonThumb = new JSONArray(modelThumb);
                                JSONArray jsonResource = new JSONArray(modelResource);
                                if ( jsonThumb.length() == 0){
                                    return;
                                }
                                list_path.clear();
                                list.clear();
                                list_stl.clear();
                                for ( int i=0;i<jsonThumb.length();i++){
                                    String urlThumb = Urls.SERVER_IMG + jsonThumb.get(i).toString();
                                    String urlResource = Urls.SERVER_IMG + modelPath +jsonResource.get(i).toString();
                                    if ( urlResource.contains(".stl")){
                                        mModelStl = urlResource;
                                        list_stl.add(mModelStl);
                                        list_path.add(urlThumb);
                                        ListItem item = new ListItem(urlThumb,jsonResource.get(i).toString());
                                        list.add(item);
                                    }else{
                                        list_path.add(urlResource);
                                    }
                                }
                                initModelImg();
                                initModelList();
                            } catch (JSONException e) {
                                getQMUITipShow("model detail error",0);
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response);
                    }
                });
    }

    //初始化图片及图片信息
    private void initModelImg(){
        mModelInfo.setText(Html.fromHtml(modelDescription));
        //轮播图
        Banner banner = (Banner) findViewById(R.id.banner);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //资源文件
        //url = "https://raw.githubusercontent.com/yipianfengye/android-adDialog/master/images/testImage1.png
        Integer[] urls ={R.drawable.model_scroll_1,R.drawable.model_scroll_1,R.drawable.model_scroll_1};
        List list = Arrays.asList(urls);
        mImages = new ArrayList(list);
        //设置自动轮播，默认为true
        banner.isAutoPlay(false);
        //设置图片集合
        banner.setImages(list_path);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }
    //初始化stl模型列表
    private void initModelList(){
        ItemAdapter itemAdapter = new ItemAdapter(ModelView.this, R.layout.model_stl_item, list);
        ListView listView = (ListView) findViewById(R.id.model_files);
        listView.setAdapter(itemAdapter);

        itemAdapter.setOnItemEditClickListener(new ItemAdapter.onItemEditListener() {
            @Override
            public void onEditClick(int i) {
                String state = getState();
                if ( state.equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                String modelName = list.get(i).getNumber();
                String modelUrl = list.get(i).getName();
                String modelStl = list_stl.get(i);
                Intent intent = new Intent();
                intent.putExtra("modelStl",modelStl);
                intent.putExtra("modelUrl",modelUrl);
                intent.putExtra("modelName",modelName);
                intent.putExtra("modelNum",mModelNum);
                intent.setClass(getBaseContext(),ModelDetail.class);
                startActivity(intent);
            }
        });

        QMUITopBar qmuiTopBar = findViewById(R.id.topbar);
        Banner banner = findViewById(R.id.banner);
        TextView button = findViewById(R.id.model_info_button);
        int heightAll = qmuiTopBar.getHeight()+banner.getHeight()+button.getHeight()+getStatusBarHeight(getBaseContext());

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int heigthP = dm.heightPixels-heightAll;
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = heigthP;
        listView.setLayoutParams(params);

    }
    //获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}