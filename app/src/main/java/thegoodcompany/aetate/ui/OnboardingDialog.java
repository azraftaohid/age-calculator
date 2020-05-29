package thegoodcompany.aetate.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

import thegoodcompany.aetate.R;

import static thegoodcompany.aetate.ui.MainActivity.LOG_D;
import static thegoodcompany.aetate.ui.MainActivity.LOG_V;

public class OnboardingDialog extends DialogFragment {
    private static final String LOG_TAG = OnboardingDialog.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG.concat(".performance");

    public OnboardingDialog() {

    }

    static OnboardingDialog newInstance() {
        return new OnboardingDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar start = null;
        if (LOG_D) start = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Showing onboarding dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        @SuppressLint("InflateParams") View root = requireActivity().getLayoutInflater().inflate(R.layout.dialog_onboarding, null);

        root.findViewById(R.id.bt_get_started).setOnClickListener(v -> Objects.requireNonNull(OnboardingDialog.this.getDialog()).dismiss());

        if (LOG_D)
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to initiate onboarding dialog");

        return builder.setView(root).create();
    }
}
