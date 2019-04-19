package com.geeetech.administrator.easyprint.Internet;

import android.content.Context;

import com.geeetech.administrator.easyprint.Activity.Myminifactory;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.rabtman.wsmanager.WsManager;
import com.rabtman.wsmanager.listener.WsStatusListener;

import okio.ByteString;

/**
 * Created by Administrator on 2018-11-26.
 */

public class WSSocket {

    public WSSocket(final Context context, final String ip, final String port, final WsManager wsManager){
        new Thread(){
            @Override
            public void run() {
                super.run();
                wsManager.startConnect();
                wsManager.setWsStatusListener(new WsStatusListener() {
                    @Override
                    public void onOpen(okhttp3.Response response) {
                        super.onOpen(response);
                        String need = "test";
                        wsManager.sendMessage(need);
                        if (Myminifactory.ws == null){
                            Myminifactory.ws = wsManager;
                        }
                        WSItem item = new WSItem(wsManager, ip, port, true, "");
                        if (Myminifactory.wsSocketGroup.size() >= 5){
                            wsManager.stopConnect();
                            ToastUtil.showToast(context,"socket is too manay");
                            return;
                        }else if (Myminifactory.wsSocketGroup.size() == 0){
                            Myminifactory.wsSocketGroup.add(item);
                        }else{
                            boolean isExist = false;
                            for (int i=0;i<Myminifactory.wsSocketGroup.size();i++){
                                if (Myminifactory.wsSocketGroup.get(i).getIp().equals(ip) ){
                                    isExist = true;
                                    if ( !Myminifactory.wsSocketGroup.get(i).getSocket().isWsConnected()){
                                        ToastUtil.showToast(context,"socket is disconnected and remove.");
                                        Myminifactory.wsSocketGroup.remove(i);
                                        Myminifactory.wsSocketGroup.add(i,item);
                                    }else{
                                        ToastUtil.showToast(context,"socket is exist and connected");
                                    }
                                    break;
                                }
                            }
                            if (!isExist){
                                Myminifactory.wsSocketGroup.add(item);
                            }
                        }
                        Myminifactory.ws = wsManager;
                    }

                    @Override
                    public void onMessage(String text) {
                        super.onMessage(text);
                        wsManager.sendMessage("back message");
                        for (int i=0;i<Myminifactory.wsSocketGroup.size();i++){
                            if (Myminifactory.wsSocketGroup.get(i).getIp().equals(ip) ){
                                Myminifactory.wsSocketGroup.get(0).setMessage(text);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onMessage(ByteString bytes) {
                        super.onMessage(bytes);
                        ByteString a = ByteString.of("a".getBytes());
                        wsManager.sendMessage(a);
                    }

                    @Override
                    public void onReconnect() {
                        super.onReconnect();
                    }

                    @Override
                    public void onClosing(int code, String reason) {
                        super.onClosing(code, reason);
                    }

                    @Override
                    public void onClosed(int code, String reason) {
                        super.onClosed(code, reason);

                    }

                    @Override
                    public void onFailure(Throwable t, okhttp3.Response response) {
                        super.onFailure(t, response);

                    }
                });
            }
        }.start();
    }
}
