package com.geeetech.administrator.easyprint.Activity.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.PrintFragment;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.FileItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
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

import static com.geeetech.administrator.easyprint.Internet.ErrorResponse.getErrorRespone;

/**
 * Created by Administrator on 2018-02-03.
 */

public class SdCard extends Fragment {
    View view;
    private List<ListItem> list = new ArrayList<>();
    private ImageView mRefresh;
    private ListView listView;
    private Boolean mState = true;
    private Handler handler = new Handler();
    private Runnable runnable;
    private FileItemAdapter itemAdapter;
    public boolean firstInitList = true;
    public static LinearLayout mLinearLayout;
    public static TextView mGetList;
    private boolean mClick = false;

    public static String mPrintFileName = "";

    public static SdCard newInstance(String info) {
        Bundle args = new Bundle();
        SdCard fragment = new SdCard();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sd_card, null);

        mGetList = view.findViewById(R.id.list_get);
        mLinearLayout = view.findViewById(R.id.linearlayout);

        listView = (ListView) view.findViewById(R.id.list_file_name);
        runnable = new Runnable() {
            @Override
            public void run() {
                mClick = false;
                mRefresh.setRotation(0);
                String state = "logout";
                try {
                    Context context = getContext();
                    List FirstUser = SpUtil.getList(context,"FirstUser");
                    state = FirstUser.get(3).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if ( state.equals("login")){
                    if ( MainActivity.isNetWork == 1){
                        if ( MainActivity.printerState.equals("2")||MainActivity.printerState.equals("3")||MainActivity.printerState.equals("4")){
                            list.clear();
                            getFileList();
                        }
                    }
                }
            }
        };
        mRefresh = view.findViewById(R.id.list_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = "logout";
                try {
                    Context context = getContext();
                    List FirstUser = SpUtil.getList(context,"FirstUser");
                    state = FirstUser.get(3).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if ( state.equals("login")){
                    //list.clear();
                    try {
                        if ( MainActivity.printerState.equals("2")||MainActivity.printerState.equals("3")||MainActivity.printerState.equals("4")){
                            mRefresh.animate().rotation(360);
                            if ( !mClick){
                                mClick = true;
                                getRefresh();
                            }
                        }else{
                            new QMUIDialog.MessageDialogBuilder(getContext())
                                    .setMessage("printer is offline.")
                                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                                        @Override
                                        public void onClick(QMUIDialog dialog, int index) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("please log in")
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
        return view;
    }
    //刷新列表
    public void getRefresh() throws JSONException {
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }
        Context context = getContext();
        //List FirstUser = SpUtil.getList(context,"FirstUser");
        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String encryStr = Data.getUserID(context,"",0);
        String encryStrId = Data.getUserID(context,serialNumber,1);
        //生成key
        /*String secretKey = "qstring1871salt";
        try {
            encryStr = AesECBUtils.encrypt(email,secretKey);
            encryStrId = AesECBUtils.encrypt(serialNumber,secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        JSONObject js = new JSONObject();
        js.put("action","refresh");
        js.put("target","printer_sd");
        js.put("token",token);
        try{
            list.removeAll(list);
            itemAdapter.notifyDataSetChanged();
            listView.setAdapter(itemAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }
        handler.postDelayed(runnable,3000);
        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"/sds";
        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if(response.code() == 403){
                            handler.removeCallbacks(runnable);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    //获取列表
    private void getFileList(){

        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }

        Context context = getContext();
        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String state = Data.getState(context);
        String encryStr = Data.getUserID(context,"",0);
        String encryStrId = Data.getUserID(context,serialNumber,1);

        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"/sds?token=" + token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        MainActivity.tipHide("");
                        String res = response.body();
                        int code = response.code();
                        if ( code == 200){
                            try{
                                list.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                List<ListItem> listChild = new ArrayList<>();
                                listChild.clear();
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String name = jsonObject.getString("file_name");
                                    ListItem item = new ListItem(name, "");
                                    listChild.add(item);
                                }
                                //if ( firstInitList ){
                                    list.addAll(listChild);
                                    initFileList();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if ( code == 410){
                            new QMUIDialog.MessageDialogBuilder(getContext())
                                    .setMessage("SD card exception")
                                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                                        @Override
                                        public void onClick(QMUIDialog dialog, int index) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    //初始化file列表
    public void initFileList(){

        itemAdapter = new FileItemAdapter(getContext(), R.layout.print_list_file_item, list);
        listView.setAdapter(itemAdapter);

        //选择打印
        itemAdapter.setOnItemEditClickListener(new FileItemAdapter.onItemEditListener() {
            @Override
            public void onEditClick(int i) {
                String state = MainActivity.printerState;
                if ( state.equals("2")) {
                    if (MainActivity.isNetWork == 1) {
                        final String mPrinterName = list.get(i).getName();
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("Are you sure to print this model now?")
                                .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        try {
                                            getPrint(mPrinterName);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }else{
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("Printer is printing.")
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
        //删除
        itemAdapter.setOnItemDeleteClickListener(new FileItemAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(final int i) {
                if ( mState){
                    final String mPrinterName = list.get(i).getName();
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("Are you sure to delete this model now?")
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    try {
                                        getDelete(mPrinterName,i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    //deleteDialog(mSerialNumber);
                }else{
                    //getQMUITipShow("Please wait",0);
                }
            }
        });
    }
    //打印
    public void getPrint(String name) throws JSONException {
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }

        Context context = getContext();
        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String state = Data.getState(context);
        String encryStr = Data.getUserID(context,"",0);
        String encryStrId = Data.getUserID(context,serialNumber,1);

        JSONObject js = new JSONObject();
        js.put("action","start");
        js.put("target","printer_task");
        js.put("value",name);
        js.put("token",token);
        mPrintFileName = name;

        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId;
        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.code() == 200){
                            PrintFragment.mStartPrint.setImageResource(R.drawable.icon_start2);
                            PrintFragment.mStartExist = false;
                            MainActivity.mViewPager.setCurrentItem(0,false);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    //删除
    public void getDelete(String name, final int i) throws JSONException {
        String serialNumber = MainActivity.mCurrentPrinter;
        if ( serialNumber.equals("")){
            return;
        }

        Context context = getContext();
        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String state = Data.getState(context);
        String encryStr = Data.getUserID(context,"",0);
        String encryStrId = Data.getUserID(context,serialNumber,1);

        JSONObject js = new JSONObject();
        js.put("action","delete");
        js.put("target","printer_sd_file");
        js.put("value",name);
        js.put("token",token);

        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId;
        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.code() == 200){
                            try{
                                list.remove(i);
                                itemAdapter.notifyDataSetChanged();
                                listView.setAdapter(itemAdapter);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }

    @Override
    public void onResume(){
        firstInitList = true;
        super.onResume();
        /*try{
            list.removeAll(list);
            itemAdapter.notifyDataSetChanged();
            listView.setAdapter(itemAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        //handler.postDelayed(runnable,0);
        //getFileList();
    }
    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }

}
