package com.geeetech.administrator.easyprint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.Activity.Config;
import com.geeetech.administrator.easyprint.Activity.Filament;
import com.geeetech.administrator.easyprint.Activity.Fragment.GallaryFragment;
import com.geeetech.administrator.easyprint.Activity.Gcode;
import com.geeetech.administrator.easyprint.Activity.Level;
import com.geeetech.administrator.easyprint.Activity.Move;
import com.geeetech.administrator.easyprint.Activity.Printers;
import com.geeetech.administrator.easyprint.Activity.PrintersProfiles;
import com.geeetech.administrator.easyprint.Activity.Speed;
import com.geeetech.administrator.easyprint.Activity.Temp;
import com.geeetech.administrator.easyprint.Activity.WifiSearch;
import com.geeetech.administrator.easyprint.StoreData.Data;
import com.geeetech.administrator.easyprint.StoreData.Urls;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONException;
import org.json.JSONObject;

import static com.geeetech.administrator.easyprint.Activity.Fragment.SdCard.mPrintFileName;
import static com.geeetech.administrator.easyprint.Internet.ErrorResponse.getErrorRespone;

public class PrintFragment extends Fragment{
    QMUIProgressBar mProgressBar;
    protected static final int STOP = 0x10000;
    protected static final int NEXT = 0x10001;
    //@BindView(R.id.rectProgressBar) QMUIProgressBar mRectProgressBar;
    //@BindView(R.id.print_start) ImageView mStartPrint;
    public static CircleProgressView mCircleProgress;
    int count;
    private LinearLayout mBaseFan;
    private LinearLayout mBaseFeed;
    private LinearLayout mBaseHeatbed;
    private LinearLayout mBaseExtruder;
    public static TextView mHeatbedTemp;
    public static TextView mExtruderTemp;
    public static ImageView mStartPrint;
    public static ImageView mStopPrint;
    public static boolean mStartExist = true;

    private LinearLayout mMove,mTemp,mSpeed,mLevel,
            mFilament,mWifiControll,mSet,mGcode;
    public static TextView mGcodeName;

    public static SlidingDrawer mSlidingDrawer;
    public static Boolean mOpen = false;
    public static View mViewModel;
    public static ImageView mSlidingOpen;
    public static View mViewTransparent;
    public static RelativeLayout mRelative;
    public static TextView mPrinterState;
    public static CircleImageView mCircleImg;

