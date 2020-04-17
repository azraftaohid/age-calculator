package com.coolninja.agecalculator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.ProfileInfoDialog;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.codes.Request;

import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_DAY;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_MONTH;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_NAME;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_YEAR;

public class WelcomeActivity extends AppCompatActivity implements ProfileInfoDialog.OnProfileInfoSubmitListener {
    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private TextView mChoseNameTextView;
    private TextView mChoseDateTextView;
    private Button mSetDobButton;
    private Button mDoneButton;

    private String mName;
    private Birthday mDob;

    private ProfileInfoDialog mProfileInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Initializing Welcome Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSetDobButton = findViewById(R.id.bt_set_dob);
        mDoneButton = findViewById(R.id.bt_done);
        mChoseNameTextView = findViewById(R.id.tv_chosed_name);
        mChoseDateTextView = findViewById(R.id.tv_chosed_date);

        mProfileInfoDialog = ProfileInfoDialog.newInstance(Request.REQUEST_NEW_PROFILE_INFO);
    }

    public void showAddProfileDialog(View view) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Displaying the add profile dialog view");
        mProfileInfoDialog.show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    public void finishWelcomeActivity(View view) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Finishing Welcome Activity");

        Intent returnIntent = new Intent();

        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Putting " + mName + " name to the return intent");
        returnIntent.putExtra(EXTRA_NAME, mName);

        if (MainActivity.LOG_V)
            Log.v(LOG_TAG, "Putting " + mDob.get(Birthday.YEAR) + " name to the return intent");
        returnIntent.putExtra(EXTRA_YEAR, mDob.get(Birthday.YEAR));

        if (MainActivity.LOG_V)
            Log.v(LOG_TAG, "Putting " + mDob.get(Birthday.MONTH) + " name to the return intent");
        returnIntent.putExtra(EXTRA_MONTH, mDob.get(Birthday.MONTH));

        if (MainActivity.LOG_V)
            Log.v(LOG_TAG, "Putting " + mDob.get(Birthday.DAY) + " name to the return intent");
        returnIntent.putExtra(EXTRA_DAY, mDob.get(Birthday.DAY));

        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onProfileInfoSubmit(int requestCode, String name, Birthday dateOfBirth) {
        mName = name;
        mDob = dateOfBirth;

        mSetDobButton.setVisibility(View.GONE);
        mDoneButton.setVisibility(View.VISIBLE);
        mChoseNameTextView.setText(name);
        mChoseNameTextView.setVisibility(View.VISIBLE);
        mChoseDateTextView.setText(String.format(getString(R.string.long_date_format),
                dateOfBirth.getMonth().getShortName(), dateOfBirth.get(Birthday.DAY), dateOfBirth.get(Birthday.YEAR)));
        mChoseDateTextView.setVisibility(View.VISIBLE);
    }
}
