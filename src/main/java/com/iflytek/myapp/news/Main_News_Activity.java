package com.iflytek.myapp.news;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻模块的主类，这个类就是新闻模块的主界面，顶部的导航栏就属于新闻模块，下面的每个标题对应的新闻是一个个碎片（fragment）。
 * IOS与安卓实现方法不同，不用细看。
 */
public class Main_News_Activity extends BaseActivity {
    ViewPager viewPager;
    static List<String> titleList;
    PagerTitleStrip mPagerTitleStrip;
    FragAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cur_news_list_activity);
        viewPager = findViewById(R.id.news_main_view_pager);
        List<Fragment_News_List> fragments = new ArrayList<>();
        fragments.add(Fragment_News_List.newInstance("推荐"));
        fragments.add(Fragment_News_List.newInstance("时政焦点"));
        fragments.add(Fragment_News_List.newInstance("科技焦点"));
        fragments.add(Fragment_News_List.newInstance("军事焦点"));
        fragments.add(Fragment_News_List.newInstance("国际焦点"));
        fragments.add(Fragment_News_List.newInstance("港澳台最新"));
        String province = getSharedPreferences("message", MODE_PRIVATE).getString("province", "安徽");
        fragments.add(Fragment_News_List.newInstance("#" + province));
        adapter = new FragAdapter(this, getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        titleList = new ArrayList<>();
        titleList.add("推荐");
        titleList.add("时政焦点");
        titleList.add("科技焦点");
        titleList.add("军事焦点");
        titleList.add("国际焦点");
        titleList.add("港澳台最新");
        titleList.add(province);
        func();
    }

    public void func() {
        mPagerTitleStrip = findViewById(R.id.news_main_pager_title_strip);
        int i = 1;
        View view = mPagerTitleStrip.getChildAt(i);
        TextView t = (TextView) view;
        t.setTextColor(Color.parseColor("#CC3333"));
    }


    public static class FragAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private FragmentManager manager;
        public static Fragment_News_List instantFragment;
        private List<Fragment_News_List> tabFragments;

        public FragAdapter(Context context, FragmentManager fm, List<Fragment_News_List> fragments) {
            super(fm);
            manager = fm;
            mContext = context;
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
            instantFragment = (Fragment_News_List) object;
            super.setPrimaryItem(container, position, object);
        }

        public Fragment_News_List getInstantFragment() {
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
        Fragment_News_List fragment1 = adapter.getInstantFragment();
        fragment1.onKeyDown(keyCode, event);
        return true;
    }
}
