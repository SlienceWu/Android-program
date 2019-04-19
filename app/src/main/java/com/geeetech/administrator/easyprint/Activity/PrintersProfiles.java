package com.geeetech.administrator.easyprint.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
public class PrintersProfiles extends BaseActivity{
    Button mAddProfile;
    boolean mState = true;
    private List<ListItem> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_profile);

        //跳转
        mAddProfile = findViewById(R.id.button_add_profile);
        mAddProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = new String[]{"Custom material profile", "Geeetech E180", "GiantArm D200", "Geeetech A30",
                        "Geeetech I3 Pro W", "Geeetech MeCreator 2", "Geeetech G2 pro", "Geeetech I3 X", "Geeetech I3 pro B",
                        "Geeetech I3 A", "Prusa I3 A Pro"};
                new QMUIDialog.MenuDialogBuilder(PrintersProfiles.this)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                if ( getState().equals("logout")){
                                    getQMUITipShow("please log in",0);
                                    return;
                                }
                                if ( which == 0){
                                    Intent intent = new Intent();
                                    intent.putExtra("action","add");
                                    intent.setClass(PrintersProfiles.this, AddPrinterProfile.class);
                                    startActivity(intent);
                                }else if ( which == 1){
                                    addPrinterProfile("E180");
                                }else if ( which == 2){
                                    addPrinterProfile("D200");
                                }else if ( which == 3){
                                    addPrinterProfile("A30");
                                }else if ( which == 4){
                                    addPrinterProfile("I3 Pro W");
                                }else if ( which == 5){
                                    addPrinterProfile("MeCreator 2");
                                }else if ( which == 6){
                                    addPrinterProfile("G2 pro");
                                }else if ( which == 7){
                                    addPrinterProfile("I3 X");
                                }else if ( which == 8){
                                    addPrinterProfile("I3 pro B");
                                }else if ( which == 9){
                                    addPrinterProfile("I3 A");
                                }else if ( which == 10){
                                    addPrinterProfile("I3 A Pro");
                                }
                            }
                        })
                        .show();
            }
        });
    }
    //获取打印机配置文件列表
    public void getProfileList(){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String encryStr = getUserID("",0);
        String token = getToken();
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( PrintersProfiles.this.isFinishing()) {
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
                        if ( PrintersProfiles.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //初始化打印机列表
    public void initList(){
        ItemAdapter itemAdapter = new ItemAdapter(PrintersProfiles.this, R.layout.print_list_profile_item, list);
        ListView listView = (ListView) findViewById(R.id.profile_list);
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
                intent.setClass(PrintersProfiles.this, AddPrinterProfile.class);
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
                //final String mPrinterName = list.get(i).getName();
                //itemAdapter.notifyDataSetChanged();
            }
        });
    }
    //删除弹窗
    public void deleteDialog(final String serialNumber){
        new QMUIDialog.MessageDialogBuilder(PrintersProfiles.this)
                .setMessage("Delete this Printer profile?")
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
                        deleteProfile(serialNumber,dialog);
                    }
                })
                .show();
    }
    //删除请求
    public void deleteProfile(String serialNumber, final QMUIDialog dialog){
        String encryStr = getUserID("",0);
        String encryStrNum = getUserID(serialNumber,1);
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printer_profiles/" + encryStrNum;
        OkGo.<String>delete(url)
                .tag(this)
                .upJson(getDeleteProfile())
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
                        getErrorRespone(response);
                    }
                });
    }

    //请求参数
    public JSONObject getDeleteProfile(){
        JSONObject js=new JSONObject();
        String token = getToken();
        try {
            js.put("action","delete");
            js.put("target","user_pprofile");
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
                    getProfileList();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //添加默认机型
    public void addPrinterProfile(String type){
        String encryStr = getUserID("",0);
        JSONObject data = getPrinterParams(type);
        String url = Urls.GET_PRINTER_LIST() + encryStr;
        OkGo.<String>post(url)
                .tag(this)
                .upJson(data)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        int responseCode = response.code();
                        String res = response.body();
                        if ( responseCode == 201){
                            getQMUITipShow("Add Success.",0);
                            onResume();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
    //默认机型参数
    public JSONObject getPrinterParams(String type){
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        String token = getToken();
        if ( type.equals("E180")){
            try {
                obj.put("name","Geeetech E180");
                obj.put("shape",0);
                obj.put("width",130);
                obj.put("height",130);
                obj.put("depth",130);
                obj.put("heatbed_exist",0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ( type.equals("D200")){
            try {
                obj.put("name","GiantArm D200");
                obj.put("shape",0);
                obj.put("width",300);
                obj.put("height",180);
                obj.put("depth",180);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ( type.equals("A30")){
            try {
                obj.put("name","Geeetech A30");
                obj.put("shape",0);
                obj.put("width",320);
                obj.put("height",420);
                obj.put("depth",320);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ( type.equals("I3 Pro W")){
            try {
                obj.put("name","Geeetech I3 Pro W");
                obj.put("shape",0);
                obj.put("width",200);
                obj.put("height",180);
                obj.put("depth",200);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ( type.equals("MeCreator 2")){
            try {
                obj.put("name","Geeetech MeCreator 2");
                obj.put("shape",0);
                obj.put("width",160);
                obj.put("height",160);
                obj.put("depth",160);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ( type.equals("G2 pro")){
            try {
                obj.put("name","Geeetech G2 pro");
                obj.put("shape",1);
                obj.put("height",200);
                obj.put("radius",190);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if ( type.equals("I3 X")){
            try {
                obj.put("name","Geeetech I3 X");
                obj.put("shape",0);
                obj.put("width",200);
                obj.put("height",170);
                obj.put("depth",200);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if ( type.equals("I3 pro B")){
            try {
                obj.put("name","Geeetech I3 pro B");
                obj.put("shape",0);
                obj.put("width",200);
                obj.put("height",180);
                obj.put("depth",200);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if ( type.equals("I3 A")){
            try {
                obj.put("name","Geeetech I3 A");
                obj.put("shape",0);
                obj.put("width",200);
                obj.put("height",180);
                obj.put("depth",200);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if ( type.equals("I3 A Pro")){
            try {
                obj.put("name","Prusa I3 A Pro");
                obj.put("shape",0);
                obj.put("width",200);
                obj.put("height",190);
                obj.put("depth",200);
                obj.put("heatbed_exist",1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            js.put("action","add");
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