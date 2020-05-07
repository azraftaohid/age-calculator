package thegoodcompany.aetate.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;

import static android.app.Activity.RESULT_OK;
import static thegoodcompany.aetate.ui.MainActivity.LOG_D;
import static thegoodcompany.aetate.ui.MainActivity.LOG_V;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidDateFormat;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidName;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_PICK_AVATAR;

public class ProfileInfoInputDialog extends DialogFragment {
    private static final String LOG_TAG = ProfileInfoInputDialog.class.getSimpleName();
    private static final String LOG_PERFORMANCE = LOG_TAG + ".performance";
    private static final String TYPE_IMAGE = "image/*";

    private OnProfileInfoSubmitListener mOnProfileInfoSubmitted;
    private ProfileManagerInterface.updatable mUpdatable;

    private EditText mDobEditText;
    private EditText mNameEditText;
    private ImageView mAvatarImageView;
    private TextView mErrorTextView;
    private Button mNextButton;

    private String mEnteredName;
    private String mEnteredDateOfBirth;
    private String mTitle;
    @Nullable
    private Avatar mAvatar;
    private BirthdayPickerDialog mBirthdayPicker;

    private int mRequestCode;
    private boolean mHasTouchedNameEditTextBefore;
    private boolean mHasTouchedDobEditTextBefore;

    public ProfileInfoInputDialog() {
        //Required public constructor
        //This constructor is called when recreating the dialog
        //e.g on orientation changes
    }

    @SuppressWarnings("WeakerAccess")
    public static ProfileInfoInputDialog newInstance(int requestCode) {
        final ProfileInfoInputDialog infoInputDialog = new ProfileInfoInputDialog();

        infoInputDialog.mRequestCode = requestCode;
        Calendar c = Calendar.getInstance();
        BirthdayPickerDialog birthdayPickerDialog = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String strDob = infoInputDialog.getString(R.string.short_date_format, month + 1, dayOfMonth, year);
                infoInputDialog.mDobEditText.setText(strDob);
                infoInputDialog.mDobEditText.requestFocus();
                infoInputDialog.mDobEditText.setSelection(strDob.length());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        infoInputDialog.setBirthdayPicker(birthdayPickerDialog);

        return infoInputDialog;
    }

