package org.hvkz.hvkz.annotations;

import android.support.annotation.IdRes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value= RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnClick
{
    @IdRes int value() default 0;
}
