package com.coolninja.agecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity implements BirthDayPicker {
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
        mBirthdayPicker = BirthdayPickerDialog.newInstance();
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

    @Override
    public void onBirthdayPick(int requestCode, int resultCode, Intent data) {
        if (requestCode == Integer.valueOf(getString(R.string.dob_request)) && resultCode == RESULT_OK) {
            int year = data.getIntExtra(getString(R.string.birth_year_key), Integer.valueOf(getString(R.string.default_birth_year_key)));
            int month = data.getIntExtra(getString(R.string.birth_month_key), Integer.valueOf(getString(R.string.default_birth_month_key)));
            int dayOfMonth = data.getIntExtra(getString(R.string.birth_day_key), Integer.valueOf(getString(R.string.default_birth_day_key)));

            mDob.set(year, month, dayOfMonth);

            mSetDobButton.setVisibility(View.GONE);
            mDoneButton.setVisibility(View.VISIBLE);
            mChoseDateTextView.setText(String.format(getString(R.string.display_chose_date), getMonth(month), dayOfMonth, year));
            mChoseDateTextView.setVisibility(View.VISIBLE);
        }
    }
}
