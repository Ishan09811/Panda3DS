package com.panda3ds.pandroid.view.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.utils.Constants;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SingleSelectionPreferences extends PreferenceCategory implements Preference.OnPreferenceClickListener {
    private final Drawable transparent = new ColorDrawable(Color.TRANSPARENT);
    private final Drawable doneDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_done);

    // New attributes for handling entries and entryValues
    private CharSequence[] entries;
    private CharSequence[] entryValues;

    public SingleSelectionPreferences(@NonNull Context context) {
        super(context);
        init();
    }

    public SingleSelectionPreferences(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SingleSelectionPreferences(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SingleSelectionPreferences(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        try {
            TypedArray color = getContext().obtainStyledAttributes(new int[]{
                    android.R.attr.textColorSecondary
            });
            entries = getEntries();
            entryValues = getEntryValues();
            doneDrawable.setTint(color.getColor(0, Color.RED));
            color.recycle();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                color.close();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "Error on obtain text color secondary: ", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Set summary based on selected entry
        if (entries != null && entryValues != null) {
            CharSequence selectedEntry = getEntry();
            if (selectedEntry != null) {
                holder.setSummary(selectedEntry);
            }
        }
    }

    @Override
    public void onAttached() {
        super.onAttached();

        for (int i = 0; i < getPreferenceCount(); i++) {
            getPreference(i).setOnPreferenceClickListener(this);
        }
    }

    public void setSelectedItem(int index) {
        onPreferenceClick(getPreference(index));
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        int index = findPreferenceIndex(preference);

        if (index != -1) {
            showMaterialDialog(index);
        }

        return true;
    }

    private int findPreferenceIndex(@NonNull Preference preference) {
        for (int i = 0; i < getPreferenceCount(); i++) {
            if (getPreference(i) == preference) {
                return i;
            }
        }
        return -1;
    }

    private void showMaterialDialog(final int selectedIndex) {
        final CharSequence[] entriesArray = new CharSequence[getPreferenceCount()];
        for (int i = 0; i < getPreferenceCount(); i++) {
            entriesArray[i] = getPreference(i).getTitle();
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(getTitle())
                .setSingleChoiceItems(entries, selectedIndex, (dialog, which) -> {
                    updateSelectedPreference(which);
                    dialog.dismiss();
                })
                .show();
    }

    private void updateSelectedPreference(int selectedIndex) {
        for (int i = 0; i < getPreferenceCount(); i++) {
            Preference item = getPreference(i);
            if (i == selectedIndex) {
                item.setIcon(doneDrawable);
            } else {
                item.setIcon(transparent);
            }
        }

        callChangeListener(selectedIndex);
    }

    // New method to get entries from the XML attribute
    private CharSequence[] getEntries() {
        if (this instanceof ListPreference) {
            return ((ListPreference) this).getEntries();
        } else {
            return null; // Handle appropriately for other types of preferences
        }
    }

    // New method to get entryValues from the XML attribute
    private CharSequence[] getEntryValues() {
        if (this instanceof ListPreference) {
            return ((ListPreference) this).getEntryValues();
        } else {
            return null; // Handle appropriately for other types of preferences
        }
    }
}
