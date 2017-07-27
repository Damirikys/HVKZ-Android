package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.models.ViewBinder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MenuItemViewHolder extends RecyclerView.ViewHolder
{
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

    @BindView(R.id.view_pager)
    private ViewPager viewPager;

    @BindView(R.id.dayofweek)
    private TextView dayView;

    @BindView(R.id.proteins)
    private TextView proteinsView;

    @BindView(R.id.fats)
    private TextView fatsView;

    @BindView(R.id.carbohydrate)
    private TextView carbohydrateView;

    @BindView(R.id.magic_indicator)
    private MagicIndicator magicIndicator;

    public MenuItemViewHolder(View itemView) {
        super(itemView);
        ViewBinder.handle(this, itemView);
    }

    public void hold(MenuStorage.MenuItem item) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, getAdapterPosition());

        String weekDay = dayFormat.format(calendar.getTime());

        dayView.setText(weekDay);
        proteinsView.setText(String.valueOf(item.bzu.get(0)));
        fatsView.setText(String.valueOf(item.bzu.get(1)));
        carbohydrateView.setText(String.valueOf(item.bzu.get(2)));

        MenuPagerAdapter menuPagerAdapter = new MenuPagerAdapter(viewPager, item.dayArray);
        magicIndicator.setNavigator(menuPagerAdapter.getCommonNavigator());
        viewPager.setAdapter(menuPagerAdapter);

        ViewPagerHelper.bind(magicIndicator, viewPager);
    }

    private Context context() {
        return viewPager.getContext();
    }
}
