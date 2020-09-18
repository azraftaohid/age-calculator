package thegoodkid.aetate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.microsoft.fluentui.snackbar.Snackbar;
import com.microsoft.fluentui.widget.ProgressBar;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import thegoodkid.aetate.BuildConfig;
import thegoodkid.aetate.R;
import thegoodkid.aetate.utilities.CommonUtilities;

abstract class BaseAppActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String FEEDBACK_EMAIL = "feedback_email";
    private static final String FEEDBACK_SUBJECT = "feedback_message_header";

    private static boolean mHasInit = false;

    private FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!mHasInit) {
            initializeRemoteConfig();
            mHasInit = true;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String defTheme = getString(R.string.pref_key_default_theme);
        String theme = preferences.getString(getString(R.string.preference_key_theme), defTheme);
        if (defTheme.equals(theme)) setTheme(R.style.AppTheme);
        else setTheme(R.style.AppTheme_Neutral);

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_base_app, menu);

        Drawable feedbackIcon = CommonUtilities.createTintedDrawable(this, R.drawable.ic_fluent_bug_report_24_regular,
                R.attr.fluentuiToolbarIconColor);
        MenuItem feedbackItem = menu.findItem(R.id.action_send_feedback);
        if (feedbackIcon != null) {
            feedbackItem.setIcon(feedbackIcon);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_send_feedback) {
            dispatchFeedbackIntent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.preference_key_theme))) {
            recreate();
        }
    }

    private void initializeRemoteConfig() {
        mRemoteConfig.activate();
        mRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12))
                .build());

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(FEEDBACK_EMAIL, "azraftaohid@outlook.com");
        defaults.put(FEEDBACK_SUBJECT, "(Age Calculator) User Feedback");
        mRemoteConfig.setDefaultsAsync(defaults);

        mRemoteConfig.fetch().addOnSuccessListener(aVoid -> mRemoteConfig.activate());
    }

    private void dispatchFeedbackIntent() {
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        ProgressBar progressBar = new ProgressBar(this, null, 0, R.style.Widget_FluentUI_CircularProgress_Small);
        DrawableCompat.setTint(progressBar.getIndeterminateDrawable(), ContextCompat.getColor(this, R.color.snackbar_drawable));

        Snackbar.Companion.make(root, getText(R.string.gather_device_info), Snackbar.LENGTH_LONG, Snackbar.Style.REGULAR)
                .setCustomView(progressBar, Snackbar.CustomViewSize.SMALL)
                .show();

        String data = generateFeedbackMessage();

        Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
        feedbackIntent.setData(Uri.parse("mailto:"));

        String sender = mRemoteConfig.getString(FEEDBACK_EMAIL);
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, mRemoteConfig.getString(FEEDBACK_SUBJECT));
        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{sender});
        feedbackIntent.putExtra(Intent.EXTRA_TEXT, data);

        if (feedbackIntent.resolveActivity(getPackageManager()) != null)
            startActivity(feedbackIntent);
        else {
            Toast.makeText(this, getString(R.string.not_resolved_activity), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log("User has no email client to send bug report");
        }
    }

    @NotNull
    @Contract(pure = true)
    private String generateFeedbackMessage() {
        return "\n\nDescribe the issue or suggestion above this line" +
                "\nDevice: " +
                Build.MODEL +
                "\nAPI level: " +
                Build.VERSION.SDK_INT +
                "\nBuild version: " +
                BuildConfig.VERSION_CODE;
    }
}
