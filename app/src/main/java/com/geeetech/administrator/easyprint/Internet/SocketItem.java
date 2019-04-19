package com.geeetech.administrator.easyprint.Internet;

import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018-11-14.
 */

//socket的结构
public class SocketItem{
    private String ip;  //IP地址
    private int port;   //端口号
    private boolean connectState;   //TCP连接状态
    private boolean isCurrent = true;    //是否为选中TCP
    private Socket socket;  //TCPsocket
    private String message; //接受的数据
    private String state = "online"; //打印机状态
    private String cExtruderTemp = "0"; //当前挤出头温度
    private String cBedTemp = "0"; //当前热床温度
    private String extruderTemp = "0"; //设置的挤出头温度
    private String bedTemp = "0";  //设置的热床温度
    private String fileName = "";   //正在打印的文件名
    private String precent = "0%";    //打印文件进度
    private String mSerialNumber = ""; //打印机序列号
    private ArrayList<String> mWifiList = new ArrayList<>();  //wifi模块列表
    private ArrayList<String> mSdList = new ArrayList<>();    //sd卡列表
    private String uploadState = "off";    //上传文件状态

    public SocketItem(Socket socket, String ip, int port, boolean connectState,String message){
        this.ip = ip;
        this.port = port;
        this.connectState = connectState;
        this.socket = socket;
        this.message = message;
    }

    public String getIp(){
        return ip;
    }
    public int getPort(){
        return port;
    }
    public boolean getConnectState(){
        return connectState;
    }
    public void setConnectState(boolean state){
        this.connectState = state;
        /*if (connectState){
            this.state = "online";
        }else{
            this.state = "offline";
        }*/
    }
    public Socket getSocket(){
        return socket;
    }
    public void setSocket(Socket socket){
        this.socket = socket;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
        if (message.equals("live")){
            return;
        }
        Log.d("SocketItem","----------------------");
        Log.d("SocketItem",message);
        Log.d("SocketItem","----------------------");
        //接受信息处理
        //打印机名字
        if (message.contains("connected")){
            String[] split = message.split(" ");
            mSerialNumber = split[0];
            Log.d("SocketItem","***********************" + mSerialNumber);
            //System.out.println();
        }
        //状态处理
        else if (message.contains("printer_idle")){
            precent = "0%";
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
            precent = "0%";
            state = "online";
        }
        //温度处理
        else if (message.contains("ok")&&message.contains("T")&&message.contains("B")){
            String[] split = message.split(" ");
            cExtruderTemp = split[1].split(":")[1];
            cBedTemp = split[3].split(":")[1];
            extruderTemp = split[2].split("/")[1];
            bedTemp = split[4].split("/")[1];
        }
        //打印机文件处理
        else if ( message.contains("M20 3DWF;")){
            String[] split = message.split(";");
            if ( mWifiList.size()== 0){
                for ( int i=0;i<split.length - 4;i++ ){
                    mWifiList.add(split[i+3]);
                }
            } else{
                if (mWifiList.size() == Integer.valueOf(split[2])){
                    return;
                }
                for ( int i=0;i<split.length - 4;i++ ){
                    boolean isExist = false;
                    for(int j=0;j<mWifiList.size();j++){
                        if (mWifiList.get(i).equals(split[i+3]) ){
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist){
                        mWifiList.add(split[i+3]);
                    }
                }
            }
        }
        else if ( message.contains("M20;")){
            String[] split = message.split(";");
            for ( int i=0;i<split.length - 3;i++ ){
                mSdList.add(split[i+3]);
            }
        }
        //上传状态处理
        else if (message.contains("M2110")&&message.contains("start")){
            uploadState = "uploading";
        } else if (message.contains("M2110")&&message.contains("ok")){
            uploadState = "uploading";
        } else if (message.contains("M2110")&&message.contains("Err")){
            uploadState = "uploadFail";
        } else if (message.contains("M2110")&&message.contains("stop")){
            uploadState = "uploadStop";
        } else if (message.contains("M2110 send completion")){
            uploadState = "uploadStop";
        }
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
    public void setCurrent (boolean isChoose){
        this.isCurrent = isChoose;
    }
    public boolean isCurrent(){
        return isCurrent;
    }
    public String getmSerialNumber(){
        return mSerialNumber;
    }
    public ArrayList<String> getWifiList(){
        return mWifiList;
    }
    public ArrayList<String> getSdList(){
        return mSdList;
    }
    public void setmWifiList(){
        mWifiList.removeAll(mWifiList);
    }
    public void setmSdList(){
        mSdList.removeAll(mSdList);
    }
    public void deleteList(int i,String listName){
        if (listName.equals("sd")){
            mSdList.remove(i);
        }else{
            mWifiList.remove(i);
        }
    }
    public void setUploadState(String state){
        uploadState = state;
    }
    public String getUploadState(){
        return uploadState;
    }
}
