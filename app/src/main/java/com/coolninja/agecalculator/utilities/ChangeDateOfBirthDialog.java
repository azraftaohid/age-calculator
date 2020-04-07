package com.coolninja.agecalculator.utilities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;

import java.util.Objects;

public class ChangeDateOfBirthDialog extends DialogFragment {private ProfileManagerInterface.onProfileUpdatedListener mOnProfileUpdatedListener;
    private EditText mNewDobEditText;
    private BirthdayPickerDialog mBirthdayPicker;

    private ChangeDateOfBirthDialog() {

    }

    public static ChangeDateOfBirthDialog newInstance(ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener, int previousYear, int previousMonth, int previousDay) {
        final ChangeDateOfBirthDialog changeDateOfBirthDialog = new ChangeDateOfBirthDialog();
        changeDateOfBirthDialog.mOnProfileUpdatedListener = onProfileUpdatedListener;

        changeDateOfBirthDialog.mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                changeDateOfBirthDialog.mNewDobEditText.setText(String.format(changeDateOfBirthDialog.requireActivity()
                        .getString(R.string.short_date_format), month + 1, dayOfMonth, year));
            }
        }, previousYear, previousMonth, previousDay);
        return changeDateOfBirthDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_change_dob, null);

        mNewDobEditText = root.findViewById(R.id.et_new_date_of_birth);
        mNewDobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBirthdayPicker.show(requireActivity().getSupportFragmentManager(), getString(R.string.birthday_picker_tag));
            }
        });

        mNewDobEditText.setShowSoftInputOnFocus(true);
        mNewDobEditText.requestFocus();

        builder.setView(root)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] mmddyyyy = mNewDobEditText.getText().toString().split("/");

                        int month = Integer.parseInt(mmddyyyy[0]) - 1;
                        int day = Integer.parseInt(mmddyyyy[1]);
                        int year = Integer.parseInt(mmddyyyy[2]);

                        mOnProfileUpdatedListener.onProfileDateOfBirthChanged(Integer.parseInt(getString(R.string.default_error_value)), year, month, day, null);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(ChangeDateOfBirthDialog.this.getDialog()).cancel();
                    }
                });

        return builder.create();
    }
}
