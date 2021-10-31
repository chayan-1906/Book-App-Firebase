package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapters.CategoryUserAdapter;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Objects;

public class DashboardUserActivity extends AppCompatActivity {

    // view binding
    private ActivityDashboardUserBinding activityDashboardUserBinding;

    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceCategories;

    private String userName;

    // arraylist to store categories
    private ArrayList<ModelCategory> modelCategoryArrayList;

    // adapter
    private CategoryUserAdapter categoryUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityDashboardUserBinding = ActivityDashboardUserBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityDashboardUserBinding.getRoot ( ) );

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceCategories = FirebaseDatabase.getInstance ( ).getReference ( "Categories" );
        checkUser ( );
        loadCategories ( );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setMessage ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );

        // edit text change listener, search
        activityDashboardUserBinding.textInputEditTextSearch.addTextChangedListener ( new TextWatcher ( ) {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // called as and when type each letter
                try {
                    categoryUserAdapter.getFilter ( ).filter ( s );
                } catch (Exception e) {
                    e.printStackTrace ( );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );

        // handle click, open profile
        activityDashboardUserBinding.imageButtonProfile.setOnClickListener ( v -> {
            if (firebaseAuth.getCurrentUser ( ) == null) {
                FancyToast.makeText ( getApplicationContext ( ), "You are not logged in", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            } else {
                startActivity ( new Intent ( DashboardUserActivity.this, ProfileActivity.class ) );
            }
        } );

        // handle click, logout
        activityDashboardUserBinding.imageViewLogout.setOnClickListener ( v -> {
            // logout user
            signOut ( );
        } );
    }

    private void loadCategories() {
        // init arraylist
        modelCategoryArrayList = new ArrayList<> ( );
        // get all categories
        databaseReferenceCategories.addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // make arraylist clear, before adding data
                modelCategoryArrayList.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    // get data
                    String id = dataSnapshot.child ( "id" ).getValue ( String.class );
                    String uid = dataSnapshot.child ( "uid" ).getValue ( String.class );
                    String category = dataSnapshot.child ( "category" ).getValue ( String.class );
                    String timestamp = dataSnapshot.child ( "timestamp" ).getValue ( String.class );
                    // add to arraylist
                    modelCategoryArrayList.add ( new ModelCategory ( id, uid, category, timestamp ) );
                }
                // setup adapter
                categoryUserAdapter = new CategoryUserAdapter ( DashboardUserActivity.this, modelCategoryArrayList );
                //set adapter to recyclerView
                activityDashboardUserBinding.recyclerViewCategories.setAdapter ( categoryUserAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @SuppressLint("SetTextI18n")
    private void checkUser() {
        // get current user
        firebaseUser = firebaseAuth.getCurrentUser ( );
        if (firebaseUser == null) {
            // user not logged in
            activityDashboardUserBinding.textViewSubtitleDashboardUser.setText ( "Not Logged in" );
        } else {
            try {
                // user logged in, get user info
                databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).addValueEventListener ( new ValueEventListener ( ) {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userName = "" + snapshot.child ( "name" ).getValue ( );
                        //set in textview of toolbar
                        activityDashboardUserBinding.textViewSubtitleDashboardUser.setText ( userName );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
            } catch (Exception e) {
                activityDashboardUserBinding.textViewSubtitleDashboardUser.setText ( "User" );
            }
        }
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder ( DashboardUserActivity.this );
        builder.setTitle ( "Do you want to logout?" );
        builder.setPositiveButton ( "Yes", (dialogInterface, i) -> {
            progressDialog.show ( );
            firebaseAuth.signOut ( );
            onBackPressed ( );
        } ).setNegativeButton ( "No", (dialogInterface, i) -> {

        } );
        builder.create ( ).show ( );
    }
}