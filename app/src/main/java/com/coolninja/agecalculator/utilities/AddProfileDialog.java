package com.coolninja.agecalculator.utilities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coolninja.agecalculator.R;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddProfileDialog extends DialogFragment {
    private OnNewProfileAddedListener mOnNewProfileAdded;

    private EditText mDobEditText;
    private EditText mNameEditText;
    private String mEnteredName;
    private String mEnteredDateOfBirth;

    private BirthdayPickerDialog mBirthdayPicker;

    public interface OnNewProfileAddedListener {
        void onSubmit(String name, Calendar dateOfBirth);
    }

    public AddProfileDialog() {

    }

    public static AddProfileDialog newInstance() {
        final AddProfileDialog addProfileDialog = new AddProfileDialog();

        Calendar c = Calendar.getInstance();
        BirthdayPickerDialog birthdayPickerDialog = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                addProfileDialog.mDobEditText.setText(String.format(Locale.ENGLISH,
                        Objects.requireNonNull(addProfileDialog.getActivity()).getString(R.string.short_date_format),
                        month + 1, dayOfMonth, year));
                addProfileDialog.mDobEditText.setSelection(addProfileDialog.mDobEditText.length());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        addProfileDialog.setBirthdayPicker(birthdayPickerDialog);

        return addProfileDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View mRoot = inflater.inflate(R.layout.dialog_add_profile, null);

        mDobEditText = mRoot.findViewById(R.id.et_dob_new_profile);
        mNameEditText = mRoot.findViewById(R.id.et_name_new_profile);
        final TextView errorMessageTextView = mRoot.findViewById(R.id.tv_error_choosing_name);

        if (mEnteredName != null) mNameEditText.setText(mEnteredName);
        if (mEnteredDateOfBirth != null) mDobEditText.setText(mEnteredDateOfBirth);

        mDobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBirthdayPicker();
            }
        });

        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mNameEditText.getText().length() < 1) {
                    errorMessageTextView.setVisibility(View.VISIBLE);

                    mNameEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() > 0) {
                                errorMessageTextView.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

        builder.setView(mRoot)
                .setPositiveButton(R.string.add_profile, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEnteredName = mNameEditText.getText().toString();
                        mEnteredDateOfBirth = mDobEditText.getText().toString();
                        String[] mmddyyyy = mEnteredDateOfBirth.split("/");

                        int month = Integer.parseInt(mmddyyyy[0]) - 1;
                        int day = Integer.parseInt(mmddyyyy[1]);
                        int year = Integer.parseInt(mmddyyyy[2]);

                        Calendar c = Calendar.getInstance();
                        c.set(year, month, day);
                        mOnNewProfileAdded.onSubmit(mEnteredName, c);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(AddProfileDialog.this.getDialog()).cancel();
                    }
                });

        return builder.create();
    }

    private void setBirthdayPicker(BirthdayPickerDialog birthdayPicker) {
        mBirthdayPicker = birthdayPicker;
    }

    private void showBirthdayPicker() {
        mBirthdayPicker.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                getString(R.string.birthday_picker_tag));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mOnNewProfileAdded = (OnNewProfileAddedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + ": must implement OnNewProfileAddedListener");
        }
    }
}
