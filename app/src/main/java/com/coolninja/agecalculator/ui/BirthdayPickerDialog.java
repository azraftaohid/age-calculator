package com.coolninja.agecalculator.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;

public class BirthdayPickerDialog extends DialogFragment {
    private static final String LOG_TAG = BirthdayPickerDialog.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG.concat(".performance");

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private DatePickerDialog mDatePickerDialog;
    private DatePickerDialog.OnDateSetListener mOnDateSetListener;

    public BirthdayPickerDialog() {
        // Required empty public constructor
    }

    @SuppressWarnings("WeakerAccess")
    public static BirthdayPickerDialog newInstance(DatePickerDialog.OnDateSetListener onDateSetListener, int year, int month, int day) {
        BirthdayPickerDialog datePicker = new BirthdayPickerDialog();
        datePicker.mOnDateSetListener = onDateSetListener;

        Bundle date = new Bundle();
        date.putInt(YEAR, year);
        date.putInt(MONTH, month);
        date.putInt(DAY, day);

        datePicker.setArguments(date);

        return datePicker;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        if (mDatePickerDialog == null) {
            Bundle args = getArguments();
            assert args != null : "No arguments found. Don't use the default constructor to initiate BirthdayPicker object. " +
                    "Use the static method instead";

            mDatePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), mOnDateSetListener,
                    args.getInt(YEAR), args.getInt(MONTH), args.getInt(DAY));
        }

        mDatePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        if (LOG_D)
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to birthday picker dialog");

        return mDatePickerDialog;
    }

}
