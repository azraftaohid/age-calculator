package thegoodkid.aetate.utilities;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import thegoodkid.aetate.BuildConfig;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

public class Reporter {
    public static final boolean COLLECT_REPORT = false;
    private static final int LOG_LEVEL = BuildConfig.DEBUG ? VERBOSE : ERROR;
    public static final boolean LOG_V = LOG_LEVEL <= VERBOSE;
    public static final boolean LOG_D = LOG_LEVEL <= DEBUG;
    public static final boolean LOG_PERFORMANCE = LOG_D;
    public static final boolean LOG_I = LOG_LEVEL <= INFO;
    public static final boolean LOG_W = LOG_LEVEL <= WARN;

    public static void reportMessage(@Nullable String tag, @NonNull String message) {
        if (LOG_I) android.util.Log.i(tag, message);
        if (COLLECT_REPORT)
            FirebaseCrashlytics.getInstance().log((tag == null ? "" : (tag + ": ")) + message);
    }

    public static void reportVerbose(@NonNull String tag, @NonNull String message) {
        if (LOG_V) android.util.Log.v(tag, message);
        if (COLLECT_REPORT) FirebaseCrashlytics.getInstance().log(tag + ": " + message);
    }

    public static void reportDebug(@NonNull String Tag, @NonNull String message) {
        if (LOG_D) Log.d(Tag, message);
    }

    public static void reportInfo(@NonNull String tag, @NonNull String message) {
        if (LOG_I) android.util.Log.i(tag, message);
        if (COLLECT_REPORT) FirebaseCrashlytics.getInstance().log(tag + ": " + message);
    }

    public static void reportWarning(@NonNull String tag, @NonNull String message) {
        if (LOG_W) android.util.Log.w(tag, message);
        if (COLLECT_REPORT) FirebaseCrashlytics.getInstance().log(tag + ": " + message);
    }

    public static void reportError(@NonNull Throwable tr) {
        tr.printStackTrace();
        if (COLLECT_REPORT) FirebaseCrashlytics.getInstance().recordException(tr);
    }

    public static void reportError(@NonNull String tag, @NonNull String message) {
        Log.e(tag, message);
        if (COLLECT_REPORT) FirebaseCrashlytics.getInstance().log(tag + ": " + message);
    }

    public static void reportError(@NonNull String tag, @NonNull String message, @Nullable Throwable e) {
        Log.e(tag, message, e);
        if (COLLECT_REPORT) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.log(tag + ": " + message);
            if (e != null) crashlytics.recordException(e);
        }
    }
}
