/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.app.mediabutton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import org.jraf.android.bikey.Constants;
import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.collect.LogCollectorService;
import org.jraf.android.bikey.backend.provider.ride.RideState;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Only care if the preference is checked
        if (!sharedPreferences.getBoolean(Constants.PREF_LISTEN_TO_HEADSET_BUTTON, Constants.PREF_LISTEN_TO_HEADSET_BUTTON_DEFAULT)) return;

        // Only care if there is a current ride
        final Uri currentRideUri = RideManager.get().getCurrentRide();
        if (currentRideUri == null) return;

        // Only care about key down
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent != null && keyEvent.getAction() != KeyEvent.ACTION_DOWN) return;

        new AsyncTask<Void, Void, Integer>() {
            private static final int RESULT_RIDE_DOES_NOT_EXIST = 0;
            private static final int RESULT_RIDE_ACTIVATED = 1;
            private static final int RESULT_RIDE_PAUSED = 2;


            @Override
            protected Integer doInBackground(Void... params) {
                // Check if the ride still exists (it may have been deleted)
                boolean rideExists = RideManager.get().isExistingRide(currentRideUri);
                Log.d("rideExists=" + rideExists);
                if (!rideExists) {
                    return RESULT_RIDE_DOES_NOT_EXIST;
                }

                RideState state = RideManager.get().getState(currentRideUri);
                switch (state) {
                    case CREATED:
                    case PAUSED:
                        context.startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, currentRideUri, context, LogCollectorService.class));
                        TextToSpeechManager.get().speak(R.string.speak_activate_ride);
                        return RESULT_RIDE_ACTIVATED;

                    case ACTIVE:
                        context.startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, currentRideUri, context, LogCollectorService.class));
                        TextToSpeechManager.get().speak(R.string.speak_pause_ride);
                        return RESULT_RIDE_PAUSED;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Integer result) {
                switch (result) {
                    case RESULT_RIDE_DOES_NOT_EXIST:
                        // Do not do anything
                        break;

                    case RESULT_RIDE_ACTIVATED:
                        // TODO: talk
                        break;

                    case RESULT_RIDE_PAUSED:
                        // TODO: talk
                        break;
                }
            }

        }.execute();
    }
}
