package thegoodcompany.aetate.utilities;

import android.util.Log;

import thegoodcompany.aetate.BuildConfig;

public class Logging {
    public static final boolean COLLECT_REPORT = true;
    public static final boolean LOG_PERFORMANCE = true;
    private static final int LOG_LEVEL = BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR;
    public static final boolean LOG_V = LOG_LEVEL <= Log.VERBOSE;
    public static final boolean LOG_D = LOG_LEVEL <= Log.DEBUG;
    public static final boolean LOG_I = LOG_LEVEL <= Log.INFO;
    public static final boolean LOG_W = LOG_LEVEL <= Log.WARN;
}
