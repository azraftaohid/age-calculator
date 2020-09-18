package thegoodkid.aetate.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import thegoodkid.aetate.R;

public class ExplanationDialog extends BaseAppDialogFragment {
    private static final String ARGS_MESSAGE = "MESSAGE";
    private static final String ARGS_CODE = "CODE";

    private int mCode;
    private String mMessage;

    public ExplanationDialog() {

    }

    @NonNull
    public static ExplanationDialog newInstance(int explanationCode, @NonNull String message) {
        Bundle args = new Bundle();
        args.putString(ARGS_MESSAGE, message);
        args.putInt(ARGS_CODE, explanationCode);

        ExplanationDialog dialog = new ExplanationDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle data = getArguments();
        if (data != null) {
            mCode = data.getInt(ARGS_CODE);
            mMessage = data.getString(ARGS_MESSAGE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage(mMessage);
        builder.setNeutralButton(R.string.dismiss, (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (getContext() instanceof OnExplain)
            ((OnExplain) getContext()).onExplanationResult(mCode, ExplanationState.UNDERSTOOD);

        super.onDismiss(dialog);
    }

    enum ExplanationState {
        UNDERSTOOD
    }

    interface OnExplain {
        void onExplanationResult(int explanationCode, ExplanationState state);
    }
}
