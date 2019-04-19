package com.geeetech.administrator.easyprint.Activity.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geeetech.administrator.easyprint.MainActivity;
import com.geeetech.administrator.easyprint.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-01-29.
 */

public class GallaryFragment extends Fragment {
    View view;
    private TabLayout mTabTl;
    public static ViewPager mContentVp;

    private List<String> mTabIndicators;
    private List<Fragment> mTabFragments;
    private ContentPagerAdapter mContentAdapter;


    public static GallaryFragment newInstance(String info) {
        Bundle args = new Bundle();
        GallaryFragment fragment = new GallaryFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gallary, null);

        mTabTl = (TabLayout) view.findViewById(R.id.tl_tab);
        mContentVp = (ViewPager) view.findViewById(R.id.viewPager_gallary);

        initContent();
        initTab();
        return view;
    }

    private void initTab(){
        mTabTl.setTabMode(TabLayout.MODE_FIXED);
        //mTabTl.setTabTextColors(ContextCompat.getColor(this, R.color.app_main_show_blue), ContextCompat.getColor(this, R.color.app_main_white));
        //mTabTl.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.app_main_white));
        ViewCompat.setElevation(mTabTl, 10);
        mTabTl.setupWithViewPager(mContentVp);
        mContentVp.setOffscreenPageLimit(2);
        mTabTl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 默认切换的时候，会有一个过渡动画，设为false后，取消动画，直接显示
                //mContentVp.setCurrentItem(tab.getPosition(), false);
                if ( tab.getPosition() == 2){
                    try{
                        String serialNumber = MainActivity.mCurrentPrinter;
                        if ( serialNumber.equals("")){
                            WifiFile.mLinearLayout.setVisibility(View.GONE);
                            return;
                        }
                        if (!serialNumber.contains("3DWF")){
                            WifiFile.mLinearLayout.setVisibility(View.GONE);
                            return;
                        }
                        WifiFile.mLinearLayout.setVisibility(View.VISIBLE);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initContent(){
        mTabIndicators = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if ( i == 0){
                mTabIndicators.add("Cloud");
            };
            if ( i == 1){
                mTabIndicators.add("SD card");
            };
            if ( i == 2){
                mTabIndicators.add("3DWiFi");
            };
        }
        mTabFragments = new ArrayList<>();
        for (String s : mTabIndicators) {
            if ( s .equals("Cloud") ){
                mTabFragments.add(Cloud.newInstance(s));
            }
            if ( s .equals("SD card")){
                mTabFragments.add(SdCard.newInstance(s));
            }
            if ( s .equals("3DWiFi") ){
                mTabFragments.add(WifiFile.newInstance(s));
            };
        }
        mContentAdapter = new ContentPagerAdapter(getFragmentManager());//getSupportFragmentManager()
        mContentVp.setAdapter(mContentAdapter);
    }

    class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mTabFragments.get(position);
        }

        @Override
        public int getCount() {
            return mTabIndicators.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabIndicators.get(position);
        }
    }
}
