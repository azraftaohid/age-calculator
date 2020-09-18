package thegoodkid.aetate.ui;

import android.Manifest;
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
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.microsoft.fluentui.datetimepicker.DateTimePickerDialog;

import org.jetbrains.annotations.NotNull;
import org.threeten.bp.Duration;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Objects;

import thegoodkid.aetate.R;
import thegoodkid.aetate.databinding.DialogProfileInfoInputBinding;
import thegoodkid.aetate.utilities.Avatar;
import thegoodkid.aetate.utilities.Birthday;
import thegoodkid.aetate.utilities.CommonUtilities;
import thegoodkid.aetate.utilities.DateStringUtils;
import thegoodkid.aetate.utilities.Reporter;
import thegoodkid.aetate.utilities.codes.Request;
import thegoodkid.aetate.utilities.profilemanagement.Profile;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManager;

import static android.app.Activity.RESULT_OK;
import static thegoodkid.aetate.ui.ProfileInfoInputDialog.UseType.CREATION;
import static thegoodkid.aetate.ui.ProfileInfoInputDialog.UseType.MODIFICATION;
import static thegoodkid.aetate.utilities.CommonUtilities.mutateAndTintDrawable;

public class ProfileInfoInputDialog extends BaseAppDialogFragment implements ExplanationDialog.OnExplain {
    private static final String LOG_TAG = ProfileInfoInputDialog.class.getSimpleName();
    private static final String LOG_PERFORMANCE = LOG_TAG + ".performance";
    private static final String TYPE_IMAGE = "image/*";
    private static final String ARGS_REQUEST_CODE = "args_request_code";
    private static final String ARGS_PROFILE_ID = "args_profile_id";
    private static final String ARGS_USE_TYPE = "args_use_type";
    private static final String AVATAR_FILE_NAME = "avatar_file_name";
    private static final String DATE_PICKER_VISIBILITY = "is_date_picker_visible";

    private DialogProfileInfoInputBinding binding;
    private UseType mUseType;
    @Nullable
    private Avatar mAvatar;
    private int mProfileId;

    private int mRequestCode;
    private boolean isDatePickerShown;
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
    public static ProfileInfoInputDialog modifyProfile(@NotNull Profile profile) {
        Bundle args = new Bundle();
        args.putInt(ARGS_USE_TYPE, MODIFICATION.ordinal());
        args.putInt(ARGS_PROFILE_ID, profile.getId());

        ProfileInfoInputDialog dialog = new ProfileInfoInputDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) throw new IllegalArgumentException("Required arguments were not present");

