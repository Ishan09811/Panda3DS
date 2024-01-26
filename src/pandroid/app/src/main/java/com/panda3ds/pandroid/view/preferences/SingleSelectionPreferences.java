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
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.utils.Constants;

public class SingleSelectionPreferences extends PreferenceCategory implements Preference.OnPreferenceClickListener {
    private final Drawable transparent = new ColorDrawable(Color.TRANSPARENT);
    private final Drawable doneDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_done);

    private CharSequence[] titles;
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
            titles = getTitles();
            entryValues = getEntryValues();
            TypedArray color = getContext().obtainStyledAttributes(new int[]{
                    android.R.attr.textColorSecondary
            });
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

        // Set summary based on selected title
        if (titles != null && entryValues != null) {
            CharSequence selectedTitle = getTitle();
            if (selectedTitle != null) {
                holder.itemView.setSummary(selectedTitle);
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
        final CharSequence[] titlesArray = new CharSequence[getPreferenceCount()];
        for (int i = 0; i < getPreferenceCount(); i++) {
            titlesArray[i] = getPreference(i).getTitle();
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(getTitle())
                .setSingleChoiceItems(titles, selectedIndex, (dialog, which) -> {
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

    private CharSequence[] getTitles() {
        CharSequence[] titles = new CharSequence[getPreferenceCount()];
        for (int i = 0; i < getPreferenceCount(); i++) {
            titles[i] = getPreference(i).getTitle();
        }
        return titles;
    }

    private CharSequence[] getEntryValues() {
        CharSequence[] values = new CharSequence[getPreferenceCount()];
        for (int i = 0; i < getPreferenceCount(); i++) {
            values[i] = getPreference(i).getKey();
        }
        return values;
    }
}
