package com.geeetech.administrator.easyprint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Activity.Fragment.GallaryFragment;
import com.geeetech.administrator.easyprint.Activity.Fragment.MyFragment;
import com.geeetech.administrator.easyprint.Activity.Fragment.SdCard;
import com.geeetech.administrator.easyprint.Internet.CommonJSONParser;
import com.geeetech.administrator.easyprint.Internet.NetWorkStateReceiver;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.DrawerItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.DrawerListItem;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.geeetech.administrator.easyprint.Internet.ErrorResponse.getErrorRespone;


public class MainActivity extends AppCompatActivity {

    private ImageView mNavagation;
    private TextView mPrinterName;
    private RelativeLayout mLinearLayout;
    public static String mCurrentPrinter="";
    public static String mCurrentName = "";
    public static TextView netTips;
    public static int isNetWork = 1;
    public static List<ListItem> printerList = new ArrayList<>();
    public static String printerState = "0";
    //private TextView mTextMessage;
    public static ViewPager mViewPager;
    private MenuItem mMenuItem;
    private BottomNavigationView mBottomNavigationView;
    private TextView mMaintopbar;
    NetWorkStateReceiver mNetWorkStateReceiver;

    //初始化侧滑栏列表
    private ListView mListView;
    private static DrawerLayout mDrawerLayout;
    private TextView mTextView;
    public static CircleImageView mPersonImg;
    public DrawerItemAdapter drawerItemAdapter;
    public boolean firstInitDrawer = true;

