package com.sschutt.billtracker3;

import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class BaseActivity extends AppCompatActivity {

    final String SHARED_PREFERENCES_NAME = "preferences";
    protected String TAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    protected void setCookieValue(String key, String value) {
        SharedPreferences settings = this.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected String getCookieValue(String key) {
        SharedPreferences settings = this.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return settings.getString(key, "");
    }

    protected void displayErrorAlert(VolleyError error) {
        String error_message = "Undefined error";
        if(error.networkResponse.data!=null) {
            try {
                String body = new String(error.networkResponse.data,"UTF-8");
                JSONObject response = new JSONObject(body);
                error_message = response.getString("message");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getStackTrace().toString());
            } catch (JSONException e) {
                Log.e(TAG, e.getStackTrace().toString());
            }
        }
        displayErrorAlert(error_message);
    }

    protected void displayErrorAlert(String error_message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.TAG)
                .setMessage(error_message)
                .setPositiveButton("OK", null)
                .show();
    }
}
