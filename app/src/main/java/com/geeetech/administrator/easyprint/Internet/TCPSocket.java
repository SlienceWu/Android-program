package com.geeetech.administrator.easyprint.Internet;

import android.content.Context;
import android.util.Log;

import com.geeetech.administrator.easyprint.Activity.Myminifactory;
import com.geeetech.administrator.easyprint.Activity.PrintersTcp;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Administrator on 2018-11-14.
 */

public class TCPSocket {
    public TCPSocket(final Context context, final String ip, final int port){
        final Socket[] socket = new Socket[1];
        new Thread() {
            @Override
            public void run() {
                try {
                    InetAddress ipAddress = InetAddress.getByName(ip);
                    //socket[0] = new Socket(ipAddress, port);//创建连接地址和端口
                    socket[0] = new Socket();
                    SocketAddress socAddress = new InetSocketAddress(ipAddress, port);
                    socket[0].connect(socAddress, 3000);//超时3秒
                    if (context == PrintersTcp.isContext){
                        System.out.println("**********************************************************");
                        System.out.println("PrintersTcp.socketGroup.size:" + PrintersTcp.socketGroup.size());
                        if (PrintersTcp.socketGroup.size() == 0){
                            PrintersTcp.isReceive = 0;
                        }else if (PrintersTcp.socketGroup.size() > 0){
                            if (PrintersTcp.socketGroup.get(PrintersTcp.mCurrentPrinter).getIp().equals(ip)){
                                PrintersTcp.isReceive = 0;
                            }
                        }
                        if (PrintersTcp.socket == null){
                            PrintersTcp.socket = socket[0];
                        }
                        SocketItem item = new SocketItem(socket[0], ip, port, true, "");
                        if (PrintersTcp.socketGroup.size() >= 5){
                            socket[0].close();
                            ToastUtil.showToast(context,"socket is too manay");
                            return;
                        }else if (PrintersTcp.socketGroup.size() == 0){
                            PrintersTcp.socketGroup.add(item);
                        }else{
                            boolean isExist = false;
                            for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                                if (PrintersTcp.socketGroup.get(i).getIp().equals(ip) ){
                                    isExist = true;
                                    if ( !PrintersTcp.socketGroup.get(i).getConnectState()){
                                        ToastUtil.showToast(context,"socket is disconnected and remove.");
                                        if (PrintersTcp.socket == PrintersTcp.socketGroup.get(i).getSocket()){
                                            PrintersTcp.socket = socket[0];
                                            ToastUtil.showToast(context,"socket is change.");
                                        }
                                        PrintersTcp.socketGroup.remove(i);
                                        PrintersTcp.socketGroup.add(i,item);
                                    }else{
                                        ToastUtil.showToast(context,"socket is exist and connected");
                                    }
                                    break;
                                }
                            }
                            if (!isExist){
                                PrintersTcp.socketGroup.add(item);
                            }
                        }
                        if (PrintersTcp.socketGroup.size()>1){
                            if (PrintersTcp.socket != null){
                                for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                                    if (ip == PrintersTcp.socketGroup.get(i).getIp()){
                                        PrintersTcp.socketGroup.get(i).setCurrent(false);
                                        break;
                                    }
                                }
                            }
                            receive(context,socket, ip, port);
                        }else{
                            receive(context,socket, ip, port);
                        }
                        return;
                    }
                    Myminifactory.isReceive = 0;
                    if (Myminifactory.socket == null){
                        Myminifactory.socket = socket[0];
                    }
                    SocketItem item = new SocketItem(socket[0], ip, port, true, "");
                    if (Myminifactory.socketGroup.size() >= 5){
                        socket[0].close();
                        ToastUtil.showToast(context,"socket is too manay");
                        return;
                    }else if (Myminifactory.socketGroup.size() == 0){
                        Myminifactory.socketGroup.add(item);
                    }else{
                        boolean isExist = false;
                        for (int i=0;i<Myminifactory.socketGroup.size();i++){
                            if (Myminifactory.socketGroup.get(i).getIp().equals(ip) ){
                                //if (Myminifactory.socketGroup.get(i).getConnectState()){
                                    isExist = true;
                                //}else {
                                if ( !Myminifactory.socketGroup.get(i).getConnectState()){
                                    ToastUtil.showToast(context,"socket is disconnected and remove.");
                                    if (Myminifactory.socket == Myminifactory.socketGroup.get(i).getSocket()){
                                        Myminifactory.socket = socket[0];
                                        ToastUtil.showToast(context,"socket is change.");
                                    }
                                    Myminifactory.socketGroup.remove(i);
                                    Myminifactory.socketGroup.add(i,item);
                                }else{
                                    ToastUtil.showToast(context,"socket is exist and connected");
                                }
                                //}
                                break;
                            }
                        }
                        if (!isExist){
                            Myminifactory.socketGroup.add(item);
                        }/*else{
                            socket[0].close();
                            ToastUtil.showToast(context,"socket is exist2");
                            return;
                        }*/
                    }
                    if (Myminifactory.socketGroup.size()>1){
                        if (Myminifactory.socket != null){
                            for (int i=0;i<Myminifactory.socketGroup.size();i++){
                                if (ip == Myminifactory.socketGroup.get(i).getIp()){
                                    Myminifactory.socketGroup.get(i).setCurrent(false);
                                    break;
                                }
                            }
                        }
                        receive(context,socket, ip, port);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Myminifactory.isReceive = 1;
                    if (PrintersTcp.socketGroup.size() == 0){
                        PrintersTcp.isReceive = 1;
                    } else if (PrintersTcp.socketGroup.size() == 1){
                        if (PrintersTcp.socketGroup.get(0).getIp().equals(ip)){
                            PrintersTcp.isReceive = 1;
                        }
                    }
                    Log.e("TCPSocket","socket connect time out...");
                }
            }
        }.start();

    }
    /**
     * 接受消息
     */
    public void receive(final Context context,final Socket[] socket, final String ip, final int port){
        new Thread() {
            @Override
            public void run() {
                try
                {
                    while (true)
                    {
                        if (context == PrintersTcp.isContext){
                            for (int j=0;j<PrintersTcp.socketGroup.size();j++){
                                if (ip == PrintersTcp.socketGroup.get(j).getIp()){
                                    //System.out.println("*******************" + PrintersTcp.socketGroup.get(j).getIp() + "*******************" + PrintersTcp.socketGroup.get(j).isCurrent());
                                    if (!PrintersTcp.socketGroup.get(j).isCurrent()){
                                        boolean isExist = true;
                                        for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                                            isExist = false;
                                            if (socket[0] == PrintersTcp.socketGroup.get(i).getSocket()){
                                                isExist = true;
                                                break;
                                            }
                                        }
                                        if (!isExist){
                                            ToastUtil.showToast(context,"socket is remove");
                                            return;
                                        }
                                        int len = 0;
                                        byte[] buffer = new byte[1024];
                                        try{
                                            InputStream inputStream = new DataInputStream( socket[0].getInputStream());
                                            len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        if (len>0){
                                            String res = new String(buffer,0,len);
                                            for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                                                if (PrintersTcp.socketGroup.get(i).getIp().equals(ip) ){
                                                    PrintersTcp.socketGroup.get(i).setConnectState(true);
                                                    PrintersTcp.socketGroup.get(i).setMessage(res);
                                                    send(socket, "hi new user");
                                                    break;
                                                }
                                            }
                                        }else{
                                            try{
                                                socket[0].sendUrgentData(0xFF);
                                            }catch(Exception e){
                                                InetAddress ipAddress = InetAddress.getByName(ip);
                                                for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                                                    if (PrintersTcp.socketGroup.get(i).getIp().equals(ip) ){
                                                        PrintersTcp.socketGroup.get(i).setConnectState(false);
                                                        break;
                                                    }
                                                }
                                                return;
                                                /*try{
                                                    socket[0] = new Socket();
                                                    SocketAddress socAddress = new InetSocketAddress(ipAddress, port);
                                                    socket[0].connect(socAddress, 3000);//超时3秒
                                                    for (int i=0;i<PrintersTcp.socketGroup.size();i++){
                                                        if (PrintersTcp.socketGroup.get(i).getIp().equals(ip) ){
                                                            PrintersTcp.socketGroup.get(i).setSocket(socket[0]);
                                                            send(socket, "reconnect user");
                                                            break;
                                                        }
                                                    }
                                                }catch (Exception e1){
                                                    ToastUtil.showToast(context,"socket connect time out.");
                                                    return;
                                                }*/
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }else if (context == Myminifactory.isContext){
                            for (int j=0;j<Myminifactory.socketGroup.size();j++){
                                if (ip == Myminifactory.socketGroup.get(j).getIp()){
                                    if (!Myminifactory.socketGroup.get(j).isCurrent()){
                                        boolean isExist = true;
                                        for (int i=0;i<Myminifactory.socketGroup.size();i++){
                                            isExist = false;
                                            if (socket[0] == Myminifactory.socketGroup.get(i).getSocket()){
                                                isExist = true;
                                                break;
                                            }
                                        }
                                        if (!isExist){
                                            ToastUtil.showToast(context,"socket is remove");
                                            return;
                                        }
                                        int len = 0;
                                        byte[] buffer = new byte[1024];
                                        try{
                                            InputStream inputStream = new DataInputStream( socket[0].getInputStream());
                                            len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        if (len>0){
                                            String res = new String(buffer,0,len);
                                            for (int i=0;i<Myminifactory.socketGroup.size();i++){
                                                if (Myminifactory.socketGroup.get(i).getIp().equals(ip) ){
                                                    Myminifactory.socketGroup.get(i).setConnectState(true);
                                                    Myminifactory.socketGroup.get(i).setMessage(res);
                                                    send(socket, "hi new user");
                                                    break;
                                                }
                                            }
                                        }else{
                                            try{
                                                socket[0].sendUrgentData(0xFF);
                                            }catch(Exception e){
                                                InetAddress ipAddress = InetAddress.getByName(ip);
                                                for (int i=0;i<Myminifactory.socketGroup.size();i++){
                                                    if (Myminifactory.socketGroup.get(i).getIp().equals(ip) ){
                                                        Myminifactory.socketGroup.get(i).setConnectState(false);
                                                        break;
                                                    }
                                                }

                                                //socket[0] = new Socket(ipAddress, port);//创建连接地址和端口
                                                try{
                                                    socket[0] = new Socket();
                                                    SocketAddress socAddress = new InetSocketAddress(ipAddress, port);
                                                    socket[0].connect(socAddress, 3000);//超时3秒

                                                    for (int i=0;i<Myminifactory.socketGroup.size();i++){
                                                        if (Myminifactory.socketGroup.get(i).getIp().equals(ip) ){
                                                            Myminifactory.socketGroup.get(i).setSocket(socket[0]);
                                                            send(socket, "reconnect user");
                                                            break;
                                                        }
                                                    }
                                                }catch (Exception e1){
                                                    ToastUtil.showToast(context,"socket connect time out.");
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    for (int i=0;i<Myminifactory.socketGroup.size();i++){
                        if (Myminifactory.socketGroup.get(i).getIp().equals(ip) ){
                            Myminifactory.socketGroup.get(i).setConnectState(false);
                            break;
                        }
                    }
                    e.printStackTrace();
                }
            }
        }.start();
    }
    /**
     * 发送消息
     */
    public void send(final Socket[] socket,final String need) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DataOutputStream writer = new DataOutputStream(socket[0].getOutputStream());
                    int len = need.getBytes().length;
                    byte[] list = need.getBytes();
                    byte[] needBuffer = new byte[len+3];
                    byte[] filelen = IntToByte(len);
                    byte end = Integer.valueOf("82", 16).byteValue();
                    if (need.equals("hi new user")||need.equals("reconnect user")){
                        end = Integer.valueOf("83", 16).byteValue();
                    }
                    needBuffer[0] = end;
                    needBuffer[1] = filelen[2];
                    needBuffer[2] = filelen[3];
                    for (int i=0;i<len;i++){
                        needBuffer[i+3] = list[i];
                    }
                    writer.write(needBuffer);
                    //writer.write(need.getBytes());
                    writer.flush();
                    System.out.println("未选中打印机发送消息:" + socket[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    //int 转 byte[]
    public static byte[] IntToByte(int num){
        byte[]bytes=new byte[4];
        bytes[0]=(byte) ((num>>24)&0xff);
        bytes[1]=(byte) ((num>>16)&0xff);
        bytes[2]=(byte) ((num>>8)&0xff);
        bytes[3]=(byte) (num&0xff);
        return bytes;
    }
}