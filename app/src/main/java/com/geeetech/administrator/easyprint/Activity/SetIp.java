package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.geeetech.administrator.easyprint.R;
import com.geeetech.administrator.easyprint.StoreData.Reserve;
import com.geeetech.administrator.easyprint.StoreData.SpUtil;
import com.geeetech.administrator.easyprint.StoreData.ToastUtil;
import com.geeetech.administrator.easyprint.StoreData.Urls;

import java.util.List;

/**
 * Created by Administrator on 2019-01-15.
 */

public class SetIp extends BaseActivity {
    private Spinner mSpinner;
    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfragment_ip);

        mSpinner = findViewById(R.id.spinner);
        mButton = findViewById(R.id.button_change);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = mSpinner.getSelectedItem().toString();
                if (value.equals("rote 1")){
                    try {
                        Reserve.reserveIp(getBaseContext(),"1");
                        Urls.setServer("47.254.41.125:5000");
                        ToastUtil.showToast(getBaseContext(), "Change rote success.");
                    } catch (Exception e) {
                        ToastUtil.showToast(getBaseContext(), "Change rote fail.");
                        e.printStackTrace();
                    }
                }else{
                    try {
                        Reserve.reserveIp(getBaseContext(),"2");
                        Urls.setServer("47.254.41.125:8000");
                        ToastUtil.showToast(getBaseContext(), "Change rote success.");
                    } catch (Exception e) {
                        ToastUtil.showToast(getBaseContext(), "Change rote fail.");
                        e.printStackTrace();
                    }
                }
                ToastUtil.showToast(getBaseContext(),Urls.GET_PRINTER_LIST());
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            List currentIp = SpUtil.getList(getBaseContext(),"CurrentIp");
            String ip = currentIp.get(0).toString();
            if (ip.equals("1")){
                mSpinner.setSelection(0,true);
            }else{
                mSpinner.setSelection(1,true);
            }
        }catch (Exception e){
            mSpinner.setSelection(0,true);
        }
    }
}
