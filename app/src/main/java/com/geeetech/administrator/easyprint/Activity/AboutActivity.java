package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-02-22.
 */

public class AboutActivity extends BaseActivity {

    private TextView mCurrentVerison,mNewVersion,mNewDataInfo;
    private ImageView mImg;
    private LinearLayout mDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfragment_about);

        mCurrentVerison = findViewById(R.id.current_version);
        mNewVersion = findViewById(R.id.new_version);
        mImg = findViewById(R.id.img_version);
        mDownload = findViewById(R.id.download);
        mNewDataInfo = findViewById(R.id.new_data_info);
    }

    @Override
    public void onResume(){
        checkVersion();
        super.onResume();
    }
    //查询版本号
    private void checkVersion(){
        String url = Urls.SERVER + "/v1/app/info";
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (AboutActivity.this.isFinishing()){
                            return;
                        }
                        if (response.code() == 200){
                            try {
                                JSONObject jsonObject = new JSONObject(response.body());
                                JSONObject newObject = new JSONObject(jsonObject.getString("android"));
                                String newVersion = newObject.getString("newest_version");
                                String updateInfo = newObject.getString("update_info");
                                JSONArray updateList = newObject.getJSONArray("update_list");
                                mNewVersion.setText(newVersion);

                                if (!mCurrentVerison.getText().toString().equals(newVersion)){
                                    mImg.setVisibility(View.VISIBLE);
                                    mNewDataInfo.setVisibility(View.VISIBLE);
                                    if (!updateInfo.equals("")){
                                        String allString = updateInfo;
                                        for (int i=0;i<updateList.length();i++){
                                            allString = allString + "\n" + i + ". " + updateList.get(i).toString();
                                        }
                                        mNewDataInfo.setText(allString);
                                    }
                                    mDownload.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            openDefaultBrowser("http://www.geeetech.com/firmware/EasyPrint_3D_Android.apk");
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (AboutActivity.this.isFinishing()||response.code()==-1||response.code()==502){
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
}
