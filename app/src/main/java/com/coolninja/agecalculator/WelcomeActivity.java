package com.coolninja.agecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity {
    private TextView mChoseDateTextView;
    private Button mSetDobButton;
    private Button mDoneButton;

    private Calendar mDob;

    private SharedPreferences pref;
    private BirthdayPickerDialog mBirthdayPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSetDobButton = findViewById(R.id.bt_set_dob);
        mDoneButton = findViewById(R.id.bt_done);
        mChoseDateTextView = findViewById(R.id.tv_chosed_date);

        pref = getSharedPreferences(getString(R.string.user_pref_key), Context.MODE_PRIVATE);
        mDob = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mDob.set(year, month, dayOfMonth);

                mSetDobButton.setVisibility(View.GONE);
                mDoneButton.setVisibility(View.VISIBLE);
                mChoseDateTextView.setText(String.format(getString(R.string.display_chose_date), getMonth(month), dayOfMonth, year));
                mChoseDateTextView.setVisibility(View.VISIBLE);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    public void showBirthDayPickerFragment(View view) {
        mBirthdayPicker.show(getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
    }

    public void finishWelcomeActivity(View view) {
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(getString(R.string.birth_year_key), mDob.get(Calendar.YEAR));
        editor.putInt(getString(R.string.birth_month_key), mDob.get(Calendar.MONTH));
        editor.putInt(getString(R.string.birth_day_key), mDob.get(Calendar.DAY_OF_MONTH));

        editor.apply();

        setResult(RESULT_OK);
        finish();
    }

    private String getMonth(int index) {
        String[] months = {
                "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"
        };

        return months[index];
    }
}
