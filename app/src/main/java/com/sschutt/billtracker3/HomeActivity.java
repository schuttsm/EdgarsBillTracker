package com.sschutt.billtracker3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends BaseActivity {

    private TextView mTextMessage;
    private String TAG = "Bill Entry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mTextMessage = (TextView) findViewById(R.id.message);

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
    }

    private void save_OnClick(View view) {
        String amount = ((EditText)findViewById(R.id.amount)).getText().toString();
        String currency = ((Spinner)findViewById(R.id.currency)).getSelectedItem().toString();
        String category = ((EditText)findViewById(R.id.category)).getText().toString();

        Save(amount, currency, category);
    }

    private void Save(String amount, String currency, String category) {
        String base_url = getString(R.string.base_url);
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("category", category);

        JSONObject parameters = new JSONObject(params);
        final String token = this.getCookieValue("token");

        Log.d(TAG, base_url + "bill");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                base_url + "bill",
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response is: "+ response.toString());
                        handleSaveResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayErrorAlert(error);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("token", token);
                return headers;
            }
        };
        queue.add(request);
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
