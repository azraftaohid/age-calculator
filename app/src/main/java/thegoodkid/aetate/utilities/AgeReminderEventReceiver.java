package thegoodkid.aetate.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import thegoodcompany.common.utils.CalendarUtils;
import thegoodcompany.common.utils.StringUtils;
import thegoodkid.aetate.R;
import thegoodkid.aetate.ui.ProfileDetailsActivity;
import thegoodkid.aetate.utilities.profilemanagement.Profile;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManager;

import static thegoodkid.aetate.utilities.codes.Extra.EXTRA_PROFILE_ID;

public class AgeReminderEventReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AgeReminderEventReceiver.class.getSimpleName();

    private static final String REMINDER_NOTIFICATION_CHANNEL = "reminder_notification_channel";
    private static final String GROUP_KEY_REMINDERS = "thegoodkid.aetate.utilities.AgeReminderEventReceiver.NOTIFICATION_GROUP_REMINDERS";
    private static final int SUMMERY_ID = 1800;

    private NotificationManagerCompat mNotificationManager;

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Reporter.reportDebug(LOG_TAG, "Broadcast received");
        Profile profile;
        int profileId = intent.getIntExtra(EXTRA_PROFILE_ID, -1);
        if (profileId == -1 || (profile = ProfileManager.getInstance(context).getProfileById(profileId)) == null) {
            Reporter.reportWarning(LOG_TAG, "Skipping reminder. Profile not found [id: " + profileId + "]");
            return;
        }

        createNotificationChannel(context);
        dispatchNotification(context, profile);
    }

    private void dispatchNotification(@NonNull Context context, @NonNull Profile profile) {
        Calendar c = Calendar.getInstance();
        Birthday birthday = profile.getBirthday();
        Avatar avatar = profile.getAvatar();
        long[] age = CalendarUtils.calculateIntervals(birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth(),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), CalendarUtils.MODE_YEAR_MONTH_DAY);

        String title = context.getString(R.string.title_age_reminder, profile.getName(), Long.valueOf(age[CalendarUtils.YEAR]).intValue());
        String description = context.getString(R.string.info_age_reminder,
                StringUtils.joinUnless(context.getResources(), age,
                        new int[]{R.plurals.suffix_year, R.plurals.suffix_month, R.plurals.suffix_day},
                        ", ", 0));

        Intent displayDetailsIntent = new Intent(context, ProfileDetailsActivity.class);
        displayDetailsIntent.putExtra(EXTRA_PROFILE_ID, profile.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, profile.getId(),
                displayDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Builder builder = new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_age_reminder)
                .setColor(ContextCompat.getColor(context, R.color.fluentui_communication_blue))
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setGroup(GROUP_KEY_REMINDERS)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_age_reminder)
                        .setColor(ContextCompat.getColor(context, R.color.fluentui_communication_blue))
                        .setContentTitle(context.getString(R.string.title_age_reminder_public))
                        .setContentText(context.getString(R.string.info_age_reminder_public))
                        .setContentIntent(pendingIntent)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setGroup(GROUP_KEY_REMINDERS)
                        .setAutoCancel(true).build());

        if (avatar != null) builder.setLargeIcon(avatar.getBitmap());

        Notification summeryNotification = new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_age_reminder)
                .setContentText(context.getString(R.string.title_age_reminder_summery))
                .setContentText(context.getString(R.string.info_age_reminder_summery))
                .setGroup(GROUP_KEY_REMINDERS)
                .setGroupSummary(true)
                .build();

        mNotificationManager.notify(profile.getId(), builder.build());
        mNotificationManager.notify(SUMMERY_ID, summeryNotification);
    }

    private void createNotificationChannel(@NonNull Context context) {
        mNotificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(REMINDER_NOTIFICATION_CHANNEL,
                    context.getString(R.string.label_reminder_notification_channel),
                    NotificationManager.IMPORTANCE_HIGH);

            channel.enableLights(true);
            channel.enableVibration(false);
            channel.setDescription(context.getString(R.string.description_reminder_notification_channel));
            Reporter.reportDebug(LOG_TAG, "Creating notification channel");
            mNotificationManager.createNotificationChannel(channel);
        } else {
            Reporter.reportDebug(LOG_TAG, "Skipping notification channel");
        }
    }
}
