package org.hvkz.hvkz.utils;

import android.util.DisplayMetrics;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Tools
{
    private static Calendar currentCalendar;
    private static String[] months = new String[] {
            "января",
            "февраля",
            "марта",
            "апреля",
            "мая",
            "июня",
            "июля",
            "августа",
            "сентября",
            "октября",
            "ноября",
            "декабря"
    };

    static {
        currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(System.currentTimeMillis());
    }

    public static String getDateStamp(long unix)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(unix);

        int message_day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
        int current_day_of_month = currentCalendar.get(Calendar.DAY_OF_MONTH);

        String message_time = getTimeFromUnix(unix);

        if (message_day_of_month == current_day_of_month)
            return "Сегодня в " + message_time;

        if (current_day_of_month - message_day_of_month == 1)
            return "Вчера в " + message_time;

        return message_day_of_month + " " + months[calendar.get(Calendar.MONTH)] + "  в " + message_time;
    }

    public static String getTimeFromUnix(long unix)
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(unix);

        return ((calendar.get(Calendar.HOUR_OF_DAY) < 10)
                ? "0"+calendar.get(Calendar.HOUR_OF_DAY)
                : calendar.get(Calendar.HOUR_OF_DAY))
                + ":"
                + ((calendar.get(Calendar.MINUTE) < 10)
                ? "0"+calendar.get(Calendar.MINUTE)
                : calendar.get(Calendar.MINUTE));
    }

    public static int dpToPx(DisplayMetrics metrics, int dp) {
        return Math.round(dp * (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(DisplayMetrics metrics, int px) {
        return Math.round(px / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Склоняем слова правильно
     * @param ed неизменяемая часть слова, которую нужно просклонять
     * @param a окончание для слова, в случае если число оканчивается на 1
     * @param b окончание для слова, в случае если число оканчивается на 2, 3 или 4
     * @param c окончание для слова, в случае если число оканчивается на 0, 5...9 и 11...19
     * @param n число, по которому идёт склонение
     * @return правильно просклонённое слово по числу
     */
    public static String padezh(String ed, String a, String b, String c, int n) {
        if (n < 0) {
            n = -n;
        }
        int last = n % 100;
        if (last > 10 && last < 21) {
            return ed + c;
        }
        last = n % 10;
        if (last == 0 || last > 4) {
            return ed + c;
        }
        if (last == 1) {
            return ed + a;
        }
        if (last < 5) {
            return ed + b;
        }
        return ed + c;
    }

    public static <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }
}
