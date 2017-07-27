package org.hvkz.hvkz.utils;


import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.hvkz.hvkz.R;

public final class Animations
{
    public static Animation slideUp(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.slide_up_animation);
    }

    public static Animation slideDown(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.slide_down_animation);
    }

    public static Animation fadeIn(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fadein);
    }

    public static Animation fadeOut(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fadeout);
    }
}
