package com.geeetech.administrator.easyprint.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.ItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.ListStateAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListStateItem;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-02-01.
 */
public class Printers extends BaseActivity {

    private LinearLayout mStartButton;
    private Button mButton;
    private RelativeLayout mBindList;
    private RelativeLayout mNoBindShow;
    private Button mStartBind;

    private List<ListItem> list = new ArrayList<>();
    private List<ListStateItem> listState = new ArrayList<>();
    //定时查询打印机列表及状态
    public Runnable runnable;
    public Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printers);
        String number = getIntent().getStringExtra("number");
        //mStartButton = (LinearLayout) findViewById(R.id.change_printer_name);
        mButton = findViewById(R.id.button_add_print);
        mBindList = findViewById(R.id.list_printer);
        mNoBindShow = findViewById(R.id.no_printer_bind);
        mStartBind = findViewById(R.id.start_bind);
        if (!number.equals("0")){
            mNoBindShow.setVisibility(View.GONE);
            mBindList.setVisibility(View.VISIBLE);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                getListState();
                handler.postDelayed(this,1000);
            }
        };
        //跳转到绑定机器页面
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Printers.this, BindPrinter.class);
                startActivity(intent);
            }
        });
        mStartBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Printers.this, BindPrinter.class);
                startActivity(intent);
            }
        });
    }

    //获取用户绑定的打印机(无状态，不用)
    public void getList() throws Exception {
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String encryStr = getUserID("",0);
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printers?token="+getToken();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( Printers.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try{
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    mBindList.setVisibility(View.GONE);
                                    mNoBindShow.setVisibility(View.VISIBLE);
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("serial_num");
                                    String mPrinterName = jsonObject.getString("name");
                                    //getListDetail(mSerialNumber);
                                    initList(mPrinterName,mSerialNumber);
                                }
                                mBindList.setVisibility(View.VISIBLE);
                                mNoBindShow.setVisibility(View.GONE);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( Printers.this.isFinishing()) {
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //初始化打印机列表(无状态，不用)
    public void initList(String name,String number){
        ListItem item = new ListItem(name, number);
        list.add(item);

        ItemAdapter itemAdapter = new ItemAdapter(Printers.this, R.layout.print_list_item, list);
        ListView listView = (ListView) findViewById(R.id.printers_list);
        listView.setAdapter(itemAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mPrinterName = list.get(position).getName();
                String mSerialNumber = list.get(position).getNumber();

                Intent intent = new Intent();
                intent.putExtra("printer_name",mPrinterName);
                intent.putExtra("serial_number",mSerialNumber);
                intent.setClass(Printers.this, PrinterDetail.class);
                startActivity(intent);
            }
        });
    }
    //进入页面定时查询
    @Override
    public void onResume () {
        super.onResume();
        handler.postDelayed(runnable,0);
    }
    //退出页面取消查询
    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }
    //获取列表及状态
    public void getListState(){
        if ( getState().equals("logout") || MainActivity.isNetWork !=1){
            return;
        }
        String encryStr = getUserID("",0);
        String url = Urls.USER_COMMON() + encryStr + "/printersinfo?token=" + getToken();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( Printers.this.isFinishing()) {
                            return;
                        }
                        try {
                            listState.clear();

                            JSONArray jsonArray = new JSONArray(response.body());
                            if ( jsonArray.length() == 0){
                                mBindList.setVisibility(View.GONE);
                                mNoBindShow.setVisibility(View.VISIBLE);
                                return;
                            }
                            List<ListStateItem> a = new ArrayList<ListStateItem>();
                            List<ListStateItem> b = new ArrayList<ListStateItem>();
                            List<ListStateItem> c = new ArrayList<ListStateItem>();
                            a.clear();
                            b.clear();
                            c.clear();
                            mBindList.setVisibility(View.VISIBLE);
                            mNoBindShow.setVisibility(View.GONE);
                            MainActivity.printerList.clear();
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                                String state = jsonObject.getString("state");
                                String number = jsonObject.getString("serial_num");
                                String name = jsonObject.getString("name");

                                if ( !state.equals("0")&& !state.equals("1")){
                                    ListStateItem item = new ListStateItem(name, number,state);
                                    a.add(item);
                                }else{
                                    ListStateItem item = new ListStateItem(name, number,state);
                                    b.add(item);
                                }

                                if ( number.equals(MainActivity.mCurrentPrinter)){
                                    MainActivity.mCurrentName = name;
                                }
                                MainActivity.initList(name,number);
                            }
                            c = Data.getListState(a,b);
                            listState.addAll(c);
                            initListState("","","");
                        } catch (Exception e) {
                            e.printStackTrace();
                            //ToastUtil.showToast(getBaseContext(),"123");
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
    //初始化打印机列表及状态
    public void initListState(String name,String number,String state){

        ListStateAdapter listStateAdapter = new ListStateAdapter(Printers.this, R.layout.print_list_item, listState);
        ListView listView = (ListView) findViewById(R.id.printers_list);
        listView.setAdapter(listStateAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mPrinterName = listState.get(position).getName();
                String mSerialNumber = listState.get(position).getNumber();

                Intent intent = new Intent();
                intent.putExtra("printer_name",mPrinterName);
                intent.putExtra("serial_number",mSerialNumber);
                intent.setClass(Printers.this, PrinterDetail.class);
                startActivity(intent);
            }
        });
    }
}