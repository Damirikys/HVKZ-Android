package org.hvkz.hvkz.app.annotations;

import android.support.annotation.LayoutRes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(value= RetentionPolicy.RUNTIME)
public @interface Layout
{
    @LayoutRes int value() default 0;
}
