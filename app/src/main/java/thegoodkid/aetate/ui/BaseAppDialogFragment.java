package thegoodkid.aetate.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import com.microsoft.fluentui.util.ThemeUtil;

import thegoodkid.aetate.R;
import thegoodkid.aetate.databinding.LayoutDialogTitleBinding;

abstract class BaseAppDialogFragment extends DialogFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NORMAL, R.style.Dialog_AppTheme);
    }

    protected void initTitleContainer(@NonNull LayoutDialogTitleBinding binding) {
        Drawable doneDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_checkmark_24_regular);
        if (doneDrawable != null) binding.done.setImageDrawable(DrawableCompat.wrap(doneDrawable));

        binding.done.setOnClickListener(view -> onFinish());
    }

    protected void onFinish() {
        dismiss();
    }

    protected void displayError(@StringRes int error, @NonNull LayoutDialogTitleBinding binding) {
        displayError(getString(error), binding);
    }

    protected void displayError(String error, @NonNull LayoutDialogTitleBinding binding) {
        binding.tvErrorMessage.setText(error);
        if (binding.tvErrorMessage.getVisibility() != View.VISIBLE)
            binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    protected void hideError(@NonNull LayoutDialogTitleBinding binding) {
        if (binding.tvErrorMessage.getVisibility() != View.GONE)
            binding.tvErrorMessage.setVisibility(View.GONE);
    }

    protected void disableDoneButton(@NonNull LayoutDialogTitleBinding binding) {
        binding.done.setEnabled(false);
        DrawableCompat.setTint(binding.done.getDrawable(), ThemeUtil.INSTANCE.getDisabledThemeAttrColor(requireContext(), R.attr.fluentuiCompoundButtonTintCheckedColor));
    }

    protected void enableDoneButton(@NonNull LayoutDialogTitleBinding binding) {
        binding.done.setEnabled(true);
        DrawableCompat.setTint(binding.done.getDrawable(), ThemeUtil.INSTANCE.getThemeAttrColor(requireContext(), R.attr.fluentuiCompoundButtonTintCheckedColor));
    }
}
