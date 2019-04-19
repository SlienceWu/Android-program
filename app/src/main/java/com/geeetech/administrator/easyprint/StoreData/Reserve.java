package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-02-11.
 */

public class Reserve {

    //储存邮箱，token值，token有效时间
    public static void reserveList(Context context,String email,String token,String tokenExpire,String state) throws Exception{
        int list = SpUtil.getInt(context,"List",0);
        List value = new ArrayList();
        value.add(email);
        value.add(token);
        value.add(tokenExpire);
        value.add(state);
        if ( list == 0){
            SpUtil.putList(context,"FirstUser",value);
            SpUtil.putInt(context,"List",1);
        }
        if ( list == 1){
            List FirstUser = SpUtil.getList(context,"FirstUser");
            if ( email.equals(FirstUser.get(0))){
                SpUtil.putList(context,"FirstUser",value);
                return;
            }
            SpUtil.putList(context,"FirstUser",value);
            SpUtil.putList(context,"SecondUser",FirstUser);
            SpUtil.putInt(context,"List",2);
        }
        if ( list == 2){
            List FirstUser = SpUtil.getList(context,"FirstUser");
            List SecondUser = SpUtil.getList(context,"SecondUser");
            if ( email.equals(FirstUser.get(0))){
                SpUtil.putList(context,"FirstUser",value);
                return;
            }
            if ( email.equals(SecondUser.get(0))){
                SpUtil.putList(context,"FirstUser",value);
                SpUtil.putList(context,"SecondUser",FirstUser);
                return;
            }
            SpUtil.putList(context,"FirstUser",value);
            SpUtil.putList(context,"SecondUser",FirstUser);
            SpUtil.putList(context,"ThirdUser",SecondUser);
            SpUtil.putInt(context,"List",3);
        }
        if ( list == 3){
            List FirstUser = SpUtil.getList(context,"FirstUser");
            List SecondUser = SpUtil.getList(context,"SecondUser");
            List ThirdUser = SpUtil.getList(context,"ThirdUser");
            if ( email.equals(FirstUser.get(0))){
                SpUtil.putList(context,"FirstUser",value);
                return;
            }
            if ( email.equals(SecondUser.get(0))){
                SpUtil.putList(context,"FirstUser",value);
                SpUtil.putList(context,"SecondUser",FirstUser);
                return;
            }
            if ( email.equals(ThirdUser.get(0))){
                SpUtil.putList(context,"FirstUser",value);
                SpUtil.putList(context,"SecondUser",FirstUser);
                SpUtil.putList(context,"ThirdUser",SecondUser);
                return;
            }
            SpUtil.putList(context,"FirstUser",value);
            SpUtil.putList(context,"SecondUser",FirstUser);
            SpUtil.putList(context,"ThirdUser",SecondUser);
            SpUtil.putInt(context,"List",3);
        }

    }
    public static void reserveOther(Context context,String name,String address,String avatar) throws Exception{
        List value = new ArrayList();
        value.add(name);
        value.add(address);
        value.add(avatar);
        SpUtil.putList(context,"OtherInfo",value);
    }

    public static void reserveIp(Context context,String address) throws Exception{
        List value = new ArrayList();
        value.add(address);
        SpUtil.putList(context,"CurrentIp",value);
    }
    //删除储存数据
    public static void deleteKey(Context context,String key){
        if ( key.equals("")){
            SpUtil.clear(context);
            return;
        }
        SpUtil.remove(context,key);
    }
}
