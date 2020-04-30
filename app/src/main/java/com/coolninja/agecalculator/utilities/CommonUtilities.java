package com.coolninja.agecalculator.utilities;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.Calendar;

@SuppressWarnings({"unused"})
public class CommonUtilities {
    private static final String LOG_TAG = CommonUtilities.class.getSimpleName();

    public static boolean hideSoftKeyboard(@NonNull Activity activity, View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)) {
                Log.e(LOG_TAG, "Couldn't hide soft input");
                return false;
            }
            return true;
        }

        Log.e(LOG_TAG, "Couldn't get input method manager");
        return false;
    }

    public static boolean showSoftKeyboard(@NonNull Activity activity, View view) {
        if (view.requestFocus()) Log.w(LOG_TAG, "Couldn't request focus");

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)) return true;

            Log.e(LOG_TAG, "Couldn't show soft input");
            return false;
        }

        Log.e(LOG_TAG, "Couldn't get input method manager");
        return false;
    }

    public static void showSoftKeyboard(@NonNull Window window, View view) {
        if (!view.requestFocus()) Log.w(LOG_TAG, "Couldn't request focus");
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static ImageView generateCustomAccessoryView(Context context, int drawableId) {
        final ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(context.getDrawable(drawableId));
        imageView.setClickable(true);
        imageView.setFocusable(true);
        return imageView;
    }

    public static boolean isValidDateFormat(Editable date) {
        String[] dates = date.toString().split("/");
        Calendar c = Calendar.getInstance();
        int thisDay = c.get(Calendar.DAY_OF_MONTH);
        int thisMonth = c.get(Calendar.MONTH) + 1;
        int thisYear = c.get(Calendar.YEAR);

        try {
            int day = Integer.parseInt(dates[1]);
            int month = Integer.parseInt(dates[0]);
            int year = Integer.parseInt(dates[2]);

            Month enumMonth = Month.values()[month - 1];
            if (Age.isLeapYear(year) && month == 2) {
                if (day < 1 || day > 29) return false;
            } else if (day < 1 || day > enumMonth.getNumberOfDays()) {
                return false;
            }

            if (month > 12) return false;
            if (year > thisYear) return false;

            if (year == thisYear) {
                if (month > thisMonth) return false;
                if (month == thisMonth && day > thisDay) return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isValidName(Editable name) {
        return name.length() > 0;
    }
}
