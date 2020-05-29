package thegoodcompany.aetate.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import thegoodcompany.aetate.R;

public class ExplanationDialog extends DialogFragment {
    private static final String MESSAGE = "com.thegoodcompany.aetate.explanationdialog.MESSAGE";

    private DialogInterface.OnDismissListener mOnDismissListener;

    public ExplanationDialog() {

    }

    public static ExplanationDialog newInstance(@NonNull String message, DialogInterface.OnDismissListener onDismissListener) {
        ExplanationDialog fragment = new ExplanationDialog();
        fragment.mOnDismissListener = onDismissListener;

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);

        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        Bundle data = getArguments();
        if (data != null) {
            builder.setMessage(data.getString(MESSAGE));
        }

        builder.setNeutralButton(R.string.dismiss, (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
        else super.onDismiss(dialog);
    }
}
