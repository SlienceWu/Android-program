package com.geeetech.administrator.easyprint.Activity;

import android.os.Bundle;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

/**
 * Created by Administrator on 2018-09-28.
 */

public class MyminiDetail extends BaseActivity {
    private TextView mParams;
    private String mUrl;
    String mToken;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minifactory_detail);

        mParams = findViewById(R.id.params);
        mUrl = getIntent().getStringExtra("url");
        mParams.setText(mUrl);

        mToken = getValueByName(mUrl,"access_token");
        String url = "https://www.myminifactory.com/api/v2/user?access_token=" + mToken;
        OkGo.<String>get(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        mParams.setText(mToken + "**************" + response.body());
                    }
                });
    }
}
