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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-03-08.
 */

public class FileItemAdapter extends ArrayAdapter<ListItem>{
    private int layoutId;
    private List<ListItem> list = new ArrayList<>();

    public FileItemAdapter(Context context, int layoutId, List<ListItem> list) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
        this.list.clear();
        this.list.addAll(list);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if ( convertView == null){
            //convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            view = LayoutInflater.from(getContext()).inflate(R.layout.print_list_file_item, null);
        } else {
            view = convertView;
        }
        ListItem item = getItem(position);
        TextView textView = (TextView) view.findViewById(R.id.printer_name);
        TextView mSerial = (TextView) view.findViewById(R.id.serial_number);
        textView.setText(item.getName());
        mSerial.setText(item.getNumber());
        textView.setTag(item);
        mSerial.setTag(item);
        ImageView mPrint = (ImageView) view.findViewById(R.id.list_print);
        ImageView mDelete = (ImageView) view.findViewById(R.id.list_delete);
        final int i = position;
        mPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemEditListener.onEditClick(i);
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemDeleteListener.onDeleteClick(i);
            }
        });
        mPrint.setTag(item);
        mDelete.setTag(item);
        return view;
    }

    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }
    public interface onItemEditListener {
        void onEditClick(int i);
    }
    private FileItemAdapter.onItemDeleteListener mOnItemDeleteListener;
    private FileItemAdapter.onItemEditListener mOnItemEditListener;

    public void setOnItemDeleteClickListener(FileItemAdapter.onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }
    public void setOnItemEditClickListener(FileItemAdapter.onItemEditListener mOnItemEditListener) {
        this.mOnItemEditListener = mOnItemEditListener;
    }
}
