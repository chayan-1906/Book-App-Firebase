package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // view binding
    private ActivityRegisterBinding activityRegisterBinding;
    private ProgressDialog progressDialog;

    // firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private String userName;
    private String email;
    private String password;
    private String confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityRegisterBinding = ActivityRegisterBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityRegisterBinding.getRoot ( ) );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReference = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        // handle click, go back
        activityRegisterBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, begin register
        activityRegisterBinding.btnRegister.setOnClickListener ( v -> validateData ( ) );

    }

    private void validateData() {
        /** Before creating account, lets do some data validation */
        // get data
        userName = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutName.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        email = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutEmail.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        password = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutPassword.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        confirmPassword = Objects.requireNonNull ( activityRegisterBinding.textInputLayoutConfirmPassword.getEditText ( ) ).getText ( ).toString ( ).trim ( );

        // validate data
        String passwordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{6,}$";
        Matcher passwordMatcher = Pattern.compile ( passwordRegex ).matcher ( password );
        if (TextUtils.isEmpty ( userName )) {
            activityRegisterBinding.textInputLayoutName.setError ( "" );
            activityRegisterBinding.textInputLayoutName.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter your full name", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher ( email ).matches ( )) {
            activityRegisterBinding.textInputLayoutEmail.setError ( "" );
            activityRegisterBinding.textInputLayoutEmail.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter a valid email", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (password.length ( ) < 6 || password.contains ( " " )) {
            activityRegisterBinding.textInputLayoutPassword.setError ( "" );
            activityRegisterBinding.textInputLayoutPassword.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Password length should be at least 6", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false ).show ( );
            return;
        } else if (!passwordMatcher.matches ( )) {
            activityRegisterBinding.textInputLayoutPassword.setError ( "" );
            activityRegisterBinding.textInputLayoutPassword.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Password should contain at least one lower case, upper case, special character, number", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (TextUtils.isEmpty ( confirmPassword ) || !confirmPassword.equals ( password )) {
            activityRegisterBinding.textInputLayoutConfirmPassword.setError ( "" );
            activityRegisterBinding.textInputLayoutConfirmPassword.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Password didn't match", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        }
        createAccount ( );
    }

    private void createAccount() {
        progressDialog.setMessage ( "Creating Account..." );
        progressDialog.show ( );

        firebaseAuth.createUserWithEmailAndPassword ( email, password ).addOnSuccessListener ( authResult -> updateUserInfo ( ) ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void updateUserInfo() {
        progressDialog.setMessage ( "Saving User Info..." );
        progressDialog.show ( );
        // timestamp
        long timestamp = System.currentTimeMillis ( );
        // get current user id
        String uid = firebaseAuth.getUid ( );
        // set data in firebase database
        HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
        stringObjectHashMap.put ( "uid", uid );
        stringObjectHashMap.put ( "email", email );
        stringObjectHashMap.put ( "name", userName );
        stringObjectHashMap.put ( "profileImage", "" );
        stringObjectHashMap.put ( "userType", "user" );
        stringObjectHashMap.put ( "timestamp", timestamp );
        // add data to firebase database
        databaseReference.child ( Objects.requireNonNull ( uid ) ).setValue ( stringObjectHashMap ).addOnSuccessListener ( unused -> {
            // data added to db
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Account created successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false ).show ( );
            startActivity ( new Intent ( RegisterActivity.this, DashboardUserActivity.class ) );
            finish ( );
        } ).addOnFailureListener ( e -> {
            // data failed to add db
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }
}