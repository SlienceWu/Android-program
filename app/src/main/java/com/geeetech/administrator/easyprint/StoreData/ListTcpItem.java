package com.geeetech.administrator.easyprint.StoreData;

/**
 * Created by Administrator on 2018-12-10.
 */

public class ListTcpItem {
    private String mName;
    private String mNumber;
    private boolean mState;

    public ListTcpItem(String Name, String Number,boolean State) {
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

    public boolean getState() {
        return mState;
    }
}
