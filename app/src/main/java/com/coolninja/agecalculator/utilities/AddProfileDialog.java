package com.coolninja.agecalculator.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coolninja.agecalculator.R;

import java.util.Objects;

public class AddProfileDialog extends DialogFragment {
    private DialogInterface.OnClickListener mOnConfirm;

    public AddProfileDialog() {

    }

    public static AddProfileDialog newInstance(DialogInterface.OnClickListener onConfirmListener) {
        AddProfileDialog dialog = new AddProfileDialog();
        dialog.setOnConfirm(onConfirmListener);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_profile, null))
                .setPositiveButton(R.string.add_profile, mOnConfirm)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(AddProfileDialog.this.getDialog()).cancel();
                    }
                });

        return builder.create();
    }

    private void setOnConfirm(DialogInterface.OnClickListener onConfirm) {
        this.mOnConfirm = onConfirm;
    }
}
