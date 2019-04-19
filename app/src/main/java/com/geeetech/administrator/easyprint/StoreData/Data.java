package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;

import com.geeetech.administrator.easyprint.Internet.AesECBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-05-28.
 */

public class Data {
    private static int resCode = 0;
    private static String taskFile = "";

    public static void setResCode(int a){
        Data.resCode = a;
    }

    public static int getResCode(){
        return Data.resCode;
    }

    public static void setTaskFile(String a){
        Data.taskFile = a;
    }

    public static String getTaskFile(){
        return Data.taskFile;
    }

    public static String getUserID(Context context,String need,int num){
        //生成key
        String secretKey = "qstring1871salt";
        //AES加密
        //long start = System.currentTimeMillis();
        String encryStr = "";
        String email = getEmail(context);
        if ( email.equals("")){
            return encryStr;
        }
        if ( num == 0){
            //AES加密
            //long start = System.currentTimeMillis();
            try {
                encryStr = AesECBUtils.encrypt(email,secretKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if ( num == 1){
            try {
                encryStr = AesECBUtils.encrypt(need,secretKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return encryStr;
    }
    //获取token值
    public static String getToken(Context context){
        String token = "";
        try{
            List FirstUser = SpUtil.getList(context,"FirstUser");
            token = FirstUser.get(1).toString();
        }catch (Exception e){
            token = "";
        }
        return token;
    }
    //获取email
    public static String getEmail(Context context){
        String email = "";
        try{
            List FirstUser = SpUtil.getList(context,"FirstUser");
            email = FirstUser.get(0).toString();
        }catch (Exception e){
            email = "";
        }
        return email;
    }
    //获取state
    public static String getState(Context context){
        String state = "logout";
        try{
            List FirstUser = SpUtil.getList(context,"FirstUser");
            state = FirstUser.get(3).toString();
        }catch (Exception e){
            state = "logout";
        }
        return state;
    }

    //合并数组
    //侧滑
    public static List<DrawerListItem> getNewDraw(List<DrawerListItem> a,List<DrawerListItem> b){
        List<DrawerListItem> c = new ArrayList<DrawerListItem>();
        c.addAll(a);
        c.addAll(b);
        return c;
    }
    //打印机列表
    public static List<ListStateItem> getListState(List<ListStateItem> a,List<ListStateItem> b){
        List<ListStateItem> c = new ArrayList<ListStateItem>();
        c.addAll(a);
        c.addAll(b);
        return c;
    }
    //Tcp打印机列表
    public static List<ListTcpItem> getListTcp(List<ListTcpItem> a,List<ListTcpItem> b){
        List<ListTcpItem> c = new ArrayList<ListTcpItem>();
        c.addAll(a);
        c.addAll(b);
        return c;
    }
}
