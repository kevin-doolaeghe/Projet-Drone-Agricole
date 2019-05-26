package fr.rostand.drone.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import fr.rostand.drone.R;
import fr.rostand.drone.model.DroneFlightController;

public class MissionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MissionActivity";

    private TextView mStatusTextView;
    private TextView mMissionTextView;
    private TextView mPositionTextView;
    private FloatingActionButton mUploadButton;
    private FloatingActionButton mGoHomeButton;
    private FloatingActionButton mLandButton;
    private FloatingActionButton mStartButton;
    private FloatingActionButton mStopButton;
    private FloatingActionButton mResumeButton;
    private FloatingActionButton mPauseButton;
    private ProgressBar mProgressBar;

    private DroneFlightController mDroneFlightController;

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

        mDroneFlightController = DroneFlightController.getInstance(getApplication());
        mDroneFlightController.setWaypointMissionOperatorListener(new WaypointMissionOperatorListener() {
            @Override
            public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

            }

            @Override
            public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {

            }

            @Override
            public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
                int percent = (int) ((((double) waypointMissionExecutionEvent.getProgress().targetWaypointIndex) / ((double) waypointMissionExecutionEvent.getProgress().totalWaypointCount)) * 100);
                showToast("Mission completed at : " + percent + "%");
                mProgressBar.setProgress(percent);
            }

            @Override
            public void onExecutionStart() {
                mProgressBar.setProgress(0);
            }

            @Override
            public void onExecutionFinish(@Nullable DJIError djiError) {
                showToast("Finish :)");
                mProgressBar.setProgress(0);
            }
        });

        initUI();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void initUI() {
        this.setTitle(getString(R.string.title_mission_activity));

        mStatusTextView = findViewById(R.id.text_mission_status);
        mMissionTextView = findViewById(R.id.text_mission_infos);
        mPositionTextView = findViewById(R.id.text_drone_position);
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
    }

    private void initComponents() {
        mDroneFlightController.initComponents();
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == mUploadButton.getId()) {
                mDroneFlightController.uploadMission();
            }

            if (v.getId() == mGoHomeButton.getId()) {
                mDroneFlightController.goHome();
            }

            if (v.getId() == mLandButton.getId()) {
                mDroneFlightController.land();
            }

            if (v.getId() == mStartButton.getId()) {
                mDroneFlightController.startMission();
            }

            if (v.getId() == mStopButton.getId()) {
                mDroneFlightController.stopMission();
            }

            if (v.getId() == mResumeButton.getId()) {
                mDroneFlightController.resumeMission();
            }

            if (v.getId() == mPauseButton.getId()) {
                mDroneFlightController.pauseMission();
            }
        } catch (Exception e) {
            mStatusTextView.setText(e.getMessage());
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplication(), toastMsg, Toast.LENGTH_LONG).show());
    }
}
