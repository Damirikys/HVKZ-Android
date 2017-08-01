package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.uimodels.ViewBinder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.hvkz.hvkz.utils.Tools.dpToPx;

class MenuItemViewHolder extends RecyclerView.ViewHolder
{
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

    @BindView(R.id.view_pager)
    private ViewPager viewPager;

    @BindView(R.id.menu_card)
    private CardView cardView;

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

    MenuItemViewHolder(View itemView) {
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

        if (getAdapterPosition() == 0) {
            dayView.setText(R.string.today);
            cardView.setCardBackgroundColor(Color.parseColor("#fffeee"));
            cardView.setRadius(0);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT) {{
                setMargins(0, 0, 0, 0);
            }});
        }


        if (getAdapterPosition() == 6) {
            int bottom = dpToPx(context().getResources().getDisplayMetrics(), 16);
            int top = dpToPx(context().getResources().getDisplayMetrics(), 8);
            cardView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT) {{
                //noinspection SuspiciousNameCombination
                setMargins(bottom, top, bottom, bottom);
            }});
        }
    }

    private Context context() {
        return viewPager.getContext();
    }
}
