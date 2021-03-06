/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.bikey.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import org.jraf.android.bikey.Constants;
import org.jraf.android.util.annotation.Background;

public class UnitUtil {
    private static final float M_TO_KM = 0.001f;
    private static final float M_S_TO_KM_H = 3.6f;
    private static final float M_S_TO_MPH = 2.2369363f;
    private static final float M_TO_MI = 0.00062137119f;

    private static DecimalFormat FORMAT_SPEED = new DecimalFormat("0.0");
    private static DecimalFormat FORMAT_DISTANCE = new DecimalFormat("0.00");
    private static DecimalFormat FORMAT_CADENCE = new DecimalFormat("0");
    private static DecimalFormat FORMAT_HEART_RATE = new DecimalFormat("0");


    private static char sDecimalSeparator;
    private static String sUnit;

    static {
        sDecimalSeparator = FORMAT_DISTANCE.getDecimalFormatSymbols().getDecimalSeparator();
    }

    @Background
    public static void readPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sUnit = preferences.getString(Constants.PREF_UNITS, Constants.PREF_UNITS_DEFAULT);
    }


    /*
     * Speed.
     */

    public static CharSequence formatSpeed(float metersPerSecond, boolean withUnit, float fractionRelativeSize) {
        String unit = "";
        float converted;
        if (Constants.PREF_UNITS_METRIC.equals(sUnit)) {
            if (withUnit) unit = " km/h";
            converted = metersPerSecond * M_S_TO_KM_H;
        } else {
            if (withUnit) unit = " mph";
            converted = metersPerSecond * M_S_TO_MPH;
        }

        if (metersPerSecond == 0f) return "0" + unit;

        if (converted >= 20) {
            // Round to remove fraction
            return String.valueOf(Math.round(converted)) + unit;
        }

        if (converted >= 5) {
            // Round to closest .5
            int truncated = (int) converted;
            float fraction = converted - truncated;
            if (fraction < .5) {
                converted = truncated;
            } else {
                converted = truncated + .5f;
            }
        }
        String speedStr = FORMAT_SPEED.format(converted) + unit;
        SpannableString builder = new SpannableString(speedStr);
        builder.setSpan(new RelativeSizeSpan(fractionRelativeSize), speedStr.indexOf(sDecimalSeparator), speedStr.length() - unit.length(), 0);
        return builder;
    }

    public static CharSequence formatSpeed(float metersPerSecond, boolean withUnit) {
        return formatSpeed(metersPerSecond, withUnit, .4f);
    }

    public static CharSequence formatSpeed(float metersPerSecond) {
        return formatSpeed(metersPerSecond, false);
    }


    /*
     * Distance.
     */

    public static CharSequence formatDistance(float meters, boolean withUnit, float fractionRelativeSize) {
        String unit = "";
        float converted;
        if (Constants.PREF_UNITS_METRIC.equals(sUnit)) {
            if (withUnit) unit = " km";
            converted = meters * M_TO_KM;
        } else {
            if (withUnit) unit = " miles";
            converted = meters * M_TO_MI;
        }

        if (meters == 0f) return "0" + unit;

        String distStr = FORMAT_DISTANCE.format(converted) + unit;
        SpannableString builder = new SpannableString(distStr);
        builder.setSpan(new RelativeSizeSpan(fractionRelativeSize), distStr.indexOf(sDecimalSeparator), distStr.length() - unit.length(), 0);
        return builder;
    }

    public static CharSequence formatDistance(float meters, boolean withUnit) {
        return formatDistance(meters, withUnit, .4f);
    }

    public static CharSequence formatDistance(float meters) {
        return formatDistance(meters, false);
    }


    /*
     * Cadence.
     */

    public static CharSequence formatCadence(Float cadence, boolean withUnit) {
        String unit = "";
        if (withUnit) unit = " rpm";

        if (cadence == null) return "?" + unit;
        return FORMAT_CADENCE.format(cadence) + unit;
    }

    public static CharSequence formatCadence(Float cadence) {
        if (cadence == null) return "?";
        return FORMAT_CADENCE.format(cadence);
    }


    /*
     * Heart rate.
     */

    public static CharSequence formatHeartRate(int bpm, boolean withUnit) {
        String unit = "";
        if (withUnit) unit = " bpm";

        return FORMAT_HEART_RATE.format(bpm) + unit;
    }


    public static CharSequence formatHeartRate(int bpm) {
        return FORMAT_HEART_RATE.format(bpm);
    }
}
