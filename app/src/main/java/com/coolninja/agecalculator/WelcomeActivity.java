package com.coolninja.agecalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void showBirthDayPickerFragment(View view) {
        DialogFragment userBirthdayPicker = UserBirthdayPickerDialog.newInstance();
        userBirthdayPicker.show(getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
    }

    public void finishWelcomeActivity(View view) {
        finish();
    }
}
