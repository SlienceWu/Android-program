package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Administrator on 2018-03-27.
 */

public class GridItemAdapter extends ArrayAdapter<ModelListItem> {
    private int layoutId;

    public GridItemAdapter(Context context, int layoutId, List<ModelListItem> list) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //String url = getItem(position);
        ModelListItem item = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.model_gridview_item, null);
        } else {
            view = convertView;
        }
        final ImageView imageView = (ImageView) view.findViewById(R.id.img);
        TextView textView = (TextView) view.findViewById(R.id.text);
        //为该ImageView设置一个Tag,防止图片错位
        imageView.setTag(item);
        textView.setTag(item);
        //为该ImageView设置显示的图片
        final String url = Urls.SERVER_IMG + item.getThumb();
        String text = item.getName();
        Picasso.with(getContext()).load(url).resize(80, 80).centerCrop().into(imageView);
        /*Picasso.with(getContext()).load(url).fetch(new Callback() {
            @Override
            public void onSuccess() {
                Picasso.with(getContext()).load(url).into(imageView);
            }
            @Override
            public void onError() {

            }
        });*/
        textView.setText(text);
        //this.notifyDataSetChanged();
        return view;
    }
}
