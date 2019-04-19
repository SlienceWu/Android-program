package com.geeetech.administrator.easyprint.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.CircleImageView;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018-02-06.
 */

public class PrinterDetail extends BaseActivity {
    private static final int PHOTO_REQUEST_CAREMA = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private File mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/easyprint", "print.jpg");
    private Uri imageUri = Uri.fromFile(mFile);
    String[] mPermissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private TextView mPrintName;
    private TextView mPrintNumber;
    private String mPrinterName;
    private String mSerialNumber;
    private CircleImageView mCircleImg;
    private ImageView mCircleImgChoose;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_detail);

        mPrintName = findViewById(R.id.printer_name);
        mPrintNumber = findViewById(R.id.printer_number);
        mCircleImg = findViewById(R.id.circle_img);
        mCircleImgChoose = findViewById(R.id.circle_img_choose);

        mPrinterName = getIntent().getStringExtra("printer_name");
        mSerialNumber = getIntent().getStringExtra("serial_number");
        mPrintName.setText(mPrinterName);
        mPrintNumber.setText(mSerialNumber);
        getImg(getBaseContext());

        if ( mSerialNumber.indexOf("E180")>0){
            Picasso.with(getBaseContext()).load(R.mipmap.icon_e180).into(mCircleImg);
        }else if ( mSerialNumber.indexOf("D200")>0){
            Picasso.with(getBaseContext()).load(R.mipmap.icon_d200).into(mCircleImg);
        }else if ( mSerialNumber.indexOf("A30")>0){
            Picasso.with(getBaseContext()).load(R.mipmap.icon_a30).into(mCircleImg);
        }else{
            Picasso.with(getBaseContext()).load(R.mipmap.icon_3dwifi).into(mCircleImg);
        }
        /*解绑*/
        Button unbind = findViewById(R.id.button_delete_print);
        unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unBind(mSerialNumber);
            }
        });
        /*修改机器名*/
        ImageView Editname = findViewById(R.id.edit_name);
        Editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(PrinterDetail.this);
                builder.setTitle("Change your printer name")
                        .setPlaceholder("")
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("Sure", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                CharSequence text = builder.getEditText().getText();
                                if (text != null && text.length() > 0) {
                                    mPrintName.setText(text);
                                    changeName();
                                    dialog.dismiss();
                                } else {
                                    ToastUtil.showToast(PrinterDetail.this, "Please write something");
                                }
                            }
                        })
                        .show();
            }
        });
        mCircleImgChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( getState().equals("login")&&MainActivity.isNetWork == 1){
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        //动态获取权限
                        ActivityCompat.requestPermissions(PrinterDetail.this, mPermissionList, 100);
                    }else{
                        gallery(PrinterDetail.this.getWindow().getDecorView());
                    }
                }
            }
        });
    }

    //解除绑定
    private void unBind(String serialNum){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String url = Urls.USER_DELETE_PRINTER() + getUserID("",0);
        OkGo.<String>delete(url)
                .tag(this)
                .upJson(getUnBind())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        int responseCode = response.code();
                        if ( responseCode == 200){
                            try {
                                getList();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if ( mSerialNumber.equals(MainActivity.mCurrentPrinter)){
                                MainActivity.mCurrentPrinter = "";
                                MainActivity.mCurrentName = "";
                            }
                            onBackPressed();
                            ToastUtil.showToast(PrinterDetail.this,"Unbind Success.");
                        }else if ( responseCode == 409){
                            getQMUITipShow("Printer is not bind.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        int responseCode = response.code();

                        if ( responseCode == 404){
                            getQMUITipShow("Printer is not exist",0);
                        }else{
                            getErrorRespone(response);
                        }
                    }
                });
    }
    //修改名称
    public void changeName(){
        if ( getState().equals("logout")){
            getQMUITipShow("please log in",0);
            return;
        }
        String encryStr = getUserID("",0);
        String encryStr1 = getUserID(mSerialNumber,1);
        String url = Urls.GET_PRINTER_LIST() + encryStr +"/printers/" + encryStr1;
        OkGo.<String>patch(url)
                .tag(this)
                .upJson(getChangeName())
                .execute(new StringCallback(){
                    @Override
                    public void onSuccess(Response<String> response){
                        if (PrinterDetail.this.isFinishing()){
                            return;
                        }
                        int responseCode = response.code();
                        String res = response.body();
                        if ( responseCode == 200){
                            getQMUITipShow("Change Success.",0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if (PrinterDetail.this.isFinishing()){
                            return;
                        }
                        getErrorRespone(response);
                    }
                });
    }
    //请求参数
    public JSONObject getUnBind(){
        JSONObject js=new JSONObject();
        String token = getToken();
        try {
            js.put("action","delete");
            js.put("target","user_printer");
            js.put("serial_num",mSerialNumber);
            js.put("token",token);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
    }
    public JSONObject getChangeName(){
        JSONObject js=new JSONObject();

        String token = getToken();
        String name = mPrintName.getText().toString();

        try {
            js.put("action","update");
            js.put("target","printer_name");
            js.put("value",name);
            js.put("token",token);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return js;
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
                    gallery(this.getWindow().getDecorView());
                }else { //拒绝权限申请
                    ToastUtil.showToast(getBaseContext(),"Permissions are denied.");
                }
                break;
            default:
                break;
        }
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
        Context context = getBaseContext();
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
                //crop(Uri.fromFile(tempFile));
            } else {
                ToastUtil.showToast(context, "Unable to store photos without finding a storage card!");
            }

        } else if (requestCode == PHOTO_REQUEST_CUT) {
            // 从剪切图片返回的数据
            if (data != null) {
                if ( mFile != null){
                    setImg(mFile);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public  void setImg(File file) {
        //这里是头像接口，通过Post请求，拼接接口地址和ID，上传数据。
        String url = Urls.USER_COMMON() +getUserID("",0)+"/printers/"+getUserID(mSerialNumber,1)+"/image";
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "update");
        params.put("target", "user_avatar");
        params.put("token", getToken());
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
                            Context context = getBaseContext();
                            getImg(context);
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
                        getErrorRespone(response);
                    }
                });
    }
    public void getImg(Context context) {
        String url = Urls.USER_COMMON() +getUserID("",0)+"/printers/"+getUserID(mSerialNumber,1)+"/image?token="+getToken();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String res = response.body();
                        String imgUrl = "";
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            imgUrl = jsonObject.getString("image");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if ( imgUrl.equals("")){
                            return;
                        }
                        Picasso.with(getBaseContext()).load(imgUrl).into(mCircleImg);
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response);
                    }
                });
    }
}
