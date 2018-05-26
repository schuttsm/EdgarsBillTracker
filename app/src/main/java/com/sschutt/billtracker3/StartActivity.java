package com.sschutt.billtracker3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.net.HttpCookie;

public class StartActivity extends BaseActivity {
    private final String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (!TextUtils.isEmpty(this.getCookieValue("token"))) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignUp_OnClick(view);
            }
        });

        findViewById(R.id.btnSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn_OnClick(view);
            }
        });
    }

    private void btnSignUp_OnClick(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void btnSignIn_OnClick(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
