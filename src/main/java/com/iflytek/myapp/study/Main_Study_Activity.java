package com.iflytek.myapp.study;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.iflytek.myapp.base.BaseFragment;
import com.iflytek.myapp.news.Fragment_News_List;

import java.util.ArrayList;
import java.util.List;

public class Main_Study_Activity extends BaseActivity {
    ViewPager viewPager;
    static List<String> titleList;
    PagerTitleStrip mPagerTitleStrip;
    List<BaseFragment> fragments;
    FragAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cur_study_list_activity);
        viewPager = findViewById(R.id.study_main_view_pager);
        fragments = new ArrayList<>();
        fragments.add(Fragment_News_List.newInstance("#学习"));
        fragments.add(Fragment_Study_List.newInstance("dangshi"));
        fragments.add(Fragment_Study_List.newInstance("xinzhongguoshi"));
        fragments.add(Fragment_Study_List.newInstance("gaigekaifangshi"));
        fragments.add(Fragment_Study_List.newInstance("shehuizhuyifazhanshi"));
        adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        titleList = new ArrayList<>();
        titleList.add("学习推荐");
        titleList.add("党史");
        titleList.add("新中国史");
        titleList.add("改革开放史");
        titleList.add("社会主义发展史");
        mPagerTitleStrip = findViewById(R.id.study_main_pager_title_strip);
        func();
    }

    public void func() {
        mPagerTitleStrip = findViewById(R.id.study_main_pager_title_strip);
        int i = 1;
        View view = mPagerTitleStrip.getChildAt(i);
        TextView t = (TextView) view;
        t.setTextColor(Color.parseColor("#CC3333"));
    }

    public static class FragAdapter extends FragmentStatePagerAdapter {
        public static BaseFragment instantFragment;
        private List<BaseFragment> tabFragments;

        public FragAdapter(FragmentManager fm, List<BaseFragment> fragments) {
            super(fm);
            tabFragments = fragments;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            // 可以即时刷新Fragment
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            instantFragment = (BaseFragment) object;
            super.setPrimaryItem(container, position, object);
        }

        public BaseFragment getInstantFragment() {
            return instantFragment;
        }

        @Override
        public int getCount() {
            return tabFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) finish();
        BaseFragment fragment1 = adapter.getInstantFragment();
        fragment1.onKeyDown(keyCode, event);
        return true;
    }
}
