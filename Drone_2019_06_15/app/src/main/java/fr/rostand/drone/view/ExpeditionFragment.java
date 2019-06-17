package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dji.common.mission.waypoint.Waypoint;
import fr.rostand.drone.R;
import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.view_model.DroneFlightControllerViewModel;
import fr.rostand.drone.view_model.FlightPlanViewModel;

public class ExpeditionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ExpeditionFragment";

    private View mView;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button mCalculateButton;
    private TextView mMissionTextView;
    private TextView mStatusTextView;
    private TextView mLocationTextView;

    private FlightPlanViewModel mFlightPlanViewModel;
    private DroneFlightControllerViewModel mDroneFlightControllerViewModel;

    private FlightPlan mFlightPlan;
    private List<Waypoint> mWaypointList;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initComponents();
        }
    };

    public ExpeditionFragment() {

    }

    public static ExpeditionFragment newInstance() {
        return (new ExpeditionFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_expedition, container, false);

        mFlightPlan = new FlightPlan();
        mWaypointList = new ArrayList<>();

        initUI();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FLAG_CONNECTION_CHANGE);
        mView.getContext().registerReceiver(mReceiver, filter);

        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mView.getContext().unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        initComponents();
        generateWaypointList();
        drawCoordList(mWaypointList);
    }

    private void initUI() {
        mSurfaceView = mView.findViewById(R.id.surface_view);
        mCalculateButton = mView.findViewById(R.id.button_calculate_mission);
        mMissionTextView = mView.findViewById(R.id.text_mission_infos);
        mStatusTextView = mView.findViewById(R.id.text_mission_status);
        mLocationTextView = mView.findViewById(R.id.text_drone_position);

        mCalculateButton.setOnClickListener(this);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
                Log.d(TAG, "onSurfaceCreated");
                drawCoordList(mWaypointList);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        mFlightPlanViewModel = ViewModelProviders.of(getActivity()).get(FlightPlanViewModel.class);
        mFlightPlanViewModel.getFlightPlan().observe(this, flightPlan -> {
            mFlightPlan = flightPlan;
            Log.d(TAG,"lat1:" + mFlightPlan.getLat1() + ";lon1:" + mFlightPlan.getLon1() + ";lat2:" + mFlightPlan.getLat2() + ";lon2:" + mFlightPlan.getLon2());
            generateWaypointList();
        });

        mDroneFlightControllerViewModel = ViewModelProviders.of(getActivity()).get(DroneFlightControllerViewModel.class);
        mDroneFlightControllerViewModel.getWaypointList().observe(this, waypointList -> {
            Log.d(TAG, waypointList.toString());
            mWaypointList = waypointList;
            drawCoordList(mWaypointList);
        });
        mDroneFlightControllerViewModel.getMission().observe(this, mission -> {
            mMissionTextView.setText(mission);
        });
        mDroneFlightControllerViewModel.getStatus().observe(this, status -> {
            mStatusTextView.setText(status);
        });
        mDroneFlightControllerViewModel.getLocation().observe(this, location -> {
            mLocationTextView.setText(location);
        });
    }

    private void initComponents() {
        mDroneFlightControllerViewModel.initComponents();
    }

    private void generateWaypointList() {
        mDroneFlightControllerViewModel.generateWaypointList(mFlightPlan.getLat1(), mFlightPlan.getLon1(), mFlightPlan.getLat2(), mFlightPlan.getLon2());
    }

    private void drawCoordList(List<Waypoint> waypointList) {
        Log.d(TAG, "onDraw");

        AsyncTask.execute(() -> {
            if (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid()) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.LTGRAY);

                double lat1 = mFlightPlan.getLat1();
                double lon1 = mFlightPlan.getLon1();
                double lat2 = mFlightPlan.getLat2();
                double lon2 = mFlightPlan.getLon2();

                if (lat1 > lat2) {
                    double temp = lat1;
                    lat1 = lat2;
                    lat2 = temp;
                }

                if (lon1 > lon2) {
                    double temp = lon1;
                    lon1 = lon2;
                    lon2 = temp;
                }

                int width = canvas.getWidth();
                int height = canvas.getHeight();
                double margin = .4;

                for (int i = 0; i < 4; i++) {
                    double x1 = 0, y1 = 0, x2 = 0, y2 = 0;

                    if (i == 0) {
                        x1 = lon1;
                        y1 = lat2;
                        x2 = lon1;
                        y2 = lat1;
                    } else if (i == 1) {
                        x1 = lon1;
                        y1 = lat2;
                        x2 = lon2;
                        y2 = lat2;
                    } else if (i == 2) {
                        x1 = lon2;
                        y1 = lat1;
                        x2 = lon1;
                        y2 = lat1;
                    } else if (i == 3) {
                        x1 = lon2;
                        y1 = lat1;
                        x2 = lon2;
                        y2 = lat2;
                    }

                    canvas.drawLine(
                            (float) ((x1 - lon1) * (width - (margin * width)) / (lon2 - lon1) + margin * width / 2),
                            (float) ((y1 - lat2) * (height - (margin * height)) / (lat1 - lat2) + margin * height / 2),
                            (float) ((x2 - lon1) * (width - (margin * width)) / (lon2 - lon1) + margin * width / 2),
                            (float) ((y2 - lat2) * (height - (margin * height)) / (lat1 - lat2) + margin * height / 2),
                            paint);
                }

                for (Waypoint waypoint : waypointList) {
                    int x = (int) ((waypoint.coordinate.getLongitude() - lon1) * (width - (margin * width)) / (lon2 - lon1) + margin * width / 2);
                    int y = (int) ((waypoint.coordinate.getLatitude() - lat2) * (height - (margin * height)) / (lat1 - lat2) + margin * height / 2);
                    canvas.drawCircle(x, y, 5, paint);
                }

                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mCalculateButton.getId()) {
            if (mFlightPlan != null && mDroneFlightControllerViewModel.createMission()) {
                Intent intent = new Intent(getActivity(), MissionActivity.class);
                startActivity(intent);
            }
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(mView.getContext(), toastMsg, Toast.LENGTH_SHORT).show());
    }
}