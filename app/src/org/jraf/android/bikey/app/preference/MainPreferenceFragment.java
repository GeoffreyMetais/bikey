/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.app.preference;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.provider.ride.RideColumns;
import org.jraf.android.bikey.util.MediaButtonUtil;
import org.jraf.android.bikey.util.UnitUtil;

public class MainPreferenceFragment extends PreferenceFragment {
    private PreferenceCallbacks mCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        updateListPreferenceSummary(Constants.PREF_UNITS);

        findPreference(Constants.PREF_HEART_MONITOR_SCAN).setOnPreferenceClickListener(mOnPreferenceClickListener);
        findPreference(Constants.PREF_EXPORT).setOnPreferenceClickListener(mOnPreferenceClickListener);
        findPreference(Constants.PREF_IMPORT).setOnPreferenceClickListener(mOnPreferenceClickListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (PreferenceCallbacks) activity;
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

    public PreferenceCallbacks getCallbacks() {
        return mCallbacks;
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        super.onStop();
    }

    private final OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateListPreferenceSummary(key);
            switch (key) {
                case Constants.PREF_UNITS:
                    UnitUtil.readPreferences(getActivity());

                    // Notify observers of rides since they display distances using a conversion depending on the preference
                    getActivity().getContentResolver().notifyChange(RideColumns.CONTENT_URI, null);
                    break;

                case Constants.PREF_LISTEN_TO_HEADSET_BUTTON:
                    if (sharedPreferences.getBoolean(key, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) {
                        MediaButtonUtil.registerMediaButtonEventReceiver(getActivity());
                    } else {
                        MediaButtonUtil.unregisterMediaButtonEventReceiver(getActivity());
                    }
                    break;

                case Constants.PREF_RECORD_CADENCE:
                    if (sharedPreferences.getBoolean(Constants.PREF_RECORD_CADENCE, Constants.PREF_RECORD_CADENCE_DEFAULT)) {
                        getCallbacks().showRecordCadenceConfirmDialog();
                    } else {
                        // The pref was unchecked because the user pressed 'No' in the confirmation dialog.
                        // Update the switch.
                        SwitchPreference pref = (SwitchPreference) getPreferenceManager().findPreference(Constants.PREF_RECORD_CADENCE);
                        pref.setChecked(false);
                    }
                    break;
            }
        }
    };

    private void updateListPreferenceSummary(String key) {
        if (Constants.PREF_UNITS.equals(key)) {
            ListPreference pref = (ListPreference) getPreferenceManager().findPreference(key);
            CharSequence entry = pref.getEntry();
            pref.setSummary(entry);
        }
    }

    private OnPreferenceClickListener mOnPreferenceClickListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (Constants.PREF_EXPORT.equals(preference.getKey())) {
                getCallbacks().startExport();
                return true;
            } else if (Constants.PREF_IMPORT.equals(preference.getKey())) {
                getCallbacks().startImport();
                return true;
            } else if (Constants.PREF_HEART_MONITOR_SCAN.equals(preference.getKey())) {
                getCallbacks().startHeartRateMonitorScan();
                return true;
            }
            return false;
        }
    };
}
