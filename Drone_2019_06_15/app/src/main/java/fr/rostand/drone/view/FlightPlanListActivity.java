package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fr.rostand.drone.R;
import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.view_model.FlightPlanListViewModel;

public class FlightPlanListActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "FlightPlanListActivity";
    public static final int ADD_FLIGHT_PLAN_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_FLIGHT_PLAN_ACTIVITY_REQUEST_CODE = 2;

    private FloatingActionButton mAddPlanButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FlightPlanListViewModel mFlightPlanListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_plan_list);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFlightPlanListViewModel.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUI() {
        this.setTitle(getString(R.string.title_flight_plan_activity));

        RecyclerView mRecyclerView = findViewById(R.id.flight_plan_list);
        mAddPlanButton = findViewById(R.id.add_flight_plan);
        mSwipeRefreshLayout = findViewById(R.id.refresh_flight_plan_list);

        mAddPlanButton.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        FlightPlanListAdapter mFlightPlanListAdapter = new FlightPlanListAdapter();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFlightPlanListAdapter);

        mFlightPlanListViewModel = ViewModelProviders.of(this).get(FlightPlanListViewModel.class);
        mFlightPlanListViewModel.getFlightPlanList().observe(this, (flightPlanList) -> {
            mFlightPlanListAdapter.setFlightPlanList(flightPlanList);
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mAddPlanButton.getId()) {
            Intent intent = new Intent(this, AddFlightPlanActivity.class);
            startActivityForResult(intent, ADD_FLIGHT_PLAN_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onRefresh() {
        mFlightPlanListViewModel.refresh();
        AsyncTask.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_FLIGHT_PLAN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                long id = data.getLongExtra("id", 0);
                String name = data.getStringExtra("name");
                double lat1 = data.getDoubleExtra("lat1", 0);
                double lon1 = data.getDoubleExtra("lon1", 0);
                double lat2 = data.getDoubleExtra("lat2", 0);
                double lon2 = data.getDoubleExtra("lon2", 0);

                FlightPlan flightPlan = new FlightPlan(id, name, lat1, lon1, lat2, lon2);
                mFlightPlanListViewModel.addFlightPlan(flightPlan);
            } else {
                showToast(getString(R.string.msg_add_plan_canceled));
            }
        }

        if (requestCode == EDIT_FLIGHT_PLAN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                long id = data.getLongExtra("id", 0);
                String name = data.getStringExtra("name");
                double lat1 = data.getDoubleExtra("lat1", 0);
                double lon1 = data.getDoubleExtra("lon1", 0);
                double lat2 = data.getDoubleExtra("lat2", 0);
                double lon2 = data.getDoubleExtra("lon2", 0);

                FlightPlan flightPlan = new FlightPlan(id, name, lat1, lon1, lat2, lon2);
                mFlightPlanListViewModel.editFlightPlan(flightPlan);
            } else {
                showToast(getString(R.string.msg_edit_plan_canceled));
            }
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show());
    }

    /**
     * Inner classes
     */
    private class FlightPlanListAdapter extends RecyclerView.Adapter<FlightPlanViewHolder> {
        private List<FlightPlan> mFlightPlanList;

        public void setFlightPlanList(List<FlightPlan> mFlightPlanList) {
            this.mFlightPlanList = mFlightPlanList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FlightPlanViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.view_holder_flight_plan, viewGroup, false);
            return new FlightPlanViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FlightPlanViewHolder viewHolder, int i) {
            FlightPlan current = mFlightPlanList.get(i);
            viewHolder.display(current);
        }

        @Override
        public int getItemCount() {
            return mFlightPlanList.size();
        }
    }

    private class FlightPlanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mIdTextView;
        private TextView mNameTextView;
        private FloatingActionButton mEditButton;
        private FloatingActionButton mRemoveButton;

        private FlightPlan mFlightPlan;

        public FlightPlanViewHolder(@NonNull View itemView) {
            super(itemView);

            initUI();
        }

        public void initUI() {
            mIdTextView = itemView.findViewById(R.id.text_flight_plan_id);
            mNameTextView = itemView.findViewById(R.id.text_flight_plan_name);
            mEditButton = itemView.findViewById(R.id.button_edit_flight_plan);
            mRemoveButton = itemView.findViewById(R.id.button_delete_flight_plan);

            itemView.setOnClickListener(this);
            mEditButton.setOnClickListener(this);
            mRemoveButton.setOnClickListener(this);
        }

        public void display(FlightPlan flightPlan) {
            this.mFlightPlan = flightPlan;

            mIdTextView.setText(Long.toString(flightPlan.getId()));
            mNameTextView.setText(flightPlan.getName());
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == itemView.getId()) {
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES_ID, 0); // 0 => Private
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("flightPlanId", mFlightPlan.getId());
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), FlightPlanActivity.class);
                intent.putExtra("id", mFlightPlan.getId());
                intent.putExtra("name", mFlightPlan.getName());
                intent.putExtra("lat1", mFlightPlan.getLat1());
                intent.putExtra("lon1", mFlightPlan.getLon1());
                intent.putExtra("lat2", mFlightPlan.getLat2());
                intent.putExtra("lon2", mFlightPlan.getLon2());
                startActivity(intent);
            }

            if (v.getId() == mEditButton.getId()) {
                Intent intent = new Intent(getApplicationContext(), EditFlightPlanActivity.class);
                intent.putExtra("id", mFlightPlan.getId());
                intent.putExtra("name", mFlightPlan.getName());
                intent.putExtra("lat1", mFlightPlan.getLat1());
                intent.putExtra("lon1", mFlightPlan.getLon1());
                intent.putExtra("lat2", mFlightPlan.getLat2());
                intent.putExtra("lon2", mFlightPlan.getLon2());
                startActivityForResult(intent, EDIT_FLIGHT_PLAN_ACTIVITY_REQUEST_CODE);
            }

            if (v.getId() == mRemoveButton.getId()) {
                mFlightPlanListViewModel.deleteFlightPlan(mFlightPlan);
            }
        }
    }
}
