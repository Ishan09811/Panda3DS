package com.panda3ds.pandroid.view.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import androidx.annotation.ArrayRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.R;

public class IntegerListPreference extends DialogPreference {

    private CharSequence[] entries;
    private int[] entryValues;

    private Integer value;
    private boolean isValueSet = false;

    private final boolean refreshRequired;

    public IntegerListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        Resources res = context.getResources();
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.IntegerListPreference, defStyleAttr, defStyleRes);

        entries = TypedArrayUtils.getTextArray(styledAttrs, R.styleable.IntegerListPreference_entries, R.styleable.IntegerListPreference_android_entries);
        int entryValuesId = TypedArrayUtils.getResourceId(styledAttrs, R.styleable.IntegerListPreference_android_entryValues, R.styleable.IntegerListPreference_android_entryValues, 0);
        entryValues = (entryValuesId != 0) ? res.getIntArray(entryValuesId) : null;

        if (TypedArrayUtils.getBoolean(styledAttrs, R.styleable.IntegerListPreference_useSimpleSummaryProvider, R.styleable.IntegerListPreference_useSimpleSummaryProvider, false)) {
            setSummaryProvider(SimpleSummaryProvider.getInstance());
        }

        refreshRequired = TypedArrayUtils.getBoolean(styledAttrs, R.styleable.IntegerListPreference_refreshRequired, R.styleable.IntegerListPreference_refreshRequired, false);

        styledAttrs.recycle();
    }

    public void setEntries(@ArrayRes int entriesResId) {
        entries = getContext().getResources().getTextArray(entriesResId);
    }

    public void setEntryValues(@ArrayRes int entryValuesResId) {
        entryValues = getContext().getResources().getIntArray(entryValuesResId);
    }

    public CharSequence getEntry() {
        if (entries != null) {
            int index = findIndexOfValue(value);
            return (index != -1) ? entries[index] : null;
        }
        return null;
    }

    private int findIndexOfValue(Integer value) {
        if (entryValues != null && value != null) {
            for (int i = entryValues.length - 1; i >= 0; i--) {
                if (entryValues[i] == value) {
                    return i;
                }
            }
        }
        return (value != null) ? value : -1;
    }

    public void setValueIndex(int index) {
        value = (entryValues != null) ? entryValues[index] : index;
    }

    private int getValueIndex() {
        return findIndexOfValue(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        value = (defaultValue != null) ? getPersistedInt((Integer) defaultValue) : getPersistedInt(0);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
    }

    private static class SavedState extends BaseSavedState {
        Integer value;

        SavedState(Parcel source) {
            super(source);
            value = (Integer) source.readSerializable();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(value);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public static class SimpleSummaryProvider implements SummaryProvider<IntegerListPreference> {

        private static SimpleSummaryProvider instance;

        private SimpleSummaryProvider() {
            // Private constructor to enforce singleton pattern
        }

        public static SimpleSummaryProvider getInstance() {
            if (instance == null) {
                instance = new SimpleSummaryProvider();
            }
            return instance;
        }

        @Override
        public CharSequence provideSummary(IntegerListPreference preference) {
            CharSequence entry = preference.getEntry();
            return (entry != null) ? entry : preference.getContext().getString(R.string.not_set);
        }
    }

    public static class IntegerListPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

        private int clickedDialogEntryIndex = 0;
        private CharSequence[] entries;
        private int[] entryValues;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                IntegerListPreference preference = getListPreference();
                if (preference.entries != null) {
                    clickedDialogEntryIndex = preference.findIndexOfValue(preference.value);
                    entries = preference.entries;
                    entryValues = preference.entryValues;
                } else {
                    throw new IllegalStateException("IntegerListPreference requires at least the entries array.");
                }
            } else {
                clickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0);
                entries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES);
                entryValues = savedInstanceState.getIntArray(SAVE_STATE_ENTRY_VALUES);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(SAVE_STATE_INDEX, clickedDialogEntryIndex);
            outState.putCharSequenceArray(SAVE_STATE_ENTRIES, entries);
            outState.putIntArray(SAVE_STATE_ENTRY_VALUES, entryValues);
        }

        private IntegerListPreference getListPreference() {
            return (IntegerListPreference) getPreference();
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            builder.setSingleChoiceItems(
                    entries, clickedDialogEntryIndex,
                    (dialog, which) -> {
                        if (clickedDialogEntryIndex != which) {
                            clickedDialogEntryIndex = which;
                            if (getListPreference().refreshRequired) {
                                Context context = getContext();
                                if (context != null) {
                                    context.getSettings().refreshRequired = true;
                                }
                            }
                        }

                        // Clicking on an item simulates the positive button click, and dismisses the dialog
                        onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
            );

            // The typical interaction for list-based dialogs is to have click-on-an-item dismiss the dialog instead
  
