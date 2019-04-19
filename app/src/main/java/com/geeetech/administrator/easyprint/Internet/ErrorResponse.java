package com.geeetech.administrator.easyprint.Internet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.geeetech.administrator.easyprint.Activity.Fragment.MyFragment;
import com.geeetech.administrator.easyprint.LoginActivity;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.PrintFragment;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.Reserve;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.geeetech.administrator.easyprint.MainActivity.tipShow;

/**
 * Created by Administrator on 2018-05-30.
 */

public class ErrorResponse {

    //请求错误调用
    public static void getErrorRespone(Response<String> response, Context context, Activity activity){
        int responseCode = response.code();
        if ( responseCode == 500){
            ToastUtil.showToast(context,"The server failed to handle the request for some unknown reasons.");
        }else if ( responseCode == 503){
            new QMUIDialog.MessageDialogBuilder(context)
                    .setMessage("The server is too overloaded to complete the request.")
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else if ( responseCode == 996){
            new QMUIDialog.MessageDialogBuilder(context)
                    .setMessage("Wrong parameters in the requested address.")
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else if ( responseCode == 997){
            new QMUIDialog.MessageDialogBuilder(context)
                    .setMessage("Token null.")
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else if ( responseCode == 998){
            if ( Data.getResCode() == 999 || Data.getResCode() == 998){
                return;
            }
            Data.setResCode(998);
            try{
                MainActivity.mCurrentPrinter = "";
                PrintFragment.mPrinterState.setText(" ");
                MainActivity.mCurrentName = "";
                //PrintFragment.mCircleImg.setVisibility(View.GONE);
                Picasso.with(context).load(R.drawable.icon_printer_off).into(PrintFragment.mCircleImg);
                //PrintFragment.mCircleProgress.setBackgroundResource(R.drawable.icon_printer_off);
                PrintFragment.mGcodeName.setText("");
                PrintFragment.mCircleProgress.setProgress(0);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                List FirstUser = SpUtil.getList(context,"FirstUser");
                String email = FirstUser.get(0).toString();
                String token = FirstUser.get(1).toString();
                String tokenExpire = FirstUser.get(2).toString();
                String state = "logout";
                Reserve.reserveList(context,email,token,tokenExpire,state);
                MyFragment.mBtnLogout.setVisibility(View.GONE);
            }catch(Exception e){
                e.printStackTrace();
            }
            ToastUtil.showToast(context,"Login validation has expired");     //Has login by other user
            Intent intent = new Intent();
            intent.setClass(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        }else if ( responseCode == 999){
            changeToken(context,activity);
            Data.setResCode(999);
        } else if( responseCode == -1 || responseCode == 502){
            //ToastUtil.showToast(context,response.body()+"");
            //ToastUtil.showToast(context,"Server fail");
            tipShow("server");
        }else{
            String error = responseCode + "";
            String head = response.headers().toString();
            /*new QMUIDialog.MessageDialogBuilder(context)
                    .setMessage(head)
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();*/
        }
    }

    //更换token值
    public static void changeToken(final Context context, final Activity activity){
        List list = SpUtil.getList(context,"FirstUser");
        String email = list.get(0).toString();
        String token = list.get(1).toString();
        //生成key
        String secretKey = "qstring1871salt";
        //AES加密
        //long start = System.currentTimeMillis();
        String encryStr = null;
        try {
            encryStr = AesECBUtils.encrypt(email,secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = Urls.USER_CHANGE() + encryStr;
        //参数
        Map<String ,String> parmas = new HashMap<>();
        parmas.put("action","update");
        parmas.put("target","user_token");
        parmas.put("token",token);
        JSONObject jsonObject = new JSONObject(parmas);

        OkGo.<String>patch( url)//
                .tag(context)
                .upJson(jsonObject)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        String res = response.body();
                        int code = response.code();
                        if ( code == 200){
                            //解析json
                            CommonJSONParser commonJSONParser = new CommonJSONParser();
                            Map<String, Object> result = commonJSONParser.parse(res);

                            String email = result.get("email").toString();
                            String newtoken = result.get("new_token").toString();
                            String tokenExpire = result.get("token_expire").toString();
                            //获取当前登录状态
                            List FirstUser = SpUtil.getList(context,"FirstUser");
                            String state = FirstUser.get(3).toString();
                            //储存数据
                            try {
                                Reserve.reserveList(context,email,newtoken,tokenExpire,state);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Data.setResCode(0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        Data.setResCode(0);
                        getErrorRespone(response,context,activity);
                    }
                });
    }
}
