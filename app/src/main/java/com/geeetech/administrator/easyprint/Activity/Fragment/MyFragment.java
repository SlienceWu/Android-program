package com.geeetech.administrator.easyprint.Activity.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Activity.AboutActivity;
import com.geeetech.administrator.easyprint.Activity.FeedbackActivity;
import com.geeetech.administrator.easyprint.Activity.MaterialProfiles;
import com.geeetech.administrator.easyprint.Activity.Printers;
import com.geeetech.administrator.easyprint.Activity.PrintersProfiles;
import com.geeetech.administrator.easyprint.Activity.SetIp;
import com.geeetech.administrator.easyprint.Activity.SettingActivity;
import com.geeetech.administrator.easyprint.CircleImageView;
import com.geeetech.administrator.easyprint.LoginActivity;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.Reserve;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.geeetech.administrator.easyprint.Internet.ErrorResponse.getErrorRespone;

/**
 * Created by Administrator on 2018-01-29.
 */

//个人中心
public class MyFragment extends Fragment {
    private static final int PHOTO_REQUEST_CAREMA = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private File tempFile;
    String[] mPermissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String IMAGE_FILE_LOCATION = "file:///" + Environment.getExternalStorageDirectory().getPath() + "/easyprint/temp.jpg";
    private File mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/easyprint", "temp.jpg");
    private Uri imageUri = Uri.fromFile(mFile);

    private String mPersonImgUrl;
    View view;
    private LinearLayout mPrinters;
    private LinearLayout mPrinterProfiles;
    private LinearLayout mMaterialProfiles;
    public static CircleImageView mUserImg;
    private TextView mLog,mAddress;
    public static TextView mPrintersNum,mPrinterProfileNum,mMaterialProfileNum;
    private TextView mSetting;
    private TextView mSetIp;
    private TextView mText4,mText5,mText6;
    private TextView mText7;
    private TextView mText8;
    public static Button mBtnLogout;

    public static MyFragment newInstance(String info) {
        Bundle args = new Bundle();
        MyFragment fragment = new MyFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my, null);
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/easyprint");
        if ( !file.exists()){
            file.mkdirs();
        }
        //获取登录状态显示隐藏个人中心登录按键
        mBtnLogout = (Button) view.findViewById(R.id.button_logout);

        mPrinters = (LinearLayout) view.findViewById(R.id.my_top_btn2);
        mPrinterProfiles = (LinearLayout) view.findViewById(R.id.my_top_btn3);
        mMaterialProfiles = (LinearLayout) view.findViewById(R.id.my_top_btn4);
        mPrintersNum = (TextView) view.findViewById(R.id.printers_num);
        mPrinterProfileNum = (TextView) view.findViewById(R.id.printer_profile_num);
        mMaterialProfileNum = (TextView) view.findViewById(R.id.material_profile_num);
        mLog = (TextView) view.findViewById(R.id.text_username);
        mAddress = (TextView) view.findViewById(R.id.text_useraddress);
        mUserImg = (CircleImageView) view.findViewById(R.id.user_img);
        mSetIp = (TextView) view.findViewById(R.id.text_my_ip);
        mSetting = (TextView) view.findViewById(R.id.text_my_setting);
        mText4 = (TextView) view.findViewById(R.id.text_my_four);
        mText5 = (TextView) view.findViewById(R.id.text_my_five);
        mText6 = (TextView) view.findViewById(R.id.text_my_six);
        mText7 = (TextView) view.findViewById(R.id.text_my_seven);
        mText8 = (TextView) view.findViewById(R.id.text_my_eight);

        mPrinters.setOnClickListener(clickListener);
        mPrinterProfiles.setOnClickListener(clickListener);
        mMaterialProfiles.setOnClickListener(clickListener);
        mLog.setOnClickListener(clickListener);
        mUserImg.setOnClickListener(clickListener);
        mBtnLogout.setOnClickListener(clickListener);

        mSetIp.setOnClickListener(clickListener);
        mSetting.setOnClickListener(clickListener);
        mText4.setOnClickListener(clickListener);
        mText5.setOnClickListener(clickListener);
        mText6.setOnClickListener(clickListener);
        mText7.setOnClickListener(clickListener);
        mText8.setOnClickListener(clickListener);

