package com.coolninja.agecalculator.utilities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;

import java.util.Objects;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_I;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;

public class RenameDialog extends DialogFragment {
    private static final String LOG_TAG = RenameDialog.class.getSimpleName();

    private ProfileManagerInterface.updatable mUpdatable;
    private EditText mNewNameEditText;
    private TextView mInvalidNameInputTextView;

    private RenameDialog() {

    }

    @SuppressWarnings("WeakerAccess")
    public static RenameDialog newInstance(ProfileManagerInterface.updatable updatable) {
        if (LOG_V) Log.v(LOG_TAG, "Initializing a new instance of Rename Dialog");

        RenameDialog renameDialog = new RenameDialog();
        renameDialog.mUpdatable = updatable;

        return renameDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (LOG_V) Log.v(LOG_TAG, "Displaying rename dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_rename, null);

        mNewNameEditText = root.findViewById(R.id.et_new_name);
        mInvalidNameInputTextView = root.findViewById(R.id.tv_invalid_name_input);

        String currentName = mUpdatable.getName();
        mNewNameEditText.setText(currentName);
        mNewNameEditText.setSelection(currentName.length());

        mNewNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mNewNameEditText.getText().length() == 0) {
                    mInvalidNameInputTextView.setVisibility(View.VISIBLE);
                    if (LOG_I) Log.i(LOG_TAG, "User has left the new name field empty");

                    if (LOG_V)
                        Log.v(LOG_TAG, "Adding a text change listener to the new name field");
                    mNewNameEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            if (s.length() > 0) {
                                mInvalidNameInputTextView.setVisibility(View.GONE);
                                if (LOG_I) Log.i(LOG_TAG, "New name is valid now");
                            }
                        }
                    });
                }
            }
        });

        mNewNameEditText.setShowSoftInputOnFocus(true);
        mNewNameEditText.requestFocus();

        builder.setView(root)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUpdatable.updateName(mNewNameEditText.getText().toString());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(RenameDialog.this.getDialog()).cancel();
                    }
                });

        return builder.create();
    }
}
