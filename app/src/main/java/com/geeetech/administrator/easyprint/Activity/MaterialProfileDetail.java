package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018-02-06.
 */

public class MaterialProfileDetail extends BaseActivity {
    @BindView(R.id.printer_name) EditText mPrinterName;
    @BindView(R.id.test_res) EditText mTestRes;
    @BindView(R.id.get_list) Button mGetList;
    @BindView(R.id.get_profile_list) Button mGetProfileList;
    @BindView(R.id.get_profile_detail_list) Button mGetProfileDetailList;
    @BindView(R.id.get_material_list) Button mGetMaterialList;
    @BindView(R.id.get_material_detail_list) Button mGetMaterialDetailList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_profile_detail);
        ButterKnife.bind(this);

        mGetList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                getListDetail(mPrinterName.getText().toString());
            }
        });
        mGetProfileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                getProfileList();
            }
        });
        mGetProfileDetailList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                getProfileDetailList(mPrinterName.getText().toString());
            }
        });
        mGetMaterialList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                getMaterialList();
            }
        });
        mGetMaterialDetailList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( getState().equals("logout")){
                    getQMUITipShow("please log in",0);
                    return;
                }
                getMaterialDetailList(mPrinterName.getText().toString());
            }
        });
    }

    //获取打印机详情信息
    public void getListDetail(String serialNumber){
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printers/"+ encryStrNum +"?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        if ( responseCode == 200){
                            mTestRes.setText(res);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        getQMUITipShow("Request error.",0);
                    }
                });
    }
    //获取打印机配置文件列表
    public void getProfileList(){
        String encryStr = getUserID("",0);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        mTestRes.setText(res);
                        if ( responseCode == 200){
                            try{
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("id");
                                    String mPrinterName = jsonObject.getString("name");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        getQMUITipShow("Request error.",0);
                    }
                });
    }
    //获取某个打印机配置文件
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
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        mTestRes.setText(res);
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        getQMUITipShow("Request error.",0);
                    }
                });
    }
    //获取材料配置文件列表
    public void getMaterialList(){
        String encryStr = getUserID("",0);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/material_profiles?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        mTestRes.setText(res);
                        if ( responseCode == 200){
                            try{
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("id");
                                    String mPrinterName = jsonObject.getString("name");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        getQMUITipShow("Request error.",0);
                    }
                });
    }
    //获取某个材料配置文件
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
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MaterialProfileDetail.this.isFinishing()) {
                            return;
                        }
                        getQMUITipShow("Request error.",0);
                    }
                });
    }

}
