package fr.rostand.drone.view;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import fr.rostand.drone.R;

public class AddFlightPlanActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddFlightPlanActivity";

    private EditText mFlightPlanNameEditText;
    private EditText mFlightPlanLat1EditText;
    private EditText mFlightPlanLon1EditText;
    private EditText mFlightPlanLat2EditText;
    private EditText mFlightPlanLon2EditText;
    private Button mAddFlightPlanButton;
    private FloatingActionButton mOpenMapsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight_plan);

        initUI();
    }

    public void initUI() {
        this.setTitle(getString(R.string.title_add_flight_plan_activity));

        mFlightPlanNameEditText = findViewById(R.id.edit_flight_plan_name);
        mFlightPlanLat1EditText = findViewById(R.id.edit_flight_plan_lat1);
        mFlightPlanLon1EditText = findViewById(R.id.edit_flight_plan_lon1);
        mFlightPlanLat2EditText = findViewById(R.id.edit_flight_plan_lat2);
        mFlightPlanLon2EditText = findViewById(R.id.edit_flight_plan_lon2);
        mAddFlightPlanButton = findViewById(R.id.button_add_flight_plan);
        mOpenMapsButton = findViewById(R.id.button_open_maps);

        mAddFlightPlanButton.setOnClickListener(this);
        mOpenMapsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mAddFlightPlanButton.getId()) {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(mFlightPlanNameEditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLat1EditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLon1EditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLat2EditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLon2EditText.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                long id = 0;
                String name = mFlightPlanNameEditText.getText().toString();
                double lat1 = Double.parseDouble(mFlightPlanLat1EditText.getText().toString());
                double lon1 = Double.parseDouble(mFlightPlanLon1EditText.getText().toString());
                double lat2 = Double.parseDouble(mFlightPlanLat2EditText.getText().toString());
                double lon2 = Double.parseDouble(mFlightPlanLon2EditText.getText().toString());

                replyIntent.putExtra("id", id);
                replyIntent.putExtra("name", name);
                replyIntent.putExtra("lat1", lat1);
                replyIntent.putExtra("lon1", lon1);
                replyIntent.putExtra("lat2", lat2);
                replyIntent.putExtra("lon2", lon2);

                setResult(RESULT_OK, replyIntent);
            }
            finish();
        }

        if (v.getId() == mOpenMapsButton.getId()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
            startActivity(intent);
        }
    }
}
