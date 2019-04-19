package com.geeetech.administrator.easyprint.StoreData;

/**
 * Created by Administrator on 2018-03-08.
 */

public class DrawerListItem {
    private String mName;
    private String mNumber;
    private String mState;
    private String mProgress;
    private String mHeatbed;
    private String mExtruder;
    private String mAvatar;

    public DrawerListItem(String Name, String Number,String State,String Progress,String Heatbed,String Extruder,String Avatar) {
        this.mName = Name;
        this.mNumber = Number;
        this.mState = State;
        this.mProgress = Progress;
        this.mHeatbed = Heatbed;
        this.mExtruder = Extruder;
        this.mAvatar = Avatar;
    }

    public String getName() {
        return mName;
    }
    public String getNumber() {
        return mNumber;
    }
    public String getState() {
        return mState;
    }
    public String getProgress() {
        return mProgress;
    }
    public String getHeatbed() {
        return mHeatbed;
    }
    public String getExtruder() {
        return mExtruder;
    }
    public String getAvatar() {
        return mAvatar;
    }
}