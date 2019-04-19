package com.geeetech.administrator.easyprint.Internet;

import com.rabtman.wsmanager.WsManager;

/**
 * Created by Administrator on 2018-11-26.
 */

public class WSItem {
    private String ip;
    private String port;
    private boolean connectState;
    private WsManager ws;
    private String message; //接受的数据
    private String state = "online"; //打印机状态
    private String cExtruderTemp = "0℃"; //当前挤出头温度
    private String cBedTemp = "0℃"; //当前热床温度
    private String extruderTemp = "0℃"; //设置的挤出头温度
    private String bedTemp = "0℃";  //设置的热床温度
    private String fileName = "";   //正在打印的文件名
    private String precent = "0%";    //打印文件进度

    public WSItem(WsManager socket, String ip, String port, boolean connectState,String message){
        this.ip = ip;
        this.port = port;
        this.connectState = connectState;
        this.ws = socket;
        this.message = message;
    }

    public String getIp(){
        return ip;
    }
    public String getPort(){
        return port;
    }
    public boolean getConnectState(){
        return connectState;
    }
    public void setConnectState(boolean state){
        this.connectState = state;
    }
    public WsManager getSocket(){
        return ws;
    }
    public void setSocket(WsManager socket){
        this.ws = socket;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
        //接受信息处理
        //状态处理
        if (message.contains("printer_idle")){
            state = "online";
        }else if (message.contains("printer_paused")){
            state = "pause";
            String[] split = message.split(";");
            if (split[1].length() == 2){
                fileName = split[1].split(":")[1];
            }
            precent = split[2].split(":")[1];
        }else if (message.contains("printer_printing")){
            state = "print";
            String[] split = message.split(";");
            if (split[1].length() == 2){
                fileName = split[1].split(":")[1];
            }
            precent = split[2].split(":")[1];
        }else if (message.contains("printer_finish")){
            state = "online";
        }
        //温度处理
        if (message.contains("ok")&&message.contains("T")&&message.contains("B")){
            String[] split = message.split(" ");
            cExtruderTemp = split[1].split(":")[1] + "℃";
            cBedTemp = split[3].split(":")[1] + "℃";
            extruderTemp = split[2].split("/")[1] + "℃";
            bedTemp = split[4].split("/")[1] + "℃";
        }
        //打印机文件处理
    }
    public String getState(){
        return state;
    }
    public String getcExtruderTemp(){
        return cExtruderTemp;
    }
    public String getcBedTemp(){
        return cBedTemp;
    }
    public String getExtruderTemp(){
        return extruderTemp;
    }
    public String getBedTemp(){
        return bedTemp;
    }
    public String getFileName(){
        return fileName;
    }
    public String getPrecent(){
        return precent;
    }
}
