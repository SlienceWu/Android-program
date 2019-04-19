package com.geeetech.administrator.easyprint.StoreData;

/**
 * Created by Administrator on 2018-03-26.
 */

public class ModelListItem {
    private String mModelName;
    private String mModelNumber;
    private String mModelThumb,mModelDescription;


    public ModelListItem(String Name, String Number, String Thumb, String Description) {
        this.mModelName = Name;
        this.mModelNumber = Number;
        this.mModelThumb = Thumb;
        this.mModelDescription = Description;
    }

    public String getName() {
        return mModelName;
    }

    public String getNumber() {
        return mModelNumber;
    }

    public String getThumb() {
        return mModelThumb;
    }

    public String getDescription() {
        return mModelDescription;
    }
}