package thegoodkid.aetate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import thegoodkid.aetate.databinding.DialogOnboardingBinding;

public class OnboardingDialog extends BaseAppDialogFragment {
    private static final String LOG_TAG = OnboardingDialog.class.getSimpleName();

    public OnboardingDialog() {

    }

    @NotNull
    static OnboardingDialog newInstance() {
        return new OnboardingDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogOnboardingBinding binding = DialogOnboardingBinding.inflate(requireActivity().getLayoutInflater());
        binding.btGetStarted.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

}
