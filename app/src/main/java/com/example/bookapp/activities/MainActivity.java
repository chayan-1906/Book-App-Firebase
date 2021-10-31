package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // view binding
    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityMainBinding = ActivityMainBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityMainBinding.getRoot ( ) );

        Log.i ( TAG, "onCreate: started" );
        // handle loginBtn click, start login screen
        activityMainBinding.btnLogin.setOnClickListener ( v -> startActivity ( new Intent ( MainActivity.this, LoginActivity.class ) ) );

        // handle skipBtn click, start continue without login screen
        activityMainBinding.btnSkipLogin.setOnClickListener ( v -> startActivity ( new Intent ( MainActivity.this, DashboardUserActivity.class ) ) );
    }
}