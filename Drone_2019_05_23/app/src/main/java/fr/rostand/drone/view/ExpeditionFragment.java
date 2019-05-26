package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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

import fr.rostand.drone.R;
import fr.rostand.drone.model.DroneFlightController;
import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.view_model.FlightPlanViewModel;

public class ExpeditionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ExpeditionFragment";

    private View mView;

    private SurfaceView mSurfaceView;
    private Button mCalculateButton;
    private TextView mMissionSummaryTextView;
    private TextView mDronePositionTextView;

    private FlightPlanViewModel mFlightPlanViewModel;

    private DroneFlightController mDroneFlightController;
    private FlightPlan mFlightPlan;

    private boolean isMissionCreated = false;

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

        mDroneFlightController = DroneFlightController.getInstance(getActivity().getApplication());

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
        createMission();
    }

    private void initUI() {
        mSurfaceView = mView.findViewById(R.id.surface_view);
        mCalculateButton = mView.findViewById(R.id.button_calculate_mission);
        mMissionSummaryTextView = mView.findViewById(R.id.text_mission_infos);
        mDronePositionTextView = mView.findViewById(R.id.text_drone_position);

        mCalculateButton.setOnClickListener(this);

        mFlightPlanViewModel = ViewModelProviders.of(getActivity()).get(FlightPlanViewModel.class);
        mFlightPlanViewModel.getFlightPlan().observe(this, flightPlan -> {
            mFlightPlan = flightPlan;
            createMission();
        });

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated : start drawing");
                drawCoordList(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void initComponents() {
        mDroneFlightController.initComponents();
    }

    private void createMission() {
        if (mFlightPlan != null) {
            isMissionCreated = mDroneFlightController.createMission(mFlightPlan.getLat1(), mFlightPlan.getLon1(), mFlightPlan.getLat2(), mFlightPlan.getLon2());

            if (!mDroneFlightController.getMissionStatus().isEmpty()) {
                showToast("Status : " + mDroneFlightController.getMissionStatus());
            }
        }
    }

    private void drawCoordList(SurfaceHolder surfaceHolder) {
        AsyncTask.execute(() -> {
            if (surfaceHolder.getSurface().isValid()) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);

                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT);

                int width = canvas.getWidth();
                int height = canvas.getHeight();

                List<Point> pointList = new ArrayList<>();
                pointList.add(new Point(1, 10));
                pointList.add(new Point(2, 13));
                pointList.add(new Point(3, 8));
                pointList.add(new Point(4, 12));
                pointList.add(new Point(5, 15));

                for (Point current : pointList) {
                    canvas.drawPoint(current.x, current.y, paint);
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mCalculateButton.getId()) {
            Log.d(TAG,"lat1:" + mFlightPlan.getLat1() + ";lon1:" + mFlightPlan.getLon1() + ";lat2:" + mFlightPlan.getLat2() + ";lon2:" + mFlightPlan.getLon2());

            if (isMissionCreated) {
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