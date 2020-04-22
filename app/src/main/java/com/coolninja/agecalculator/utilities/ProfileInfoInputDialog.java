package com.coolninja.agecalculator.utilities;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;
import static com.coolninja.agecalculator.utilities.codes.Request.REQUEST_PICK_AVATAR;

public class ProfileInfoInputDialog extends DialogFragment {
    private static final String LOG_TAG = ProfileInfoInputDialog.class.getSimpleName();
    private static final String LOG_PERFORMANCE = LOG_TAG + ".performance";
    private static final String TYPE_IMAGE = "image/*";

    private OnProfileInfoSubmitListener mOnProfileInfoSubmitted;
    private ProfileManagerInterface.updatable mUpdatable;

    private EditText mDobEditText;
    private EditText mNameEditText;
    private ImageView mAvatarImageView;
    private String mEnteredName;
    private String mEnteredDateOfBirth;
    private Avatar mAvatar;
    private String mTitle;

    private Calendar mStart;
    private BirthdayPickerDialog mBirthdayPicker;
    private int mRequestCode;

    private ProfileInfoInputDialog() {

    }

    public static ProfileInfoInputDialog newInstance(int requestCode) {
        final ProfileInfoInputDialog profileInfoInputDialog = new ProfileInfoInputDialog();

        profileInfoInputDialog.mRequestCode = requestCode;
        Calendar c = Calendar.getInstance();
        BirthdayPickerDialog birthdayPickerDialog = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                profileInfoInputDialog.mDobEditText.setText(String.format(Locale.ENGLISH,
                        profileInfoInputDialog.getString(R.string.short_date_format),
                        month + 1, dayOfMonth, year));
                profileInfoInputDialog.mDobEditText.setSelection(profileInfoInputDialog.mDobEditText.length());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        profileInfoInputDialog.setBirthdayPicker(birthdayPickerDialog);

        return profileInfoInputDialog;
    }

    /** Use this method when asking for input about existing profiles */
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
                infoInputDialog.mDobEditText.setText(String.format(Locale.ENGLISH, infoInputDialog.getString(R.string.short_date_format),
                        month + 1, dayOfMonth, year));
                infoInputDialog.mDobEditText.setSelection(infoInputDialog.mDobEditText.getText().length());
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
        if (LOG_D) mStart = Calendar.getInstance();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_profile_info_input, null);

        mDobEditText = root.findViewById(R.id.et_dob);
        mNameEditText = root.findViewById(R.id.et_name);
        mAvatarImageView = root.findViewById(R.id.iv_avatar);
        final TextView errorMessageTextView = root.findViewById(R.id.tv_invalid_name_input);
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
                            mUpdatable.updateAvatar(mAvatar);
                            mUpdatable.updateBirthday(year, month, day);
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

        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mNameEditText.getText().length() < 1) {
                    errorMessageTextView.setVisibility(View.VISIBLE);
                    ((AlertDialog) Objects.requireNonNull(ProfileInfoInputDialog.this.getDialog())).
                            getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

            }
        });

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isFilled = isValidName(mDobEditText.getText()) && isValidName(s);

                errorMessageTextView.setVisibility(View.GONE);
                ((AlertDialog) Objects.requireNonNull(ProfileInfoInputDialog.this.getDialog())).
                        getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isFilled);
            }
        });

        mDobEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ((AlertDialog) Objects.requireNonNull(ProfileInfoInputDialog.this.getDialog())).
                        getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValidDateFormat(s) && isValidName(mNameEditText.getText()));
            }
        });

        mAvatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchAvatarPickerIntent();
            }
        });

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (LOG_D) Log.d(LOG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - mStart.getTimeInMillis()) +
                " milliseconds to show Profile Details Input dialog");
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

    private static boolean isValidDateFormat(Editable date) {
        String[] dates = date.toString().split("/");

        try {
            int day = Integer.parseInt(dates[1]);
            int month = Integer.parseInt(dates[0]);
            int year = Integer.parseInt(dates[2]);

            Month enumMonth = Month.values()[month - 1];
            if (Age.isLeapYear(year) && month == 2) {
                if (!(day > 0 && day <= 29)) return false;
            } else if (!(day > 0 && day <= enumMonth.getNumberOfDays())) {
                return false;
            }

            if (!(month <= 12)) return false;
            if (!(year <= Calendar.getInstance().get(Calendar.YEAR))) return false;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private static boolean isValidName(Editable name) {
        return name.length() > 0;
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

        if (LOG_D) Log.d(LOG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                " milliseconds to process this activity result");
    }

    private void setBirthdayPicker(BirthdayPickerDialog birthdayPicker) {
        mBirthdayPicker = birthdayPicker;
    }

    private void showBirthdayPicker() {
        mBirthdayPicker.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(),
                getString(R.string.birthday_picker_tag));
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
