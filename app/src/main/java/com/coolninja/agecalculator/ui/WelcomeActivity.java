package com.coolninja.agecalculator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Avatar;
import com.coolninja.agecalculator.utilities.ProfileInfoInputDialog;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.codes.Request;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_AVATAR_FILE_NAME;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_DAY;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_MONTH;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_NAME;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_YEAR;

public class WelcomeActivity extends AppCompatActivity implements ProfileInfoInputDialog.OnProfileInfoSubmitListener {
    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private TextView mChoseNameTextView;
    private TextView mChoseDateTextView;
    private Button mSetDobButton;
    private Button mDoneButton;

    private String mName;
    private Birthday mDob;
    @Nullable private Avatar mAvatar;

    private ProfileInfoInputDialog mProfileInfoInputDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOG_V) Log.v(LOG_TAG, "Initializing Welcome Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSetDobButton = findViewById(R.id.bt_set_dob);
        mDoneButton = findViewById(R.id.bt_done);
        mChoseNameTextView = findViewById(R.id.tv_chosed_name);
        mChoseDateTextView = findViewById(R.id.tv_chosed_date);

        mProfileInfoInputDialog = ProfileInfoInputDialog.newInstance(Request.REQUEST_NEW_PROFILE_INFO);
    }

    public void showAddProfileDialog(View view) {
        if (LOG_V) Log.v(LOG_TAG, "Displaying the add profile dialog view");
        mProfileInfoInputDialog.show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    public void finishWelcomeActivity(View view) {
        if (LOG_V) Log.v(LOG_TAG, "Finishing Welcome Activity");

        Intent returnIntent = new Intent();

        if (LOG_V) Log.v(LOG_TAG, "Putting " + mName + " name into the return intent");
        returnIntent.putExtra(EXTRA_NAME, mName);

        if (LOG_V) Log.v(LOG_TAG, "Putting " + mDob.get(Birthday.YEAR) + " name into the return intent");
        returnIntent.putExtra(EXTRA_YEAR, mDob.get(Birthday.YEAR));

        if (LOG_V) Log.v(LOG_TAG, "Putting " + mDob.get(Birthday.MONTH) + " name into the return intent");
        returnIntent.putExtra(EXTRA_MONTH, mDob.get(Birthday.MONTH));

        if (LOG_V) Log.v(LOG_TAG, "Putting " + mDob.get(Birthday.DAY) + " name into the return intent");
        returnIntent.putExtra(EXTRA_DAY, mDob.get(Birthday.DAY));

        if (LOG_V) if (mAvatar != null) {
            Log.v(LOG_TAG, "Putting " + mAvatar.getAvatarFileName() + " avatar name into the return intent");
            returnIntent.putExtra(EXTRA_AVATAR_FILE_NAME, mAvatar.getAvatarFileName());
        }

        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onProfileInfoSubmit(int requestCode, @Nullable Avatar avatar, String name, Birthday dateOfBirth) {
        mName = name;
        mDob = dateOfBirth;
        mAvatar = avatar;

        mSetDobButton.setVisibility(View.GONE);
        mDoneButton.setVisibility(View.VISIBLE);
        mChoseNameTextView.setText(name);
        mChoseNameTextView.setVisibility(View.VISIBLE);
        mChoseDateTextView.setText(String.format(getString(R.string.long_date_format),
                dateOfBirth.getMonth().getShortName(), dateOfBirth.get(Birthday.DAY), dateOfBirth.get(Birthday.YEAR)));
        mChoseDateTextView.setVisibility(View.VISIBLE);
    }
}