        mRequestCode = args.getInt(ARGS_REQUEST_CODE);
        mUseType = UseType.values()[args.getInt(ARGS_USE_TYPE, CREATION.ordinal())];
        mProfileId = args.getInt(ARGS_PROFILE_ID, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogProfileInfoInputBinding.inflate(inflater, container, false);
        initTitleContainer(binding.titleContainer);

        LayerDrawable layer = (LayerDrawable) ContextCompat.getDrawable(requireContext(), R.drawable.img_add_photo);
        if (layer != null) {
            Drawable addImageDrawable = layer.findDrawableByLayerId(R.id.add_photo_drawable);
            addImageDrawable = mutateAndTintDrawable(addImageDrawable, ContextCompat.getColor(requireContext(), R.color.add_photo_foreground));
            layer.setDrawableByLayerId(R.id.add_photo_drawable, addImageDrawable);
        }
        binding.avatar.setImageDrawable(layer);

        switch (mUseType) {
            case CREATION:
                binding.titleContainer.title.setText(R.string.create_profile);
                break;
            case MODIFICATION:
                binding.titleContainer.title.setText(R.string.modify);
                Profile profile = ProfileManager.getInstance(getContext()).getProfileById(mProfileId);

                binding.name.setText(profile.getName());
                binding.name.setSelection(profile.getName().length());

                Birthday birthday = profile.getBirthday();

                String enteredDateOfBirth = DateStringUtils
                        .formatDateShort(requireContext(), birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
                binding.dateOfBirth.setText(enteredDateOfBirth);
                binding.dateOfBirth.setSelection(enteredDateOfBirth.length());

                Avatar avatar = profile.getAvatar();
                if (avatar != null) binding.avatar.setImageDrawable(avatar.getCircularDrawable());
                break;
            default:
                throw new InvalidParameterException("Specified use type is not supported: " + mUseType);
        }

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

        binding.datePicker.setOnClickListener(v -> showBirthdayPicker());
        binding.avatar.setOnClickListener(v -> dispatchAvatarPickerIntent());

        binding.name.setOnFocusChangeListener(onFocusChangeListener);
        binding.dateOfBirth.setOnFocusChangeListener(onFocusChangeListener);

        binding.name.addTextChangedListener(inputWatcher);
        binding.dateOfBirth.addTextChangedListener(inputWatcher);

        if (savedInstanceState != null) {
            String avatarFileName = savedInstanceState.getString(AVATAR_FILE_NAME);
            if (avatarFileName != null) {
                mAvatar = Avatar.retrieveAvatar(requireContext(), avatarFileName);
                binding.avatar.setImageDrawable(mAvatar.getCircularDrawable());
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getBoolean(DATE_PICKER_VISIBILITY, false)) {
            showBirthdayPicker();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ensureInputValidity(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(DATE_PICKER_VISIBILITY, isDatePickerShown);
        if (mAvatar != null) outState.putString(AVATAR_FILE_NAME, mAvatar.getAvatarFileName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Request.REQUEST_PICK_AVATAR) {
                if (Reporter.LOG_V) Log.v(LOG_TAG, "Received result from request pick avatar");

                assert data != null;
                Uri avatarUri = data.getData();
                if (avatarUri == null) {
                    Log.e(LOG_TAG, "Couldn't get avatar uri");
                    return;
                }

                if (Reporter.LOG_V) Log.v(LOG_TAG, "Initiating avatar bitmap");
                Bitmap avatarBitmap = null;
                try {
                    ContentResolver resolver = Objects.requireNonNull(requireContext().getApplicationContext()).getContentResolver();
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
                            Reporter.reportError(LOG_TAG, "Couldn't point cursor to the specified image", e);
                        }

                    }

                    if (avatarBitmap != null) {
                        Reporter.reportVerbose(LOG_TAG, "Received avatar bitmap thumbnail");
                    } else {
                        Reporter.reportError(LOG_TAG, "Couldn't load avatar bitmap");
                        Toast.makeText(getContext(), getString(R.string.error_message_default), Toast.LENGTH_SHORT).show();
                        return;
                    }

                } catch (IOException | NoSuchMethodError | IllegalArgumentException e) {
                    Reporter.reportError(e);
                    Toast.makeText(getContext(), getString(R.string.error_message_default), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAvatar = new Avatar(requireContext(), avatarBitmap);

                Reporter.reportVerbose(LOG_TAG, "Setting the bitmap as avatar");
                binding.avatar.setImageDrawable(mAvatar.getCircularDrawable());
            }
        }
    }

    @Override
    public void onExplanationResult(int explanationCode, ExplanationDialog.ExplanationState state) {
        if (explanationCode == R.id.explanation_avatar_pick_permission)
            requestPermissionsRequiredForPickingAvatar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Reporter.LOG_D) Log.d(LOG_TAG, "Received request permissions result");

        if (requestCode == Request.REQUEST_PICKING_AVATAR_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Reporter.LOG_V)
                    Log.v(LOG_TAG, "Permission granted, dispatching avatar picker intent");
                dispatchAvatarPickerIntent();
            } else {
                if (Reporter.LOG_W) Log.w(LOG_TAG, "User denied storage access permission");
            }
        }
    }

    public void onBirthdayPicked(@NotNull ZonedDateTime zonedDateTime, @NotNull Duration duration) {
        isDatePickerShown = false;
        String strDate = DateStringUtils.formatDateShort(requireContext(), zonedDateTime.getYear(),
                zonedDateTime.getMonthValue() - 1, zonedDateTime.getDayOfMonth());

        binding.dateOfBirth.setText(strDate);
        binding.dateOfBirth.setSelection(strDate.length());
        binding.dateOfBirth.requestFocus();
    }

