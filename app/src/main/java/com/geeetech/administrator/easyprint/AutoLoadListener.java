package com.geeetech.administrator.easyprint;

import android.os.Parcelable;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Created by Administrator on 2018-03-28.
 */

public class AutoLoadListener implements OnScrollListener {
    // 存储滑动位置状态
    public static Parcelable state;
    public interface AutoLoadCallBack {
        void execute();
    }

    private int getLastVisiblePosition = 0, lastVisiblePositionY = 0;
    private AutoLoadCallBack mCallback;

    public AutoLoadListener(AutoLoadCallBack callback) {
        this.mCallback = callback;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            state = view.onSaveInstanceState();

            //滚动到底部
            if (view.getLastVisiblePosition() > (view.getCount() - 61)) {
                mCallback.execute();
                /*View v = (View) view.getChildAt(view.getChildCount() - 1);
                int[] location = new int[2];
                v.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
                int y = location[1];

                if (view.getLastVisiblePosition() != getLastVisiblePosition && lastVisiblePositionY != y)//第一次拖至底部
                {
                    Toast.makeText(view.getContext(), lastVisiblePositionY+"已经拖动至底部，再次拖动即可翻页"+y, Toast.LENGTH_SHORT).show();
                    getLastVisiblePosition = view.getLastVisiblePosition();
                    lastVisiblePositionY = y;
                    return;
                } else if (view.getLastVisiblePosition() == getLastVisiblePosition && lastVisiblePositionY == y)//第二次拖至底部
                {
                    mCallback.execute();
                }*/
            }

            //未滚动到底部，第二次拖至底部都初始化
            getLastVisiblePosition = 0;
            lastVisiblePositionY = 0;
        }
    }

    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

    }
}