package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    // firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_splash );

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReference = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        // start main screen after 2 seconds
        new Handler ( ).postDelayed ( this::checkUser, 2000 );

    }

    private void checkUser() {
        // get current user, if logged in
        firebaseUser = firebaseAuth.getCurrentUser ( );
        if (firebaseUser != null) {
            // user logged in check user type
            Log.i ( TAG, "checkUser: " + firebaseUser.getUid ( ) );
            databaseReference.child ( Objects.requireNonNull ( firebaseUser ).getUid ( ) ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // get user type
                    String userType = "" + snapshot.child ( "userType" ).getValue ( );
                    // check user type
                    if (userType.equals ( "user" )) {
                        // this is simple user, open admin dashboard
                        startActivity ( new Intent ( SplashActivity.this, DashboardUserActivity.class ) );
                        finish ( );
                    } else if (userType.equals ( "admin" )) {
                        // this is admin, open admin dashboard
                        startActivity ( new Intent ( SplashActivity.this, DashboardAdminActivity.class ) );
                        finish ( );
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            } );
        } else if (firebaseUser == null) {
            startActivity ( new Intent ( SplashActivity.this, MainActivity.class ) );
            finish ( );
        }
    }
}