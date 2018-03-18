package com.sschutt.billtracker3;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class SignUpActivity extends BaseActivity {

    private static final String TAG = "Sign Up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignUp_OnClick(view);
            }
        });
    }

    private void btnSignUp_OnClick(View view) {
        String Email = ((EditText)findViewById(R.id.email)).getText().toString();
        String Password = ((EditText)findViewById(R.id.password)).getText().toString();

        SignUp(Email, Password);
    }

    private void SignUp(String Email, String Password) {
        String base_url = getString(R.string.base_url);
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap();
        params.put("email", Email);
        params.put("password", Password);
        params.put("api_client_id", getString(R.string.api_client_id));

        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, base_url + "signup");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                base_url + "signup",
                parameters,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Response is: "+ response.toString());
                    handleSignupResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, String.valueOf(error.networkResponse.statusCode));
                    Log.d(TAG, new String(error.networkResponse.data));
                    displayErrorAlert(error);
                }
            }

        );
        queue.add(request);
    }

    private void handleSignupResponse(JSONObject response) {
        try {
            if (response.getString("status").equals("success")) {
                this.setCookieValue("token", response.getString("token"));
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
            else {
                Log.d(TAG, response.getString("message"));
                displayErrorAlert(response.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
