package com.coolninja.agecalculator.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

@SuppressWarnings({"unused", "WeakerAccess"})
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
}
