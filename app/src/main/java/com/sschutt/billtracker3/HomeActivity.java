package com.sschutt.billtracker3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.birbit.android.jobqueue.AsyncAddCallback;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.callback.JobManagerCallback;
import com.birbit.android.jobqueue.config.Configuration;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.PropertyExpression;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HomeActivity extends BaseLoggedInActivity {
    protected TextView status_bar;
    private int num_updates;
    private String TAG = "Bill Entry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        status_bar = (TextView) findViewById(R.id.status_bar);
        num_updates = 0;
        jobManager.addCallback(this.setupStatusBarMessaging());

        Spinner dropdown = findViewById(R.id.currency);
        String[] items = new String[]{"ALL", "Euro", "USD"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_OnClick(view);
            }
        });


        setCategoryAutoComplete();
    }

    private void setCategoryAutoComplete() {
        try {
            Database db = ((BillTrackerApplication) this.getApplication()).getDatabase();
            Query query = QueryBuilder.select(SelectResult.property("category"))
                    .from(DataSource.database(db))
                    .groupBy(PropertyExpression.property("category"))
                    .orderBy(Ordering.property("category"));
            ResultSet result = query.execute();
            List<Result> result_rows = result.allResults();
            ArrayList<String> categories = new ArrayList<String>();
            for (int index = 0; index < result_rows.size(); index++) {
                categories.add(result_rows.get(index).getString("category"));
            }
            ArrayAdapter<String> category_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
            AutoCompleteTextView category = ((AutoCompleteTextView) findViewById(R.id.category));
            category.setAdapter(category_adapter);
        }
        catch(CouchbaseLiteException e) {
            Log.e(TAG, e.toString());
            status_bar.setText(e.toString());
        }
    }

    private JobManagerCallback setupStatusBarMessaging() {
        final String cache_filename = this.getFilesDir().toString() + "/billReport.json";
        return new JobManagerCallback() {
            @Override
            public void onJobAdded(@NonNull Job job) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        num_updates++;
                        status_bar.setText("Uploading " + Integer.toString(num_updates) + " bills");
                    }
                });
            }

            @Override
            public void onJobRun(@NonNull Job job, int resultCode) {

            }

            @Override
            public void onJobCancelled(@NonNull Job job, boolean byCancelRequest, @Nullable Throwable throwable) {
                final String message = throwable.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        status_bar.setText("Error saving bill" + message);
                    }
                });
            }

            @Override
            public void onDone(@NonNull Job job) {

            }

            @Override
            public void onAfterJobRun(@NonNull Job job, int resultCode) {
                Log.d(TAG, Integer.toString(resultCode));
                final int code = resultCode;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (code == 1) {
                            num_updates--;
                            if (num_updates == 0) {
                                status_bar.setText("Success");
                                setCategoryAutoComplete();

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
                                Log.d(TAG, "Uploading " + Integer.toString(num_updates) + " bills");
                                status_bar.setText("Uploading " + Integer.toString(num_updates) + " bills");
                            }
                        }
                        else {
                            Log.d(TAG, "Error running job, resultCode" + code);
                        }
                    }
                });

            }
        };
    }

    private void save_OnClick(View view) {
        String amount = ((EditText)findViewById(R.id.amount)).getText().toString();
        String currency = ((Spinner)findViewById(R.id.currency)).getSelectedItem().toString();
        String category = ((EditText)findViewById(R.id.category)).getText().toString();

        Save(amount, currency, category);
    }

    private void Save(String amount, String currency, String category) {
        final String token = this.getCookieValue("token");
        Log.d(TAG, token);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("category", category);
        params.put("timezone", Integer.toString(TimeZone.getDefault().getRawOffset()));
        jobManager.addJobInBackground(new HttpDataJob(new HttpDataOptions(params, Request.Method.POST, "bill", false, true)));

        ((EditText)findViewById(R.id.amount)).setText("");
        ((Spinner)findViewById(R.id.currency)).setSelection(0);
        ((EditText)findViewById(R.id.category)).setText("");

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void handleSaveResponse(JSONObject response) {
        try {
            if (response.getString("status").equals("success")) {
                ((EditText)findViewById(R.id.amount)).setText("");
                ((Spinner)findViewById(R.id.currency)).setSelection(0);
                ((EditText)findViewById(R.id.category)).setText("");
                ((EditText)findViewById(R.id.amount)).requestFocus();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Success")
                        .setPositiveButton("OK", null);

                final AlertDialog dlg = builder.create();
                dlg.show();

                final Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        dlg.dismiss();
                        t.cancel();
                    }
                }, 1500);
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Signup error")
                        .setMessage(response.getString("message"))
                        .show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
