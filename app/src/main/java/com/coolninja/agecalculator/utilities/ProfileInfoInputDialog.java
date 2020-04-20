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

    private OnProfileInfoSubmitListener mOnNewProfileAdded;

    private EditText mDobEditText;
    private EditText mNameEditText;
    private ImageView mAvatarImageView;
    private String mEnteredName;
    private String mEnteredDateOfBirth;
    @Nullable private Avatar mAvatar;

    private Calendar mStart;

    private int mRequestCode;

    private BirthdayPickerDialog mBirthdayPicker;

    public ProfileInfoInputDialog() {

    }

    public static ProfileInfoInputDialog newInstance(int requestCode) {
        final ProfileInfoInputDialog profileInfoInputDialog = new ProfileInfoInputDialog();

        profileInfoInputDialog.mRequestCode = requestCode;
        Calendar c = Calendar.getInstance();
        BirthdayPickerDialog birthdayPickerDialog = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                profileInfoInputDialog.mDobEditText.setText(String.format(Locale.ENGLISH,
                        Objects.requireNonNull(profileInfoInputDialog.getActivity()).getString(R.string.short_date_format),
                        month + 1, dayOfMonth, year));
                profileInfoInputDialog.mDobEditText.setSelection(profileInfoInputDialog.mDobEditText.length());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        profileInfoInputDialog.setBirthdayPicker(birthdayPickerDialog);

        return profileInfoInputDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mStart = Calendar.getInstance();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_profile_info_input, null);

        mDobEditText = root.findViewById(R.id.et_dob);
        mNameEditText = root.findViewById(R.id.et_name);
        mAvatarImageView = root.findViewById(R.id.iv_avatar);
        final TextView errorMessageTextView = root.findViewById(R.id.tv_invalid_name_input);
        ImageView datePickerImageView = root.findViewById(R.id.iv_date_picker);

        if (mEnteredName != null) mNameEditText.setText(mEnteredName);
        if (mEnteredDateOfBirth != null) mDobEditText.setText(mEnteredDateOfBirth);

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

                    mNameEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() > 0) {
                                errorMessageTextView.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

        mAvatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchAvatarPickerIntent();
            }
        });

        builder.setView(root)
                .setPositiveButton(R.string.add_profile, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEnteredName = mNameEditText.getText().toString();
                        mEnteredDateOfBirth = mDobEditText.getText().toString();
                        String[] mmddyyyy = mEnteredDateOfBirth.split("/");

                        int month = Integer.parseInt(mmddyyyy[0]) - 1;
                        int day = Integer.parseInt(mmddyyyy[1]);
                        int year = Integer.parseInt(mmddyyyy[2]);

                        Birthday birthday = new Birthday(year, month, day);
                        mOnNewProfileAdded.onProfileInfoSubmit(mRequestCode, mAvatar, mEnteredName, birthday);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(ProfileInfoInputDialog.this.getDialog()).cancel();
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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        if (mAvatar != null) {
            if (mAvatar.deleteAvatarFile()) {
                if (LOG_V) Log.v(LOG_TAG, "Avatar file successfully deleted");
            } else Log.e(LOG_TAG, "There was an error deleting avatar file");
        }
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

        try {
            mOnNewProfileAdded = (OnProfileInfoSubmitListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + ": must implement OnNewProfileAddedListener");
        }
    }

    public interface OnProfileInfoSubmitListener {
        void onProfileInfoSubmit(int requestCode, @Nullable Avatar avatar, String name, Birthday dateOfBirth);
    }
}
