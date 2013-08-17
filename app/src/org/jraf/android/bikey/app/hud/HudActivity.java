package org.jraf.android.bikey.app.hud;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

import org.jraf.android.bikey.R;
import org.jraf.android.bikey.app.BaseFragmentActivity;
import org.jraf.android.bikey.app.hud.fragment.averagemovingspeed.AverageMovingSpeedHudFragment;
import org.jraf.android.bikey.app.hud.fragment.elapsedtime.ElapsedTimeHudFragment;
import org.jraf.android.bikey.app.hud.fragment.speed.SpeedHudFragment;
import org.jraf.android.bikey.app.hud.fragment.totaldistance.TotalDistanceHudFragment;
import org.jraf.android.bikey.backend.LogCollectorService;
import org.jraf.android.bikey.backend.location.LocationManager;
import org.jraf.android.bikey.backend.location.LocationManager.StatusListener;
import org.jraf.android.bikey.backend.ride.RideManager;
import org.jraf.android.util.Log;

public class HudActivity extends BaseFragmentActivity {
    private static final long DELAY_HIDE_CONTROLS = 2000;

    private Handler mHandler = new Handler();

    private ImageView mImgGpsStatus;
    private ToggleButton mTogRecording;
    private View mConTabsLeft;

    private boolean mNavigationBarHiding = false;
    private Uri mRideUri;
    private FragmentCycler mFragmentCycler;
    private boolean mTabsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// TODO only if setting says so

        mRideUri = getIntent().getData();

        setContentView(R.layout.hud);

        mTogRecording = (ToggleButton) findViewById(R.id.togRecording);
        mTogRecording.setEnabled(false);
        toggleRecordingIfActive();
        mImgGpsStatus = (ImageView) findViewById(R.id.imgGpsStatus);
        findViewById(R.id.vieFragmentCycle).setOnTouchListener(mFragmentCycleOnTouchListener);
        mConTabsLeft = findViewById(R.id.conTabsLeft);

        setupFragments();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setupNavigationBarHiding();
        }
        scheduleHideTabs();
    }

    private void toggleRecordingIfActive() {
        new AsyncTask<Void, Void, Void>() {
            private Uri mActiveRideUri;

            @Override
            protected Void doInBackground(Void... params) {
                mActiveRideUri = RideManager.get().getActiveRide();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mRideUri.equals(mActiveRideUri)) {
                    mTogRecording.setChecked(true);
                }
                mTogRecording.setEnabled(true);
                mTogRecording.setOnCheckedChangeListener(mRecordingOnCheckedChangeListener);
            }
        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // GPS status
        LocationManager.get().addStatusListener(mGpsStatusListener);
    }

    @Override
    protected void onStop() {
        // GPS status
        LocationManager.get().removeStatusListener(mGpsStatusListener);

        super.onStop();
    }

    private void setupFragments() {
        mFragmentCycler = new FragmentCycler(R.id.conFragments);
        mFragmentCycler.add(this, SpeedHudFragment.newInstance(), R.id.chkTabSpeed);
        mFragmentCycler.add(this, ElapsedTimeHudFragment.newInstance(), R.id.chkTabDuration);
        mFragmentCycler.add(this, TotalDistanceHudFragment.newInstance(), R.id.chkTabDistance);
        mFragmentCycler.add(this, AverageMovingSpeedHudFragment.newInstance(), R.id.chkTabAverageMovingSpeed);
        mFragmentCycler.show(this);
    }

    private OnTouchListener mFragmentCycleOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mFragmentCycler.cycle(HudActivity.this);
            }
            return true;
        }
    };


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupNavigationBarHiding() {
        findViewById(android.R.id.content).setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                Log.d("visibility=" + visibility);
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
                    Log.d("Navigation bar showing");
                    if (!isPaused()) mFragmentCycler.cycle(HudActivity.this);
                    scheduleHideNavigationBar();
                    showTabs();
                    scheduleHideTabs();
                }
            }
        });
        scheduleHideNavigationBar();
        mNavigationBarHiding = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("ev=" + ev);
        if (mNavigationBarHiding) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.d("up");
                    scheduleHideNavigationBar();
                    scheduleHideTabs();
                    break;

                default:
                    mHandler.removeCallbacks(mHideNavigationBarRunnable);
                    mHandler.removeCallbacks(mHideTabsRunnable);
                    showTabs();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private Runnable mHideNavigationBarRunnable = new Runnable() {
        @Override
        public void run() {
            hideNavigationBar();
        }
    };

    private void scheduleHideNavigationBar() {
        mHandler.removeCallbacks(mHideNavigationBarRunnable);
        mHandler.postDelayed(mHideNavigationBarRunnable, DELAY_HIDE_CONTROLS * 2);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void hideNavigationBar() {
        Log.d();
        findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private OnCheckedChangeListener mRecordingOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("isChecked=" + isChecked);
            if (isChecked) {
                startService(new Intent(LogCollectorService.ACTION_START_COLLECTING, mRideUri, HudActivity.this, LogCollectorService.class));
            } else {
                startService(new Intent(LogCollectorService.ACTION_STOP_COLLECTING, mRideUri, HudActivity.this, LogCollectorService.class));
            }
        }
    };

    private StatusListener mGpsStatusListener = new StatusListener() {
        @Override
        public void onStatusChanged(boolean active) {
            if (active) {
                mImgGpsStatus.setImageResource(R.color.hud_gps_first_fix);
            } else {
                mImgGpsStatus.setImageResource(R.color.hud_gps_stopped);
            }
        }
    };

    public Uri getRideUri() {
        return mRideUri;
    }


    /*
     * Tabs.
     */
    private void hideTabs() {
        mTabsVisible = false;
        mConTabsLeft.animate().alpha(0).translationX(-mConTabsLeft.getWidth()).setInterpolator(new AccelerateInterpolator())
                .setDuration(getResources().getInteger(R.integer.animation_controls_showHide));
    }

    private void showTabs() {
        if (mTabsVisible) return;
        mTabsVisible = true;
        mConTabsLeft.animate().alpha(1).translationX(-0).setInterpolator(new DecelerateInterpolator())
                .setDuration(getResources().getInteger(R.integer.animation_controls_showHide));
    }


    private Runnable mHideTabsRunnable = new Runnable() {
        @Override
        public void run() {
            hideTabs();
        }
    };

    private void scheduleHideTabs() {
        mHandler.removeCallbacks(mHideTabsRunnable);
        mHandler.postDelayed(mHideTabsRunnable, DELAY_HIDE_CONTROLS);
    }
}
