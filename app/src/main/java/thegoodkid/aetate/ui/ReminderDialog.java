package thegoodkid.aetate.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView.OnEditorActionListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

import thegoodkid.aetate.R;
import thegoodkid.aetate.databinding.DialogReminderBinding;
import thegoodkid.aetate.databinding.ViewstubReminderPrecisionBinding;
import thegoodkid.aetate.utilities.Birthday;
import thegoodkid.aetate.utilities.Reporter;

public class ReminderDialog extends BaseAppDialogFragment {
    private static final String INIT_YEAR = "init_year";
    private static final String INIT_MONTH = "init_month";
    private static final String INIT_DAY = "init_day";
    private static final String BIRTH_YEAR = "year";
    private static final String BIRTH_MONTH = "month";
    private static final String BIRTH_DAY = "day";

    private DialogReminderBinding binding;
    @Nullable
    private ViewstubReminderPrecisionBinding precisionBinding;
    private OnReminderSetListener mListener;

    private int birthDay;
    private int birthMonth;
    private int birthYear;
    @Nullable
    private Integer mInitDay;
    @Nullable
    private Integer mInitMonth;
    @Nullable
    private Integer mInitYear;

    private long mSetMillis;

    private boolean hasTouchedBefore;

    public ReminderDialog() {

    }

    @NonNull
    public static ReminderDialog newInstance(@NonNull Birthday birthday) {
        Bundle args = new Bundle();
        args.putInt(BIRTH_YEAR, birthday.getYear());
        args.putInt(BIRTH_MONTH, birthday.getMonthValue());
        args.putInt(BIRTH_DAY, birthday.getDayOfMonth());

        ReminderDialog dialog = new ReminderDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    public static ReminderDialog newInstance(int initYear, int initMonth, int initDay, @NonNull Birthday birthday) {
        Bundle args = new Bundle();
        args.putInt(INIT_YEAR, initYear);
        args.putInt(INIT_MONTH, initMonth);
        args.putInt(INIT_DAY, initDay);
        args.putInt(BIRTH_DAY, birthday.getDayOfMonth());
        args.putInt(BIRTH_MONTH, birthday.getMonthValue());
        args.putInt(BIRTH_YEAR, birthday.getYear());

        ReminderDialog dialog = new ReminderDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle received = getArguments();
        if (received == null) return;

        birthDay = received.getInt(BIRTH_DAY);
        birthMonth = received.getInt(BIRTH_MONTH);
        birthYear = received.getInt(BIRTH_YEAR);
        if (received.containsKey(INIT_YEAR)) mInitYear = received.getInt(INIT_YEAR);
        if (received.containsKey(INIT_MONTH)) mInitMonth = received.getInt(INIT_MONTH);
        if (received.containsKey(INIT_DAY)) mInitDay = received.getInt(INIT_DAY);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogReminderBinding.inflate(inflater, container, false);
        initTitleContainer(binding.titleContainer);
        binding.titleContainer.title.setText(getString(R.string.title_set_reminder));
        disableDoneButton(binding.titleContainer);

        if (mInitDay != null) binding.day.setText(mInitDay);
        if (mInitMonth != null) binding.month.setText(mInitMonth);
        if (mInitYear != null) binding.year.setText(mInitYear);

        binding.showMore.setOnClickListener(view -> {
            inflatePrecision();
            binding.showMore.setVisibility(View.GONE);
        });

        initListeners(binding.year, binding.month, binding.day);

        return binding.getRoot();
    }

    @Override
    protected void onFinish() {
        if (mListener != null) {
            mListener.onReminderSet(mSetMillis);
        }

        super.onFinish();
    }

    private void initListeners(@NonNull EditText... editTexts) {
        int len = editTexts.length;
        boolean isTriplet = len == 3;
        OnEditorActionListener[] editorActionListeners = null;

        if (isTriplet) {
            editorActionListeners = new OnEditorActionListener[]{
                    (textView, i, keyEvent) -> {
                        if (i == EditorInfo.IME_ACTION_NEXT) {
                            editTexts[1].requestFocus();
                            return true;
                        }

                        return false;
                    },
                    (textView, i, keyEvent) -> {
                        switch (i) {
                            case EditorInfo.IME_ACTION_NEXT:
                                editTexts[2].requestFocus();
                                return true;
                            case EditorInfo.IME_ACTION_PREVIOUS:
                                editTexts[0].requestFocus();
                                return true;
                        }

                        return false;
                    },
                    (textView, i, keyEvent) -> {
                        switch (i) {
                            case EditorInfo.IME_ACTION_DONE:
                                if (binding.titleContainer.done.isEnabled())
                                    binding.titleContainer.done.performClick();
                                return true;
                            case EditorInfo.IME_ACTION_PREVIOUS:
                                editTexts[1].requestFocus();
                                return true;
                        }

                        return false;
                    }
            };
        } else {
            Reporter.reportWarning(ReminderDialog.class.getSimpleName(), "Passed edit texts are not triplet. Skipping editor action listeners");
        }

        OnFocusChangeListener focusChangeListener = (view, b) -> {
            if (!b) ensureValidity(view);
        };

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ensureValidity(null);
            }
        };

