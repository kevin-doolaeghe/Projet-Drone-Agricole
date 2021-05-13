package fr.rostand.drone.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.rostand.drone.model.MissionImage;
import fr.rostand.drone.util.VolleyMultipartRequest;
import fr.rostand.drone.util.VolleyRequestQueue;
import fr.rostand.drone.view.MainActivity;

public class MissionImageRepository {
    private static final String TAG = "MissionImageRepository";

    private static MissionImageRepository instance;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    /**
     * Constructor
     */
    private MissionImageRepository(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
    }

    /**
     * Singleton pattern
     */
    public static MissionImageRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MissionImageRepository(context);
        }
        return instance;
    }

    /**
     * Upload image list
     */
    public void uploadImageList(List<MissionImage> missionImageList) {
        for (MissionImage missionImage : missionImageList) {
            uploadImage(missionImage);
        }
    }

    /**
     * Performs HTTP request to send image to server
     */
    private void uploadImage(MissionImage missionImage) {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);
        long flightPlanId = mSharedPreferences.getLong("flightPlanId", 0);

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmssSS").format(new Date());
        String fileName = "img_" + timeStamp + ".jpg";

        Bitmap image = Bitmap.createBitmap(missionImage.getImage());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                baseUrl + "image/upload/" + flightPlanId,
                response -> {
                    Log.d(TAG, response.toString());
                },
                error -> {

                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("number", Long.toString(missionImage.getPosition()));

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> byteData = new HashMap<>();
                byteData.put("file", new DataPart(fileName, byteArray));

                return byteData;
            }
        };

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);
    }

    /**
     * Performs HTTP request to delete image list for selected plan
     */
    public void deleteImageList() {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);
        long flightPlanId = mSharedPreferences.getLong("flightPlanId", 0);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                baseUrl + "image/delete/by-flight-plan/" + flightPlanId,
                response -> {
                    Log.d(TAG, response);
                },
                error -> {

                });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);
    }

    /**
     * Performs HTTP request to delete final image for selected plan
     */
    public void deleteFinalImage() {
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);
        long flightPlanId = mSharedPreferences.getLong("flightPlanId", 0);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                baseUrl + "final-image/delete/by-flight-plan/" + flightPlanId,
                response -> {
                    Log.d(TAG, response);
                },
                error -> {

                });

        VolleyRequestQueue.getInstance(mContext).addToRequestQueue(request);
    }
}