    /**
     * Invoke this method when asking for input about existing profiles
     */
    public static ProfileInfoInputDialog newInstance(Context context, int requestCode, String title, @NonNull ProfileManagerInterface.updatable updatable) {
        final ProfileInfoInputDialog infoInputDialog = new ProfileInfoInputDialog();

        infoInputDialog.mTitle = title;
        infoInputDialog.mRequestCode = requestCode;
        infoInputDialog.mUpdatable = updatable;

        infoInputDialog.mEnteredName = updatable.getName();

        Birthday birthday = updatable.getBirthday();

        infoInputDialog.mEnteredDateOfBirth = String.format(Locale.ENGLISH, context.getString(R.string.short_date_format),
                birthday.get(Birthday.MONTH) + 1, birthday.get(Birthday.DAY), birthday.get(Birthday.YEAR));

        infoInputDialog.mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String strDob = infoInputDialog.getString(R.string.short_date_format, month + 1, dayOfMonth, year);
                infoInputDialog.mDobEditText.setText(strDob);
                infoInputDialog.mDobEditText.requestFocus();
                infoInputDialog.mDobEditText.setSelection(strDob.length());
            }
        }, birthday.get(Birthday.YEAR), birthday.get(Birthday.MONTH), birthday.get(Birthday.DAY));

        Avatar avatar = updatable.getAvatar();
        if (avatar != null) {
            infoInputDialog.mAvatar = avatar;
        }

        return infoInputDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_profile_info_input, null);

        mDobEditText = root.findViewById(R.id.et_dob);
        mNameEditText = root.findViewById(R.id.et_name);
        mAvatarImageView = root.findViewById(R.id.iv_avatar);
        mErrorTextView = root.findViewById(R.id.tv_error_message);
        ImageView datePickerImageView = root.findViewById(R.id.iv_date_picker);

        if (mTitle == null) mTitle = getString(R.string.create_profile);
        if (mEnteredName != null) mNameEditText.setText(mEnteredName);
        if (mEnteredDateOfBirth != null) mDobEditText.setText(mEnteredDateOfBirth);
        if (mAvatar != null) mAvatarImageView.setImageDrawable(mAvatar.getCircularDrawable());

        builder.setTitle(mTitle);
        builder.setView(root)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEnteredName = mNameEditText.getText().toString();
                        mEnteredDateOfBirth = mDobEditText.getText().toString();
                        String[] mmddyyyy = mEnteredDateOfBirth.split("/");

                        int month = Integer.parseInt(mmddyyyy[0]) - 1;
                        int day = Integer.parseInt(mmddyyyy[1]);
                        int year = Integer.parseInt(mmddyyyy[2]);

                        if (mUpdatable != null) {
                            mUpdatable.updateName(mEnteredName);
                            mUpdatable.updateBirthday(year, month, day);

                            if (mAvatar != null) mUpdatable.updateAvatar(mAvatar);
                        } else {
                            Birthday birthday = new Birthday(year, month, day);
                            mOnProfileInfoSubmitted.onProfileInfoSubmit(mRequestCode, mAvatar, mEnteredName, birthday);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        datePickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBirthdayPicker();
            }
        });

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) ensureInputValidity(v);
            }
        };

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                ensureInputValidity(null);
            }
        };

        mNameEditText.setOnFocusChangeListener(onFocusChangeListener);
        mNameEditText.addTextChangedListener(inputWatcher);
        mDobEditText.setOnFocusChangeListener(onFocusChangeListener);
        mDobEditText.addTextChangedListener(inputWatcher);

        mAvatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchAvatarPickerIntent();
            }
        });

        if (LOG_D)
            Log.d(LOG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to show Profile Details Input dialog");

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        mNextButton = ((AlertDialog) Objects.requireNonNull(ProfileInfoInputDialog.this.getDialog()))
                .getButton(AlertDialog.BUTTON_POSITIVE);

        ensureInputValidity(null);
    }

    private void dispatchAvatarPickerIntent() {
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Calendar start;
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
                    avatarBitmap = Objects.requireNonNull(getActivity()).getContentResolver().loadThumbnail(avatarUri, new Size(512, 512), null);
                    if (LOG_V) Log.v(LOG_TAG, "Received avatar bitmap thumbnail");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mAvatar = new Avatar(getContext(), avatarBitmap);

                if (LOG_V) Log.v(LOG_TAG, "Setting the bitmap as avatar");
                mAvatarImageView.setImageDrawable(mAvatar.getCircularDrawable());
            }
        }

        if (LOG_D)
            Log.d(LOG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to process this activity result");
    }

    private void setBirthdayPicker(BirthdayPickerDialog birthdayPicker) {
        mBirthdayPicker = birthdayPicker;
    }

    private void showBirthdayPicker() {
        mBirthdayPicker.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                getString(R.string.birthday_picker_tag));
    }

    private void ensureInputValidity(@Nullable View viewThatLostFocus) {
        if (mNextButton == null) {
            mNextButton = ((AlertDialog) Objects.requireNonNull(ProfileInfoInputDialog.this.getDialog()))
                    .getButton(DialogInterface.BUTTON_POSITIVE);
        }

        if (viewThatLostFocus != null && !(mHasTouchedDobEditTextBefore && mHasTouchedNameEditTextBefore)) {
            if (viewThatLostFocus == mNameEditText) mHasTouchedNameEditTextBefore = true;
            else if (viewThatLostFocus == mDobEditText) mHasTouchedDobEditTextBefore = true;
        }

        //These are negatively true
        boolean isInValidName = !isValidName(mNameEditText.getText());
        boolean isInValidDate = !isValidDateFormat(mDobEditText.getText());

        if (isInValidName && isInValidDate && mHasTouchedNameEditTextBefore && mHasTouchedDobEditTextBefore) {
            mErrorTextView.setText(getString(R.string.error_message_invalid_name_and_dob));
        } else if (isInValidName && mHasTouchedNameEditTextBefore) {
            mErrorTextView.setText(getString(R.string.error_message_invalid_name));
        } else if (isInValidDate && mHasTouchedDobEditTextBefore) {
            mErrorTextView.setText(getString(R.string.error_message_invalid_dob, getString(R.string.set_birthday_hint)));
        } else {
            if (mErrorTextView.getVisibility() != View.GONE)
                mErrorTextView.setVisibility(View.GONE);
            mNextButton.setEnabled(!(isInValidName || isInValidDate));
            return;
        }

        if (mErrorTextView.getVisibility() != View.VISIBLE)
            mErrorTextView.setVisibility(View.VISIBLE);
        mNextButton.setEnabled(false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnProfileInfoSubmitListener)
            mOnProfileInfoSubmitted = (OnProfileInfoSubmitListener) context;

    }

    public interface OnProfileInfoSubmitListener {
        void onProfileInfoSubmit(int requestCode, @Nullable Avatar avatar, String name, Birthday dateOfBirth);
    }
}
