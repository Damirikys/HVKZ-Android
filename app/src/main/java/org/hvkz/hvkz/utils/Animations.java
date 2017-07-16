package org.hvkz.hvkz.utils;


import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;

public class Animations
{
    private static Animation slideUp = AnimationUtils.loadAnimation(HVKZApp.getAppContext(),
            R.anim.slide_up_animation);
    private static Animation slideDown = AnimationUtils.loadAnimation(HVKZApp.getAppContext(),
            R.anim.slide_down_animation);
    private static Animation fadeIn = AnimationUtils.loadAnimation(HVKZApp.getAppContext(),
            R.anim.fadein);
    private static Animation fadeOut = AnimationUtils.loadAnimation(HVKZApp.getAppContext(),
            R.anim.fadeout);

    public static Animation slideUp()
    { return slideUp; }

    public static Animation slideDown()
    { return slideDown; }

    public static Animation fadeIn()
    { return fadeIn; }

    public static Animation fadeOut()
    { return fadeOut; }

}
