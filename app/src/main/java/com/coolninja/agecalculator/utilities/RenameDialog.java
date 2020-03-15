package com.coolninja.agecalculator.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class RenameDialog extends DialogFragment {
    private ProfileManagerInterface.onProfileUpdatedListener mOnProfileUpdatedListener;
    private EditText mNewNameEditText;
    private TextView mInvalidNameInputTextView;

    private RenameDialog() {

    }
    public static RenameDialog newInstance(ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener) {
        RenameDialog renameDialog = new RenameDialog();
        renameDialog.mOnProfileUpdatedListener = onProfileUpdatedListener;

        return renameDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_rename, null);

        mNewNameEditText = root.findViewById(R.id.et_new_name);
        mInvalidNameInputTextView = root.findViewById(R.id.tv_invalid_name_input);

        mNewNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mNewNameEditText.getText().length() == 0) {
                    mInvalidNameInputTextView.setVisibility(View.VISIBLE);

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
                        mOnProfileUpdatedListener.onProfileNameChanged(Integer.parseInt(getString(R.string.default_error_value)), mNewNameEditText.getText().toString(), null);
                    }})
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(RenameDialog.this.getDialog()).cancel();
                    }
                });

        return builder.create();
    }
}
