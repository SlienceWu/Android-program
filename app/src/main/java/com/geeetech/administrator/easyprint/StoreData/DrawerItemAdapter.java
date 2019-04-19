package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2018-03-08.
 */

public class DrawerItemAdapter extends ArrayAdapter<DrawerListItem>{
    private int layoutId;

    public DrawerItemAdapter(Context context, int layoutId, List<DrawerListItem> list) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerListItem item = getItem(position);
        View view;
        if ( convertView == null){
            //convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            view = LayoutInflater.from(getContext()).inflate(R.layout.print_drawer_list_item, null);
        } else {
            view = convertView;
        }
        ImageView mPrinterImg = (ImageView) view.findViewById(R.id.printer_img);
        TextView mPrinterName = (TextView) view.findViewById(R.id.printer_name);
        TextView mPrinterState = (TextView) view.findViewById(R.id.printer_state);
        TextView mPrinterHeatbed = (TextView) view.findViewById(R.id.printer_heatbed);
        TextView mPrinterExtruder = (TextView) view.findViewById(R.id.printer_extruder);
        ProgressBar mPrinterProgress = (ProgressBar) view.findViewById(R.id.printer_progress);
        TextView mPrinterProgressShow = (TextView) view.findViewById(R.id.printer_progress_show);
        mPrinterImg.setTag(item);
        mPrinterName.setTag(item);
        mPrinterState.setTag(item);
        mPrinterHeatbed.setTag(item);
        mPrinterExtruder.setTag(item);
        mPrinterProgress.setTag(item);
        mPrinterProgressShow.setTag(item);

        /*try{
            Picasso.with(getContext()).load(item.getAvatar()).into(mPrinterImg);
        }catch (Exception e){
            Picasso.with(getContext()).load(R.drawable.icon_printer_off).into(mPrinterImg);
        }*/
        String number = item.getNumber();
        String image = item.getAvatar();
        /*if ( number.indexOf("E180")>0){
            Picasso.with(getContext()).load(R.mipmap.icon_e180).into(mPrinterImg);
        }else if ( number.indexOf("D200")>0){
            Picasso.with(getContext()).load(R.mipmap.icon_d200).into(mPrinterImg);
        }else if ( number.indexOf("A30")>0){
            Picasso.with(getContext()).load(R.mipmap.icon_a30).into(mPrinterImg);
        }else{
            Picasso.with(getContext()).load(R.mipmap.icon_3dwf).into(mPrinterImg);
        }*/
        mPrinterName.setText(item.getName());

        if (item.getState().equals("0")||item.getState().equals("1")){
            mPrinterState.setText("Offline");
            Picasso.with(getContext()).load(R.drawable.icon_printer_off).into(mPrinterImg);
        }else if(item.getState().equals("3")){
            if ( !image.equals("")){
                Picasso.with(getContext()).load(image).into(mPrinterImg);
            }
            mPrinterState.setText("Printing");
        }else if(item.getState().equals("4")){
            if ( !image.equals("")){
                Picasso.with(getContext()).load(image).into(mPrinterImg);
            }
            mPrinterState.setText("Pause");
        }else{
            if ( !image.equals("")){
                Picasso.with(getContext()).load(image).into(mPrinterImg);
            }
            mPrinterState.setText("Online");
        }
        if (image.equals("")&&!item.getState().equals("0")&&!item.getState().equals("1")) {
            if ( number.indexOf("E180")>0){
                Picasso.with(getContext()).load(R.mipmap.icon_e180).into(mPrinterImg);
            }else if ( number.indexOf("D200")>0){
                Picasso.with(getContext()).load(R.mipmap.icon_d200).into(mPrinterImg);
            }else if ( number.indexOf("A30")>0){
                Picasso.with(getContext()).load(R.mipmap.icon_a30).into(mPrinterImg);
            }else{
                Picasso.with(getContext()).load(R.mipmap.icon_3dwifi).into(mPrinterImg);
            }
        }
        mPrinterHeatbed.setText(item.getHeatbed());
        mPrinterExtruder.setText(item.getExtruder());
        Double a = (double) Math.round(Double.valueOf(item.getProgress()) * 100);
        int progress = a.intValue();
        mPrinterProgressShow.setText(a+"%");
        //int progress = Integer.parseInt(item.getProgress());
        mPrinterProgress.setProgress(progress);
        return view;
    }
    /*class ViewHolder {
        //侧滑栏相关
        ImageView mPrinterImg;
        ProgressBar mPrinterProgress;
        TextView mPrinterName,mPrinterState,mPrinterHeatbed,mPrinterExtruder,mPrinterProgressShow;
    }*/
}
