package com.geeetech.administrator.easyprint.StoreData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geeetech.administrator.easyprint.R;

import java.util.List;

/**
 * Created by Administrator on 2018-03-31.
 */

public class SpinnerAdapter extends BaseAdapter {
    private List<ListItem> mList;
    private Context mContext;
    private int mLayout;
    public SpinnerAdapter(Context context,int layoutId,List<ListItem> list){
        this.mContext = context;
        this.mList = list;
        this.mLayout = layoutId;
    }
    @Override
    public int getCount(){
        return mList.size();
    }
    @Override
    public Object getItem(int postion){
        return mList.get(postion);
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        convertView = layoutInflater.inflate(mLayout,null);
        if ( convertView != null){
            TextView _TextView1=(TextView)convertView.findViewById(R.id.textView1);
            TextView _TextView2=(TextView)convertView.findViewById(R.id.textView2);
            _TextView1.setText(mList.get(position).getName());
            _TextView2.setText(mList.get(position).getNumber());
        }
        return convertView;
    }
}
