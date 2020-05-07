package thegoodcompany.aetate.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import thegoodcompany.aetate.R;

import static thegoodcompany.aetate.ui.MainActivity.LOG_D;
import static thegoodcompany.aetate.ui.MainActivity.LOG_V;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidDateFormat;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidName;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_DAY;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_MONTH;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_NAME;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_YEAR;

public class WelcomeActivity extends AppCompatActivity {
    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG.concat(".performance");

    private Button mDoneButton;
    private EditText mNameEditText;
    private EditText mDobEditText;
    private TextView mErrorTextView;

    private boolean mHasTouchedNameEditTextBefore;
    private boolean mHasTouchedDobEditTextBefore;

    private BirthdayPickerDialog mBirthdayPickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Initializing Welcome Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mNameEditText = findViewById(R.id.et_name_first_profile);
        mDobEditText = findViewById(R.id.et_dob_first_profile);
        mDoneButton = findViewById(R.id.bt_done);
        mErrorTextView = findViewById(R.id.tv_error_message);

        View.OnFocusChangeListener onInputFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) ensureInputValidity(v);
            }
        };

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ensureInputValidity(null);
            }
        };

        mNameEditText.addTextChangedListener(inputWatcher);
        mNameEditText.setOnFocusChangeListener(onInputFocusChangeListener);

        mDobEditText.addTextChangedListener(inputWatcher);
        mDobEditText.setOnFocusChangeListener(onInputFocusChangeListener);

        Calendar c = Calendar.getInstance();
        mBirthdayPickerDialog = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String strDob = getString(R.string.short_date_format, month + 1, dayOfMonth, year);
                mDobEditText.setText(strDob);
                mDobEditText.requestFocus();
                mDobEditText.setSelection(strDob.length());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));


        if (LOG_D)
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to show welcome screen");
    }

    public void showBirthdayPicker(View view) {
        mBirthdayPickerDialog.show(getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
    }

    @Deprecated
    public void showBirthdayPicker0(View view) {
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String strDob = getString(R.string.short_date_format, month + 1, dayOfMonth, year);
                mDobEditText.setText(strDob);
                mDobEditText.requestFocus();
                mDobEditText.setSelection(strDob.length());
            }
        };

        Editable currentText = mDobEditText.getText();
        if (isValidDateFormat(currentText)) {
            String[] mmddyyyy = currentText.toString().split("/");

            int month = Integer.parseInt(mmddyyyy[0]) - 1;
            int day = Integer.parseInt(mmddyyyy[1]);
            int year = Integer.parseInt(mmddyyyy[2]);

            BirthdayPickerDialog.newInstance(onDateSetListener, year, month, day).show(getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
        } else {
            Calendar c = Calendar.getInstance();
            BirthdayPickerDialog.newInstance(onDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                    .show(getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
        }
    }

    public void finishWelcomeActivity(View view) {
        if (LOG_V) Log.v(LOG_TAG, "Finishing Welcome Activity");

        String[] mmddyyyy = mDobEditText.getText().toString().split("/");

        int month = Integer.parseInt(mmddyyyy[0]) - 1;
        int day = Integer.parseInt(mmddyyyy[1]);
        int year = Integer.parseInt(mmddyyyy[2]);

        Intent returnIntent = new Intent();

        if (LOG_V) Log.v(LOG_TAG, "Putting name into the return intent");
        returnIntent.putExtra(EXTRA_NAME, mNameEditText.getText().toString());

        if (LOG_V) Log.v(LOG_TAG, "Putting name into the return intent");
        returnIntent.putExtra(EXTRA_YEAR, year);

        if (LOG_V) Log.v(LOG_TAG, "Putting name into the return intent");
        returnIntent.putExtra(EXTRA_MONTH, month);

        if (LOG_V) Log.v(LOG_TAG, "Putting name into the return intent");
        returnIntent.putExtra(EXTRA_DAY, day);

        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void ensureInputValidity(@Nullable View viewThatLostFocus) {
        if (viewThatLostFocus != null && !(mHasTouchedDobEditTextBefore && mHasTouchedNameEditTextBefore)) {
            if (viewThatLostFocus == mNameEditText) mHasTouchedNameEditTextBefore = true;
            else if (viewThatLostFocus == mDobEditText) mHasTouchedDobEditTextBefore = true;
        }

        //These are negatively true
        boolean isInValidName = !isValidName(mNameEditText.getText());
        boolean isInValidDate = !isValidDateFormat(mDobEditText.getText());

        if (isInValidName && isInValidDate && mHasTouchedNameEditTextBefore && mHasTouchedDobEditTextBefore) {
            mErrorTextView.setText(getString(R.string.error_message_invalid_name_and_dob));
        } else if (isInValidName && mHasTouchedNameEditTextBefore) {
            mErrorTextView.setText(getString(R.string.error_message_invalid_name));
        } else if (isInValidDate && mHasTouchedDobEditTextBefore) {
            mErrorTextView.setText(getString(R.string.error_message_invalid_dob, getString(R.string.set_birthday_hint)));
        } else {
            if (mErrorTextView.getVisibility() != View.GONE)
                mErrorTextView.setVisibility(View.GONE);
            mDoneButton.setEnabled(!(isInValidName || isInValidDate));
            return;
        }

        if (mErrorTextView.getVisibility() != View.VISIBLE)
            mErrorTextView.setVisibility(View.VISIBLE);
        mDoneButton.setEnabled(false);
    }

}
