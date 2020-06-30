package thegoodcompany.aetate.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.widget.ImageView;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.microsoft.fluentui.util.ThemeUtil;
import com.microsoft.fluentui.util.ThemeUtilsKt;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import thegoodcompany.aetate.R;
import thegoodkid.common.utils.CalendarUtils;

public class CommonUtilities {
    @NonNull
    public static ImageView createCustomView(@NonNull Context context, @DrawableRes int drawableId, @AttrRes int tint) {
        ImageView view = new ImageView(context);
        view.setImageDrawable(getTintedDrawable(context, drawableId, tint));

        return view;
    }

    public static Drawable getTintedDrawable(@NotNull Context context, @DrawableRes int drawableId, @AttrRes int tint) {
        Drawable drawable = context.getDrawable(drawableId);
        if (drawable != null) drawable.setTint(ThemeUtil.INSTANCE.getThemeAttrColor(context, tint));

        return drawable;
    }

    public static Drawable createNavigationBackDrawable(@NonNull Context context) {
        return ThemeUtilsKt.getTintedDrawable(context, R.drawable.ic_fluent_arrow_left_24_regular,
                ThemeUtil.INSTANCE.getThemeAttrColor(context, R.attr.fluentuiToolbarIconColor));
    }

    public static boolean isValidDateFormat(@NotNull Editable date) {
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
            if (CalendarUtils.isLeapYear(year) && month == 2) {
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

    public static boolean isValidName(@NotNull Editable name) {
        return name.length() > 0;
    }

    @NotNull
    public static int[] createCalendarAppendants() {
        int[] appendants = new int[CalendarUtils.getFieldsCount()];

        appendants[CalendarUtils.DAY] = R.plurals.suffix_day;
        appendants[CalendarUtils.MONTH] = R.plurals.suffix_month;
        appendants[CalendarUtils.YEAR] = R.plurals.suffix_year;
        appendants[CalendarUtils.MINUTE] = R.plurals.suffix_minute;
        appendants[CalendarUtils.HOUR] = R.plurals.suffix_hour;
        appendants[CalendarUtils.SECOND] = R.plurals.suffix_second;

        return appendants;
    }

    @NonNull
    public static long[] calculateDaysLeftForBirthday(int relativeToYear, int relativeToMonth, int relativeToDay, @NonNull Birthday birthday, int mode) {
        long[] daysLeftForBirthday;
        if (birthday.getMonthValue() < relativeToMonth || (birthday.getMonthValue() == relativeToMonth && birthday.getDayOfMonth() < relativeToDay)) {
            daysLeftForBirthday = CalendarUtils.calculateIntervals(relativeToYear, relativeToMonth, relativeToDay,
                    relativeToYear + 1, birthday.getMonthValue(), birthday.getDayOfMonth(), mode);
        } else {
            daysLeftForBirthday = CalendarUtils.calculateIntervals(relativeToYear, relativeToMonth, relativeToDay,
                    relativeToYear, birthday.getMonthValue(), birthday.getDayOfMonth(), mode);
        }

        return daysLeftForBirthday;
    }
}
