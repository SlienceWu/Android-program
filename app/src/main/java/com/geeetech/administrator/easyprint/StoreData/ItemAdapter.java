package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2018-03-08.
 */

public class ItemAdapter extends ArrayAdapter<ListItem>{
    private int layoutId;

    public ItemAdapter(Context context, int layoutId, List<ListItem> list) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if ( convertView == null){
            viewHolder = new ViewHolder();
            ListItem item = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, null);
            //convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.printer_name);
            viewHolder.mSerial = (TextView) convertView.findViewById(R.id.serial_number);
            viewHolder.textView.setText(item.getName());
            viewHolder.mSerial.setText(item.getNumber());
            viewHolder.textView.setTag(item);
            viewHolder.mSerial.setTag(item);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if ( layoutId == R.layout.print_list_material_item){
            viewHolder.mEdit = (ImageView) convertView.findViewById(R.id.edit_material);
            viewHolder.mDelete = (ImageView) convertView.findViewById(R.id.delete_material);
            final int i = position;
            viewHolder.mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemEditListener.onEditClick(i);
                }
            });
            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemDeleteListener.onDeleteClick(i);
                }
            });
            viewHolder.mEdit.setTag(position);
            viewHolder.mDelete.setTag(position);
            convertView.setTag(viewHolder);
        }else if ( layoutId == R.layout.print_list_profile_item){
            viewHolder.mEdit = (ImageView) convertView.findViewById(R.id.edit_profile);
            viewHolder.mDelete = (ImageView) convertView.findViewById(R.id.delete_profile);
            final int i = position;
            viewHolder.mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemEditListener.onEditClick(i);
                }
            });
            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemDeleteListener.onDeleteClick(i);
                }
            });
            viewHolder.mEdit.setTag(position);
            viewHolder.mDelete.setTag(position);
            convertView.setTag(viewHolder);
        }else if ( layoutId == R.layout.print_list_file_item){
            viewHolder.mPrint = (ImageView) convertView.findViewById(R.id.list_print);
            viewHolder.mDelete = (ImageView) convertView.findViewById(R.id.list_delete);
            final int i = position;
            viewHolder.mPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemEditListener.onEditClick(i);
                }
            });
            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemDeleteListener.onDeleteClick(i);
                }
            });
            viewHolder.mPrint.setTag(position);
            viewHolder.mDelete.setTag(position);
            convertView.setTag(viewHolder);
        }else if ( layoutId == R.layout.model_stl_item) {
            ListItem item = getItem(position);
            viewHolder.mModel = (ImageView) convertView.findViewById(R.id.model_img);
            viewHolder.mButton = (Button) convertView.findViewById(R.id.button_print);
            final int i = position;
            viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemEditListener.onEditClick(i);
                }
            });
            Picasso.with(getContext()).load(item.getName()).resize(80, 80).centerCrop().into( viewHolder.mModel);
            viewHolder.mButton.setTag(position);
            convertView.setTag(viewHolder);
        }
        return convertView;
    }
    /*ListItem item = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            TextView textView = (TextView) view.findViewById(R.id.printer_name);
            TextView mSerial = (TextView) view.findViewById(R.id.serial_number);
            textView.setText(item.getName());
            mSerial.setText(item.getNumber());*/
    class ViewHolder {
        TextView textView, mSerial;
        ImageView mEdit,mDelete,mPrint,mModel;
        Button mButton;
        //侧滑栏相关
        ImageView mPrinterImg;
        ProgressBar mPrinterProgress;
        TextView mPrinterName,mPrinterState,mPrinterHeatbed,mPrinterExtruder;
    }
    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }
    public interface onItemEditListener {
        void onEditClick(int i);
    }

    private onItemDeleteListener mOnItemDeleteListener;
    private onItemEditListener mOnItemEditListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }
    public void setOnItemEditClickListener(onItemEditListener mOnItemEditListener) {
        this.mOnItemEditListener = mOnItemEditListener;
    }
    /*public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }

    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }*/
}
