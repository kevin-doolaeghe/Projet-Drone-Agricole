package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import fr.rostand.drone.R;
import fr.rostand.drone.view_model.DroneFlightControllerViewModel;

public class MissionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MissionActivity";

    private TextView mStatusTextView;
    private TextView mMissionTextView;
    private TextView mLocationTextView;
    private FloatingActionButton mUploadButton;
    private FloatingActionButton mGoHomeButton;
    private FloatingActionButton mLandButton;
    private FloatingActionButton mStartButton;
    private FloatingActionButton mStopButton;
    private FloatingActionButton mResumeButton;
    private FloatingActionButton mPauseButton;
    private ProgressBar mProgressBar;

    private DroneFlightControllerViewModel mDroneFlightControllerViewModel;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initComponents();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        initUI();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initComponents();
    }

    public void initUI() {
        this.setTitle(getString(R.string.title_mission_activity));

        mStatusTextView = findViewById(R.id.text_mission_status);
        mMissionTextView = findViewById(R.id.text_mission_infos);
        mLocationTextView = findViewById(R.id.text_drone_position);
        mUploadButton = findViewById(R.id.button_upload_mission);
        mGoHomeButton = findViewById(R.id.button_go_home);
        mLandButton = findViewById(R.id.button_land_drone);
        mStartButton = findViewById(R.id.button_start_mission);
        mStopButton = findViewById(R.id.button_stop_mission);
        mResumeButton = findViewById(R.id.button_resume_mission);
        mPauseButton = findViewById(R.id.button_pause_mission);
        mProgressBar = findViewById(R.id.mission_progress_bar);

        mUploadButton.setOnClickListener(this);
        mGoHomeButton.setOnClickListener(this);
        mLandButton.setOnClickListener(this);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mResumeButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);

        mDroneFlightControllerViewModel = ViewModelProviders.of(this).get(DroneFlightControllerViewModel.class);
        mDroneFlightControllerViewModel.getMission().observe(this, mission -> {
            mMissionTextView.setText(mission);
        });
        mDroneFlightControllerViewModel.getStatus().observe(this, status -> {
            mStatusTextView.setText(status);
        });
        mDroneFlightControllerViewModel.getLocation().observe(this, location -> {
            mLocationTextView.setText(location);
        });
        mDroneFlightControllerViewModel.getProgress().observe(this, progress -> {
            mProgressBar.setProgress(progress);
        });
    }

    private void initComponents() {
        mDroneFlightControllerViewModel.initComponents();
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == mUploadButton.getId()) {
                mDroneFlightControllerViewModel.uploadMission();
            }

            if (v.getId() == mGoHomeButton.getId()) {
                mDroneFlightControllerViewModel.goHome();
            }

            if (v.getId() == mLandButton.getId()) {
                mDroneFlightControllerViewModel.land();
            }

            if (v.getId() == mStartButton.getId()) {
                mDroneFlightControllerViewModel.startMission();
            }

            if (v.getId() == mStopButton.getId()) {
                mDroneFlightControllerViewModel.stopMission();
            }

            if (v.getId() == mResumeButton.getId()) {
                mDroneFlightControllerViewModel.resumeMission();
            }

            if (v.getId() == mPauseButton.getId()) {
                mDroneFlightControllerViewModel.pauseMission();
            }
        } catch (Exception e) {
            showToast(e.getMessage());
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplication(), toastMsg, Toast.LENGTH_LONG).show());
    }
}
