package thegoodcompany.aetate.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.microsoft.fluentui.theming.FluentUIContextThemeWrapper;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.databinding.DialogOnboardingBinding;

import static thegoodcompany.aetate.utilities.Logging.LOG_D;
import static thegoodcompany.aetate.utilities.Logging.LOG_V;

public class OnboardingDialog extends DialogFragment {
    private static final String LOG_TAG = OnboardingDialog.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG.concat(".performance");

    public OnboardingDialog() {

    }

    @NotNull
    static OnboardingDialog newInstance() {
        return new OnboardingDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        long start = 0;
        if (LOG_D) start = System.currentTimeMillis();

        if (LOG_V) Log.v(LOG_TAG, "Showing onboarding dialog");

        DialogOnboardingBinding binding = DialogOnboardingBinding.inflate(requireActivity().getLayoutInflater());
        binding.btGetStarted.setOnClickListener(v -> Objects.requireNonNull(OnboardingDialog.this.getDialog()).dismiss());

        FluentUIContextThemeWrapper wrapper = new FluentUIContextThemeWrapper(requireContext());
        wrapper.setTheme(R.style.Dialog_AppTheme);

        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        if (LOG_D)
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (System.currentTimeMillis() - start) +
                    " milliseconds to initialize onboarding dialog");

        return builder.setView(binding.getRoot()).create();
    }
}
