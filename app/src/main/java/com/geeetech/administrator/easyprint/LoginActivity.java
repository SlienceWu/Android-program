package com.geeetech.administrator.easyprint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Activity.BaseActivity;
import com.geeetech.administrator.easyprint.Activity.ForgetPassword;
import com.geeetech.administrator.easyprint.Activity.Fragment.MyFragment;
import com.geeetech.administrator.easyprint.Internet.AesECBUtils;
import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.MDUtil;
import com.geeetech.administrator.easyprint.StoreData.Reserve;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-09.
 */

//登录页
public class LoginActivity extends BaseActivity {
    EditText mEmail;
    EditText mPassword;
    Button mLogin;
    TextView mForgetPassword;
    TextView mAcount;
    String mJsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.user_email);
        mPassword = findViewById(R.id.user_password);
        mLogin = findViewById(R.id.button_login);
        mForgetPassword = findViewById(R.id.forget_password);
        mAcount = findViewById(R.id.acount);
        onClickListener();
    }

    //按键监听
    private void onClickListener(){
        //登录
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJsonData = mEmail.getText().toString();
                //生成key
                String secretKey = "qstring1871salt";
                //AES加密
                String encryStr = null;
                try {
                    encryStr = AesECBUtils.encrypt(mJsonData,secretKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if( MainActivity.isNetWork == 0){
                    getQMUITipShow("Network connection fail",0);
                    return;
                }
                LoginResquest(encryStr);
            }
        });
        //忘记密码
        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(), ForgetPassword.class);
                startActivity(intent);
            }
        });
        //注册
        mAcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(), PostActivity.class);
                startActivity(intent);
            }
        });
    }

    private void LoginResquest(String emailAes){
        String url = Urls.USER_LOGIN() + emailAes;
        String password = mPassword.getText().toString();
        Context context = getBaseContext();
        if ( TextUtils.isEmpty(mJsonData)){
            QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(LoginActivity.this);
            mDialog .setTitle("")
                    .setMessage("Please enter the email")
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }
        if ( TextUtils.isEmpty(password)){
            QMUIDialog.MessageDialogBuilder mDialog = new QMUIDialog.MessageDialogBuilder(LoginActivity.this);
            mDialog .setTitle("")
                    .setMessage("Please enter the password")
                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        OkGo.<String>post( url )
                .tag(this)
                .upJson(getLogin())
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        if ( LoginActivity.this.isFinishing()) {
                            return;
                        }
                        Context context = getBaseContext();
                        String res = null;
                        int responseCode = response.code();
                        if ( responseCode == 200){
                            res = response.body();
                            //解析json
                            CommonJSONParser commonJSONParser = new CommonJSONParser();
                            Map<String, Object> result = commonJSONParser.parse(res);
                            //获取token 及 token有效时间
                            String token = result.get("token").toString();
                            String tokenExpire = result.get("token_expire").toString();
                            String name = result.get("name").toString();
                            String address = result.get("address").toString();
                            String avatar = result.get("avatar").toString();
                            //储存数据
                            try {
                                Reserve.reserveList(context,mJsonData,token,tokenExpire,"login");
                                Reserve.reserveOther(context,name,address,avatar);
                                getImageToShare(getBaseContext());
                            } catch (Exception e) {
                                getQMUITipShow("Login error",0);
                                e.printStackTrace();
                                return;
                            }
                            MyFragment.mBtnLogout.setVisibility(View.VISIBLE);
                            //onBackPressed();
                            Intent intent = new Intent();
                            intent.setClass(getBaseContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            try{
                                MainActivity.mViewPager.setCurrentItem(2,false);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            startActivity(intent);
                            finish();
                        }else if ( responseCode == 400){
                            ToastUtil.showToast(context, "Params are wrong!");
                        }else if ( responseCode == 401){
                            ToastUtil.showToast(context, "Password is wrong!");
                        }else if ( responseCode == 402){
                            ToastUtil.showToast(context, "Mailbox unverified!");
                        }else if ( responseCode == 410){
                            ToastUtil.showToast(context, "Email is not exist");
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( LoginActivity.this.isFinishing()) {
                            return;
                        }
                        Context context = getBaseContext();
                        int responseCode = response.code();
                        if( responseCode == -1 || responseCode == 502){
                            ToastUtil.showToast(context,"Server fail");
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }

    //请求参数
    public JSONObject getLogin(){
        JSONObject js=new JSONObject();
        String password = MDUtil.md5(mPassword.getText().toString());
        try {
            js.put("action","login");
            js.put("target","user");
            js.put("password",password);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }

    @Override
    public void onResume(){
        Data.setResCode(0);
        try{
            List FirstUser = SpUtil.getList(getBaseContext(),"FirstUser");
            String email = FirstUser.get(0).toString();
            mEmail.setText(email);
        }catch (Exception e){

        }
        super.onResume();
    }

    //登录成功后获取头像
    public void getImageToShare(Context context) {
        int exist = SpUtil.getInt(context,"List",0);
        if ( exist == 0){
            return;
        }
        List list = SpUtil.getList(context,"FirstUser");
        String state = list.get(3).toString();
        if ( state.equals("logout")){
            return;
        }
        String token = list.get(1).toString();
        String encryStr = getUserID("",0);
        String url = Urls.USER_COMMON() +encryStr+"/avatar?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        if ( response.code() == 200){
                            try {
                                JSONObject result = new JSONObject(res);
                                String mPersonImgUrl = result.getString("avatar");
                                List otherInfo = SpUtil.getList(getBaseContext(),"OtherInfo");
                                String name = otherInfo.get(0).toString();
                                String address = otherInfo.get(1).toString();
                                Reserve.reserveOther(getBaseContext(),name,address,mPersonImgUrl);
                                if (mPersonImgUrl.equals("")){
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(MyFragment.mUserImg);
                                }else{
                                    Picasso.with(getBaseContext()).load(mPersonImgUrl).error(R.drawable.icon_logo).into(MyFragment.mUserImg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else if( response.code() == 410){
                            try{
                                List otherInfo = SpUtil.getList(getBaseContext(),"OtherInfo");
                                String avatar = otherInfo.get(2).toString();
                                if (avatar.equals("")){
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(MyFragment.mUserImg);
                                }else{
                                    Picasso.with(getBaseContext()).load(avatar).error(R.drawable.icon_logo).into(MyFragment.mUserImg);
                                }
                            }catch (Exception e){
                                Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(MyFragment.mUserImg);
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( response.code()==-1||response.code()==502){
                            try{
                                List otherInfo = SpUtil.getList(getBaseContext(),"OtherInfo");
                                String avatar = otherInfo.get(2).toString();
                                if (avatar.equals("")){
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(MyFragment.mUserImg);
                                }else{
                                    Picasso.with(getBaseContext()).load(avatar).error(R.drawable.icon_logo).into(MyFragment.mUserImg);
                                }
                            }catch (Exception e){
                                Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(MyFragment.mUserImg);
                            }
                        }
                        getErrorRespone(response);
                    }
                });
    }

    //返回键按钮
    @Override
    public void addLeftBackTopBar() throws Exception{
        QMUITopBar mTopBar = (QMUITopBar) findViewById(R.id.topbar);
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                try{
                    MainActivity.mViewPager.setCurrentItem(2,false);
                }catch (Exception e){
                    e.printStackTrace();
                }
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            try{
                MainActivity.mViewPager.setCurrentItem(2,false);
            }catch (Exception e){
                e.printStackTrace();
            }
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}