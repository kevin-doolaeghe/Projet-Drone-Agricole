package fr.rostand.drone.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.toolbox.ImageRequest;

import fr.rostand.drone.R;
import fr.rostand.drone.util.VolleyRequestQueue;
import fr.rostand.drone.view.MainActivity;

public class FinalImageRepository {
    private static final String TAG = "FinalImageRepository";

    private static FinalImageRepository instance;
    private MutableLiveData<Bitmap> mFinalImage;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private FinalImageRepository(Context context) {
        mFinalImage = new MutableLiveData<>();

        mContext = context;
        mSharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
    }

    public static FinalImageRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FinalImageRepository(context);
        }
        return instance;
    }

    public LiveData<Bitmap> getFinalImage() {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);
        long flightPlanId = mSharedPreferences.getLong("flightPlanId", 0);

        ImageRequest imageRequest = new ImageRequest(
                baseUrl + "final-image/download/by-flight-plan/" + flightPlanId,
                response -> {
                    Log.d(TAG, response.toString());

                    mFinalImage.setValue(response);
                },
                0,
                0,
                null,
                Bitmap.Config.RGB_565,
                error -> {

                }
        );

        // Adding request to request queue
        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(imageRequest);

        if (mFinalImage.getValue() == null) {
            mFinalImage.setValue(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pic_phantom));
        }
        return mFinalImage;
    }
}
