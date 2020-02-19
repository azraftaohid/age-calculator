package com.coolninja.agecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity {
    private Calendar mDob;
    private Button mSetDobButton;
    private Button mDoneButton;
    private TextView mChoseDateTextView;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pref = getSharedPreferences(getString(R.string.user_pref_key), Context.MODE_PRIVATE);
        mDob = Calendar.getInstance();
        mSetDobButton = findViewById(R.id.bt_set_dob);
        mDoneButton = findViewById(R.id.bt_done);
        mChoseDateTextView = findViewById(R.id.tv_chosed_date);
    }

    public void showBirthDayPickerFragment(View view) {
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mDob.set(year, month, dayOfMonth);

                mSetDobButton.setVisibility(View.GONE);
                mDoneButton.setVisibility(View.VISIBLE);
                mChoseDateTextView.setText(String.format(getString(R.string.display_chose_date), getMonth(month), dayOfMonth, year));
                mChoseDateTextView.setVisibility(View.VISIBLE);
            }
        }, mDob.get(Calendar.YEAR), mDob.get(Calendar.MONTH), mDob.get(Calendar.DAY_OF_MONTH));

        datePicker.show();

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