        return view;
    }

    //点击事件监听
    View.OnClickListener clickListener=new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent();
            String state = Data.getState(getContext());

            switch (v.getId()) {
                case R.id.my_top_btn2:
                    if ( MainActivity.isNetWork == 2){
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("Server fail")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }
                    if ( state.equals("logout") ){
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("please log in")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                        Intent intent = new Intent();
                                        intent.setClass(getContext(), LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                        return;
                    }
                    String a = mPrintersNum.getText().toString();
                    intent.putExtra("number",a);
                    intent.setClass(getActivity(), Printers.class);
                    //intent.setClass(getActivity(), PrintersModeSelect.class);
                    startActivity(intent);
                    break;
                case R.id.my_top_btn3:
                    if ( MainActivity.isNetWork == 2){
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("Server fail")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }
                    if ( state.equals("logout") ){
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("please log in")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                        Intent intent = new Intent();
                                        intent.setClass(getContext(), LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                        return;
                    }
                    intent.setClass(getActivity(), PrintersProfiles.class);
                    startActivity(intent);
                    break;
                case R.id.my_top_btn4:
                    if ( MainActivity.isNetWork == 2){
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("Server fail")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return;
                    }
                    if ( state.equals("logout") ){
                        new QMUIDialog.MessageDialogBuilder(getContext())
                                .setMessage("please log in")
                                .addAction("OK", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index) {
                                        dialog.dismiss();
                                        Intent intent = new Intent();
                                        intent.setClass(getContext(), LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                        return;
                    }
                    intent.setClass(getActivity(), MaterialProfiles.class);
                    startActivity(intent);
                    break;
                case R.id.text_username:
                    if ( state.equals("logout")){
                        intent.setClass(getActivity(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                    break;
                case R.id.button_logout:
                    LogoutRequest();
                    break;
                case R.id.text_my_ip:
                    intent.setClass(getActivity(), SetIp.class);
                    startActivity(intent);
                    break;
                case R.id.text_my_setting:
                    intent.setClass(getActivity(), SettingActivity.class);
                    startActivity(intent);
                    break;
                case R.id.text_my_four:
                    openDefaultBrowser("https://www.facebook.com/geeetech");
                    break;
                case R.id.text_my_five:
                    openDefaultBrowser("https://twitter.com/geeetech");
                    break;
                case R.id.text_my_six:
                    openDefaultBrowser("https://www.youtube.com/channel/UCCDcof33Kp6i_JGCJ83GHPw");
                    break;
                case R.id.text_my_seven:
                    intent.setClass(getActivity(), FeedbackActivity.class);
                    startActivity(intent);
                    break;
                case R.id.text_my_eight:
                    intent.setClass(getActivity(), AboutActivity.class);
                    startActivity(intent);
                    break;
                case R.id.user_img:
                    Context context = getContext();
                    int exist = SpUtil.getInt(context,"List",0);
                    if ( exist == 0){
                        return;
                    }
                    if ( state.equals("logout")){
                        return;
                    }
                    if ( MainActivity.isNetWork != 1){
                        return;
                    }
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        //动态获取权限
                        ActivityCompat.requestPermissions(getActivity(), mPermissionList, 100);
                    }else{
                        gallery(view);
                    }
                    break;
            }
        }
    };

    //退出登录
    private void LogoutAfter(){
        List FirstUser = SpUtil.getList(getContext(),"FirstUser");
        String email = FirstUser.get(0).toString();
        String token = FirstUser.get(1).toString();
        String tokenExpire = FirstUser.get(2).toString();
        try {
            Reserve.reserveList(getContext(),email,token,tokenExpire,"logout");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        MainActivity.mCurrentPrinter = "";
        MainActivity.mCurrentName = "";
        Intent intentLogin = new Intent();
        intentLogin.setClass(getActivity(), LoginActivity.class);
        startActivity(intentLogin);
        Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
        mBtnLogout.setVisibility(View.GONE);
    }
    private void LogoutRequest(){
        LogoutAfter();
        Context context = getContext();
        String token = Data.getToken(context);
        String email = Data.getEmail(context);
        String encryStr = Data.getUserID(context,"",0);

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
                        if (getActivity().isFinishing()){
                            return;
                        }
                        getActivity().finish();
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (getActivity().isFinishing()){
                            return;
                        }
                        getActivity().finish();
                    }
                });
    }

    //从相册获取
    public void gallery(View view) {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }
    /**
     * 权限申请返回结果
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults  申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //同意权限申请
                    gallery(view);
                }else { //拒绝权限申请
                    ToastUtil.showToast(getContext(),"Permissions are denied.");
                }
                break;
            default:
                break;
        }
    }
    //从相机获取
    public void camera(View view) {
        // 激活相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {
            tempFile = new File(Environment.getExternalStorageDirectory(),
                    "temp.jpg"); //icon_logo.jpg
            // 从文件中创建uri
            Uri uri = Uri.fromFile(tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA
        startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
    }
    //剪切图片
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
    //判断sd card是否被挂载
    private boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Context context = getContext();
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                crop(uri);
            }

        } else if (requestCode == PHOTO_REQUEST_CAREMA) {
            // 从相机返回的数据
            if (hasSdcard()) {
                crop(Uri.fromFile(tempFile));
            } else {
                ToastUtil.showToast(context, "Unable to store photos without finding a storage card!");
            }

        } else if (requestCode == PHOTO_REQUEST_CUT) {
            // 从剪切图片返回的数据
            if (data != null) {
                /*String realPathFromUri = RealPathFromUriUtils.getRealPathFromUri(getContext(), imageUri);
                File file = new File(realPathFromUri);
                Toast.makeText(context, realPathFromUri, Toast.LENGTH_SHORT).show();*/
                if ( mFile != null){
                    /*Bitmap bitmap = data.getParcelableExtra("data");
                    compressBmpToFile(bitmap,mFile);*/
                    putImageToShare(context,mFile);
                }
                /*Bitmap bitmap = data.getParcelableExtra("data");
                mUserImg.setImageBitmap(bitmap);
                putImageToShare(context,bitmap);*/
            }
            try {
                // 将临时文件删除
                //tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void putImageToShare(Context context, File file) {
        setImgByStr("",file);
    }

    public  void setImgByStr(String imgStr,File file) {
        byte[] byteArray = Base64.decode(imgStr, Base64.DEFAULT);
        //这里是头像接口，通过Post请求，拼接接口地址和ID，上传数据。
        Context context = getContext();
        String token = Data.getToken(context);
        String email = Data.getEmail(context);
        String encryStr = Data.getUserID(context,"",0);

        String url = Urls.USER_COMMON() +encryStr+"/avatar";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "update");
        params.put("target", "user_avatar");
        params.put("token", token);
        params.put("value", "");/*.*//*.params("file",file)"http://192.168.1.247:8081/"*/
        OkGo.<String>post(url)
                .tag(this)
                .isMultipart(true)
                .params(params)
                .params("file",file)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( response.code() == 200){
                            Context context = getContext();
                            getImageToShare(context);
                        }
                    }
                    @Override
                    public void uploadProgress(Progress progress) {
                        /*Context context = getContext();
                        Toast.makeText(context, progress.toString(),Toast.LENGTH_SHORT).show();*/
                        /*EditText test = view.findViewById(R.id.testshow);
                        test.setText(progress.toString());*/
                        //这里回调上传进度(该回调在主线程,可以直接更新ui)
                        //mProgressBar.setProgress((int) (100 * progress));
                        //mTextView2.setText("已上传" + currentSize/1024/1024 + "MB, 共" + totalSize/1024/1024 + "MB;");
                    }
                    @Override
                    public void onError(Response<String> response) {
                        mFile.delete();
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    public void getImageToShare(Context context) {
        int exist = SpUtil.getInt(context,"List",0);
        if ( exist == 0){
            return;
        }
        String state = Data.getState(context);
        if ( state.equals("logout")){
            return;
        }
        String email = Data.getEmail(context);
        String token = Data.getToken(context);

        String encryStr = Data.getUserID(context,"",0);

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
                                mPersonImgUrl = result.getString("avatar");
                                List otherInfo = SpUtil.getList(getContext(),"OtherInfo");
                                String name = otherInfo.get(0).toString();
                                String address = otherInfo.get(1).toString();
                                Reserve.reserveOther(getContext(),name,address,mPersonImgUrl);
                                if (mPersonImgUrl.equals("")){
                                    Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                                }else{
                                    Picasso.with(getContext()).load(mPersonImgUrl).error(R.drawable.icon_logo).into(mUserImg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else if( response.code() == 410){
                            try{
                                List otherInfo = SpUtil.getList(getContext(),"OtherInfo");
                                String avatar = otherInfo.get(2).toString();
                                if (avatar.equals("")){
                                    Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                                }else{
                                    Picasso.with(getContext()).load(avatar).error(R.drawable.icon_logo).into(mUserImg);
                                }
                            }catch (Exception e){
                                Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( response.code()==-1||response.code()==502){
                            try{
                                List otherInfo = SpUtil.getList(getContext(),"OtherInfo");
                                String avatar = otherInfo.get(2).toString();
                                if (avatar.equals("")){
                                    Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                                }else{
                                    Picasso.with(getContext()).load(avatar).error(R.drawable.icon_logo).into(mUserImg);
                                }
                            }catch (Exception e){
                                Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                            }
                        }
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    @Override
    public void onResume(){
        Context context = getContext();
        try {
            List FirstUser = SpUtil.getList(context,"FirstUser");
            if ( FirstUser == null){
                MyFragment.mBtnLogout.setVisibility(View.GONE);
            }else{
                String state = FirstUser.get(3).toString();

                if ( state.equals("logout")){
                    mLog.setText("Log in");
                    mAddress.setText("");
                    mPrintersNum.setText("0");
                    mPrinterProfileNum.setText("0");
                    mMaterialProfileNum.setText("0");
                    Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                    MyFragment.mBtnLogout.setVisibility(View.GONE);
                }else{
                    try {
                        //getList();
                        getInfo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }
    //获取个人信息详情
    public void getInfo(){
        try{
            List otherInfo = SpUtil.getList(getContext(),"OtherInfo");
            String name = otherInfo.get(0).toString();
            String address = otherInfo.get(1).toString();
            String avatar = otherInfo.get(2).toString();
            mLog.setText(name);
            mAddress.setText(address);
            if (avatar.equals("")){
                Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
            }else{
                Picasso.with(getContext()).load(avatar).placeholder(R.drawable.icon_logo).error(R.drawable.icon_logo).into(mUserImg);
            }
        }catch (Exception e){
            Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
        }
        if ( MainActivity.isNetWork != 1){
            return;
        }

        String email = Data.getEmail(getContext());
        String token = Data.getToken(getContext());
        String encryStr = Data.getUserID(getContext(),"",0);

        String url = Urls.USER_COMMON() + encryStr +"?token=" + token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( response.code() == 200){
                            try {
                                JSONObject jsonObject = new JSONObject(response.body());
                                String name = jsonObject.getString("name");
                                String address = jsonObject.getString("address");
                                String machines = jsonObject.getString("machines");
                                String machineProfiles = jsonObject.getString("machineProfiles");
                                String materialProfiles = jsonObject.getString("materialProfiles");
                                JSONArray machinesArray = new JSONArray(machines);
                                JSONArray machinePArray = new JSONArray(machineProfiles);
                                JSONArray mateialPArray = new JSONArray(materialProfiles);
                                int a = 0,b = 0,c = 0,num = 0;
                                for( int i=0;i<machinesArray.length();i++){
                                    if (machinesArray.get(i).equals("*")){
                                        num = num + 1;
                                    }
                                }
                                a = machinesArray.length() - num;
                                b = machinePArray.length();
                                c = mateialPArray.length();
                                mLog.setText(name);
                                mAddress.setText(address);
                                mPrintersNum.setText(a+"");
                                mPrinterProfileNum.setText(b+"");
                                mMaterialProfileNum.setText(c+"");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( response.code() == -1||response.code() == 502){
                            return;
                        }
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    //获取用户绑定的打印机
    public void getList() throws Exception {
        Context context = getContext();

        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String state = Data.getState(context);
        String encryStr = Data.getUserID(context,"",0);

        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printers?token="+token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        MainActivity.tipHide("");
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        if ( responseCode == 200){
                            try{
                                MainActivity.printerList.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("serial_num");
                                    String mPrinterName = jsonObject.getString("name");
                                    initList(mPrinterName,mSerialNumber);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        MainActivity.tipHide("");
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    private void initList(String name,String number){
        ListItem item = new ListItem(name, number);
        MainActivity.printerList.add(item);
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            Context context = getContext();
            try {
                List FirstUser = SpUtil.getList(context,"FirstUser");
                if ( FirstUser == null){
                    MyFragment.mBtnLogout.setVisibility(View.GONE);
                }else{
                    String state = FirstUser.get(3).toString();

                    if ( state.equals("logout")){
                        mLog.setText("Log in");
                        mAddress.setText("");
                        mPrintersNum.setText("0");
                        mPrinterProfileNum.setText("0");
                        mMaterialProfileNum.setText("0");
                        Picasso.with(getContext()).load(R.drawable.icon_logo).into(mUserImg);
                        MyFragment.mBtnLogout.setVisibility(View.GONE);
                    }else{
                        try {
                            getInfo();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //相当于Fragment的onPause
        }
    }
    //网页跳转
    public void openDefaultBrowser(String load_url)
    {
        /*try{
            //系统默认浏览器打开网页
            Intent intent = new Intent();
            intent.setAction("Android.intent.action.VIEW");
            Uri content_url = Uri.parse(load_url);
            intent.setData(content_url);
            intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
            startActivity(intent);
        }catch (Exception e){*/
            //选择浏览器打开网页
            Uri uri = Uri.parse(load_url);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        //}
    }
}