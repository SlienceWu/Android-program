package com.geeetech.administrator.easyprint.StoreData;

/**
 * Created by Administrator on 2018-03-08.
 */

public class ListItem {
    private String mName;
    private String mNumber;

    public ListItem(String Name, String Number) {
        this.mName = Name;
        this.mNumber = Number;
    }

    public String getName() {
        return mName;
    }

    public String getNumber() {
        return mNumber;
    }
}