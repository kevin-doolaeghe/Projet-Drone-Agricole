package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import fr.rostand.drone.R;
import fr.rostand.drone.model.DroneMediaManager;
import fr.rostand.drone.view_model.MissionImageViewModel;
import fr.rostand.drone.view_model.FlightPlanViewModel;

public class SynthesisFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SynthesisFragment";

    private View mView;

    private Button mStartSynthesisButton;
    private Button mSendImagesButton;

    private MissionImageViewModel mMissionImageViewModel;
    private FlightPlanViewModel mFlightPlanViewModel;

    private DroneMediaManager mDroneMediaManager;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initComponents();
        }
    };

    public SynthesisFragment() {

    }

    public static SynthesisFragment newInstance() {
        return (new SynthesisFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_synthesis, container, false);

        mDroneMediaManager = DroneMediaManager.getInstance(getActivity().getApplication());

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

    private void initUI() {
        mStartSynthesisButton = mView.findViewById(R.id.button_start_synthesis);
        mSendImagesButton = mView.findViewById(R.id.button_send_images);

        mStartSynthesisButton.setOnClickListener(this);
        mSendImagesButton.setOnClickListener(this);

        mMissionImageViewModel = ViewModelProviders.of(getActivity()).get(MissionImageViewModel.class);
        mFlightPlanViewModel = ViewModelProviders.of(getActivity()).get(FlightPlanViewModel.class);
    }

    private void initComponents() {
        mDroneMediaManager.initComponents();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mStartSynthesisButton.getId()) {
            mFlightPlanViewModel.startAnalysis();
        }

        if (v.getId() == mSendImagesButton.getId()) {
            // TODO: A finaliser
            /*
            List<MediaFile> files = mDroneMissionManagerViewModel.getDroneMissionManager().getValue().getFileList();
            List<Waypoint> waypoints = mDroneMissionManagerViewModel.getDroneMissionManager().getValue().getWaypointList();

            showToast(waypoints.size() + " waypoints");

            List<Pair<Bitmap, Waypoint>> missionData = new ArrayList<>();
            int index = 0;

            for (Waypoint waypoint : waypoints) {
                Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.pic_phantom);

                try {
                    MediaFile file = files.get(index);
                    image = file.getPreview();
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }

                missionData.add(new Pair<>(image, waypoint));
                index++;
            }

            mMissionImageViewModel.deleteImageListFromPlan();
            //mMissionImageViewModel.setMissionImageList(missionData);
            mMissionImageViewModel.uploadImageList();
            */

            /*
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.pic_phantom);

            List<Pair<Bitmap, Waypoint>> missionData = new ArrayList<>();
            missionData.add(new Pair<>(image, new Waypoint(5, 5, 5)));
            mMissionImageViewModel.deleteImageFromPlan(mFlightPlanId);
            mMissionImageViewModel.init(missionData);
            mMissionImageViewModel.uploadMultipleImage(mFlightPlanId);
            */
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(this.getContext(), toastMsg, Toast.LENGTH_SHORT).show());
    }
}