        for (int i = 0; i < len; i++) {
            EditText editText = editTexts[i];
            editText.addTextChangedListener(textWatcher);
            editText.setOnFocusChangeListener(focusChangeListener);
            if (isTriplet) editText.setOnEditorActionListener(editorActionListeners[i]);

        }
    }

    private void inflatePrecision() {
        precisionBinding = ViewstubReminderPrecisionBinding.bind(binding.moreOptionsViewStub.inflate());
        initListeners(precisionBinding.hour, precisionBinding.minute, precisionBinding.second);

        binding.day.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.day.setOnEditorActionListener((textView, i, keyEvent) -> {
            switch (i) {
                case EditorInfo.IME_ACTION_NEXT:
                    precisionBinding.hour.requestFocus();
                    return true;
                case EditorInfo.IME_ACTION_PREVIOUS:
                    binding.month.requestFocus();
                    return true;
            }

            return false;
        });
    }

    private void ensureValidity(@Nullable View lostFocus) {
        if (!hasTouchedBefore && lostFocus != null) {
            hasTouchedBefore = true;
        }

        boolean hasDefinedAtLeastOne = false;

        String strInputDay = binding.day.getText().toString();
        String strInputMonth = binding.month.getText().toString();
        String strInputYear = binding.year.getText().toString();
        int inputDay = !TextUtils.isEmpty(strInputDay) && (hasDefinedAtLeastOne = true) ? Integer.parseInt(strInputDay) : 0;
        int inputMonth = !TextUtils.isEmpty(strInputMonth) && (hasDefinedAtLeastOne = true) ? Integer.parseInt(strInputMonth) : 0;
        int inputYear = !TextUtils.isEmpty(strInputYear) && (hasDefinedAtLeastOne = true) ? Integer.parseInt(strInputYear) : 0;

        Calendar c = Calendar.getInstance();
        c.set(birthYear, birthMonth, birthDay, 0, 0, 0);
        c.add(Calendar.DAY_OF_MONTH, inputDay);
        c.add(Calendar.MONTH, inputMonth);
        c.add(Calendar.YEAR, inputYear);

        if (precisionBinding != null) {
            String strInputHour = precisionBinding.hour.getText().toString();
            String strInputMinute = precisionBinding.minute.getText().toString();
            String strInputSecond = precisionBinding.second.getText().toString();

            int inputHour = !TextUtils.isEmpty(strInputHour) && (hasDefinedAtLeastOne = true) ? Integer.parseInt(strInputHour) : 0;
            int inputMinute = !TextUtils.isEmpty(strInputMinute) && (hasDefinedAtLeastOne = true) ? Integer.parseInt(strInputMinute) : 0;
            int inputSecond = !TextUtils.isEmpty(strInputSecond) && (hasDefinedAtLeastOne = true) ? Integer.parseInt(strInputSecond) : 0;

            c.add(Calendar.HOUR, inputHour);
            c.add(Calendar.MINUTE, inputMinute);
            c.add(Calendar.SECOND, inputSecond);
        }

        if (hasDefinedAtLeastOne && (mSetMillis = c.getTimeInMillis()) > System.currentTimeMillis()) {
            hideError(binding.titleContainer);
            enableDoneButton(binding.titleContainer);
        } else if (hasTouchedBefore) {
            disableDoneButton(binding.titleContainer);
            displayError(R.string.error_message_invalid_reminder, binding.titleContainer);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnReminderSetListener) mListener = (OnReminderSetListener) context;
        else
            Reporter.reportError(ReminderDialog.class.getSimpleName(), "Parent fragment or activity do not implement OnReminderSetListener interface");
    }

    public interface OnReminderSetListener {
        void onReminderSet(long atMillis);
    }
}
