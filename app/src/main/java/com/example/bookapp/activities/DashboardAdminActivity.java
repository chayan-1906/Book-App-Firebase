package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapters.CategoryAdminAdapter;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DashboardAdminActivity extends AppCompatActivity {

    // view binding
    private ActivityDashboardAdminBinding activityDashboardAdminBinding;

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
    private CategoryAdminAdapter categoryAdminAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityDashboardAdminBinding = ActivityDashboardAdminBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityDashboardAdminBinding.getRoot ( ) );

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
        activityDashboardAdminBinding.textInputEditTextSearch.addTextChangedListener ( new TextWatcher ( ) {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // called as and when type each letter
                try {
                    categoryAdminAdapter.getFilter ( ).filter ( s );
                } catch (Exception e) {
                    e.printStackTrace ( );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );

        // handle click, open profile
        activityDashboardAdminBinding.imageButtonProfile.setOnClickListener ( v -> startActivity ( new Intent ( DashboardAdminActivity.this, ProfileActivity.class ) ) );

        // handle click, logout
        activityDashboardAdminBinding.imageViewLogout.setOnClickListener ( v -> {
            // logout user
            signOut ( );
        } );

        // handle click, start add category screen
        activityDashboardAdminBinding.btnAddCategory.setOnClickListener ( v -> startActivity ( new Intent ( DashboardAdminActivity.this, CategoryAddActivity.class ) ) );

        // handle click, start add category screen
        activityDashboardAdminBinding.floatingActionButtonAddPdf.setOnClickListener ( v -> startActivity ( new Intent ( DashboardAdminActivity.this, AddPdfAdminActivity.class ) ) );
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
                categoryAdminAdapter = new CategoryAdminAdapter ( DashboardAdminActivity.this, modelCategoryArrayList );
                //set adapter to recyclerView
                activityDashboardAdminBinding.recyclerViewCategories.setAdapter ( categoryAdminAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void checkUser() {
        // get current user
        firebaseUser = firebaseAuth.getCurrentUser ( );
        if (firebaseUser == null) {
            // user not logged in
            // start main screen
            startActivity ( new Intent ( DashboardAdminActivity.this, MainActivity.class ) );
            finish ( );
        } else {
            try {
                // user logged in, get user info
                databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).addValueEventListener ( new ValueEventListener ( ) {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userName = "" + snapshot.child ( "name" ).getValue ( );
                        //set in textview of toolbar
                        activityDashboardAdminBinding.textViewSubtitleDashboardAdmin.setText ( userName );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
            } catch (Exception e) {
                activityDashboardAdminBinding.textViewSubtitleDashboardAdmin.setText ( "Admin" );
            }
        }
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder ( DashboardAdminActivity.this );
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