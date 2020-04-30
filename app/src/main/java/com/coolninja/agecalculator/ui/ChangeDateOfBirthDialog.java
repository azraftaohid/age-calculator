package com.coolninja.agecalculator.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.CommonUtilities;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;

import java.util.Objects;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;

public class ChangeDateOfBirthDialog extends DialogFragment {
    private static final String LOG_TAG = ChangeDateOfBirthDialog.class.getSimpleName();

    private ProfileManagerInterface.updatable mUpdatable;
    private EditText mNewDobEditText;
    private BirthdayPickerDialog mBirthdayPicker;

    private ChangeDateOfBirthDialog() {

    }

    public static ChangeDateOfBirthDialog newInstance(ProfileManagerInterface.updatable updatable) {
        if (LOG_V) Log.v(LOG_TAG, "Initializing a new instance of change date of birth dialog");

        final ChangeDateOfBirthDialog changeDateOfBirthDialog = new ChangeDateOfBirthDialog();
        changeDateOfBirthDialog.mUpdatable = updatable;

        Birthday currentBday = updatable.getBirthday();
        changeDateOfBirthDialog.mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                changeDateOfBirthDialog.mNewDobEditText.setText(String.format(changeDateOfBirthDialog.requireActivity()
                        .getString(R.string.short_date_format), month + 1, dayOfMonth, year));
            }
        }, currentBday.get(Birthday.YEAR), currentBday.get(Birthday.MONTH), currentBday.get(Birthday.DAY));
        return changeDateOfBirthDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (LOG_V) Log.v(LOG_TAG, "Showing change date of birth dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_change_dob, null);

        mNewDobEditText = root.findViewById(R.id.et_new_date_of_birth);
        ImageView dobPickerImageView = root.findViewById(R.id.iv_new_dob_picker);

        dobPickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBirthdayPicker.show(requireActivity().getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
            }
        });

        builder.setView(root)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] mmddyyyy = mNewDobEditText.getText().toString().split("/");

                        int month = Integer.parseInt(mmddyyyy[0]) - 1;
                        int day = Integer.parseInt(mmddyyyy[1]);
                        int year = Integer.parseInt(mmddyyyy[2]);

                        mUpdatable.updateBirthday(year, month, day);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(ChangeDateOfBirthDialog.this.getDialog()).cancel();
                    }
                });

        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null)
            CommonUtilities.showSoftKeyboard(window, mNewDobEditText);
        else Log.e(LOG_TAG, "Couldn't get dialog window");

        return dialog;
    }
}
