package com.sschutt.billtracker3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.callback.JobManagerCallback;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.OrderBy;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BillListActivity extends BaseLoggedInActivity {
    private TextView status_bar;
    private ListView bill_grid;
    private String TAG = "Bill List";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        status_bar = (TextView) findViewById(R.id.status_bar);
        bill_grid = (ListView) findViewById(R.id.bill_grid);

        jobManager.addCallback(this.setupStatusBarMessaging());

        HttpDataOptions opts = new HttpDataOptions(new HashMap<String, String>(), Request.Method.GET, "billReport", true, false);
        jobManager.addJobInBackground(new HttpDataJob(opts));
        BindDataToGrid();
    }

    private List<BillReportRow> getBillReportRows() {
        try {
            Database db = ((BillTrackerApplication)this.getApplication()).getDatabase();
            Query query = QueryBuilder.select(SelectResult.property("amount"),
                    SelectResult.property("category"),
                    SelectResult.property("server_id"),
                    SelectResult.property("date"),
                    SelectResult.property("currency"))
                    .from(DataSource.database(db))
                    .orderBy(Ordering.property("date").descending());
            ResultSet result = query.execute();
            List<Result> result_rows = result.allResults();
            List<BillReportRow> rows = new ArrayList<BillReportRow>();
            for(int index = 0; index < result_rows.size(); index++) {
                Result obj = result_rows.get(index);
                BillReportRow row = new BillReportRow();
                row.amount = obj.getString("amount");
                row.category = obj.getString("category");
                row.currency = obj.getString("currency");
                row.date = new Date(obj.getLong("date"));
                row.id = obj.getString("server_id");
                rows.add(row);
            }
            return rows;
        }
        catch (CouchbaseLiteException ex) {
            Log.e(TAG, ex.toString());
            status_bar.setText(ex.toString());
            return new ArrayList<BillReportRow>();
        }
    }

    private void BindDataToGrid() {
        List<BillReportRow> data = getBillReportRows();
        bill_grid.setAdapter(new BillReportListAdapter(data, jobManager, this));
    }

    private JobManagerCallback setupStatusBarMessaging() {
        return new JobManagerCallback() {
            @Override
            public void onJobAdded(@NonNull Job job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status_bar.setText("Running");
                    }
                });
            }

            @Override
            public void onJobRun(@NonNull Job job, int resultCode) {

            }

            @Override
            public void onJobCancelled(@NonNull Job job, boolean byCancelRequest, @Nullable Throwable throwable) {
                status_bar.setText("Error retrieving bills");
            }

            @Override
            public void onDone(@NonNull Job job) {

            }

            @Override
            public void onAfterJobRun(@NonNull Job job, int resultCode) {
                if (resultCode == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status_bar.setText("Success");
                            BindDataToGrid();
                        }
                    });
                    final Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    status_bar.setText("");
                                    t.cancel();
                                }
                            });
                        }
                    }, 1500);
                }
                else {
                    final int code = resultCode;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String err_msg = "Error running job, resultCode" + code;
                            status_bar.setText(err_msg);
                            Log.d(TAG, err_msg);
                        }
                    });
                }
            }
        };
    }


}
