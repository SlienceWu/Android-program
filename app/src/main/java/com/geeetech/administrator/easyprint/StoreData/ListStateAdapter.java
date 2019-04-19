package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;

import java.util.List;

/**
 * Created by Administrator on 2018-05-29.
 */

public class ListStateAdapter extends ArrayAdapter<ListStateItem> {
    private int layoutId;
    //private List<ListStateItem> list = new ArrayList<>();

    public ListStateAdapter(Context context, int layoutId, List<ListStateItem> list) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
        //this.list.clear();
        //this.list.addAll(list);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if ( convertView == null){
            viewHolder = new ViewHolder();
            ListStateItem item = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, null);
            //convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.printer_name);
            viewHolder.mSerial = (TextView) convertView.findViewById(R.id.serial_number);
            viewHolder.mState = (ImageView) convertView.findViewById(R.id.printer_state);
            viewHolder.textView.setText(item.getName());
            viewHolder.mSerial.setText(item.getNumber());
            if ( item.getState().equals("0")||item.getState().equals("1")){
                viewHolder.mState.setImageResource(R.drawable.icon_heatbed_no);
            }else{
                viewHolder.mState.setImageResource(R.drawable.icon_heatbed_yes);
            }
            viewHolder.textView.setTag(item);
            viewHolder.mSerial.setTag(item);
            viewHolder.mState.setTag(item);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    class ViewHolder {
        TextView textView, mSerial;
        ImageView mState;
    }
}
