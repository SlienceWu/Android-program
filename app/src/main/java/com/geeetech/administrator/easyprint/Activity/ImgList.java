package com.geeetech.administrator.easyprint.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.geeetech.administrator.easyprint.Activity.Fragment.Cloud;
import com.geeetech.administrator.easyprint.AutoLoadListener;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.GridItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.ModelListItem;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-03-24.
 */

public class ImgList extends BaseActivity {
    public static ImgList instance = null;
    public static List<ModelListItem> modelList = new ArrayList<>();
    public int currentPage = 1;
    private int mCategoryID;

    GridView gridView;
    private EditText keyWord;
    private Button searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_view);
        instance = this;
        mCategoryID = getIntent().getIntExtra("categoryID",0);

        //keyWord = findViewById(R.id.keyword);
        //searchBtn = findViewById(R.id.search);

//        searchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String keyword = keyWord.getText().toString();
//                if (keyword.length() < 5){
//                    getQMUITipShow("please write more",0);
//                    return;
//                }
//                searchModel(keyword);
//            }
//        });
    }

    @Override
    public void onResume(){
        try {
            if ( AutoLoadListener.state == null){
                modelList.clear();
                currentPage = 1;
                initModelList(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }
    //初始化模型列表
    public void initModelList(int num) throws Exception{
        //GET /v1/modelbase/categories/{categoryID}/pages/{num}
        String categoryID = Cloud.mainModelList.get(mCategoryID).getNumber();
        String url = Urls.SERVER + "/v1/modelbase/categories/" + categoryID + "/pages/" +num;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( response.code() == 200){
                            try{
                                JSONArray jsonArray = new JSONArray(response.body());
                                //getQMUITipShow(response.body(),0);
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                String modelName = "",modelId = "",modelThumb = "",modelDescription = "";
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    modelName = jsonObject.getString("model_name");
                                    modelId = jsonObject.getString("model_id");
                                    modelThumb = jsonObject.getString("model_thumb");
                                    modelDescription = "";//jsonObject.getString("model_description");
                                    ModelListItem item = new ModelListItem(modelName,modelId,modelThumb,modelDescription);
                                    modelList.add(item);
                                }
                                initList();
                            }catch (Exception e){
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
    //搜索指定模型名称
//    public void searchModel(String keyword){
//
//        String categoryID = Cloud.mainModelList.get(mCategoryID).getNumber();
//        final String url = Urls.SERVER + "/v1/modelbase/categories/" + categoryID + "?keyword=" + keyword;
//        OkGo.<String>get(url)
//                .tag(this)
//                .execute(new StringCallback(){
//                    @Override
//                    public void onSuccess(Response<String> response){
//                        int code = response.code();
//                        if (code == 200){
//                            modelList.clear();
//                            try{
//                                JSONArray jsonArray = new JSONArray(response.body());
//                                if ( jsonArray.length() == 0){
//                                    getQMUITipShow("Find nothing",0);
//                                    return;
//                                }
//                                String modelName = "",modelId = "",modelThumb = "",modelDescription = "";
//                                for (int i=0; i < jsonArray.length(); i++){
//                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                    modelName = jsonObject.getString("model_name");
//                                    modelId = jsonObject.getString("model_id");
//                                    modelThumb = jsonObject.getString("model_thumb");
//                                    modelDescription = "";//jsonObject.getString("model_description");
//                                    ModelListItem item = new ModelListItem(modelName,modelId,modelThumb,modelDescription);
//                                    modelList.add(item);
//                                }
//                                initList();
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    @Override
//                    public void onError(Response<String> response) {
//                        ToastUtil.showToast(getBaseContext(),url + "*****" + response.toString() + "===" + response.code() + "***" + response.body());
//                        return;
//                        //getErrorRespone(response);
//                    }
//                });
//    }
    //UI
    public void initList(){
        //String modelName,String modelId,String modelThumb,String modelDescription
        GridItemAdapter gridAdapter = new GridItemAdapter(ImgList.this,R.layout.model_gridview_item,modelList);
        //gridAdapter.notifyDataSetChanged();
        gridView = findViewById(R.id.img_list);
        gridView.setAdapter(gridAdapter);

        AutoLoadListener autoLoadListener = new AutoLoadListener(callBack);
        gridView.setOnScrollListener(autoLoadListener);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = modelList.get(i).getNumber();

                Intent intent = new Intent();
                intent.putExtra("modelNum",id);
                intent.setClass(getBaseContext(),ModelView.class);
                startActivity(intent);
            }
        });
        try{
            gridView.onRestoreInstanceState(AutoLoadListener.state);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    AutoLoadListener.AutoLoadCallBack callBack = new AutoLoadListener.AutoLoadCallBack() {

        public void execute() {
            //这段代码是用来请求下一页数据的
            if (modelList.size() < 60){
                return;
            }
            currentPage++;
            try {
                initModelList(currentPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
