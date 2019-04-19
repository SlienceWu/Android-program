package com.geeetech.administrator.easyprint.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.geeetech.administrator.easyprint.R;

/**
 * Created by Administrator on 2018-12-06.
 */

public class PrintersModeSelect extends BaseActivity {
    private Button mBtnBindPrinter;
    private Button mBtnAddLocal;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_connection_mode);
        final String number = getIntent().getStringExtra("number");
        mBtnBindPrinter = findViewById(R.id.button_printer);
        mBtnAddLocal = findViewById(R.id.button_local);

        mBtnBindPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("number",number);
                intent.setClass(PrintersModeSelect.this, Printers.class);
                startActivity(intent);
            }
        });
        mBtnAddLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PrintersModeSelect.this, PrintersTcp.class);
                startActivity(intent);
            }
        });
    }
}
