package com.geeetech.administrator.easyprint.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-04-28.
 */

public class ForgetPassword extends BaseActivity{
    public static ForgetPassword instance = null;

    private EditText mEmail,mVerificationCode;
    private Button mGetCode,mSure;
    private RelativeLayout mRelative;

    Handler handler = new Handler();
    private Runnable runnable;
    int time = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        instance = this;

        mEmail = findViewById(R.id.user_email);
        mVerificationCode = findViewById(R.id.verification_code);
        mGetCode = findViewById(R.id.btn_get);
        mSure = findViewById(R.id.btn_sure);
        mRelative = findViewById(R.id.relative_show);
        mRelative.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        mGetCode.setOnClickListener(clickListener);
        mSure.setOnClickListener(clickListener);
        runnable = new Runnable() {
            @Override
            public void run() {
                --time;
                String newTime = time+"s ";
                mGetCode.setText(newTime);
                mGetCode.setOnClickListener(null);
                if ( time == 0){
                    resetVerify();
                    return;
                }
                handler.postDelayed(this,1000);
            }
        };
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_get:
                    String email = mEmail.getText().toString();
                    if ( isEmail(email)) {
                        getCode();
                    }else{
                        getQMUITipShow("please enter the correct mailbox.",0);
                    }
                    break;
                case R.id.btn_sure:
                    String verify = mVerificationCode.getText().toString();
                    if ( mGetCode.getText().toString().equals("Get secutity code")){
                        getQMUITipShow("please get secutity code.",0);
                    }else{
                        if ( verify.length() == 6){
                            sendVerify();
                        }else{
                            getQMUITipShow("Verification code error.",0);
                        }

                    }
                    break;
            }
        }
    };
    //获取验证码
    private void getCode(){
        mRelative.setVisibility(View.VISIBLE);
        String url = Urls.USER_COMMON();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action","verify");
            jsonObject.put("target","user_email");
            String email = mEmail.getText().toString();
            jsonObject.put("value",email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>patch(url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (ForgetPassword.this.isFinishing()){
                            return;
                        }
                        mRelative.setVisibility(View.GONE);
                        if ( response.code() == 201){
                            String newTime = time+"s ";
                            mGetCode.setText(newTime);
                            //mGetCode.setBackgroundColor(getResources().getColor(R.color.app_main_hui));
                            mGetCode.setOnClickListener(null);
                            handler.postDelayed(runnable,1000);
                            getQMUITipShow("Please check your mailbox.",0);
                        }else if (response.code() == 409){
                            getQMUITipShow("The mailbox is not registered.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (ForgetPassword.this.isFinishing()){
                            return;
                        }
                        mRelative.setVisibility(View.GONE);
                        resetVerify();
                        if (response.code() == -1|| response.code() == 502){
                            getQMUITipShow("Server fail,please enter after.",0);
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //确认验证码
    public void sendVerify(){
        mRelative.setVisibility(View.VISIBLE);
        String url = Urls.USER_COMMON();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action","verify");
            jsonObject.put("target","user_vcode");
            JSONObject obj = new JSONObject();
            String email = mEmail.getText().toString();
            String code = mVerificationCode.getText().toString();
            obj.put("email",email);
            obj.put("code",code);
            jsonObject.put("object",obj);
        } catch (JSONException e) {
            e.printStackTrace();
            mRelative.setVisibility(View.GONE);
        }
        OkGo.<String>patch(url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (ForgetPassword.this.isFinishing()){
                            return;
                        }
                        mRelative.setVisibility(View.GONE);
                        if (response.code() == 200){
                            resetVerify();
                            Intent intent = new Intent();
                            String email = mEmail.getText().toString();
                            intent.putExtra("email",email);
                            intent.setClass(getBaseContext(),ResetPassword.class);
                            startActivity(intent);
                        }else if (response.code() == 401){
                            getQMUITipShow("Verification code error.",0);
                        }else if (response.code() == 404){
                            getQMUITipShow("Make sure your email is correct.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (ForgetPassword.this.isFinishing()){
                            return;
                        }
                        mRelative.setVisibility(View.GONE);
                        getErrorRespone(response);
                    }
                });
    }
    //重置获取验证码
    public void resetVerify(){
        time = 60;
        mGetCode.setText("Get secutity code");
        mGetCode.setOnClickListener(clickListener);
        handler.removeCallbacks(runnable);
    }
    //邮箱验证
    public static boolean isEmail(String strEmail) {
        //^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$
        String strPattern = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }
}
