package thegoodkid.aetate.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import thegoodkid.aetate.R;
import thegoodkid.aetate.databinding.ActivitySettingsBinding;
import thegoodkid.aetate.utilities.CommonUtilities;

public class SettingsActivity extends BaseAppActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupAppbar();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.container.getId(), new SettingsFragment())
                .commit();
    }

    private void setupAppbar() {
        setSupportActionBar(binding.appBar.getToolbar());
        binding.appBar.getToolbar().setTitle(R.string.label_settings);
        binding.appBar.getToolbar().setNavigationIcon(CommonUtilities.createNavigationBackDrawable(this));
        binding.appBar.getToolbar().setNavigationOnClickListener(view -> onBackPressed());
    }
}
