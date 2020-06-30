package thegoodcompany.aetate.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.microsoft.fluentui.datetimepicker.DateTimePickerDialog;
import com.microsoft.fluentui.util.ThemeUtil;

import org.jetbrains.annotations.NotNull;
import org.threeten.bp.Duration;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Objects;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.databinding.DialogProfileInfoInputBinding;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.CommonUtilities;
import thegoodcompany.aetate.utilities.DateStringUtils;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;

import static android.app.Activity.RESULT_OK;
import static thegoodcompany.aetate.ui.ProfileInfoInputDialog.UseType.CREATION;
import static thegoodcompany.aetate.ui.ProfileInfoInputDialog.UseType.MODIFICATION;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidDateFormat;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidName;
import static thegoodcompany.aetate.utilities.Logging.LOG_D;
import static thegoodcompany.aetate.utilities.Logging.LOG_V;
import static thegoodcompany.aetate.utilities.Logging.LOG_W;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_PICKING_AVATAR_PERMISSIONS;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_PICK_AVATAR;

public class ProfileInfoInputDialog extends DialogFragment implements ExplanationDialog.OnExplain {
    private static final String ARGS_REQUEST_CODE = "args_request_code";

    private static final String LOG_TAG = ProfileInfoInputDialog.class.getSimpleName();
    private static final String LOG_PERFORMANCE = LOG_TAG + ".performance";
    private static final String TYPE_IMAGE = "image/*";
    private static final String ARGS_PROFILE_ID = "args_profile_id";
    private static final String ARGS_USE_TYPE = "args_use_type";
    DialogProfileInfoInputBinding binding;
    private UseType mUseType;
    @Nullable
    private Avatar mAvatar;
    private int mProfileId;

    private int mRequestCode;
    private boolean mHasTouchedNameEditTextBefore;
    private boolean mHasTouchedDobEditTextBefore;

    public ProfileInfoInputDialog() {
        //Required public constructor
    }

    @NotNull
    public static ProfileInfoInputDialog newInstance(int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARGS_USE_TYPE, CREATION.ordinal());
        args.putInt(ARGS_REQUEST_CODE, requestCode);

        ProfileInfoInputDialog dialog = new ProfileInfoInputDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NotNull
    public static ProfileInfoInputDialog newInstance(@NotNull Profile profile) {
        Bundle args = new Bundle();
        args.putInt(ARGS_USE_TYPE, MODIFICATION.ordinal());
        args.putInt(ARGS_PROFILE_ID, profile.getId());

        ProfileInfoInputDialog dialog = new ProfileInfoInputDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        long startMillis = 0;
        if (LOG_D) startMillis = System.currentTimeMillis();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogProfileInfoInputBinding.inflate(inflater);

        LayerDrawable layer = (LayerDrawable) requireContext().getDrawable(R.drawable.img_add_photo);
        if (layer != null) {
            Drawable addImageDrawable = layer.findDrawableByLayerId(R.id.add_photo_drawable);
            DrawableCompat.setTint(addImageDrawable, ContextCompat.getColor(requireContext(), R.color.add_photo_foreground));
        }
        binding.avatar.setImageDrawable(layer);

        Bundle args = getArguments();
        if (args == null) {
            IllegalArgumentException e = new IllegalArgumentException("Required arguments were not present");

            FirebaseCrashlytics.getInstance().recordException(e);
            throw e;
        }

        mRequestCode = args.getInt(ARGS_REQUEST_CODE);
        mUseType = UseType.values()[args.getInt(ARGS_USE_TYPE)];

        switch (mUseType) {
            case CREATION:
                binding.title.setText(R.string.create_profile);
                break;
            case MODIFICATION:
                binding.title.setText(R.string.modify);

                mProfileId = args.getInt(ARGS_PROFILE_ID);
                Profile profile = ProfileManager.getInstance(getContext()).getProfileById(mProfileId);

                binding.name.setText(profile.getName());
                binding.name.setSelection(profile.getName().length());

                Birthday birthday = profile.getBirthday();

                String enteredDateOfBirth = DateStringUtils
                        .formatDate(requireContext(), birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
                binding.dateOfBirth.setText(enteredDateOfBirth);
                binding.dateOfBirth.setSelection(enteredDateOfBirth.length());

                Avatar avatar = profile.getAvatar();
                if (avatar != null) binding.avatar.setImageDrawable(avatar.getCircularDrawable());
                break;
            default:
                throw new InvalidParameterException("Specified use type is not supported: " + mUseType);
        }

        binding.ivDatePicker.setOnClickListener(v -> showBirthdayPicker());
        binding.done.setOnClickListener(this::finish);

        View.OnFocusChangeListener onFocusChangeListener = (v, hasFocus) -> {
            if (!hasFocus) ensureInputValidity(v);
        };

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ensureInputValidity(null);
            }
        };

        binding.name.setOnFocusChangeListener(onFocusChangeListener);
        binding.name.addTextChangedListener(inputWatcher);
        binding.dateOfBirth.setOnFocusChangeListener(onFocusChangeListener);
        binding.dateOfBirth.addTextChangedListener(inputWatcher);
        binding.avatar.setOnClickListener(v -> dispatchAvatarPickerIntent());

