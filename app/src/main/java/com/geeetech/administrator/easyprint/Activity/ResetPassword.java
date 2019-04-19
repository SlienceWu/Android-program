package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.MDUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018-04-28.
 */

public class ResetPassword extends BaseActivity {
    private String mEmail;
    private EditText mNewPassword,mRepeatPassword;
    private Button mReset;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mEmail = getIntent().getStringExtra("email");
        mNewPassword = findViewById(R.id.user_newpass);
        mRepeatPassword = findViewById(R.id.user_re_newpass);
        mReset = findViewById(R.id.button_reset);
        mReset.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.button_reset:
                    resetPassword();
                    break;
            }
        }
    };

    //确认修改密码
    public void resetPassword(){
        String passOne = mNewPassword.getText().toString();
        String passTwo = mRepeatPassword.getText().toString();
        if (TextUtils.isEmpty(passOne) || TextUtils.isEmpty(passTwo)){
            getQMUITipShow("Params are null",0);
            return;
        }else if ( passOne.length()<7){
            getQMUITipShow("Invalid Password.",0);
            return;
        }else if( !passOne.equals(passTwo)){
            getQMUITipShow("The two password do not match.",0);
            return;
        }
        String url = Urls.USER_COMMON();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action","verify");
            jsonObject.put("target","user_password");
            JSONObject obj = new JSONObject();
            obj.put("email",mEmail);
            obj.put("password", MDUtil.md5(passOne));
            jsonObject.put("object",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>patch(url)
                .tag(this)
                .upJson(jsonObject)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (ResetPassword.this.isFinishing()){
                            return;
                        }
                        if ( response.code() == 200){
                            new QMUIDialog.MessageDialogBuilder(ResetPassword.this)
                                    .setMessage("Reset password success.")
                                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                                        @Override
                                        public void onClick(QMUIDialog dialog, int index) {
                                            dialog.dismiss();
                                            ForgetPassword.instance.finish();
                                            onBackPressed();
                                        }
                                    })
                                    .show();
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (ResetPassword.this.isFinishing()){
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
}
