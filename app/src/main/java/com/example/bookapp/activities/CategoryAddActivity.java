package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityCategoryAddBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;
import java.util.Objects;

public class CategoryAddActivity extends AppCompatActivity {

    // view binding
    private ActivityCategoryAddBinding activityCategoryAddBinding;
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceCategories;

    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityCategoryAddBinding = ActivityCategoryAddBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityCategoryAddBinding.getRoot ( ) );

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceCategories = FirebaseDatabase.getInstance ( ).getReference ( "Categories" );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // handle click, begin upload category
        activityCategoryAddBinding.btnAddCategory.setOnClickListener ( v -> validateData ( ) );

        // handle click, go back
        activityCategoryAddBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );
    }

    private void validateData() {
        /* Before adding validate data */
        // get data
        category = Objects.requireNonNull ( activityCategoryAddBinding.textInputLayoutCategory.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        if (TextUtils.isEmpty ( category )) {
            activityCategoryAddBinding.textInputLayoutCategory.setError ( "" );
            activityCategoryAddBinding.textInputLayoutCategory.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter a category", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        }
        addCategory ( );
    }

    private void addCategory() {
        // show progress dialog
        progressDialog.setMessage ( "Adding Category..." );
        progressDialog.show ( );
        // timestamp
        String timestamp = String.valueOf ( System.currentTimeMillis ( ) );
        // get current user id
        String uid = firebaseAuth.getUid ( );
        // set data in firebase database
        HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
        stringObjectHashMap.put ( "id", "" + timestamp );
        stringObjectHashMap.put ( "uid", "" + uid );
        stringObjectHashMap.put ( "timestamp", "" + timestamp );
        stringObjectHashMap.put ( "category", "" + category );
        // add data to firebase database... Database Root -> Categories -> categoryId -> categoryInfo
        databaseReferenceCategories.child ( timestamp ).setValue ( stringObjectHashMap ).addOnSuccessListener ( unused -> {
            // category added successfully
            progressDialog.dismiss ( );
            // navigating to DashboardAdminActivity.class
            Intent intentDashboardAdminActivity = new Intent ( CategoryAddActivity.this, DashboardAdminActivity.class );
            intentDashboardAdminActivity.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( intentDashboardAdminActivity );
            FancyToast.makeText ( getApplicationContext ( ), "Category added successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false ).show ( );
        } ).addOnFailureListener ( e -> {
            // fail to add category
            FancyToast.makeText ( getApplicationContext ( ), e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }
}