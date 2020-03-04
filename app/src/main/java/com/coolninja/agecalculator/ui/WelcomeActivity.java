package com.coolninja.agecalculator.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coolninja.agecalculator.utilities.AddProfileDialog;
import com.coolninja.agecalculator.R;

import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity implements AddProfileDialog.OnNewProfileAddedListener {
    private TextView mChoseNameTextView;
    private TextView mChoseDateTextView;
    private Button mSetDobButton;
    private Button mDoneButton;

    private String mName;
    private Calendar mDob;

    private AddProfileDialog mAddProfileDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSetDobButton = findViewById(R.id.bt_set_dob);
        mDoneButton = findViewById(R.id.bt_done);
        mChoseNameTextView = findViewById(R.id.tv_chosed_name);
        mChoseDateTextView = findViewById(R.id.tv_chosed_date);

        mDob = Calendar.getInstance();

        mAddProfileDialog = AddProfileDialog.newInstance();
    }

    public void showAddProfileDialog(View view) {
        mAddProfileDialog.show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    public void finishWelcomeActivity(View view) {
        Intent returnIntent = new Intent();

        returnIntent.putExtra(MainActivity.EXTRA_NAME, mName);
        returnIntent.putExtra(MainActivity.EXTRA_YEAR, mDob.get(Calendar.YEAR));
        returnIntent.putExtra(MainActivity.EXTRA_MONTH, mDob.get(Calendar.MONTH));
        returnIntent.putExtra(MainActivity.EXTRA_DAY, mDob.get(Calendar.DAY_OF_MONTH));

        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onSubmit(String name, Calendar dateOfBirth) {
        mName = name;
        mDob = dateOfBirth;

        mSetDobButton.setVisibility(View.GONE);
        mDoneButton.setVisibility(View.VISIBLE);
        mChoseNameTextView.setText(name);
        mChoseNameTextView.setVisibility(View.VISIBLE);
        mChoseDateTextView.setText(String.format(getString(R.string.display_chose_date),
                getMonth(dateOfBirth.get(Calendar.DAY_OF_MONTH)), dateOfBirth.get(Calendar.MONTH), dateOfBirth.get(Calendar.YEAR)));
        mChoseDateTextView.setVisibility(View.VISIBLE);
    }

    private String getMonth(int index) {
        String[] months = {
                "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"
        };

        return months[index];
    }
}
