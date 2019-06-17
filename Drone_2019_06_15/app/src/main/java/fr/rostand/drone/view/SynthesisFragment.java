package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.rostand.drone.R;
import fr.rostand.drone.model.MissionImage;
import fr.rostand.drone.view_model.DroneMediaManagerViewModel;
import fr.rostand.drone.view_model.MissionImageViewModel;
import fr.rostand.drone.view_model.FlightPlanViewModel;

public class SynthesisFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SynthesisFragment";

    private View mView;

    private Button mSetFlightPlanData;
    private Button mRefreshImageListButton;
    private Button mDeleteDroneFileListButton;
    private Button mDeleteFinalImageButton;
    private Button mDeleteImageListButton;
    private Button mSendImageListButton;
    private Button mStartSynthesisButton;

    private MissionImageViewModel mMissionImageViewModel;
    private FlightPlanViewModel mFlightPlanViewModel;
    private DroneMediaManagerViewModel mDroneMediaManagerViewModel;

    private List<Bitmap> mImageList;

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

        mImageList = new ArrayList<>();

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
    }

    private void initUI() {
        mSetFlightPlanData = mView.findViewById(R.id.button_set_flight_plan_data);
        mRefreshImageListButton = mView.findViewById(R.id.button_refresh_image_list);
        mDeleteDroneFileListButton = mView.findViewById(R.id.button_delete_drone_file_list);
        mDeleteFinalImageButton = mView.findViewById(R.id.button_delete_final_image);
        mDeleteImageListButton = mView.findViewById(R.id.button_delete_image_list);
        mSendImageListButton = mView.findViewById(R.id.button_send_image_list);
        mStartSynthesisButton = mView.findViewById(R.id.button_start_synthesis);

        mSetFlightPlanData.setOnClickListener(this);
        mRefreshImageListButton.setOnClickListener(this);
        mDeleteDroneFileListButton.setOnClickListener(this);
        mDeleteFinalImageButton.setOnClickListener(this);
        mDeleteImageListButton.setOnClickListener(this);
        mSendImageListButton.setOnClickListener(this);
        mStartSynthesisButton.setOnClickListener(this);

        mMissionImageViewModel = ViewModelProviders.of(getActivity()).get(MissionImageViewModel.class);
        mFlightPlanViewModel = ViewModelProviders.of(getActivity()).get(FlightPlanViewModel.class);
        mDroneMediaManagerViewModel = ViewModelProviders.of(getActivity()).get(DroneMediaManagerViewModel.class);
        mDroneMediaManagerViewModel.getImageList().observe(this, imageList -> {
            mImageList = imageList;
            Log.d(TAG, imageList.size() + " image(s) loaded !");
        });
    }

    private void initComponents() {
        mDroneMediaManagerViewModel.initComponents();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mSetFlightPlanData.getId()) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
            int latAxisImgCount = sharedPreferences.getInt("latAxisImgCount", 0);
            int lonAxisImgCount = sharedPreferences.getInt("lonAxisImgCount", 0);
            mFlightPlanViewModel.addDataToFlightPlan(latAxisImgCount, lonAxisImgCount);
        }

        if (v.getId() == mRefreshImageListButton.getId()) {
            mDroneMediaManagerViewModel.refreshFileList();
        }

        if (v.getId() == mDeleteDroneFileListButton.getId()) {
            mDroneMediaManagerViewModel.deleteFileList();
        }

        if (v.getId() == mDeleteFinalImageButton.getId()) {
            mMissionImageViewModel.deleteFinalImage();
        }

        if (v.getId() == mDeleteImageListButton.getId()) {
            mMissionImageViewModel.deleteImageList();
        }

        if (v.getId() == mSendImageListButton.getId()) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
            int latAxisImgCount = sharedPreferences.getInt("latAxisImgCount", 0);
            int lonAxisImgCount = sharedPreferences.getInt("lonAxisImgCount", 0);
            mFlightPlanViewModel.addDataToFlightPlan(latAxisImgCount, lonAxisImgCount);

            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.pic_phantom);
            List<MissionImage> missionImageList = new ArrayList<>();
            int totalImgCount = latAxisImgCount * lonAxisImgCount;
            for (int i = 0; i < totalImgCount; i++) {
                try {
                    image = mImageList.get(i);
                } catch (Exception e) {
                    Log.d(TAG, "Error : " + e.getMessage());
                }
                missionImageList.add(new MissionImage(image, totalImgCount - i));
                Log.d(TAG, "Image position : " + (totalImgCount - i));
            }
            Log.d(TAG, missionImageList.size() + " images to send.");

            mMissionImageViewModel.setImageList(missionImageList);
            mMissionImageViewModel.uploadImageList();
        }

        if (v.getId() == mStartSynthesisButton.getId()) {
            mFlightPlanViewModel.startAnalysis();
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(this.getContext(), toastMsg, Toast.LENGTH_SHORT).show());
    }
}
