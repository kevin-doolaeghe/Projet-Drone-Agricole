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
import android.widget.TextView;

import fr.rostand.drone.R;

public class EditFlightPlanActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EditFlightPlanActivity";

    private TextView mFlightPlanIdTextView;
    private EditText mFlightPlanNameEditText;
    private EditText mFlightPlanLat1EditText;
    private EditText mFlightPlanLon1EditText;
    private EditText mFlightPlanLat2EditText;
    private EditText mFlightPlanLon2EditText;
    private Button mEditFlightPlanButton;
    private FloatingActionButton mOpenMapsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_flight_plan);

        initUI();
    }

    public void initUI() {
        this.setTitle(getString(R.string.title_edit_flight_plan_activity));

        mFlightPlanIdTextView = findViewById(R.id.text_flight_plan_id);
        mFlightPlanNameEditText = findViewById(R.id.edit_flight_plan_name);
        mFlightPlanLat1EditText = findViewById(R.id.edit_flight_plan_lat1);
        mFlightPlanLon1EditText = findViewById(R.id.edit_flight_plan_lon1);
        mFlightPlanLat2EditText = findViewById(R.id.edit_flight_plan_lat2);
        mFlightPlanLon2EditText = findViewById(R.id.edit_flight_plan_lon2);
        mEditFlightPlanButton = findViewById(R.id.button_edit_flight_plan);
        mOpenMapsButton = findViewById(R.id.button_open_maps);

        Intent data = getIntent();
        long id = data.getLongExtra("id", 0);
        String name = data.getStringExtra("name");
        double lat1 = data.getDoubleExtra("lat1", 0);
        double lon1 = data.getDoubleExtra("lon1", 0);
        double lat2 = data.getDoubleExtra("lat2", 0);
        double lon2 = data.getDoubleExtra("lon2", 0);

        mFlightPlanIdTextView.setText(Long.toString(id));
        mFlightPlanNameEditText.setText(name);
        mFlightPlanLat1EditText.setText(Double.toString(lat1));
        mFlightPlanLon1EditText.setText(Double.toString(lon1));
        mFlightPlanLat2EditText.setText(Double.toString(lat2));
        mFlightPlanLon2EditText.setText(Double.toString(lon2));

        mEditFlightPlanButton.setOnClickListener(this);
        mOpenMapsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mEditFlightPlanButton.getId()) {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(mFlightPlanNameEditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLat1EditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLon1EditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLat2EditText.getText())
                    && TextUtils.isEmpty(mFlightPlanLon2EditText.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                long id = Long.parseLong(mFlightPlanIdTextView.getText().toString());
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

        // Open Google Maps
        if (v.getId() == mOpenMapsButton.getId()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
            startActivity(intent);
        }
    }
}
