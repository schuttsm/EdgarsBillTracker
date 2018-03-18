package com.sschutt.billtracker3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends BaseActivity {
    String TAG = "Sign In";
    int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        findViewById(R.id.SignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn_OnClick(view);
            }
        });
    }

    private void btnSignIn_OnClick(View view) {
        String Email = ((EditText)findViewById(R.id.email)).getText().toString();
        String Password = ((EditText)findViewById(R.id.password)).getText().toString();
        SignIn(Email, Password);
    }

    private void SignIn(String Email, String Password) {
        String base_url = getString(R.string.base_url);
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap();
        params.put("email", Email);
        params.put("password", Password);
        params.put("api_client_id", getString(R.string.api_client_id));

        JSONObject parameters = new JSONObject(params);

        Log.d(TAG, base_url + "signin");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                base_url + "signin",
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response is: "+ response.toString());
                        handleSigninResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayErrorAlert(error);
            }
        }

        );
        queue.add(request);
    }


    private void handleSigninResponse(JSONObject response) {
        try {
            if (response.getString("status").equals("success")) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
            else {
                displayErrorAlert(response.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
