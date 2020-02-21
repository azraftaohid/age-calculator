package com.coolninja.agecalculator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class BirthdayPickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private DatePickerDialog mDatePickerDialog;

    public BirthdayPickerDialog() {
        // Required empty public constructor
    }


    public static BirthdayPickerDialog newInstance(){
        return new BirthdayPickerDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        mDatePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), this, year, month, day);

        return mDatePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(getString(R.string.birth_year_key), year);
        resultIntent.putExtra(getString(R.string.birth_month_key), month);
        resultIntent.putExtra(getString(R.string.birth_day_key), dayOfMonth);

        if (getActivity() != null) {
            if (getActivity() instanceof BirthDayPicker) {
                ((BirthDayPicker) getActivity()).onBirthdayPick(Integer.valueOf(getString(R.string.dob_request)), RESULT_OK, resultIntent);
            }
        } else if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, resultIntent);
        }
    }

    public DatePickerDialog getDatePickerDialog() {
        return mDatePickerDialog;
    }
}