    public static PrintFragment newInstance(String info) {
        Bundle args = new Bundle();
        PrintFragment fragment = new PrintFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base, null);
        //ButterKnife.bind(this, view);
        mPrinterState = view.findViewById(R.id.printer_state);
        mHeatbedTemp = view.findViewById(R.id.heatbed_temp);
        mExtruderTemp = view.findViewById(R.id.extruder_temp);
        mStartPrint = view.findViewById(R.id.print_start);
        mStopPrint = view.findViewById(R.id.print_stop);
        mCircleProgress = view.findViewById(R.id.circle_progress);
        mCircleImg = view.findViewById(R.id.circle_img);
        mGcodeName = view.findViewById(R.id.textView);
        mStartPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( MainActivity.printerState.equals("3")){
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("Do you want to pause the print job now?")
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    try {
                                        startPrint("pause");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else if ( MainActivity.printerState.equals("4")){
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("Do you want to start the print job now?")
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    try {
                                        startPrint("resume");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }else if ( MainActivity.printerState.equals("2")){
                    MainActivity.mViewPager.setCurrentItem(1,false);
                    try{
                        GallaryFragment.mContentVp.setCurrentItem(1,false);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        mStopPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( MainActivity.printerState.equals("3") || MainActivity.printerState.equals("4")){
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("Stop printing or not?")
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    try {
                                        startPrint("stop");
                                        mPrintFileName = "";
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });

        /*导航 Print 获取ID*/
        mBaseFan = (LinearLayout) view.findViewById(R.id.base_fan);
        mBaseFeed = (LinearLayout) view.findViewById(R.id.base_feed);
        mBaseHeatbed = (LinearLayout) view.findViewById(R.id.base_heatbed);
        mBaseExtruder = (LinearLayout) view.findViewById(R.id.base_extruder);
        mBaseFan.setOnClickListener(clickListener);
        mBaseFeed.setOnClickListener(clickListener);
        mBaseHeatbed.setOnClickListener(clickListener);
        mBaseExtruder.setOnClickListener(clickListener);
        //弹出导航
        mMove = (LinearLayout) view.findViewById(R.id.move_controll);
        mTemp = (LinearLayout) view.findViewById(R.id.temp_controll);
        mSpeed = (LinearLayout) view.findViewById(R.id.speed_controll);
        mLevel = (LinearLayout) view.findViewById(R.id.level_controll);
        mFilament = (LinearLayout) view.findViewById(R.id.filament_controll);
        mSet = (LinearLayout) view.findViewById(R.id.set_controll);
        mGcode = (LinearLayout) view.findViewById(R.id.gcode_controll);
        mWifiControll = (LinearLayout) view.findViewById(R.id.wifi_controll);
        mMove.setOnClickListener(clickListener);
        mTemp.setOnClickListener(clickListener);
        mSpeed.setOnClickListener(clickListener);
        mLevel.setOnClickListener(clickListener);
        mFilament.setOnClickListener(clickListener);
        mSet.setOnClickListener(clickListener);
        mWifiControll.setOnClickListener(clickListener);
        mGcode.setOnClickListener(clickListener);
        //上滑
        mViewTransparent = (View) view.findViewById(R.id.view_transparent);
        //mViewModel = (View) view.findViewById(R.id.view_model);
        mSlidingOpen = (ImageView) view.findViewById(R.id.sliding_show);
        mRelative = view.findViewById(R.id.relative_sliding);

        mSlidingDrawer = (SlidingDrawer) view.findViewById(R.id.sliding_drawer);
        mSlidingDrawer.setClosedPostionHeight(100);
        mSlidingDrawer.setPartlyPositionHeight(300);
        mSlidingDrawer.setAutoRewindHeight(280);
        mSlidingOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( mOpen ){
                    mOpen = false;
                    mRelative.setVisibility(View.GONE);
                    //mSlidingDrawer.close();
                }else{
                    mOpen = true;
                    mSlidingOpen.setVisibility(View.GONE);
                    mRelative.setVisibility(View.VISIBLE);
                    //mSlidingDrawer.open();
                }
            }
        });
        mViewTransparent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOpen = false;
                mSlidingOpen.setVisibility(View.VISIBLE);
                mRelative.setVisibility(View.GONE);
                //mSlidingDrawer.close();
            }
        });

        return view;
    }

    /*导航 Print  onclick事件*/
    View.OnClickListener clickListener=new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.base_fan:
                    intent.setClass(getActivity(), Printers.class);
                    startActivity(intent);
                    break;
                case R.id.base_feed:
                intent.setClass(getActivity(), PrintersProfiles.class);
                startActivity(intent);
                break;
                case R.id.base_heatbed:
                    intent.setClass(getActivity(), Temp.class);
                    startActivity(intent);
                    break;
                case R.id.base_extruder:
                    intent.setClass(getActivity(), Temp.class);
                    startActivity(intent);
                    break;
                case R.id.move_controll:
                    intent.setClass(getActivity(), Move.class);
                    startActivity(intent);
                    break;
                case R.id.temp_controll:
                    intent.setClass(getActivity(), Temp.class);
                    startActivity(intent);
                    break;
                case R.id.speed_controll:
                    intent.setClass(getActivity(), Speed.class);
                    startActivity(intent);
                    break;
                case R.id.level_controll:
                    intent.setClass(getActivity(), Level.class);
                    startActivity(intent);
                    break;
                case R.id.filament_controll:
                    intent.setClass(getActivity(), Filament.class);
                    startActivity(intent);
                    break;
                case R.id.wifi_controll:
                    intent.setClass(getActivity(), WifiSearch.class);
                    startActivity(intent);
                    break;
                case R.id.set_controll:
                    intent.setClass(getActivity(), Config.class);
                    startActivity(intent);
                    break;
                case R.id.gcode_controll:
                    intent.setClass(getActivity(), Gcode.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        mOpen = false;
        mSlidingOpen.setVisibility(View.VISIBLE);
        mRelative.setVisibility(View.GONE);
        //mSlidingDrawer.close();
    }

    public static void getSlideClose() {
        mOpen = false;
        mSlidingOpen.setVisibility(View.VISIBLE);
        mRelative.setVisibility(View.GONE);
        //mSlidingDrawer.close();
    }

    //开始/暂停打印
    public void startPrint(final String action) throws JSONException {
        String serialNumber = "";
        try{
            serialNumber = MainActivity.mCurrentPrinter;
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        Context context = getContext();
        //List FirstUser = SpUtil.getList(context,"FirstUser");
        String email = Data.getEmail(context);
        String token = Data.getToken(context);
        String state = Data.getState(context);
        String encryStr = Data.getUserID(context,"",0);
        String encryStrId = Data.getUserID(context,serialNumber,1);

        String name = mPrintFileName;
        JSONObject js = new JSONObject();
        js.put("action",action);
        js.put("target","printer_task");
        js.put("value",name);
        js.put("token",token);

        String url = Urls.USER_COMMON() + encryStr + "/printers/" + encryStrId;
        OkGo.<String>post(url)
                .tag(this)
                .upJson(js)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (response.code() == 200){
                            /*if ( action.equals("start")){
                                Picasso.with(getContext()).load(R.drawable.icon_start2).into(mStartPrint);
                            }else{
                                Picasso.with(getContext()).load(R.drawable.icon_start1).into(mStartPrint);
                            }*/

                            /*new QMUIDialog.MessageDialogBuilder(getContext())
                                    .setMessage("Order has reciaved.")
                                    .addAction("OK", new QMUIDialogAction.ActionListener() {
                                        @Override
                                        public void onClick(QMUIDialog dialog, int index) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();*/
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        getErrorRespone(response,getContext(),getActivity());
                    }
                });
    }

}
