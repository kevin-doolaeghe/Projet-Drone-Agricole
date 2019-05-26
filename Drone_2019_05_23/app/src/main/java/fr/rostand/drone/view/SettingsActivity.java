package fr.rostand.drone.view;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import fr.rostand.drone.R;
import fr.rostand.drone.util.VolleyRequestQueue;

import static android.view.Gravity.CENTER;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SettingsActivity";

    private EditText mUrlEditText;
    private EditText mMinAltitudeEditText;
    private EditText mMaxAltitudeEditText;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initUI();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void initUI() {
        mUrlEditText = findViewById(R.id.edit_url);
        mMinAltitudeEditText = findViewById(R.id.edit_min_altitude);
        mMaxAltitudeEditText = findViewById(R.id.edit_max_altitude);

        mSharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0);
        String baseUrl = mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL);
        float minAltitude = mSharedPreferences.getFloat("minAltitude", 10);
        float maxAltitude = mSharedPreferences.getFloat("maxAltitude", 15);

        mUrlEditText.setText(baseUrl);
        mMinAltitudeEditText.setText(Float.toString(minAltitude));
        mMaxAltitudeEditText.setText(Float.toString(maxAltitude));

        mUrlEditText.setOnClickListener(this);
        mMinAltitudeEditText.setOnClickListener(this);
        mMaxAltitudeEditText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mUrlEditText.getId()) {
            InputFilter[] inputFilter = { new InputFilter.LengthFilter(30) };

            EditText result = new EditText(this);
            result.setGravity(CENTER);
            result.setText(mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL));
            result.setHint(R.string.hint_ip);
            result.setSingleLine(true);
            result.setHorizontallyScrolling(true);
            result.setInputType(InputType.TYPE_CLASS_TEXT);
            result.setFilters(inputFilter);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setCancelable(true)
                    .setTitle(getString(R.string.title_ip))
                    .setView(result)
                    .setPositiveButton(R.string.action_validate, (DialogInterface dialog, int which) -> {
                        if (!TextUtils.isEmpty(result.getText())) {
                            String baseUrl = result.getText().toString();

                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("baseUrl", baseUrl);
                            editor.apply();
                        }
                        dialog.dismiss();
                        mUrlEditText.setText(mSharedPreferences.getString("baseUrl", VolleyRequestQueue.BASE_URL));
                    })
                    .show();
        }

        if (v.getId() == mMinAltitudeEditText.getId()) {
            InputFilter[] inputFilter = { new InputFilter.LengthFilter(10) };

            EditText result = new EditText(this);
            result.setGravity(CENTER);
            result.setText(Float.toString(mSharedPreferences.getFloat("minAltitude", 10)));
            result.setHint(R.string.hint_min_altitude);
            result.setSingleLine(true);
            result.setHorizontallyScrolling(true);
            result.setInputType(InputType.TYPE_CLASS_NUMBER);
            result.setFilters(inputFilter);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setCancelable(true)
                    .setTitle(getString(R.string.title_min_alititude))
                    .setView(result)
                    .setPositiveButton(R.string.action_validate, (DialogInterface dialog, int which) -> {
                        if (!TextUtils.isEmpty(result.getText())) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            float minAltitude = Float.valueOf(result.getText().toString());

                            if (minAltitude > mSharedPreferences.getFloat("maxAltitude", 15) || minAltitude < 2) {
                                if (minAltitude < mSharedPreferences.getFloat("minAltitude", 10)) {
                                    editor.putFloat("minAltitude", mSharedPreferences.getFloat("minAltitude", 10));
                                } else if (minAltitude < 2) {
                                    editor.putFloat("minAltitude", 2);
                                }
                            } else {
                                editor.putFloat("minAltitude", minAltitude);
                            }

                            editor.apply();
                        }
                        dialog.dismiss();
                        mMinAltitudeEditText.setText(Float.toString(mSharedPreferences.getFloat("minAltitude", 10)));
                    })
                    .show();
        }

        if (v.getId() == mMaxAltitudeEditText.getId()) {
            InputFilter[] inputFilter = { new InputFilter.LengthFilter(10) };

            EditText result = new EditText(this);
            result.setGravity(CENTER);
            result.setText(Float.toString(mSharedPreferences.getFloat("maxAltitude", 15)));
            result.setHint(R.string.hint_max_altitude);
            result.setSingleLine(true);
            result.setHorizontallyScrolling(true);
            result.setInputType(InputType.TYPE_CLASS_NUMBER);
            result.setFilters(inputFilter);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setCancelable(true)
                    .setTitle(getString(R.string.title_max_alititude))
                    .setView(result)
                    .setPositiveButton(R.string.action_validate, (DialogInterface dialog, int which) -> {
                        if (!TextUtils.isEmpty(result.getText())) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            float maxAltitude = Float.valueOf(result.getText().toString());

                            if (maxAltitude < mSharedPreferences.getFloat("minAltitude", 10) || maxAltitude > 100) {
                                if (maxAltitude < mSharedPreferences.getFloat("minAltitude", 10)) {
                                    editor.putFloat("maxAltitude", mSharedPreferences.getFloat("minAltitude", 10));
                                } else if (maxAltitude > 100) {
                                    editor.putFloat("maxAltitude", 100);
                                }
                            } else {
                                editor.putFloat("maxAltitude", maxAltitude);
                            }

                            editor.apply();
                        }
                        dialog.dismiss();
                        mMaxAltitudeEditText.setText(Float.toString(mSharedPreferences.getFloat("maxAltitude", 15)));
                    })
                    .show();
        }
    }
}
