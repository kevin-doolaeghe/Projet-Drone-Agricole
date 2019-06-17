package fr.rostand.drone.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fr.rostand.drone.R;
import fr.rostand.drone.model.FlightPlan;
import fr.rostand.drone.view_model.DroneFlightControllerViewModel;
import fr.rostand.drone.view_model.DroneMediaManagerViewModel;
import fr.rostand.drone.view_model.FinalImageViewModel;
import fr.rostand.drone.view_model.FlightPlanViewModel;
import fr.rostand.drone.view_model.MissionImageViewModel;

public class FlightPlanActivity extends AppCompatActivity {
    private static final String TAG = "FlightPlanActivity";

    private FlightPlanViewModel mFlightPlanViewModel;
    private FinalImageViewModel mFinalImageViewModel;
    private MissionImageViewModel mMissionImageViewModel;
    private DroneFlightControllerViewModel mDroneFlightControllerViewModel;
    private DroneMediaManagerViewModel mDroneMediaManagerViewModel;

    private FlightPlan mFlightPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_plan);

        Intent intent = getIntent();
        long id = intent.getLongExtra("id", 0);
        String name = intent.getStringExtra("name");
        double lat1 = intent.getDoubleExtra("lat1", 0);
        double lon1 = intent.getDoubleExtra("lon1", 0);
        double lat2 = intent.getDoubleExtra("lat2", 0);
        double lon2 = intent.getDoubleExtra("lon2", 0);
        mFlightPlan = new FlightPlan(id, name, lat1, lon1, lat2, lon2);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDroneMediaManagerViewModel.destroyComponents();
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
        setTitle(R.string.title_menu_activity);

        mFlightPlanViewModel = ViewModelProviders.of(this).get(FlightPlanViewModel.class);
        mFinalImageViewModel = ViewModelProviders.of(this).get(FinalImageViewModel.class);
        mMissionImageViewModel = ViewModelProviders.of(this).get(MissionImageViewModel.class);
        mDroneFlightControllerViewModel = ViewModelProviders.of(this).get(DroneFlightControllerViewModel.class);
        mDroneMediaManagerViewModel = ViewModelProviders.of(this).get(DroneMediaManagerViewModel.class);

        mFlightPlanViewModel.setFlightPlan(mFlightPlan);

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                switch (i) {
                    case 0:
                        return ExpeditionFragment.newInstance();
                    case 1:
                        return SynthesisFragment.newInstance();
                    case 2:
                        return ViewerFragment.newInstance();
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.title_expedition_tab);
                    case 1:
                        return getString(R.string.title_synthesis_tab);
                    case 2:
                        return getString(R.string.title_viewer_tab);
                    default:
                        return null;
                }
            }
        };

        ViewPager viewPager = findViewById(R.id.menu_view_pager);
        viewPager.setAdapter(fragmentPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.menu_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