        builder.setView(binding.getRoot());

        if (LOG_D) Log.d(LOG_PERFORMANCE, "It took " + (System.currentTimeMillis() - startMillis +
                " milliseconds to display Profile Info Input Dialog"));

        return builder.create();
    }

    private void dispatchAvatarPickerIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent pickAvatarIntent = new Intent();
            pickAvatarIntent.setType(TYPE_IMAGE);
            pickAvatarIntent.setAction(Intent.ACTION_GET_CONTENT);

            Intent chooserIntent = Intent.createChooser(pickAvatarIntent, "Choose from");

            if (chooserIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null)
                startActivityForResult(chooserIntent, REQUEST_PICK_AVATAR);
            else {
                Log.e(LOG_TAG, "Couldn't find any activity to pick image");
                Toast.makeText(getContext(), getString(R.string.not_resolved_activity), Toast.LENGTH_SHORT).show();
            }

        } else {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ExplanationDialog.newInstance(R.id.explanation_avatar_pick_permission, getString(R.string.explanation_permission_read_external_storage))
                            .show(getActivity().getSupportFragmentManager(), getString(R.string.explanation_dialog_tag));

                } else {
                    requestPermissionsRequiredForPickingAvatar();
                }

                return;
            }

            Intent pickAvatarIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (pickAvatarIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
                startActivityForResult(pickAvatarIntent, REQUEST_PICK_AVATAR);
            } else {
                Log.e(LOG_TAG, "Couldn't find any app to pick image");
                Toast.makeText(getContext(), getString(R.string.not_resolved_activity), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        ensureInputValidity(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Calendar start = null;
        if (LOG_D) {
            start = Calendar.getInstance();
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_AVATAR) {
                if (LOG_V) Log.v(LOG_TAG, "Received result from request pick avatar");

                assert data != null;
                Uri avatarUri = data.getData();
                if (avatarUri == null) {
                    Log.e(LOG_TAG, "Couldn't get avatar uri");
                    return;
                }

                if (LOG_V) Log.v(LOG_TAG, "Initiating avatar bitmap");
                Bitmap avatarBitmap = null;
                try {
                    ContentResolver resolver = Objects.requireNonNull(Objects.requireNonNull(getContext()).getApplicationContext()).getContentResolver();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        avatarBitmap = resolver.loadThumbnail(avatarUri, new Size(256, 256), null);

                    } else {
                        String[] projection = new String[]{
                                MediaStore.Images.ImageColumns._ID
                        };

                        //We're passing an Uri that points to only one file
                        //no need to pass selection, selectionArgs and sortOrder arguments
                        try (Cursor cursor = resolver.query(avatarUri, projection, null, null, null)) {
                            if (cursor == null) {
                                Log.e(LOG_TAG, "Returned cursor was null");
                                Toast.makeText(getContext(), getString(R.string.no_permission_or_moved), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (cursor.getCount() > 1 || cursor.getCount() == 0) {
                                Log.wtf(LOG_TAG, "Cursor found more than one item or none at all; proceeding w/ the first one");
                            }

                            cursor.moveToFirst();
                            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                            long id = cursor.getLong(idColumn);

                            avatarBitmap = MediaStore.Images.Thumbnails.getThumbnail(resolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                        } catch (CursorIndexOutOfBoundsException e) {
                            Log.e(LOG_TAG, "Couldn't point cursor to the specified image");
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }

                    }

                    if (avatarBitmap != null) {
                        if (LOG_V) Log.v(LOG_TAG, "Received avatar bitmap thumbnail");
                    } else {
                        Log.e(LOG_TAG, "Couldn't load avatar bitmap");
                        Toast.makeText(getContext(), getString(R.string.error_message_default), Toast.LENGTH_SHORT).show();
                        return;
                    }

                } catch (IOException | NoSuchMethodError | IllegalArgumentException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    e.printStackTrace();
                    Toast.makeText(getContext(), getString(R.string.error_message_default), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAvatar = new Avatar(getContext(), avatarBitmap);

                if (LOG_V) Log.v(LOG_TAG, "Setting the bitmap as avatar");
                binding.avatar.setImageDrawable(mAvatar.getCircularDrawable());
            }
        }

        if (LOG_D)
            Log.d(LOG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to process this activity result");
    }

    @Override
    public void onExplanationResult(int explanationCode, ExplanationDialog.ExplanationState state) {
        if (explanationCode == R.id.explanation_avatar_pick_permission)
            requestPermissionsRequiredForPickingAvatar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (LOG_D) Log.d(LOG_TAG, "Received request permissions result");

        if (requestCode == REQUEST_PICKING_AVATAR_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (LOG_V) Log.v(LOG_TAG, "Permission granted, dispatching avatar picker intent");
                dispatchAvatarPickerIntent();
            } else {
                if (LOG_W) Log.w(LOG_TAG, "User denied storage access permission");
            }
        }
    }

    public void onBirthdayPicked(@NotNull ZonedDateTime zonedDateTime, @NotNull Duration duration) {
        String strDate = DateStringUtils.formatDate(requireContext(), zonedDateTime.getYear(),
                zonedDateTime.getMonthValue() - 1, zonedDateTime.getDayOfMonth());

        binding.dateOfBirth.setText(strDate);
        binding.dateOfBirth.setSelection(strDate.length());
        binding.dateOfBirth.requestFocus();
    }

    private void requestPermissionsRequiredForPickingAvatar() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PICKING_AVATAR_PERMISSIONS);
    }

    private void showBirthdayPicker() {
        ZonedDateTime dateTime;
        Editable strDate = binding.dateOfBirth.getText();

        if (CommonUtilities.isValidDateFormat(strDate)) {
            dateTime = ZonedDateTime.of(DateStringUtils.getYear(strDate.toString()),
                    DateStringUtils.getMonth(strDate.toString()) + 1, DateStringUtils.getDay(strDate.toString()), 0, 0, 0, 0, ZoneId.systemDefault());
        } else {
            dateTime = ZonedDateTime.now();
        }

        DateTimePickerDialog picker = new DateTimePickerDialog(requireContext(), DateTimePickerDialog.Mode.DATE,
                DateTimePickerDialog.DateRangeMode.NONE, dateTime, Duration.ZERO);
        picker.setOnDateTimePickedListener(this::onBirthdayPicked);

        picker.show();
    }

    private void finish(View v) {
        String enteredName = binding.name.getText().toString();
        String enteredDateOfBirth = binding.dateOfBirth.getText().toString();
        int month = DateStringUtils.getMonth(enteredDateOfBirth);
        int day = DateStringUtils.getDay(enteredDateOfBirth);
        int year = DateStringUtils.getYear(enteredDateOfBirth);

        switch (mUseType) {
            case MODIFICATION:
                Profile profile = ProfileManager.getInstance(getContext()).getProfileById(mProfileId);
                profile.updateName(enteredName);
                profile.updateBirthday(year, month, day);

                if (mAvatar != null) profile.updateAvatar(mAvatar);
                break;
            case CREATION:
                if (getActivity() instanceof OnProfileInfoSubmitListener) {
                    Birthday birthday = new Birthday(year, month, day);
                    ((OnProfileInfoSubmitListener) getActivity()).onProfileInfoSubmit(mRequestCode, mAvatar, enteredName, birthday);
                }
        }

        this.dismiss();
    }

    private void ensureInputValidity(@Nullable View viewThatLostFocus) {
        if (viewThatLostFocus != null && !(mHasTouchedDobEditTextBefore && mHasTouchedNameEditTextBefore)) {
            if (viewThatLostFocus == binding.name) mHasTouchedNameEditTextBefore = true;
            else if (viewThatLostFocus == binding.dateOfBirth) mHasTouchedDobEditTextBefore = true;
        }

        //These are negatively true
        boolean isInValidName = !isValidName(binding.name.getText());
        boolean isInValidDate = !isValidDateFormat(binding.dateOfBirth.getText());

        if (isInValidName && isInValidDate && mHasTouchedNameEditTextBefore && mHasTouchedDobEditTextBefore) {
            binding.tvErrorMessage.setText(getString(R.string.error_message_invalid_name_and_dob));
        } else if (isInValidName && mHasTouchedNameEditTextBefore) {
            binding.tvErrorMessage.setText(getString(R.string.error_message_invalid_name));
        } else if (isInValidDate && mHasTouchedDobEditTextBefore) {
            binding.tvErrorMessage.setText(getString(R.string.error_message_invalid_dob, getString(R.string.set_birthday_hint)));
        } else {
            if (binding.tvErrorMessage.getVisibility() != View.GONE)
                binding.tvErrorMessage.setVisibility(View.GONE);

            if (isInValidName || isInValidDate) {
                disableDoneButton();
            } else {
                enableDoneButton();
            }

            return;
        }

        if (binding.tvErrorMessage.getVisibility() != View.VISIBLE)
            binding.tvErrorMessage.setVisibility(View.VISIBLE);
        disableDoneButton();
    }

    private void disableDoneButton() {
        binding.done.setEnabled(false);
        DrawableCompat.setTint(binding.done.getDrawable(), ThemeUtil.INSTANCE.getDisabledThemeAttrColor(requireContext(), R.attr.fluentuiCompoundButtonTintCheckedColor));
    }

    private void enableDoneButton() {
        binding.done.setEnabled(true);
        DrawableCompat.setTint(binding.done.getDrawable(), ThemeUtil.INSTANCE.getThemeAttrColor(requireContext(), R.attr.fluentuiCompoundButtonTintCheckedColor));
    }

    public enum UseType {
        MODIFICATION, CREATION
    }

    public interface OnProfileInfoSubmitListener {
        void onProfileInfoSubmit(int requestCode, @Nullable Avatar avatar, @NonNull String name, @NonNull Birthday dateOfBirth);
    }
}
