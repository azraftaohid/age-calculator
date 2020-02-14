package com.example.agecalculator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Objects;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private Button mSetDobButton;
    private Button mDoneButton;
    private TextView mChosedDateTextView;

    private SharedPreferences pref;

    public DatePickerFragment() {
        // Required empty public constructor
    }


    public static DatePickerFragment newInstance() {
        return new DatePickerFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        pref = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.pref_user_info_key), Context.MODE_PRIVATE);
        mSetDobButton = getActivity().findViewById(R.id.bt_set_dob);
        mDoneButton = getActivity().findViewById(R.id.bt_done);
        mChosedDateTextView = getActivity().findViewById(R.id.tv_chosed_date);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(Objects.requireNonNull(getActivity()), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(getString(R.string.saved_birth_year_key), year);
        editor.putInt(getString(R.string.saved_birth_month_key), month);
        editor.putInt(getString(R.string.saved_birth_day_key), dayOfMonth);

        editor.apply();

        mSetDobButton.setVisibility(View.GONE);
        mDoneButton.setVisibility(View.VISIBLE);
        mChosedDateTextView.setText(String.format(getString(R.string.display_chosed_date), getMonth(month), dayOfMonth, year));
        mChosedDateTextView.setVisibility(View.VISIBLE);
    }

    private String getMonth(int index) {
        String[] months = {
                "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"
        };

        return months[index];
    }
}