    //定时查询打印机状态
    final Handler handler = new Handler();
    Runnable runnable;
    //定时查询server次数
    private int mReconnect = 1;
    //侧滑栏显示时定时查询所有打印机信息
    final Handler handlerDrawer = new Handler();
    Runnable runnableDrawer;
    final List<DrawerListItem> listDrawer = new ArrayList<DrawerListItem>();
    //侧滑列表
    final List<String> list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);

        mListView=(ListView) findViewById(R.id.v4_listview);
        mDrawerLayout=(DrawerLayout) findViewById(R.id.v4_drawerlayout);
        mDrawerLayout.addDrawerListener(listen);
        mPersonImg = (CircleImageView) findViewById(R.id.person_img);

        mLinearLayout = findViewById(R.id.linearlayout_top);
        mPrinterName = findViewById(R.id.printer_name);
        mNavagation = findViewById(R.id.img_controll_drawer);
        mNavagation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.showDrawerLayout();
            }
        });

        initView();
        //获取登录状态显示隐藏个人中心登录按键
        try {
            Context context = getBaseContext();
            List FirstUser = SpUtil.getList(context,"FirstUser");
            String state = FirstUser.get(3).toString();
            if ( state.equals("logout")){
                MyFragment.mBtnLogout.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //侧滑栏定时
        runnableDrawer = new Runnable() {
            @Override
            public void run() {
                handlerDrawer.postDelayed(this,1000);
                if ( isNetWork != 1){
                    return;
                }
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                    //String url = "http://192.168.1.247:8081/getDrawerlist";
                    Context context = getBaseContext();
                    String email = "";
                    String token = Data.getToken(context);
                    String state = Data.getState(context);
                    String encryStr = Data.getUserID(context,"",0);
                    if ( state.equals("logout")){
                        return;
                    }

                    String url = Urls.USER_COMMON() + encryStr + "/printersinfo?token=" + token;
                    OkGo.<String>get(url)
                            .tag(this)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    try {
                                        listDrawer.clear();
                                        JSONArray jsonArray = new JSONArray(response.body());
                                        if (jsonArray.length() == 0){
                                            return;
                                        }
                                        List<DrawerListItem> newDrawer = new ArrayList<DrawerListItem>();
                                        newDrawer.clear();

                                        List<DrawerListItem> a = new ArrayList<DrawerListItem>();
                                        List<DrawerListItem> b = new ArrayList<DrawerListItem>();
                                        List<DrawerListItem> c = new ArrayList<DrawerListItem>();
                                        a.clear();
                                        b.clear();
                                        c.clear();
                                        for (int i=0;i<jsonArray.length();i++){
                                            JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                                            String state = jsonObject.getString("state");
                                            String number = jsonObject.getString("serial_num");
                                            String avatar = "";
                                            String progress = "0";
                                            String heatbed = "0";
                                            String extruder = "0";
                                            String name = jsonObject.getString("name");

                                            if ( !state.equals("0")&&!state.equals("1")){
                                                avatar = jsonObject.getString("image");
                                                progress = jsonObject.getString("progress");
                                                heatbed = jsonObject.getString("bed_current_temp");
                                                extruder = jsonObject.getString("extruder_current_temp");
                                                DrawerListItem aa = new DrawerListItem(name,number,state,progress,heatbed,extruder,avatar);
                                                a.add(aa);
                                            }
                                            if(state.equals("0")||state.equals("1")){
                                                DrawerListItem bb = new DrawerListItem(name,number,state,progress,heatbed,extruder,avatar);
                                                b.add(bb);
                                            }
                                            c = Data.getNewDraw(a,b);
                                            DrawerListItem drawerListItem = new DrawerListItem(name,number,state,progress,heatbed,extruder,avatar);
                                            newDrawer.add(drawerListItem);
                                        }
                                        if ( firstInitDrawer ){
                                            listDrawer.addAll(c);
                                            firstInitDrawer = false;
                                            initDrawer();
                                        }else{
                                            listDrawer.addAll(c);
                                            drawerItemAdapter.notifyDataSetChanged();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onError(Response<String> response){
                                    getErrorRespone(response,getBaseContext(),MainActivity.this);
                                }
                            });
                }else{
                    firstInitDrawer = true;
                }
            }
        };
        //定时获取打印机状态
        runnable = new Runnable() {
            @Override
            public void run() {
                Context context = getBaseContext();
                String state = Data.getState(context);

                if ( state.equals("login") && isNetWork == 1){
                    if ( mCurrentPrinter.equals("")){
                        try {
                            getList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    getPrinterState();
                }else{
                    try{
                        PrintFragment.mHeatbedTemp.setText("0℃");
                        PrintFragment.mExtruderTemp.setText("0℃");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                String url = Urls.USER_CHECK();
                OkGo.<String>get(url)
                        .tag(this)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                if (response.code() == 200){
                                    mReconnect = 1;
                                    tipHide("");
                                }
                            }
                            @Override
                            public void onError(Response<String> response) {
                                //ToastUtil.showToast(getBaseContext(),"server fail"+isNetWork);
                                mReconnect ++;
                                if ( isNetWork == 0){
                                    return;
                                }
                                if ( mReconnect>3){
                                    tipShow("server");
                                }
                            }
                        });
                handler.postDelayed(this,2000);
            }
        };

        //网络连接提示view
        netTips = (TextView) findViewById(R.id.network);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mMaintopbar=findViewById(R.id.Maintopbar);
        setTopBarText("Print");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_dashboard:
                                mViewPager.setCurrentItem(0,false);
                                item.setIcon(R.drawable.print_1);
                                setTopBarText("Print");
                                break;
                            case R.id.navigation_notifications:
                                mViewPager.setCurrentItem(1,false);
                                item.setIcon(R.drawable.commin_y);
                                setTopBarText("Gallery");
                                break;
                            case R.id.navigation_me:
                                mViewPager.setCurrentItem(2,false);
                                item.setIcon(R.drawable.my_1);
                                setTopBarText("Me");
                                break;
                        }
                        return false;
                    }
                });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mMenuItem != null) {
                    mMenuItem.setChecked(false);
                } else {
                    mBottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                mMenuItem = mBottomNavigationView.getMenu().getItem(position);
                mMenuItem.setChecked(true);
                if ( position == 0){
                    PrintFragment.getSlideClose();
                    setTopBarText("Print");
                }else if ( position == 1){
                    setTopBarText("Gallery");
                    String serialNumber = MainActivity.mCurrentPrinter;
                    if ( serialNumber.equals("")){
                        SdCard.mLinearLayout.setVisibility(View.GONE);
                        return;
                    }
                    SdCard.mGetList.setText(MainActivity.mCurrentName);
                    SdCard.mLinearLayout.setVisibility(View.VISIBLE);
                }else if ( position == 2){
                    setTopBarText("Me");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //禁止ViewPager滑动
//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
        setupViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(2);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(PrintFragment.newInstance("Print"));
        adapter.addFragment(GallaryFragment.newInstance("Gallery"));
        adapter.addFragment(MyFragment.newInstance("Me"));
        viewPager.setAdapter(adapter);
    }

    /*顶部导航文字*/
    private void setTopBarText(String text) {
        if ( text.equals("")){
            mMaintopbar.setText("");
            return;
        }else if( text.equals("Print")){
            mMaintopbar.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.VISIBLE);
            //if ( !mCurrentName.equals("")){
                mPrinterName.setText(mCurrentName);
            //}
            //启用侧滑
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            return;
        }
        //commonRequest(viewPager);
        //禁用侧滑
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mLinearLayout.setVisibility(View.GONE);
        mMaintopbar.setVisibility(View.VISIBLE);
        mMaintopbar.setText(text);
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {
        if (mNetWorkStateReceiver == null) {
            mNetWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkStateReceiver, filter);
        //System.out.println("注册");
        try{
            handler.postDelayed(runnable,0);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            handlerDrawer.postDelayed(runnableDrawer,0);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onResume();
        //timer.schedule(task,0,5000);
    }
    //onPause()方法注销
    @Override
    protected void onPause() {
        try{
            handler.removeCallbacks(runnable);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            handlerDrawer.removeCallbacks(runnableDrawer);
        }catch (Exception e){
            e.printStackTrace();
        }

        unregisterReceiver(mNetWorkStateReceiver);
        //System.out.println("注销");
        super.onPause();
    }

    //网络异常提示
    public static void tipShow(String text) {
        /*if(netTips.getVisibility() == View.VISIBLE){
            return;
        }*/
        if ( text.equals("server")){
            isNetWork = 2;
            netTips.setText(R.string.server_tip);
        }else if( text.equals("routing")){
            isNetWork = 2;
            netTips.setText(R.string.rout_tip);
            //return;
        }else{
            isNetWork = 0;
            netTips.setText(R.string.network_tip);
            //return;
        }
        //PrintFragment.getSlideClose();
        //netTips.setVisibility(View.GONE);
        netTips.setVisibility(View.VISIBLE);
        //isNetWork = false;
    }
    //隐藏异常提示
    public static void tipHide(String text) {
        netTips.setVisibility(View.GONE);
        isNetWork = 1;
        //isNetWork = true;
    }

    //获取用户绑定的打印机
    public void getList() throws Exception {
        Context context = getBaseContext();
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
                        tipHide("");
                        if ( MainActivity.this.isFinishing()) {
                            return;
                        }
                        String res = null;
                        int responseCode = response.code();
                        res = response.body();
                        //解析json
                        CommonJSONParser commonJSONParser = new CommonJSONParser();
                        Map<String, Object> result = commonJSONParser.parse(res);
                        if ( responseCode == 200){
                            try{
                                printerList.clear();
                                //list.clear();
                                JSONArray jsonArray = new JSONArray(res);
                                if ( jsonArray.length() == 0){
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_printer_off).into(PrintFragment.mCircleImg);
                                    PrintFragment.mPrinterState.setText("Unbind");
                                    return;
                                }
                                for (int i=0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String mSerialNumber = jsonObject.getString("serial_num");
                                    String mPrinterName = jsonObject.getString("name");
                                    initList(mPrinterName,mSerialNumber);
                                    //list.add(mPrinterName);
                                }
                                initView();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response){
                        if ( MainActivity.this.isFinishing()) {
                            return;
                        }
                        tipHide("");
                        getErrorRespone(response,getBaseContext(),MainActivity.this);
                    }
                });
    }
    public static void initList(String name,String number){
        ListItem item = new ListItem(name, number);
        printerList.add(item);
        if ( mCurrentPrinter.equals("")){
            mCurrentPrinter = printerList.get(0).getNumber();
            mCurrentName = printerList.get(0).getName();
        }
    }

    //获取打印机状态
    public void getPrinterState(){
        String serialNumber = mCurrentPrinter;
        if ( mCurrentPrinter.equals("")){
            try{
                Picasso.with(getBaseContext()).load(R.drawable.icon_printer_off).into(PrintFragment.mCircleImg);
            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }
        Context context = getBaseContext();
        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String state = Data.getState(context);
        String encryStr = Data.getUserID(context,"",0);
        String encryStrId = Data.getUserID(context,serialNumber,1);

        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId +"?token=" + token;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if ( MainActivity.this.isFinishing()) {
                            return;
                        }
                        tipHide("");
                        int code = response.code();
                        if ( code == 200){
                            String res = response.body();
                            try {
                                JSONObject result = new JSONObject(res);
                                String state = result.getString("state");
                                String bedSetTemp,bedNowTemp,exSetTemp,exNowTemp;
                                PrintFragment.mGcodeName.setText("");
                                PrintFragment.mCircleProgress.setProgress(0);
                                mPrinterName.setText(mCurrentName);
                                //urlImg("http://192.168.1.220:8888/machine/E180.png");
                                if ( state.equals("2")){
                                    printerState = "2";
                                    PrintFragment.mPrinterState.setText("Online");
                                    bedNowTemp = result.getString("bed_current_temp")+"℃";
                                    exNowTemp = result.getString("extruder_current_temp")+"℃";
                                    PrintFragment.mHeatbedTemp.setText(bedNowTemp);
                                    PrintFragment.mExtruderTemp.setText(exNowTemp);
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_start1).into(PrintFragment.mStartPrint);
                                }else if ( state.equals("0")){
                                    //PrintFragment.mCircleImg.setVisibility(View.GONE);
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_printer_off).into(PrintFragment.mCircleImg);
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_start1).into(PrintFragment.mStartPrint);
                                    //PrintFragment.mCircleProgress.setBackgroundResource(R.drawable.icon_printer_off);
                                    printerState = "0";
                                    PrintFragment.mHeatbedTemp.setText("0℃");
                                    PrintFragment.mExtruderTemp.setText("0℃");
                                    PrintFragment.mPrinterState.setText("Offline");
                                    return;
                                }else if ( state.equals("1")){
                                    //PrintFragment.mCircleImg.setVisibility(View.GONE);
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_printer_off).into(PrintFragment.mCircleImg);
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_start1).into(PrintFragment.mStartPrint);
                                    //PrintFragment.mCircleProgress.setBackgroundResource(R.drawable.icon_printer_off);
                                    printerState = "1";
                                    PrintFragment.mHeatbedTemp.setText("0℃");
                                    PrintFragment.mExtruderTemp.setText("0℃");
                                    PrintFragment.mPrinterState.setText("Offline");
                                    return;
                                }else if ( state.equals("3")){
                                    printerState = "3";
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_start2).into(PrintFragment.mStartPrint);
                                    PrintFragment.mPrinterState.setText("Printing");
                                    bedNowTemp = result.getString("bed_current_temp")+"℃";
                                    exNowTemp = result.getString("extruder_current_temp")+"℃";
                                    PrintFragment.mHeatbedTemp.setText(bedNowTemp);
                                    PrintFragment.mExtruderTemp.setText(exNowTemp);
                                    Double a = result.getDouble("progress")*100;
                                    int progress = a.intValue();
                                    PrintFragment.mCircleProgress.setProgress(progress);
                                    PrintFragment.mGcodeName.setText(SdCard.mPrintFileName);
                                }else if ( state.equals("4")){
                                    printerState = "4";
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_start1).into(PrintFragment.mStartPrint);
                                    PrintFragment.mPrinterState.setText("Pause");
                                    bedNowTemp = result.getString("bed_current_temp")+"℃";
                                    exNowTemp = result.getString("extruder_current_temp")+"℃";
                                    try{
                                        PrintFragment.mHeatbedTemp.setText(bedNowTemp);
                                        PrintFragment.mExtruderTemp.setText(exNowTemp);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    Double a = result.getDouble("progress")*100;
                                    int progress = a.intValue();
                                    PrintFragment.mCircleProgress.setProgress(progress);
                                    try{
                                        String taskFile = result.getString("task_file");
                                        PrintFragment.mGcodeName.setText(SdCard.mPrintFileName);
                                    }catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                getImg(getBaseContext(),mCurrentPrinter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( MainActivity.this.isFinishing()) {
                            return;
                        }
                        tipHide("");
                        getErrorRespone(response,getBaseContext(),MainActivity.this);
                    }
                });
    }

    //返回按键退出
    private long time = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (System.currentTimeMillis() - time > 2000) {
                time = System.currentTimeMillis();
                ToastUtil.showToast(getBaseContext(), "Press once again to exit!");
            } else{
                //finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //初始化侧滑栏列表
    private void initView()
    {
        setDrawerRightEdgeSize(mDrawerLayout,this, 0.2f);
        try {
            //获取侧滑图像
            getDrawerImg(getBaseContext());
            //添加跳转登录
            String state = Data.getState(getBaseContext());
            TextView personImg = findViewById(R.id.person_name);
            if ( state.equals("logout")){
                personImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                });
            }else{
                personImg.setOnClickListener(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initDrawer();
    }

    //侧滑显示隐藏
    public static void showDrawerLayout() {
        if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }
    /**
     * 设置drawerLayout滑动边距
     * @param drawerLayout
     * @param activity
     * @param proportion
     */
    public static void setDrawerRightEdgeSize(DrawerLayout drawerLayout, Activity activity, float proportion){
        if(drawerLayout==null||activity==null){
            return;
        }
        try {
            Field field = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            field.setAccessible(true);
            ViewDragHelper mLeftDragger = (ViewDragHelper) field.get(drawerLayout);
            Field field1 = mLeftDragger.getClass().getDeclaredField("mEdgeSize");
            field1.setAccessible(true);
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            field1.setInt(mLeftDragger, (int) (metrics.widthPixels*proportion));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //侧滑头像显示
    public void getDrawerImg(Context context) throws Exception{
        int exist = SpUtil.getInt(context,"List",0);
        if ( exist == 0){
            return;
        }
        String state = Data.getState(context);
        if ( state.equals("logout")){
            TextView personImg = findViewById(R.id.person_name);
            personImg.setText("Log in");
            Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(mPersonImg);
            return;
        }

        List otherInfo = SpUtil.getList(getBaseContext(),"OtherInfo");
        String name = otherInfo.get(0).toString();
        String avatar = otherInfo.get(2).toString();
        if (avatar.equals("")){
            Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(mPersonImg);
        }else{
            Picasso.with(getBaseContext()).load(avatar).error(R.drawable.icon_logo).into(mPersonImg);
        }
        TextView personImg = findViewById(R.id.person_name);
        personImg.setText(name);

        if (isNetWork != 1){
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
                                String mPersonImgUrl = result.getString("avatar");
                                if ( mPersonImgUrl.equals("")){
                                    Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(mPersonImg);
                                }else{
                                    Picasso.with(getBaseContext()).load(mPersonImgUrl).into(mPersonImg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else if( response.code() == 410){
                            Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(mPersonImg);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        if ( response.code() == -1||response.code()==502){
                            try{
                                List otherInfo = SpUtil.getList(getBaseContext(),"OtherInfo");
                                String avatar = otherInfo.get(2).toString();
                                Picasso.with(getBaseContext()).load(avatar).error(R.drawable.icon_logo).into(mPersonImg);
                            }catch (Exception e){
                                Picasso.with(getBaseContext()).load(R.drawable.icon_logo).into(mPersonImg);
                            }
                        }else{
                            getErrorRespone(response,getBaseContext(),MainActivity.this);
                        }
                    }
                });
    }
    //侧滑列表刷新
    public void initDrawer(){
        drawerItemAdapter = new DrawerItemAdapter(this,R.layout.print_drawer_list_item, listDrawer);
        mListView.setAdapter(drawerItemAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    //mCurrentPrinter = printerList.get(position).getNumber();
                    //mCurrentName = printerList.get(position).getName();
                    mCurrentPrinter = listDrawer.get(position).getNumber();
                    mCurrentName = listDrawer.get(position).getName();
                    mPrinterName.setText(mCurrentName);
                }catch (Exception e){
                    mCurrentName = "";
                    mPrinterName.setText(mCurrentName);
                }

                try{
                    Double a = Double.valueOf(listDrawer.get(position).getProgress())*100;
                    int progress = a.intValue();
                    PrintFragment.mCircleProgress.setProgress(progress);
                    //PrintFragment.mCircleProgress.setProgress(Integer.parseInt("0.2")*100);//listDrawer.get(position).getProgress()
                }catch (Exception e){
                    e.printStackTrace();
                }
                showDrawerLayout();
            }
        });
    }
    DrawerLayout.DrawerListener listen = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            View mContent = mDrawerLayout.getChildAt(0);
            View mMenu = drawerView;
            float scale = 1 - slideOffset;
            //改变DrawLayout侧栏透明度，若不需要效果可以不设置
            ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
            ViewHelper.setTranslationX(mContent,
                    mMenu.getMeasuredWidth() * (1 - scale));
            ViewHelper.setPivotX(mContent, 0);
            ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
            mContent.invalidate();
        }
        @Override
        public void onDrawerOpened(View drawerView) {
            ImageView img = findViewById(R.id.img_controll_drawer);
            img.setImageResource(R.mipmap.icon_menu_off);
            if (mCurrentPrinter.equals("")){
                try{
                    listDrawer.removeAll(listDrawer);
                    drawerItemAdapter.notifyDataSetChanged();
                    mListView.setAdapter(drawerItemAdapter);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            initView();
        }
        @Override
        public void onDrawerClosed(View drawerView) {
            ImageView img = findViewById(R.id.img_controll_drawer);
            img.setImageResource(R.mipmap.icon_menu_on);
        }
        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    //获取打印机图片
    public void getImg(Context context,String serialNumber) {
        String url = Urls.USER_COMMON() + Data.getUserID(context,"",0)+"/printers/"+Data.getUserID(context,serialNumber,1)+"/image?token="+Data.getToken(context);
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
                        if( imgUrl.equals("")){
                            PrintFragment.mCircleImg.setVisibility(View.VISIBLE);
                            if ( mCurrentPrinter.indexOf("E180")>0){
                                //PrintFragment.mCircleProgress.setBackgroundResource(R.mipmap.icon_e180);
                                Picasso.with(getBaseContext()).load(R.mipmap.icon_e180).into(PrintFragment.mCircleImg);
                            }else if ( mCurrentPrinter.indexOf("D200")>0){
                                //PrintFragment.mCircleProgress.setBackgroundResource(R.mipmap.icon_d200);
                                Picasso.with(getBaseContext()).load(R.mipmap.icon_d200).into(PrintFragment.mCircleImg);
                            }else if ( mCurrentPrinter.indexOf("A30")>0){
                                //PrintFragment.mCircleProgress.setBackgroundResource(R.mipmap.icon_a30);
                                Picasso.with(getBaseContext()).load(R.mipmap.icon_a30).into(PrintFragment.mCircleImg);
                            }else{
                                //PrintFragment.mCircleProgress.setBackgroundResource(R.mipmap.icon_3dwifi);
                                Picasso.with(getBaseContext()).load(R.mipmap.icon_3dwifi).into(PrintFragment.mCircleImg);
                            }
                        }else{
                            //PrintFragment.mCircleImg.setVisibility(View.VISIBLE);
                            Picasso.with(getBaseContext()).load(imgUrl).into(PrintFragment.mCircleImg);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response,getBaseContext(),MainActivity.this);
                    }
                });
    }
}