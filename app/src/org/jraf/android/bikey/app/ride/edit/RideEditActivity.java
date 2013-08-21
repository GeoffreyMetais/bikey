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
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.bikey.app.ride.edit;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;

public class RideEditActivity extends FragmentActivity {
    private EditText mEdtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_edit);
        mEdtName = (EditText) findViewById(R.id.edtName);
        mEdtName.setOnEditorActionListener(mNameOnEditorActionListener);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        View customActionBarView = getLayoutInflater().inflate(R.layout.ride_edit_actionbar, null);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View btnDone = customActionBarView.findViewById(R.id.actionbar_done);
        btnDone.setOnClickListener(mDoneOnClickListener);

        View btnDiscard = customActionBarView.findViewById(R.id.actionbar_discard);
        btnDiscard.setOnClickListener(mDiscardOnClickListener);
    }

    private void createRide() {
        new TaskFragment(new Task<RideEditActivity>() {
            private Uri mCreatedRideUri;

            @Override
            protected void doInBackground() throws Throwable {
                String name = getActivity().mEdtName.getText().toString().trim();
                mCreatedRideUri = RideManager.get().create(name);
            }

            @Override
            protected void onPostExecuteOk() {
                setResult(RESULT_OK, new Intent(null, mCreatedRideUri));
                getActivity().finish();
            }
        }).execute(getSupportFragmentManager());
    }

    private OnClickListener mDoneOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            createRide();
        }
    };

    private OnClickListener mDiscardOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private OnEditorActionListener mNameOnEditorActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            createRide();
            return true;
        }
    };
}
