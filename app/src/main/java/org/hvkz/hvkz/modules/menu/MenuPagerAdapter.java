package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.BezierPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.hvkz.hvkz.R;

import java.util.List;

class MenuPagerAdapter extends PagerAdapter
{
    private final String[] titleMap;
    private final String[] timeMap;

    private Context context;
    private ViewPager viewPager;
    private CommonNavigatorAdapter navigatorAdapter;

    private List<String> data;

    MenuPagerAdapter(ViewPager viewPager, List<String> data) {
        this.viewPager = viewPager;
        this.context = viewPager.getContext();
        this.navigatorAdapter = new MenuNavigatorAdapter();
        this.data = data;

        this.titleMap = context.getResources().getStringArray(R.array.menu_titles);
        this.timeMap = context.getResources().getStringArray(R.array.menu_times);
    }

    CommonNavigator getCommonNavigator() {
        CommonNavigator navigator = new CommonNavigator(context);
        navigator.setAdapter(navigatorAdapter);
        return navigator;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_pager_item, collection, false);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView timeView = (TextView) view.findViewById(R.id.time);
        timeView.setText(timeMap[position]);
        textView.setSelected(true);
        textView.setText(data.get(position));
        textView.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(data.get(position))
                .create()
                .show());

        collection.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleMap[position];
    }

    private class MenuNavigatorAdapter extends CommonNavigatorAdapter
    {
        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public IPagerTitleView getTitleView(Context context, final int index) {
            SimplePagerTitleView simplePagerTitleView = new MenuPagerTitleView(context);
            simplePagerTitleView.setText(getPageTitle(index));
            simplePagerTitleView.setTextSize(18);
            simplePagerTitleView.setNormalColor(Color.GRAY);
            simplePagerTitleView.setSelectedColor(Color.BLACK);
            simplePagerTitleView.setOnClickListener(v -> viewPager.setCurrentItem(index));
            return simplePagerTitleView;
        }

        @Override
        public IPagerIndicator getIndicator(Context context) {
            BezierPagerIndicator indicator = new BezierPagerIndicator(context);
            indicator.setColors(
                    Color.parseColor("#fcde64"),
                    Color.parseColor("#bfccde"),
                    Color.parseColor("#50c777"),
                    Color.parseColor("#e68e50"),
                    Color.parseColor("#ff3333")
            );

            return indicator;
        }
    }
}