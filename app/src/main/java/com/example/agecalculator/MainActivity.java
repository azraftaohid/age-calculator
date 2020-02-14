package com.example.agecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Calendar mDateOfBirth;

    private TextView mCurrentAgeTextView;

    //TODO Add a try with different DOB text view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(getString(R.string.pref_user_info_key), MODE_PRIVATE);

        int birthDay = pref.getInt(getString(R.string.saved_birth_day_key), Integer.parseInt(getString(R.string.saved_birth_day_default_key)));
        if (birthDay == Integer.parseInt(getString(R.string.saved_birth_day_default_key))) {
            launchWelcomeActivity();
        }

        mCurrentAgeTextView = findViewById(R.id.tv_current_age);

        int birthMonth = pref.getInt(getString(R.string.saved_birth_month_key), Integer.parseInt(getString(R.string.saved_birth_month_default_key)));
        int birthYear = pref.getInt(getString(R.string.saved_birth_year_key), Integer.parseInt(getString(R.string.saved_birth_year_default_key)));

        mDateOfBirth = Calendar.getInstance();
        mDateOfBirth.set(birthYear, birthMonth, birthDay);

        //TODO Convert Millis to year, month and day
        mCurrentAgeTextView.setText(String.valueOf(Calendar.getInstance().getTimeInMillis() - mDateOfBirth.getTimeInMillis()));
    }

    void launchWelcomeActivity() {
        Intent initialDoneIntent = new Intent(this, WelcomeActivity.class);
        startActivity(initialDoneIntent);
    }
}
