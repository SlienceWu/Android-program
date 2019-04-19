package com.geeetech.administrator.easyprint.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.geeetech.administrator.easyprint.Activity.Fragment.MyFragment;
import com.geeetech.administrator.easyprint.LoginActivity;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.MDUtil;
import com.geeetech.administrator.easyprint.StoreData.Reserve;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-20.
 */

public class SettingActivity extends BaseActivity {

    EditText mOldPassword;
    EditText mNewPassword;
    EditText mReNewPassword;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfragment_setting);

        mOldPassword = findViewById(R.id.user_oldpass);
        mNewPassword = findViewById(R.id.user_newpass);
        mReNewPassword = findViewById(R.id.user_re_newpass);
        customHint(mOldPassword,"Current password");
        customHint(mNewPassword,"New password");
        customHint(mReNewPassword,"Re-enter new password");

        mButton = findViewById(R.id.button_change);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    List firstUser = SpUtil.getList(getBaseContext(),"FirstUser");
                    String state = firstUser.get(3).toString();
                    if ( state.equals("logout")){
                        getQMUITipShow("please log in",0);
                        return;
                    }
                }catch (Exception e){
                    getQMUITipShow("please log in",0);
                    return;
                }
                String oldpass = mOldPassword.getText().toString();
                String newpass = mNewPassword.getText().toString();
                String renewpass = mReNewPassword.getText().toString();
                if (TextUtils.isEmpty(oldpass)||TextUtils.isEmpty(newpass)||TextUtils.isEmpty(renewpass)){
                    getQMUITipShow("Params are null",0);
                    return;
                }else if ( !newpass.equals(renewpass)){
                    getQMUITipShow("The two password do not match.",0);
                    return;
                }else if (newpass.length()<7||renewpass.length()<7){
                    getQMUITipShow("Invalid Password",0);
                    return;
                }
                ChangePassword();
            }
        });

    }

    //修改密码
    private void ChangePassword(){
        String encryStr = getUserID("",0);
        String url = Urls.USER_PASSWORD_DEL() + encryStr;

        OkGo.<String>patch( url )
                .tag(this)
                .upJson(getChangePassword())
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        Context context = getBaseContext();
                        int responseCode = response.code();
                        if ( responseCode == 200){
                            ToastUtil.showToast(context, "Change success!");
                            LogoutRequest();
                        }else if ( responseCode == 400){
                            ToastUtil.showToast(context, "Params are wrong!");
                        }else{
                            ToastUtil.showToast(context, "Original password error!");
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }

    //请求参数
    public JSONObject getChangePassword(){
        JSONObject js = new JSONObject();
        JSONObject obj = new JSONObject();
        String oldpass = MDUtil.md5(mOldPassword.getText().toString());
        String newpass = MDUtil.md5(mNewPassword.getText().toString());
        try {
            js.put("action","update");
            js.put("target","user_password");
            obj.put("old_password",oldpass);
            obj.put("new_password",newpass);
            js.put("object",obj);
            js.put("token",getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return js;
    }
    //退出登录
    private void LogoutRequest(){
        String token = getToken();
        String encryStr = getUserID("",0);
        String url = Urls.USER_OUT() + encryStr;

        Map<String ,String> params = new HashMap<>();
        params.put("action","logout");
        params.put("target","user");
        params.put("token",token);
        JSONObject jsonObject = new JSONObject(params);

        OkGo.<String>delete( url )
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        Context context = getBaseContext();
                        String res = null;
                        int responseCode = response.code();
                        if ( responseCode == 200){
                            res = response.body();
                            List FirstUser = SpUtil.getList(context,"FirstUser");
                            String email = FirstUser.get(0).toString();
                            String token = FirstUser.get(1).toString();
                            String tokenExpire = FirstUser.get(2).toString();
                            try {
                                Reserve.reserveList(context,email,token,tokenExpire,"logout");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            MainActivity.mCurrentPrinter = "";
                            MainActivity.mCurrentName = "";
                            Intent intent = new Intent();
                            intent.setClass(getBaseContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            //Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(mUserImg);
                            MyFragment.mBtnLogout.setVisibility(View.GONE);
                        }else if ( responseCode == 400){
                            //Toast.makeText(context, "Params is wrong!",Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response);
                    }
                });
    }
    //设置placeholder字体大小
    public static void customHint(EditText editText, String hintString) {
        SpannableString spannableString = new SpannableString(hintString);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(12, true);
        spannableString.setSpan(absoluteSizeSpan , 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editText.setHint(new SpannedString(spannableString)); // 一定要进行转换,否则属性只有一次
    }
}