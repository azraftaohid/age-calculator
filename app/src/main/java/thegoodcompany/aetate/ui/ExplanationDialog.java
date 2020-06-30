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
    private static final String ARGS_MESSAGE = "com.thegoodcompany.aetate.explanationdialog.MESSAGE";
    private static final String ARGS_CODE = "com.thegoodcompany.aetate.explanationdialog.CODE";

    private int mCode;

    public ExplanationDialog() {

    }

    @NonNull
    public static ExplanationDialog newInstance(int explanationCode, @NonNull String message) {
        ExplanationDialog fragment = new ExplanationDialog();

        Bundle args = new Bundle();
        args.putString(ARGS_MESSAGE, message);
        args.putInt(ARGS_CODE, explanationCode);

        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        Bundle data = getArguments();
        if (data != null) {
            mCode = data.getInt(ARGS_CODE);
            builder.setMessage(data.getString(ARGS_MESSAGE));
        }

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
