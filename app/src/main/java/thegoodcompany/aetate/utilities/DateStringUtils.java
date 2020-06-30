package thegoodcompany.aetate.utilities;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import thegoodcompany.aetate.R;

public class DateStringUtils {
    @NotNull
    public static String getHint(@NotNull Context context) {
        return context.getString(R.string.set_birthday_hint);
    }

    /**
     * Month values are zero based; so January is 0
     */
    @NotNull
    public static String formatDate(@NotNull Context context, int year, int month, int day) {
        return context.getString(R.string.short_date_format, month + 1, day, year);
    }

    public static int getYear(@NotNull String formattedDate) {
        String[] mmddyyyy = formattedDate.split("/");
        return Integer.parseInt(mmddyyyy[2]);
    }

    public static int getMonth(@NotNull String formattedDate) {
        String[] mmddyyyy = formattedDate.split("/");
        return Integer.parseInt(mmddyyyy[0]) - 1;
    }

    public static int getDay(@NotNull String formattedDate) {
        String[] mmddyyyy = formattedDate.split("/");
        return Integer.parseInt(mmddyyyy[1]);
    }


}
