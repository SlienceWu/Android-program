package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2018-05-17.
 */

public class ToastUtil {
    private static Toast myToast;
    public static void showToast(Context context, String str) {
        try{
            if(myToast == null) {
                myToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            } else {
                myToast.setText(str);
            }
            myToast.show();
        }catch (Exception e){
            System.out.println("Toast show error:" + str);
            e.printStackTrace();
        }
    }
}
