package com.geeetech.administrator.easyprint.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.FileItemAdapter;
import com.geeetech.administrator.easyprint.StoreData.ListItem;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-12-05.
 */

public class PrintersControll extends BaseActivity {
    public String mIp;
    public int mPort;

    public TextView mCurrentAddress;
    public Button mBtnReconnect;
    public EditText mSendMessage;
    public Button mSend;
    public TextView mShow;
    public TextView mAllMessage;
    //定时查询信息
    public Runnable runnable;
    public Handler handler = new Handler();
    //控制
    public TextView mDetail,mMove,mUpload,mPrint;
    //视图
    public LinearLayout mViewDetail,mViewMove,mViewUpload,mViewPrint;
    //温度显示 打印进度
    public LinearLayout mBtnSetExtruder,mBtnSetHotbed;
    public TextView mCurrentExtruder,mCurrentHotbed,mAlreadyExtruder,mAlreadyHotbed;
    public ProgressBar mProgress;
    public TextView mProgressText;
    public ImageView mPrintStop,mPrintStart,mChangeExtruderTemp,mChangeHotbedTemp;
    public String mState;
    //移动操作
    public TextView mMoveTop, mMoveLeft, mMoveRight, mMoveBottom,mMoveZTop,mMoveZBottom;
    public ImageView mMoveHome;
    public Spinner mMoveSize,mRateSize;
    public int mMoveValue,mRateValue;
    public Button mSetBaudRate;
    //上传操作
    public Button mChooseFile,mUpLoadFile;
    public TextView mFileName,mProgressRate,mFileNameTwo,mFileNameThree;
    public static ProgressBar mProgressBar;
    public LinearLayout mShowUploadProgress,mStateViewUploading;
    public ImageView mStateViewSuccess,mStateViewReload,mStateViewFail,mStartUpload,mStopUpload;
    //打印列表
    public int mListShow = 1;   //当前显示的列表 1.sd卡列表 2.WiFi列表
    public int isRefreshClick = 0;  //是否刷新
    public ListView mListView;
    public LinearLayout mSdList,mWifiList;
    public TextView mSdText,mWifiText;
    public ImageView mRefreshSd,mRefreshWifi;
    public Runnable runnableList;
    public Handler handlerList = new Handler();
    //遮罩层
    /*public LinearLayout mMaskLayer;
    public TextView mBtnCancle,mBtnBack;*/

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_controll);
        mIp = getIntent().getStringExtra("ip");
        mPort = getIntent().getIntExtra("port",0);
        //导航
        mDetail = findViewById(R.id.controller_detail);
        mMove = findViewById(R.id.controller_move);
        mUpload = findViewById(R.id.controller_upload);
        mPrint = findViewById(R.id.controller_print);
        mViewDetail = findViewById(R.id.show_detail);
        mViewMove = findViewById(R.id.controll_xy);
        mViewUpload = findViewById(R.id.show_upload);
        mViewPrint = findViewById(R.id.show_list);
        //温度 打印
        mBtnSetExtruder = findViewById(R.id.btn_set_extruder);
        mBtnSetHotbed = findViewById(R.id.btn_set_hotbed);
        mCurrentExtruder = findViewById(R.id.current_extruder);
        mCurrentHotbed = findViewById(R.id.current_hotbed);
        mAlreadyExtruder = findViewById(R.id.already_extruder);
        mAlreadyHotbed = findViewById(R.id.already_hotbed);
        mChangeExtruderTemp = findViewById(R.id.change_extruder_temp);
        mChangeHotbedTemp = findViewById(R.id.change_hotbed_temp);
        mProgress = findViewById(R.id.main_progress);
        mProgressText = findViewById(R.id.main_progress_show);
        mPrintStart = findViewById(R.id.print_start);
        mPrintStop = findViewById(R.id.print_stop);
        //移动操作
        mMoveTop = findViewById(R.id.move_top);
        mMoveLeft = findViewById(R.id.move_left);
        mMoveHome = findViewById(R.id.move_home);
        mMoveRight = findViewById(R.id.move_right);
        mMoveBottom = findViewById(R.id.move_bottom);
        mMoveZTop = findViewById(R.id.move_z_top);
        mMoveZBottom = findViewById(R.id.move_z_bottom);
        mMoveSize = findViewById(R.id.spinner);
        mRateSize = findViewById(R.id.spinner_rate);
        mSetBaudRate = findViewById(R.id.button_set);
        //上传操作
        mChooseFile = findViewById(R.id.choose_file);
        mUpLoadFile = findViewById(R.id.upload_file);
        mFileName = findViewById(R.id.file_name);
        mFileNameTwo = findViewById(R.id.file_name_two);
        mFileNameThree = findViewById(R.id.file_name_three);
        mShowUploadProgress = findViewById(R.id.upload_progress);
        mProgressRate = findViewById(R.id.printer_progress_show);
        mProgressBar = findViewById(R.id.printer_progress);
        mShowUploadProgress = findViewById(R.id.upload_progress);
        mStartUpload = findViewById(R.id.start_upload);
        mStopUpload = findViewById(R.id.stop_upload);
        mStateViewUploading = findViewById(R.id.state_uploading);
        mStateViewSuccess = findViewById(R.id.state_upload_success);
        mStateViewReload = findViewById(R.id.state_reupload);
        mStateViewFail = findViewById(R.id.state_upload_fail);
        //打印操作
        mListView = findViewById(R.id.file_list);
        mSdList = findViewById(R.id.sd_list);
        mWifiList = findViewById(R.id.wifi_list);
        mSdText = findViewById(R.id.sd_list_text);
        mWifiText = findViewById(R.id.wifi_list_text);
        mRefreshSd = findViewById(R.id.refresh_sd);
        mRefreshWifi = findViewById(R.id.refresh_wifi);
        //遮罩层
        /*mMaskLayer = findViewById(R.id.mask_layer);
        mBtnCancle = findViewById(R.id.btn_cancle);
        mBtnBack = findViewById(R.id.btn_back);*/
        //事件监听
        mDetail.setOnClickListener(onClickListener);
        mMove.setOnClickListener(onClickListener);
        mUpload.setOnClickListener(onClickListener);
        mPrint.setOnClickListener(onClickListener);

        mChangeExtruderTemp.setOnClickListener(onClickListener);
        mChangeHotbedTemp.setOnClickListener(onClickListener);
        mPrintStart.setOnClickListener(onClickListener);
        mPrintStop.setOnClickListener(onClickListener);

        mMoveTop.setOnClickListener(onClickListener);
        mMoveLeft.setOnClickListener(onClickListener);
        mMoveHome.setOnClickListener(onClickListener);
        mMoveRight.setOnClickListener(onClickListener);
        mMoveBottom.setOnClickListener(onClickListener);
        mMoveZTop.setOnClickListener(onClickListener);
        mMoveZBottom.setOnClickListener(onClickListener);
        mSetBaudRate.setOnClickListener(onClickListener);

        mChooseFile.setOnClickListener(onClickListener);
        mUpLoadFile.setOnClickListener(onClickListener);
        mStartUpload.setOnClickListener(onClickListener);
        mStopUpload.setOnClickListener(onClickListener);
        mStateViewReload.setOnClickListener(onClickListener);

        mSdText.setOnClickListener(onClickListener);
        mWifiText.setOnClickListener(onClickListener);
        mRefreshSd.setOnClickListener(onClickListener);
        mRefreshWifi.setOnClickListener(onClickListener);

        /*mBtnCancle.setOnClickListener(onClickListener);
        mBtnBack.setOnClickListener(onClickListener);*/

        //test********************************//
        mFileName.setText(PrintersTcp.name);
        mFileNameTwo.setText(PrintersTcp.name);
        mFileNameThree.setText(PrintersTcp.name);
        mCurrentAddress = findViewById(R.id.current_address);
        mBtnReconnect = findViewById(R.id.btn_reconnect);
        mSendMessage = findViewById(R.id.send_message);
        mSend = findViewById(R.id.button_send);
        mShow = findViewById(R.id.show_message);
        mAllMessage = findViewById(R.id.all_message);

        mCurrentAddress.setText("Address:  " + mIp + ":" + mPort);
        /*if (!PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getConnectState()){
            mBtnReconnect.setVisibility(View.VISIBLE);
        }else{
            mBtnReconnect.setVisibility(View.GONE);
        }*/
        mBtnReconnect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try{
                    PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).setConnectState(false);
                    PrintersTcp.socket = new Socket();
                    SocketAddress socAddress = new InetSocketAddress(mIp, mPort);
                    PrintersTcp.socket.connect(socAddress, 3000);//超时3秒
                    PrintersTcp.isReceive = 0;
                    for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                        if (PrintersTcp.socketGroup.get(i).getIp().equals(mIp) ){
                            PrintersTcp.socketGroup.get(i).setSocket(PrintersTcp.socket);
                            PrintersTcp.send(getBaseContext(),"reconnect user");
                            break;
                        }
                    }
                }catch (Exception e1){
                    PrintersTcp.isReceive = 1;
                    PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).setConnectState(false);
                    //ToastUtil.showToast(getBaseContext(),"reconnect fail.");
                    Log.e("PrintersControll","reconnect fail.");
                }
            }
        });
        //定时刷新UI
        runnable = new Runnable() {
            @Override
            public void run() {
                if (PrintersTcp.socketGroup.size() == 0){
                    handler.postDelayed(this,1000);
                    return;
                }
                if (!PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getConnectState()){
                    //mMaskLayer.setVisibility(View.VISIBLE);
                    new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
                            .setMessage(R.string.tcp_tips)
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction("Back to Connect", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    onBackPressed();
                                    finish();
                                }
                            })
                            .show();
                    return;
                }
                //上传进度
                if (PrintersTcp.allBuffer != null && PrintersTcp.allBuffer.length > 0){
                    if (PrintersTcp.fileSendLen*PrintersTcp.fileSendLocation >= PrintersTcp.allBuffer.length){
                        ToastUtil.showToast(getBaseContext(),"Upload done.");
                    }else if (PrintersTcp.fileSendLocation == -1){
                        mProgressBar.setProgress(0);
                        mProgressRate.setText("0%");
                        if (mStateViewUploading.getVisibility() == View.VISIBLE){
                            showUploadView(1,false);
                        }
                    }else if (PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getUploadState().equals("uploadFail")){
                        mStartUpload.setTag("stoping");
                        mStartUpload.setImageResource(R.mipmap.icon_start_upload);
                        showUploadView(2,false);
                    }else if (PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getUploadState().equals("uploadStop")){
                        mStartUpload.setTag("stoping");
                        mStartUpload.setImageResource(R.mipmap.icon_start_upload);
                    }else{
                        float progress = (float)PrintersTcp.fileSendLen*PrintersTcp.fileSendLocation/PrintersTcp.allBuffer.length*100;
                        int p = (int)progress;
                        mProgressBar.setProgress(p);
                        mProgressRate.setText(String.format("%.2f",progress) + "%");
                        showUploadView(0,false);
                        mStartUpload.setImageResource(R.mipmap.icon_stop_upload);
                    }
                }
                //温度
                mAlreadyExtruder.setText(PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getExtruderTemp());
                mAlreadyHotbed.setText(PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getBedTemp());
                mCurrentExtruder.setText(PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getcExtruderTemp());
                mCurrentHotbed.setText(PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getcBedTemp());
                //打印进度
                String[] split = PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getPrecent().split("%");
                int progress = Integer.valueOf(split[0]);
                mProgress.setProgress(progress);
                mProgressText.setText(PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getPrecent());
                mState = PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getState();
                if ( mState.equals("online")){
                    mPrintStart.setImageResource(R.mipmap.icon_start_upload);
                } else if( mState.equals("print")){
                    mPrintStart.setImageResource(R.mipmap.icon_stop_upload);
                } else if( mState.equals("pause")){
                    mPrintStart.setImageResource(R.mipmap.icon_start_upload);
                }
                handler.postDelayed(this,1000);
            }
        };
        runnableList = new Runnable() {
            @Override
            public void run() {
                initFileList();
            }
        };
        mSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String need = mSendMessage.getText().toString();
                PrintersTcp.send(getBaseContext(),need);
                String message = "";
                mAllMessage.setText(message);
                for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                    message = i + ": " + PrintersTcp.socketGroup.get(i).getMessage();
                    mAllMessage.setText(mAllMessage.getText() + "\n" + message);
                    //String a = "message:" + PrintersTcp.socketGroup.get(i).getMessage() + "state:" + PrintersTcp.socketGroup.get(i).getState() + "\n" + "fileName:" + PrintersTcp.socketGroup.get(i).getFileName() + "\n" + "present:" + PrintersTcp.socketGroup.get(i).getPrecent();
                    //mAllMessage.setText(a + "\n" + "extruderTemp:" + PrintersTcp.socketGroup.get(i).getcExtruderTemp() + "\n" +"bedTemp:" + PrintersTcp.socketGroup.get(i).getcBedTemp());
                }
            }
        });
        //test********************************//
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMoveValue = Integer.parseInt(mMoveSize.getSelectedItem().toString());
            mRateValue = Integer.parseInt(mRateSize.getSelectedItem().toString());
            switch (v.getId()){
                case R.id.controller_detail:
                    changeView(mDetail,mViewDetail);
                    break;
                case R.id.controller_move:
                    changeView(mMove,mViewMove);
                    break;
                case R.id.controller_upload:
                    changeView(mUpload,mViewUpload);
                    break;
                case R.id.controller_print:
                    changeView(mPrint,mViewPrint);
                    initFileList();
                    break;
                case R.id.change_extruder_temp:
                    extruderTemp();
                    break;
                case R.id.change_hotbed_temp:
                    hotbedTemp();
                    break;
                case R.id.print_start:
                    if ( mState.equals("print")){
                        new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
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
                                        //暂停打印
                                        PrintersTcp.send(getBaseContext(),xor("N-0 M25"));
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else if ( mState.equals("pause")){
                        new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
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
                                        //暂停过后开始打印
                                        PrintersTcp.send(getBaseContext(),xor("N-0 M24"));
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else{
                        changeView(mPrint,mViewPrint);
                        if ( mListShow == 1){
                            refreshList(mRefreshSd, xor("N-0 M20"));
                            handlerList.postDelayed(runnableList,3000);
                        }else{
                            refreshList(mRefreshWifi, xor("N-0 M20 3DWIFI"));
                            handlerList.postDelayed(runnableList,3000);
                        }
                    }
                    break;
                case R.id.print_stop:
                    if ( !mState.equals("online")){
                        new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
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
                                        PrintersTcp.send(getBaseContext(),xor("N-0 M2103"));
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                    break;
                case R.id.move_top:
                    if (isOnline()){
                        String need1 = "N-0 M2111 S3 Y" + mMoveValue;
                        PrintersTcp.send(getBaseContext(),xor(need1));
                    }
                    break;
                case R.id.move_left:
                    if (isOnline()){
                        String need2 = "N-0 M2111 S2 X-" + mMoveValue;
                        PrintersTcp.send(getBaseContext(),xor(need2));
                    }
                    break;
                case R.id.move_home:
                    if (isOnline()){
                        PrintersTcp.send(getBaseContext(),xor("N-0 G28"));
                    }
                    break;
                case R.id.move_right:
                    if (isOnline()){
                        String need4 = "N-0 M2111 S2" + mMoveValue;
                        PrintersTcp.send(getBaseContext(),xor(need4));
                    }
                    break;
                case R.id.move_bottom:
                    if (isOnline()){
                        String need5 = "N-0 M2111 S3 Y-" + mMoveValue;
                        PrintersTcp.send(getBaseContext(),xor(need5));
                    }
                    break;
                case R.id.move_z_top:
                    if (isOnline()){
                        String need6 = "N-0 M2111 S4 Z" + mMoveValue;
                        PrintersTcp.send(getBaseContext(),xor(need6));
                    }
                    break;
                case R.id.move_z_bottom:
                    if (isOnline()){
                        String need7 = "N-0 M2111 S4 Z-" + mMoveValue;
                        PrintersTcp.send(getBaseContext(),xor(need7));
                    }
                    break;
                case R.id.button_set:
                    String need8 = "N-0 M2113 S" + mRateValue;
                    PrintersTcp.send(getBaseContext(),xor(need8));
                    break;
                case R.id.choose_file:
                    chooseFile();
                    break;
                case R.id.upload_file:
                    upLoadFile();
                    break;
                case R.id.start_upload:
                    reUpload();
                    break;
                case R.id.stop_upload:
                    stopUpload();
                    break;
                case R.id.state_reupload:
                    reUpload();
                    break;
                case R.id.sd_list_text:
                    mListShow = 1;
                    mSdText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mWifiText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    if ( PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getSdList().size()>0){

                    }else{
                        refreshList(mRefreshSd, xor("N-0 M20"));
                        handlerList.postDelayed(runnableList,3000);
                    }
                    break;
                case R.id.wifi_list_text:
                    mListShow = 2;
                    mWifiText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mSdText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    if ( PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getWifiList().size()>0){

                    }else{
                        refreshList(mRefreshWifi, xor("N-0 M20 3DWIFI"));
                        handlerList.postDelayed(runnableList,3000);
                    }
                    //PrintersTcp.send(getBaseContext(),xor("N-0 M20 3DWIFI"));
                    break;
                case R.id.refresh_sd:
                    if ( mListShow == 1){
                        refreshList(mRefreshSd, xor("N-0 M20"));
                        handlerList.postDelayed(runnableList,3000);
                    }
                    break;
                case R.id.refresh_wifi:
                    if ( mListShow == 2){
                        refreshList(mRefreshWifi, xor("N-0 M20 3DWIFI"));
                        handlerList.postDelayed(runnableList,3000);
                    }
                    break;
                /*case R.id.btn_cancle:
                    mMaskLayer.setVisibility(View.GONE);
                    break;
                case R.id.btn_back:
                    mMaskLayer.setVisibility(View.GONE);
                    onBackPressed();
                    finish();
                    break;*/
            }
        }
    };
    //获取状态
    public boolean isOnline(){
        if(!PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getState().equals("online")){
            ToastUtil.showToast(getBaseContext(),"Printer is printing.");
            return false;
        }else{
            return true;
        }
    }
    //试图切换
    public void changeView(TextView btnView, LinearLayout showView){
        List<TextView> btnAll = new ArrayList<>();
        btnAll.add(mDetail);
        btnAll.add(mMove);
        btnAll.add(mUpload);
        btnAll.add(mPrint);
        List<LinearLayout> showAll = new ArrayList<>();
        showAll.add(mViewDetail);
        showAll.add(mViewMove);
        showAll.add(mViewUpload);
        showAll.add(mViewPrint);
        btnView.setBackgroundColor(getResources().getColor(R.color.app_main_white));
        btnView.setTextColor(getResources().getColor(R.color.app_color_checked));
        showView.setVisibility(View.VISIBLE);
        for(int i=0;i<btnAll.size();i++){
            if (btnAll.get(i) != btnView){
                btnAll.get(i).setBackgroundColor(getResources().getColor(R.color.app_color_checked));
                btnAll.get(i).setTextColor(getResources().getColor(R.color.app_main_white));
                showAll.get(i).setVisibility(View.GONE);
            }
        }
    }
    //设置温度
    private void extruderTemp(){
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(PrintersControll.this);
        builder.setTitle("Set printer extruder temp")
                .setPlaceholder("0-250")
                .setInputType(InputType.TYPE_CLASS_NUMBER)
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
                        if (text != null && text.length() >0) {
                            String a = text.toString();
                            int b = Integer.valueOf(a);
                            if ( b < 251){
                                mAlreadyExtruder.setText(text);
                            }else{
                                ToastUtil.showToast(PrintersControll.this, "Params are too large.");
                                return;
                            }
                            String need = "N-0 M104 T0 S" + b;
                            PrintersTcp.send(getBaseContext(),xor(need));
                            dialog.dismiss();
                        } else {
                            ToastUtil.showToast(PrintersControll.this, "Please write something.");
                        }
                    }
                })
                .show();
    }
    private void hotbedTemp(){
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(PrintersControll.this);
        builder.setTitle("Set printer hotbed temp")
                .setPlaceholder("0-120")
                .setInputType(InputType.TYPE_CLASS_NUMBER)
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
                        if (text != null && text.length() >0) {
                            String a = text.toString();
                            int b = Integer.valueOf(a);
                            if ( b < 121){
                                mAlreadyHotbed.setText(text);
                            }else{
                                ToastUtil.showToast(PrintersControll.this, "Params are too large.");
                                return;
                            }
                            String need = "N-0 M140 S" + b;
                            PrintersTcp.send(getBaseContext(),xor(need));
                            dialog.dismiss();
                        } else {
                            ToastUtil.showToast(PrintersControll.this, "Please write something.");
                        }
                    }
                })
                .show();
    }
    //文件选择
    public void chooseFile(){
        if (mStateViewUploading.getVisibility() == View.VISIBLE&&mShowUploadProgress.getVisibility() == View.VISIBLE){
            ToastUtil.showToast(getBaseContext(),"File is uploading");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select the file"), 0);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getBaseContext(), "Dear,there is no file manager. -_-!!", Toast.LENGTH_SHORT).show();
        }
    }
    //*********文件上传*********//
    public void upLoadFile(){
        try{
            if (PrintersTcp.path.equals("")){
                ToastUtil.showToast(getBaseContext(),"Please choose a file.");
                return;
            }
            if (mStateViewUploading.getVisibility() == View.VISIBLE&&mShowUploadProgress.getVisibility() == View.VISIBLE){
                ToastUtil.showToast(getBaseContext(),"File is uploading");
                return;
            }
            PrintersTcp.allBuffer = readFileToBuffer(PrintersTcp.path);
            PrintersTcp.sendBuffer(getBaseContext(),PrintersTcp.allBuffer);
            //showUploadView(0,false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //上传过程中暂停或终止start time:Mon Dec 24 17:23:44 GMT+08:00 2018end time:Mon Dec 24 17:27:50 GMT+08:00 2018v6500352
    public void stopUpload(){
        new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
                .setMessage("Stop uploading or not?")
                .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("OK", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).setUploadState("uploadStop");
                        String need = "N-0 M2110 send end";
                        PrintersTcp.send(getBaseContext(),xor(need));
                        showUploadView(0,true);
                        PrintersTcp.path = "";
                        PrintersTcp.name = "";
                        PrintersTcp.fileSendLocation = -1;
                        PrintersTcp.allBuffer = new byte[0];
                        PrintersTcp.nextBuffer = new byte[0];
                        PrintersTcp.currentBuffer = new byte[0];
                        mFileName.setText(PrintersTcp.name);
                        mFileNameTwo.setText(PrintersTcp.name);
                        mFileNameThree.setText(PrintersTcp.name);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public void reUpload(){
        if ( mStartUpload.getTag().equals("uploading")){
            mStartUpload.setTag("stoping");
            mStartUpload.setImageResource(R.mipmap.icon_start_upload);
            //String need = "N-0 M2110 stop";
            String need = "N-0 M2110 send end";
            PrintersTcp.send(getBaseContext(),xor(need));
        } else{
            mStartUpload.setImageResource(R.mipmap.icon_stop_upload);
            mStartUpload.setTag("uploading");
            String need = "N-0 M2110 Continue";
            PrintersTcp.send(getBaseContext(),xor(need));
        }
    }
    //显示上传状态
    public void showUploadView(int need,boolean isInit){
        List<View> btnAll = new ArrayList<>();
        btnAll.add(mStateViewUploading);
        btnAll.add(mStateViewSuccess);
        btnAll.add(mStateViewReload);
        btnAll.add(mStateViewFail);
        if (isInit){
            mShowUploadProgress.setVisibility(View.GONE);
            for(int i=0;i<4;i++){
                btnAll.get(i).setVisibility(View.GONE);
            }
            return;
        }
        mShowUploadProgress.setVisibility(View.VISIBLE);
        btnAll.get(need).setVisibility(View.VISIBLE);
        for(int i=0;i<4;i++){
            if (i != need){
                btnAll.get(i).setVisibility(View.GONE);
            }
        }
    }
    public byte[] readFileToBuffer(String fileName) throws IOException {
        byte[] res = new byte[0];
        try{
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = buffer;
            fin.close();
        } catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                PrintersTcp.path = uri.getPath();
                String[] keyValue = PrintersTcp.path.split("/");
                PrintersTcp.name = keyValue[keyValue.length - 1];
                //mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + path, name);
                mFileName.setText(PrintersTcp.name);
                mFileNameTwo.setText(PrintersTcp.name);
                mFileNameThree.setText(PrintersTcp.name);
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                try{
                    PrintersTcp.path = getPath(this, uri);
                }catch (Exception e){
                    mFileName.setText("get file error");
                    return;
                }
                String[] keyValue = PrintersTcp.path.split("/");
                PrintersTcp.name = keyValue[keyValue.length - 1];
                //mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + path, name);
                mFileName.setText(PrintersTcp.name);
                mFileNameTwo.setText(PrintersTcp.name);
                mFileNameThree.setText(PrintersTcp.name);
            } else {//4.4以下下系统调用方法
                try{

                }catch (Exception e){
                    mFileName.setText("get file error1");
                }
                PrintersTcp.path = getRealPathFromURI(uri);
                String[] keyValue = PrintersTcp.path.split("/");
                PrintersTcp.name = keyValue[keyValue.length - 1];
                //mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + path, name);
                mFileName.setText(PrintersTcp.name);
                mFileNameTwo.setText(PrintersTcp.name);
                mFileNameThree.setText(PrintersTcp.name);
            }
        }
    }
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null != cursor && cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }
    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) throws Exception {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    //*********文件上传*********//
    //*********打印列表*********//
    //刷新列表
    public void refreshList(final ImageView v, String need){
        if ( isRefreshClick == 0){
            isRefreshClick = 1;
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    isRefreshClick = 0;
                    v.setRotation(0);
                }
            };
            v.animate().rotation(360);
            handler.postDelayed(runnable, 2000);
            PrintersTcp.send(getBaseContext(),need);
        }else{
            ToastUtil.showToast(getBaseContext(),"Please wait...");
        }
    }
    //初始化列表
    public void initFileList(){
        final List<ListItem> list = new ArrayList<>();
        ArrayList<String> a;
        if ( mListShow == 1){
            a = PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getSdList();
        }else{
            a = PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getWifiList();
        }
        /*if (a.size() <= 0){
            ToastUtil.showToast(getBaseContext(),"list is empty.");
            System.out.println("**************************** list is empty");
        }*/
        //System.out.println("**************************** list init");
        for(int i=0;i<a.size();i++){
            ListItem item = new ListItem(a.get(i), "");
            list.add(item);
        }
        /*ListItem item = new ListItem("test.gcode", "");
        list.add(item);*/
        FileItemAdapter itemAdapter = new FileItemAdapter(getBaseContext(), R.layout.print_list_file_item, list);
        mListView.setAdapter(itemAdapter);
        //选择打印
        itemAdapter.setOnItemEditClickListener(new FileItemAdapter.onItemEditListener() {
            @Override
            public void onEditClick(int i) {
                String state = PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getState();
                if ( state.equals("online")) {
                    final String mPrinterName = list.get(i).getName();
                    new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
                            .setMessage("Are you sure to print this model now?")
                            .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    getPrint(mPrinterName);
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }else{
                    if (state.equals("offline")){
                        ToastUtil.showToast(PrintersControll.this,"Printer is offline.");
                        return;
                    }
                    new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
                            .setMessage("Printer is printing.")
                            .addAction("OK", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
        //删除
        itemAdapter.setOnItemDeleteClickListener(new FileItemAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(final int i) {
                final String mPrinterName = list.get(i).getName();
                new QMUIDialog.MessageDialogBuilder(PrintersControll.this)
                        .setMessage("Are you sure to delete this model now?")
                        .addAction("Cancel", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("OK", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                getDelete(mPrinterName,i);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }
    public void getPrint(String name){
        String need = "N-0 M23 " + name;
        PrintersTcp.send(getBaseContext(),xor(need));
        changeView(mDetail,mViewDetail);
    }
    public void getDelete(String name, final int i){
        String need = "N-0 M30 " + name;
        PrintersTcp.send(getBaseContext(),xor(need));
        if ( mListShow == 1){
            PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).deleteList(i,"sd");
        }else{
            PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).deleteList(i,"wifi");
        }
        initFileList();
    }
    //*********打印列表*********//
    //进入页面定时查询
    @Override
    public void onResume () {
        super.onResume();
        handler.postDelayed(runnable,0);
    }
    //退出页面取消查询
    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