    private void dispatchAvatarPickerIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent pickAvatarIntent = new Intent();
            pickAvatarIntent.setType(TYPE_IMAGE);
            pickAvatarIntent.setAction(Intent.ACTION_GET_CONTENT);

            Intent chooserIntent = Intent.createChooser(pickAvatarIntent, "Choose from");

            if (chooserIntent.resolveActivity(requireActivity().getPackageManager()) != null)
                startActivityForResult(chooserIntent, Request.REQUEST_PICK_AVATAR);
            else {
                Log.e(LOG_TAG, "Couldn't find any activity to pick image");
                Toast.makeText(getContext(), getString(R.string.not_resolved_activity), Toast.LENGTH_SHORT).show();
            }

        } else {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ExplanationDialog.newInstance(R.id.explanation_avatar_pick_permission, getString(R.string.explanation_permission_read_external_storage))
                            .show(requireActivity().getSupportFragmentManager(), getString(R.string.explanation_dialog_tag));

                } else {
                    requestPermissionsRequiredForPickingAvatar();
                }

                return;
            }

            Intent pickAvatarIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (pickAvatarIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(pickAvatarIntent, Request.REQUEST_PICK_AVATAR);
            } else {
                Log.e(LOG_TAG, "Couldn't find any app to pick image");
                Toast.makeText(getContext(), getString(R.string.not_resolved_activity), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void requestPermissionsRequiredForPickingAvatar() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Request.REQUEST_PICKING_AVATAR_PERMISSIONS);
    }

    private void showBirthdayPicker() {
        isDatePickerShown = true;

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
        picker.setOnDismissListener(dialogInterface -> isDatePickerShown = false);

        picker.show();
    }

    @Override
    protected void onFinish() {
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
                    OnProfileInfoSubmitListener listener = null;
                    if (getActivity() instanceof OnProfileInfoSubmitListener)
                        listener = (OnProfileInfoSubmitListener) getActivity();
                    else if (getParentFragment() instanceof OnProfileInfoSubmitListener)
                        listener = (OnProfileInfoSubmitListener) getParentFragment();
                    else
                        Reporter.reportError(LOG_TAG, "Parent fragment or activity do not implement OnProfileInfoSubmitListener interface");

                    if (listener != null)
                        listener.onProfileInfoSubmit(mRequestCode, mAvatar, enteredName, birthday);
                }
        }

        super.onFinish();
    }

    private void ensureInputValidity(@Nullable View viewThatLostFocus) {
        if (viewThatLostFocus != null && !(mHasTouchedDobEditTextBefore && mHasTouchedNameEditTextBefore)) {
            if (viewThatLostFocus == binding.name) mHasTouchedNameEditTextBefore = true;
            else if (viewThatLostFocus == binding.dateOfBirth) mHasTouchedDobEditTextBefore = true;
        }

        //Notice these are negatively true
        boolean isInvalidName = !CommonUtilities.isValidName(binding.name.getText());
        boolean isInvalidDate = !CommonUtilities.isValidDateFormat(binding.dateOfBirth.getText());

        if (isInvalidName && isInvalidDate && mHasTouchedNameEditTextBefore && mHasTouchedDobEditTextBefore) {
            displayError(R.string.error_message_invalid_name_and_dob, binding.titleContainer);
        } else if (isInvalidName && mHasTouchedNameEditTextBefore) {
            displayError(R.string.error_message_invalid_name, binding.titleContainer);
        } else if (isInvalidDate && mHasTouchedDobEditTextBefore) {
            displayError(getString(R.string.error_message_invalid_dob, getString(R.string.set_birthday_hint)), binding.titleContainer);
        } else {
            hideError(binding.titleContainer);

            if (isInvalidName || isInvalidDate) {
                disableDoneButton(binding.titleContainer);
            } else {
                enableDoneButton(binding.titleContainer);
            }

            return;
        }

        disableDoneButton(binding.titleContainer);
    }

    public enum UseType {
        MODIFICATION, CREATION
    }

    public interface OnProfileInfoSubmitListener {
        void onProfileInfoSubmit(int requestCode, @Nullable Avatar avatar, @NonNull String name, @NonNull Birthday dateOfBirth);
    }
}
