package com.sschutt.billtracker3;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import android.content.SharedPreferences;



public class HttpDataJob extends Job {
    final String SHARED_PREFERENCES_NAME = "preferences";
    private String TAG = "Bill Entry";
    public static final int PRIORITY = 1;
    private HttpDataOptions options;

    public HttpDataJob(HttpDataOptions options) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.

        super(new Params(PRIORITY).requireNetwork().persist());
        this.options = options;
    }
    @Override
    public void onAdded() {
        Log.d(TAG, "Job Added");
    }
    @Override
    public void onRun() throws Throwable {
        // Job logic goes here. In this example, the network call to post to Twitter is done here.
        // All work done here should be synchronous, a job is removed from the queue once 
        // onRun() finishes.
        Log.d(TAG, "running");
        String base_url = this.getApplicationContext().getString(R.string.base_url);
        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());
        final String token = this.getCookieValue("token");

        JSONObject parameters = new JSONObject(this.options.params);
        Log.d(TAG, String.format("preparing to make a %s request data to %s ", this.options.method, base_url + this.options.url));
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(this.options.method,
                base_url + this.options.url,
                parameters,
                future,
                future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("token", token);
                return headers;
            }
        };
        queue.add(request);

        Log.d(TAG, "sending data to: " + base_url + options.url);
        JSONObject response = future.get(30, TimeUnit.SECONDS);
        Log.d(TAG, "Response is: "+ response.toString());
        BillTrackerApplication app = ((BillTrackerApplication) this.getApplicationContext());
        if (this.options.save_output) {
            if (response.getString("status").equals("success")) {
                Log.d(TAG, String.format("writing response to couchbase"));
                app.clearDatabase();
                Database db = app.getDatabase();
                this.saveDocs(response.getJSONArray("items"), db);
            } else {
                throw new Exception(String.format("Error loading http data: %s", response.toString()));
            }
        }

        if (this.options.refresh_bills) {
            JobManager jobManager = app.getJobManager();
            HttpDataOptions opts = new HttpDataOptions(new HashMap<String, String>(), Request.Method.GET, "billReport", true, false);
            jobManager.addJobInBackground(new HttpDataJob(opts));
        }
        return;
    }

    protected void saveDocs(JSONArray items, Database db) throws Throwable {
        for(int index = 0; index < items.length(); index++) {
            JSONObject obj = items.getJSONObject(index);
            MutableDocument doc = new MutableDocument();
            doc.setString("amount", obj.getString("amount"));
            doc.setString("currency", obj.getString("currency"));
            doc.setString("category", obj.getString("category"));
            doc.setLong("date", obj.getLong("date"));
            doc.setString("server_id", obj.getString("_id"));
            db.save(doc);
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        Log.d(TAG, "Attempting rerun");
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
    @Override
    protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    }


    protected String getCookieValue(String key) {
        SharedPreferences settings = this.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return settings.getString(key, "");
    }
}