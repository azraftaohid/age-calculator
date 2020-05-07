package thegoodcompany.aetate.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.utilities.CommonUtilities;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;

import static thegoodcompany.aetate.ui.MainActivity.LOG_D;
import static thegoodcompany.aetate.ui.MainActivity.LOG_V;
import static thegoodcompany.aetate.utilities.CommonUtilities.isValidName;

public class RenameDialog extends DialogFragment {
    private static final String LOG_TAG = RenameDialog.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG.concat(".performance");

    private ProfileManagerInterface.updatable mUpdatable;
    private EditText mNewNameEditText;

    public RenameDialog() {

    }

    @SuppressWarnings("WeakerAccess")
    public static RenameDialog newInstance(ProfileManagerInterface.updatable updatable) {
        if (LOG_V) Log.v(LOG_TAG, "Initializing a new instance of Rename Dialog");

        RenameDialog renameDialog = new RenameDialog();
        renameDialog.mUpdatable = updatable;

        return renameDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Creating rename dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_rename, null);

        mNewNameEditText = root.findViewById(R.id.et_name_first_profile);

        String currentName = mUpdatable.getName();
        mNewNameEditText.setText(currentName);
        mNewNameEditText.setSelection(currentName.length());

        mNewNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ((AlertDialog) Objects.requireNonNull(RenameDialog.this.getDialog())).
                        getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValidName(s));
            }
        });

        builder.setTitle(getString(R.string.rename_long));
        builder.setView(root)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUpdatable.updateName(mNewNameEditText.getText().toString());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Objects.requireNonNull(RenameDialog.this.getDialog()).cancel();
                    }
                });

        Dialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null)
            CommonUtilities.showSoftKeyboard(window, mNewNameEditText);
        else Log.e(LOG_TAG, "Couldn't get dialog window");

        if (LOG_D)
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                    " milliseconds to show rename dialog");

        return dialog;
    }

}
