package com.coolninja.agecalculator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class BirthdayPickerDialog extends DialogFragment {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private DatePickerDialog mDatePickerDialog;
    private DatePickerDialog.OnDateSetListener mOnDateSetListener;

    public BirthdayPickerDialog() {
        // Required empty public constructor
    }


    public static BirthdayPickerDialog newInstance(DatePickerDialog.OnDateSetListener onDateSetListener, int year, int month, int day) {
        BirthdayPickerDialog datePicker =  new BirthdayPickerDialog();
        datePicker.setOnDateSetListener(onDateSetListener);

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
        if (mDatePickerDialog == null) {
            Bundle args = getArguments();
            assert args != null : "No arguments found. Don't use the default constructor to initiate BirthdayPicker object. " +
                    "Use static method instead";

            mDatePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), mOnDateSetListener,
                    args.getInt(YEAR), args.getInt(MONTH), args.getInt(DAY));
        }

        return mDatePickerDialog;
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.mOnDateSetListener = onDateSetListener;
    }
}
