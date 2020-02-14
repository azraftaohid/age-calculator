package com.example.agecalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.prefs.Preferences;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SharedPreferences pref = getSharedPreferences(getString(R.string.pref_user_info_key), MODE_PRIVATE);
    }

    public void showBirthDayPickerFragment(View view) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), getString(R.string.tag_date_picker_fragment));
    }

    public void finishWelcomeActivity(View view) {
        finish();
    }
}
