package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;

import java.util.List;

/**
 * Created by Administrator on 2018-12-04.
 */

public class ListTcpAdapter extends ArrayAdapter<ListTcpItem> implements OnClickListener{
    private int layoutId;
    //private List<ListStateItem> list = new ArrayList<>();
    private Callback mCallback;
    /**
     * 自定义接口，用于回调按钮点击事件到Activity
     * @author Ivan Xu
     * 2014-11-26
     */
    public interface Callback {
        public void click(View v);
    }

    public ListTcpAdapter(Context context, int layoutId, List<ListTcpItem> list, Callback callback) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
        //this.list.clear();
        //this.list.addAll(list);
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if ( convertView == null){
            viewHolder = new ViewHolder();
            ListTcpItem item = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, null);
            //convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.printer_name);
            viewHolder.mSerial = (TextView) convertView.findViewById(R.id.serial_number);
            //viewHolder.mState = (ImageView) convertView.findViewById(R.id.printer_state);
            viewHolder.mState = (Button) convertView.findViewById(R.id.button_connect);
            viewHolder.textView.setText(item.getName());
            viewHolder.mSerial.setText(item.getNumber());

            if ( !item.getState()){
                viewHolder.mState.setText("Connect");
                viewHolder.mState.setBackgroundColor(getContext().getResources().getColor(R.color.app_main_show_blue));
            }else{
                viewHolder.mState.setText("Disconnect");
                viewHolder.mState.setBackgroundColor(getContext().getResources().getColor(R.color.app_color_red));
            }
            viewHolder.textView.setOnClickListener(this);
            viewHolder.mState.setOnClickListener(this);
            viewHolder.textView.setTag(position);
            viewHolder.mSerial.setTag(position);
            viewHolder.mState.setTag(position);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    class ViewHolder {
        TextView textView, mSerial;
        //ImageView mState;
        Button mState;
    }
    //响应按钮点击事件,调用子定义接口，并传入View
    @Override
    public void onClick(View v) {
        mCallback.click(v);
    }
}