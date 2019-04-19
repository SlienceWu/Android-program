package com.geeetech.administrator.easyprint;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Activity.BaseActivity;
import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.StoreData.MDUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2018-02-11.
 */

//注册页
public class PostActivity extends BaseActivity {
    Button mPost;
    EditText mEmail;
    EditText mPassword;
    EditText mPasswordCheck;
    EditText mName;
    TextView mArea;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mEmail = findViewById(R.id.user_email);
        mPassword = findViewById(R.id.user_password);
        mPasswordCheck = findViewById(R.id.user_password2);
        mName = findViewById(R.id.user_name);
        mArea = findViewById(R.id.user_area);
        mPost = findViewById(R.id.button_post);

        //设置文本可见
        //mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        //设置文本隐藏
        mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mPasswordCheck.setTransformationMethod(PasswordTransformationMethod.getInstance());
        getArea();
        onClickListener();
    }

    //点击事件
    private void onClickListener(){

        //注册
        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Urls.USER_POST();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String passwordCheck = mPasswordCheck.getText().toString();
                String name = mName.getText().toString();
                String area = mArea.getText().toString();
                if ( !isUsername(name)){
                    QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(PostActivity.this);
                    mDialog .setTitle("")
                            .setMessage("Invalid user name")
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return;
                }
                if ( !isEmail(email)){
                    QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(PostActivity.this);
                    mDialog .setTitle("")
                            .setMessage("Invalid Email address")
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return;
                }
                if ( password.equals(passwordCheck)){
                    /*if( !isPassword(password)){
                        QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(PostActivity.this);
                        mDialog .setTitle("")
                                .setMessage("Invalid Password")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }*/
                    if ( passwordCheck.length()<7){
                        QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(PostActivity.this);
                        mDialog .setTitle("")
                                .setMessage("Invalid Password")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }
                }else{
                    QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(PostActivity.this);
                    mDialog .setTitle("")
                            .setMessage("Passwords don't match")
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    //Toast.makeText(PostActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                    return;
                }

                OkGo.<String>post( url )//
                        .tag(this)
                        .upJson(getPost())
                        .execute(new StringCallback(){
                            @Override
                            public void onSuccess(Response<String> response){
                                if ( PostActivity.this.isFinishing()) {
                                    return;
                                }
                                Context context = getBaseContext();
                                String res = null;
                                int responseCode = response.code();
                                if ( responseCode == 201){
                                    res = response.body();
                                    //解析json
                                    CommonJSONParser commonJSONParser = new CommonJSONParser();
                                    Map<String, Object> result = commonJSONParser.parse(res);
                                    ToastUtil.showToast(context, "Please check your email to verify your account.");
                                    onBackPressed();
                                }else if ( responseCode == 400){
                                    ToastUtil.showToast(context, "Params are wrong!");
                                }else if ( responseCode == 409){
                                    ToastUtil.showToast(context, "Email is exist.");
                                }
                            }
                            @Override
                            public void onError(Response<String> response){
                                if ( PostActivity.this.isFinishing()) {
                                    return;
                                }
                                Context context = getBaseContext();
                                int responseCode = response.code();
                                if( responseCode == -1 || responseCode == 502){
                                    ToastUtil.showToast(context,"Please check your network.");
                                    return;
                                }
                                getErrorRespone(response);
                            }
                        });
            }
        });
    }
    //请求参数
    public JSONObject getPost(){
        JSONObject js=new JSONObject();
        JSONObject obj=new JSONObject();
        String email = mEmail.getText().toString();
        String password = MDUtil.md5(mPassword.getText().toString());
        String name = mName.getText().toString();
        String area = mArea.getText().toString();
        try {
            obj.put("email",email);
            obj.put("password",password);
            obj.put("name",name);
            obj.put("area",area);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            js.put("action","add");
            js.put("target","user");
            js.put("object",obj);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }

    //获取网络ip所在地
    private void getArea(){
        OkGo.<String>get( "http://ip-api.com/json" )//
                .tag(this)
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        if (PostActivity.this == null || PostActivity.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        String country = result.get("country").toString();
                        TextView userArea = findViewById(R.id.user_area);
                        userArea.setText( country+"");
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (PostActivity.this == null || PostActivity.this.isFinishing()) {
                            return;
                        }
                        Context context = getBaseContext();
                        ToastUtil.showToast(context, "Get Area fail");
                    }
                });
    }

    /* 验证手机格式*/
    public static boolean isMobile(String number) {
    /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        //"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        String num = "[1][358]\\d{9}";
        if (TextUtils.isEmpty(number)) {
            return false;
        } else {
            //matches():字符串是否在给定的正则表达式匹配
            return number.matches(num);
        }
    }
    //邮箱验证
    public static boolean isEmail(String strEmail) {
        //^[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$
        String strPattern = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }
    //密码验证
    /*public static boolean isPassword(String strPassword) {
        String strPattern = "^[a-zA-Z0-9]{7,16}$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strPassword.matches(strPattern);
        }
    }*/
    //用户名验证
    public static boolean isUsername(String strUsername) {
        String strPattern = "^[a-zA-Z0-9-_\\[\\]]{3,15}$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strUsername.matches(strPattern);
        }
    }
}
