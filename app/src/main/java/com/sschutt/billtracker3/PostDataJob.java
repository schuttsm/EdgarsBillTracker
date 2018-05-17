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
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import android.content.SharedPreferences;

// A job to send a tweet

public class PostDataJob extends Job {
    final String SHARED_PREFERENCES_NAME = "preferences";
    private String TAG = "Bill Entry";
    public static final int PRIORITY = 1;
    private String amount;
    private String category;
    private String currency;
    private String token;

    public PostDataJob(String amount, String category, String currency, String token) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.

        super(new Params(PRIORITY).requireNetwork().persist());
        this.amount = amount;
        this.category = category;
        this.currency = currency;
        this.token = token;
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

        Map<String, String> params = new HashMap();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("category", category);

        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, "preparing to post data to: " + base_url);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                base_url + "bill",
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

        Log.d(TAG, "Posting data to: " + base_url + "bill");
        JSONObject response = future.get(20, TimeUnit.SECONDS);
        Log.d(TAG, "Response is: "+ response.toString());
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