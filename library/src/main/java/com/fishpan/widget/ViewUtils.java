package com.fishpan.widget;

import android.content.Context;

/**
 * Created by yupan on 17/6/24.
 */
public class ViewUtils {
    public static float dp2px(Context context, float dp){
        return context.getApplicationContext().getResources().getDisplayMetrics().density * dp;
    }
}
