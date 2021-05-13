package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.rostand.drone.R;
import fr.rostand.drone.view_model.FinalImageViewModel;

public class ViewerFragment extends Fragment implements View.OnClickListener {
    private View mView;

    private ImageView mImageView;
    private FloatingActionButton mDownloadImageButton;

    private FinalImageViewModel mFinalImageViewModel;

    public ViewerFragment() {

    }

    public static ViewerFragment newInstance() {
        return (new ViewerFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_viewer, container, false);

        initUI();

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mFinalImageViewModel.refresh();
    }

    /**
     * Init UI
     */
    private void initUI() {
        mImageView = mView.findViewById(R.id.final_image_view);
        mDownloadImageButton = mView.findViewById(R.id.download_image_button);

        mDownloadImageButton.setOnClickListener(this);

        mFinalImageViewModel = ViewModelProviders.of(getActivity()).get(FinalImageViewModel.class);
        mFinalImageViewModel.getFinalImage().observe(this, bitmap -> {
            // Display image
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageView.setImageBitmap(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.pic_phantom));
            }
        });
    }

    @Override
    public void onClick(View v) {
        // Save image in device
        if (v.getId() == mDownloadImageButton.getId()) {
            downloadFile();
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(this.getContext(), toastMsg, Toast.LENGTH_LONG).show());
    }

    /**
     * Save image in device
     */
    private void downloadFile() {
        AsyncTask.execute(() -> {
            Bitmap image = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();

            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());

            String dirName = "drone";
            String fileName = "img_" + timeStamp + ".jpg";
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dirName);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, fileName);

            try {
                FileOutputStream writer = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG, 90, writer);
                writer.flush();
                writer.close();
                showToast(getString(R.string.msg_download_success));
            } catch (Exception e) {
                showToast(e.getMessage());
            }
        });
    }
}
