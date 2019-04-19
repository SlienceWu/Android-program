package com.geeetech.administrator.easyprint.StoreData;

/**
 * Created by Administrator on 2018-05-29.
 */

public class ListStateItem {
    private String mName;
    private String mNumber;
    private String mState;

    public ListStateItem(String Name, String Number,String State) {
        this.mName = Name;
        this.mNumber = Number;
        this.mState = State;
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
}
