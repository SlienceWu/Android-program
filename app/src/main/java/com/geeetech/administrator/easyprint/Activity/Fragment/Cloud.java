package com.geeetech.administrator.easyprint.Activity.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.geeetech.administrator.easyprint.Activity.ImgList;
import com.geeetech.administrator.easyprint.AutoLoadListener;
import com.geeetech.administrator.easyprint.GlideImageLoader;
import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import static com.geeetech.administrator.easyprint.Internet.ErrorResponse.getErrorRespone;

/**
 * Created by Administrator on 2018-02-03.
 */

public class Cloud extends Fragment implements OnBannerListener {
    private View view;
    List<?> mImages=new ArrayList<>();
    private ImageView mImgPrint,mImgEducation,mImgArt,
            mImgFashion,mImgGadget,mImgHobby,
            mImgHousehold,mImgTools,mImgToysGames;
    public static List<ListItem> mainModelList = new ArrayList<>();
    //资源文件
    private ArrayList<String> list_path = new ArrayList<>();
    private Banner banner;

    private ImageView mBtnToMini;

    public static Cloud newInstance(String info) {
        Bundle args = new Bundle();
        Cloud fragment = new Cloud();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cloud, null);

        //轮播图
        banner = (Banner) view.findViewById(R.id.banner);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        banner.setOnBannerListener(this);
        initBanner();
        mImgPrint = view.findViewById(R.id.imageView_model_1);
        mImgEducation = view.findViewById(R.id.imageView_model_2);
        mImgArt = view.findViewById(R.id.imageView_model_3);
        mImgFashion = view.findViewById(R.id.imageView_model_4);
        mImgGadget = view.findViewById(R.id.imageView_model_5);
        mImgHobby = view.findViewById(R.id.imageView_model_6);
        mImgHousehold = view.findViewById(R.id.imageView_model_7);
        mImgTools = view.findViewById(R.id.imageView_model_8);
        mImgToysGames = view.findViewById(R.id.imageView_model_9);

        mImgPrint.setOnClickListener(clickListener);
        mImgEducation.setOnClickListener(clickListener);
        mImgArt.setOnClickListener(clickListener);
        mImgFashion.setOnClickListener(clickListener);
        mImgGadget.setOnClickListener(clickListener);
        mImgHobby.setOnClickListener(clickListener);
        mImgHousehold.setOnClickListener(clickListener);
        mImgTools.setOnClickListener(clickListener);
        mImgToysGames.setOnClickListener(clickListener);

        //test
        mBtnToMini = view.findViewById(R.id.imageView_middle);
        mBtnToMini.setOnClickListener(clickListener);
        return view;
    }

    View.OnClickListener clickListener = new View.OnClickListener(){
        public void onClick(View view){
            Intent intent = new Intent();
            try{
                AutoLoadListener.state = null;
            }catch (Exception e){
                e.printStackTrace();
            }
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
            }else if ( MainActivity.isNetWork == 0){
                new QMUIDialog.MessageDialogBuilder(getContext())
                        .setMessage("Network connection fail")
                        .addAction("OK", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return;
            }
            switch (view.getId()){
                case R.id.imageView_model_1:
                    intent.putExtra("categoryID",0);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_2:
                    intent.putExtra("categoryID",1);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_3:
                    intent.putExtra("categoryID",2);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_4:
                    intent.putExtra("categoryID",3);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_5:
                    intent.putExtra("categoryID",4);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_6:
                    intent.putExtra("categoryID",5);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_7:
                    intent.putExtra("categoryID",6);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_8:
                    intent.putExtra("categoryID",7);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_model_9:
                    intent.putExtra("categoryID",8);
                    intent.setClass(getActivity(), ImgList.class);
                    startActivity(intent);
                    break;
                case R.id.imageView_middle:
                    //download();
                    //intent.setClass(getActivity(), Myminifactory.class);
                    //startActivity(intent);
                    break;
            }
        }
    };
    //test下载
    public void download(){
        //下载路径，如果路径无效了，可换成你的下载路径
        final String url = "http://www.geeetech.com/firmware/Toolkits.zip";
        final long startTime = System.currentTimeMillis();
        Log.i("DOWNLOAD","startTime="+startTime);

        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                ToastUtil.showToast(getContext(),"download failed");
                Log.i("DOWNLOAD","download failed");
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    String mSDCardPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/easyprint";
                    File dest = new File(mSDCardPath,   url.substring(url.lastIndexOf("/") + 1));
                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());

                    bufferedSink.close();
                    ToastUtil.showToast(getContext(),"download success");
                    Log.i("DOWNLOAD","download success");
                    Log.i("DOWNLOAD","totalTime="+ (System.currentTimeMillis() - startTime));
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast(getContext(),"download failed");
                    Log.i("DOWNLOAD","download failed");
                } finally {
                    if(bufferedSink != null){
                        bufferedSink.close();
                    }

                }
            }
        });
    }
    //轮播图的监听方法
    @Override
    public void OnBannerClick(int position) {
        //http://www.giantarm.com/featured_product/d200-3dprinter.html
        openDefaultBrowser("http://www.geeetech.com/");
    }

    @Override
    public void onResume(){
        if ( MainActivity.isNetWork == 1){
            getModelBase();
            initBanner();
        }
        super.onResume();
    }
    //获取模型库id
    public void getModelBase(){
        String url = Urls.SERVER + "/v1/modelbase/categories";
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if( response.code() == 200){
                            try{
                                JSONArray jsonArray = new JSONArray(response.body());
                                if ( jsonArray.length() == 0){
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String modelName = jsonObject.getString("category_name");
                                    String modelId = jsonObject.getString("category_id");
                                    ListItem item = new ListItem(modelName,modelId);
                                    mainModelList.add(item);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }
    //获取轮播图图片
    public void initBanner(){
        String url = Urls.USER_BANNER();
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response.body());
                            if ( jsonArray.length() == 0){
                                return;
                            }
                            list_path.clear();
                            for (int i=0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String path = jsonObject.getString("image_path");
                                list_path.add(path);
                            }
                            //设置图片集合
                            banner.setImages(list_path);
                            //banner设置方法全部调用完毕时最后调用
                            banner.start();
                        } catch (JSONException e) {
                            errorBanner();
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        errorBanner();
                    }
                });
    }
    //获取轮播图错误
    public void errorBanner(){
        //资源文件
        Integer[] urls ={R.drawable.icon_error,R.drawable.icon_error,R.drawable.icon_error};
        List list = Arrays.asList(urls);
        mImages = new ArrayList(list);
        //设置图片集合
        banner.setImages(mImages);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
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