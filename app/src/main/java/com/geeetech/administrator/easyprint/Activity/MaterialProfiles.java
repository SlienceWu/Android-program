package com.geeetech.administrator.easyprint.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-01.
 */
public class MaterialProfiles extends BaseActivity {
    ImageView mEdit;
    ImageView mDelete;
    Button mAddMaterial;
    boolean mState = true;
    private List<ListItem> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_profile);

        //跳转
        mAddMaterial = findViewById(R.id.button_add_material);
        mAddMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String[] items = new String[]{"Custom material profile", "PLA", "ABS"};
                new QMUIDialog.MenuDialogBuilder(MaterialProfiles.this)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(getBaseContext(), "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                if ( getState().equals("logout")){
                                    getQMUITipShow("please log in",0);
                                    return;
                                }
                                if ( which == 0){
                                    Intent intent = new Intent();
                                    intent.putExtra("action","add");
                                    intent.setClass(MaterialProfiles.this, AddMaterialProfile.class);
                                    startActivity(intent);
                                }else if ( which == 1){
                                    addMaterialProfile("PLA");
                                }else if ( which == 2){
                                    addMaterialProfile("ABS");
                                }
                            }
                        })
                        .show();

                /*Intent intent = new Intent();
                intent.putExtra("action","add");
                intent.setClass(MaterialProfiles.this, AddMaterialProfile.class);
                startActivity(intent);*/
            }
        });
    }
    //获取材料配置文件列表
    public void getMaterialList(){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String encryStr = getUserID("",0);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/material_profiles?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( MaterialProfiles.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        if ( responseCode == 200){
                            try{
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    initList();
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String id = jsonObject.getString("id");
                                    String name = jsonObject.getString("name");
                                    ListItem item = new ListItem(name, id);
                                    list.add(item);
                                }
                                initList();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MaterialProfiles.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //初始化打印机列表
    public void initList(){
        ItemAdapter itemAdapter = new ItemAdapter(MaterialProfiles.this, R.layout.print_list_material_item, list);
        ListView listView = (ListView) findViewById(R.id.material_list);
        listView.setAdapter(itemAdapter);

        //编辑修改页
        itemAdapter.setOnItemEditClickListener(new ItemAdapter.onItemEditListener() {
            @Override
            public void onEditClick(int i) {
                final String mPrinterName = list.get(i).getName();
                final String mSerialNumber = list.get(i).getNumber();
                Intent intent = new Intent();
                intent.putExtra("action","update");
                intent.putExtra("name",mPrinterName);
                intent.putExtra("id",mSerialNumber);
                intent.setClass(MaterialProfiles.this, AddMaterialProfile.class);
                startActivity(intent);
            }
        });
        //删除
        itemAdapter.setOnItemDeleteClickListener(new ItemAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                if ( mState){
                    final String mSerialNumber = list.get(i).getNumber();
                    deleteDialog(mSerialNumber);
                }else{
                    getQMUITipShow("Please wait",0);
                }
            }
        });
    }
    //删除弹窗
    public void deleteDialog(final String serialNumber){
        new QMUIDialog.MessageDialogBuilder(MaterialProfiles.this)
                .setMessage("Delete this Material profile?")
                .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("OK", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        mState = false;
                        deleteMaterial(serialNumber,dialog);
                    }
                })
                .show();
    }
    //删除请求
    public void deleteMaterial(String serialNumber, final QMUIDialog dialog){
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/material_profiles/" + encryStrNum;
        OkGo.<String>delete(url)
                .tag(this)
                .upJson(getDeleteMaterial())
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        int responseCode = response.code();
                        String res = response.body();
                        if ( responseCode == 200){
                            mState = true;
                            getQMUITipShow("Delete Success.",0);
                            dialog.dismiss();
                            onResume();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        mState = true;
                        int responseCode = response.code();
                        String res = response.body();
                        getErrorRespone(response);
                    }
                });
    }
    //请求参数
    public JSONObject getDeleteMaterial(){
        JSONObject js=new JSONObject();
        String token = getToken();
        try {
            js.put("action","delete");
            js.put("target","user_mprofile");
            js.put("token",token);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }
    //初始化
    public void onResume () {
        super.onResume();
        try {
            list.clear();
            if ( list.isEmpty()){
                if (MainActivity.isNetWork == 1){
                    getMaterialList();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //添加默认材料
    public void addMaterialProfile(String type){
        String encryStr = getUserID("",0);
        String url = Urls.GET_PRINTER_LIST() + encryStr;
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        if (type.equals("PLA")){
            try {
                obj.put("name","PLA");
                obj.put("diameter",1.75);
                obj.put("extruder_temp",200);
                obj.put("bed_temp",70);
            } catch (JSONException e) {
                e.printStackTrace();
                getQMUITipShow("Add fail",0);
                return;
            }
        }else{
            try {
                obj.put("name","ABS");
                obj.put("diameter",1.75);
                obj.put("extruder_temp",230);
                obj.put("bed_temp",90);
            } catch (JSONException e) {
                e.printStackTrace();
                getQMUITipShow("Add fail",0);
                return;
            }
        }

        try {
            js.put("action","add");
            js.put("target","user_mprofile");
            js.put("object",obj);
            js.put("token",getToken());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            getQMUITipShow("Add fail",0);
            return;
        }
        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        if( MaterialProfiles.this.isFinishing()){
                            return;
                        }
                        int responseCode = response.code();
                        String res = response.body();
                        if ( responseCode == 201){
                            getQMUITipShow("Add Success.",0);
                            onResume();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if( MaterialProfiles.this.isFinishing()){
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
}