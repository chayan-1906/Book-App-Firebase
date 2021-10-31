package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    // view binding
    private ActivityLoginBinding activityLoginBinding;
    private ProgressDialog progressDialog;

    // firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private String email;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityLoginBinding = ActivityLoginBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityLoginBinding.getRoot ( ) );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReference = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        // handle click, go to register screen
        activityLoginBinding.btnLogin.setOnClickListener ( v -> validateData ( ) );

        activityLoginBinding.textViewRegister.setOnClickListener ( v -> startActivity ( new Intent ( LoginActivity.this, RegisterActivity.class ) ) );
    }

    private void validateData() {
        /** Before login, lets do some data validation */
        // get data
        email = Objects.requireNonNull ( activityLoginBinding.textInputLayoutEmail.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        password = Objects.requireNonNull ( activityLoginBinding.textInputLayoutPassword.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        // validate data
        String passwordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,}$";
        Matcher passwordMatcher = Pattern.compile ( passwordRegex ).matcher ( password );
        if (!Patterns.EMAIL_ADDRESS.matcher ( email ).matches ( )) {
            activityLoginBinding.textInputLayoutEmail.setError ( "" );
            activityLoginBinding.textInputLayoutEmail.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter a valid email", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (password.length ( ) < 6 || password.contains ( " " )) {
            activityLoginBinding.textInputLayoutPassword.setError ( "" );
            activityLoginBinding.textInputLayoutPassword.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Password length should be at least 6", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false ).show ( );
            return;
        } else if (!passwordMatcher.matches ( )) {
            activityLoginBinding.textInputLayoutPassword.setError ( "" );
            activityLoginBinding.textInputLayoutPassword.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Password should contain at least one lower case, upper case, special character, number", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        }
        loginUser ( );
    }

    private void loginUser() {
        progressDialog.setMessage ( "Login in progress..." );
        progressDialog.show ( );
        firebaseAuth.signInWithEmailAndPassword ( email, password ).addOnSuccessListener ( authResult -> {
            // login successful, check if user is user or admin
            checkUser ( );
        } ).addOnFailureListener ( e -> {
            // login failed
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void checkUser() {
        progressDialog.setMessage ( "Checking User..." );
        // check if user is user or admin from realtime database
        firebaseUser = FirebaseAuth.getInstance ( ).getCurrentUser ( );
        // check in db
        databaseReference.child ( Objects.requireNonNull ( firebaseUser ).getUid ( ) ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss ( );
                // get user type
                String userType = "" + snapshot.child ( "userType" ).getValue ( );
                // check user type
                if (userType.equals ( "user" )) {
                    // this is simple user, open admin dashboard
                    Intent intentDashboardUserActivity = new Intent ( LoginActivity.this, DashboardUserActivity.class );
                    intentDashboardUserActivity.setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
                    startActivity ( intentDashboardUserActivity );
                    finish ( );
                } else if (userType.equals ( "admin" )) {
                    // this is admin, open admin dashboard
                    Intent intentDashboardAdminActivity = new Intent ( LoginActivity.this, DashboardAdminActivity.class );
                    intentDashboardAdminActivity.setFlags ( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
                    startActivity ( intentDashboardAdminActivity );
                    finish ( );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
}