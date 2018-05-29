package com.sschutt.billtracker3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;

public class BaseLoggedInActivity extends BaseActivity {

    BottomNavigationView bottomNav;
    private String TAG = "BaseLoggedInActivity";
    Context context;
    private int tab_id;
    protected JobManager jobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        jobManager = ((BillTrackerApplication)this.getApplication()).getJobManager();
        super.onCreate(savedInstanceState);



        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            tab_id = 1;
        else
            tab_id = bundle.getInt("tab_id");
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav = (BottomNavigationView)findViewById(R.id.navigation);
        bottomNav.setSelectedItemId(tab_id);
        context = this.getApplicationContext();
        bottomNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCookieValue("token", null);
                Intent intent = new Intent(context, StartActivity.class);
                startActivity(intent);
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("tab_id", R.id.navigation_home);
                    startActivity(intent);
                    return true;
                case R.id.navigation_list:
                    intent = new Intent(context, BillListActivity.class);
                    intent.putExtra("tab_id", R.id.navigation_list);
                    startActivity(intent);
                    return true;
                case R.id.navigation_report:
                    intent = new Intent(context, ReportActivity.class);
                    intent.putExtra("tab_id", R.id.navigation_report);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

}
