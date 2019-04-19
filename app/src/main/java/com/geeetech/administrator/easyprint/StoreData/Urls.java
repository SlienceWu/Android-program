
package com.geeetech.administrator.easyprint.StoreData;

public class Urls {
    //47.254.41.125:5000
    public static String SERVER_WIFI = "47.254.41.125:5000";//http://192.168.1.247:8081 www.geeetech.net //192.168.1.220:5000
    public static String SERVER = "http://47.254.41.125:5000";//api.geeetech.com";c83c83e2cf00d984698564f10cf75f52http://192.168.1.220:5000
    public static void setServer(String a){
        Urls.SERVER_WIFI = a;
    }
    public static String getServerWifi(){
        return SERVER_WIFI;
    }
    public static String getServer(){
        return "http://" +SERVER_WIFI;
    }
    public static final String SERVER_IMG = "http://www.geeetech.com/3d_models/public";
    //控制打印流程
    public static String PRINTER_CONTROL(){return getServer() + "/v1/printers/12";}
    //浏览SD卡模型文件
    public static String PRINTER_SD_SHOW(){return getServer() + "/v1/printers/12/sds";}
    public static String PRINTER_SD_CONTROL(){return getServer() + "/v1/printers/12/sds/0";}
    //进行云切片
    public static String GET_PRINTER_LIST(){return getServer() + "/v1/users/";}
    public static String GET_PRINTER_PROFILES_LIST(){return getServer() + "/v1/users/13/printer_profiles";}
    public static String GET_PRINTER_PROFILES(){return getServer() + "/v1/users/13/printer_profiles/0";}
    public static String GET_MATERIAL_PROFILES_LIST(){return getServer() + "/v1/users/13/material_profiles";}
    public static String GET_MATERIAL_PROFILES(){return getServer() + "/v1/users/13/material_profiles/0";}
    //账户管理
    public static String USER_LOGIN(){return getServer() + "/v1/accs/";}
    public static String USER_POST(){return getServer() + "/v1/users";}
    public static String USER_OUT(){return getServer() + "/v1/accs/";}
    public static String USER_PASSWORD_DEL(){return getServer() + "/v1/users/";}
    public static String USER_BIND_PRINTER(){return getServer() + "/v1/users/";}
    public static String USER_DELETE_PRINTER(){return getServer() + "/v1/users/";}

    public static String USER_COMMON(){return getServer() + "/v1/users/";}
    public static String USER_AD(){return getServer() + "/v1/app/ad";}
    public static String USER_BANNER(){return getServer() + "/v1/app/banner";}
    public static String USER_CHECK(){return getServer() + "/v1/app/heartbeat";}

    public static String PRINTER_PROFILE_ADD(){return getServer() + "/v1/user/13";}
    public static String PRINTER_PROFILE_CHANGE(){return getServer() + "/v1/user/13/printer_profiles/14";}
    public static String PRINTER_PROFILE_DELETE(){return getServer() + "/v1/user/13/printer_profiles/14";}

    public static String MATERIAL_PROFILE_ADD(){return getServer() + "/v1/users/13";}
    public static String MATERIAL_PROFILE_CHANGE(){return getServer() + "/v1/users/13/material_profile/14";}
    public static String MATERIAL_PROFILE_DELETE(){return getServer() + "/v1/users/13/material_profile/14";}

    public static String PRINTER_CHANGE(){return getServer() + "/v1/printers/12";}
    public static String USER_CHANGE(){return getServer() + "/v1/accs/";}
    //App版本
    public static String USER_SUGGEST(){return getServer() + "/v1/app/feedback";}
    public static String APP_DETAIL(){return getServer() + "/v1/app/update_info";}

}
