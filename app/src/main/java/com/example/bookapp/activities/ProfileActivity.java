package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.PdfFavoriteAdapter;
import com.example.bookapp.databinding.ActivityProfileBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // view binding
    private ActivityProfileBinding activityProfileBinding;

    // firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;

    // arraylist to hold list of data of type ModelPdf
    private ArrayList<ModelPdf> pdfArrayList;

    // adapter to set recycler view
    private PdfFavoriteAdapter pdfFavoriteAdapter;

    String email;
    String fullName;
    String profileImage;
    String timestamp;
    String uid;
    String userType;
    String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityProfileBinding = ActivityProfileBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityProfileBinding.getRoot ( ) );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        loadUserInfo ( );
        loadFavBooks ( );

        // handle click, go back
        activityProfileBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, start profile edit page
        activityProfileBinding.imageButtonEditProfile.setOnClickListener ( v -> startActivity ( new Intent ( ProfileActivity.this, ProfileEditActivity.class ) ) );

    }

    private void loadUserInfo() {
        Log.i ( TAG, "loadUserInfo: started" );
        databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get all info of user
                email = "" + snapshot.child ( "email" ).getValue ( );
                fullName = "" + snapshot.child ( "name" ).getValue ( );
                profileImage = "" + snapshot.child ( "profileImage" ).getValue ( );
                timestamp = "" + snapshot.child ( "timestamp" ).getValue ( );
                uid = "" + snapshot.child ( "uid" ).getValue ( );
                userType = "" + snapshot.child ( "userType" ).getValue ( );
                // format date to dd/MM/yyyy
                formattedDate = MyApplication.formatTimestamp ( timestamp );
                // set data to ui
                activityProfileBinding.textViewFullName.setText ( fullName );
                activityProfileBinding.textViewEmail.setText ( email );
                activityProfileBinding.textViewAccountType.setText ( userType );
                activityProfileBinding.textViewMemberSince.setText ( formattedDate );
                // set image using glide
                Glide.with ( ProfileActivity.this ).load ( profileImage ).placeholder ( R.drawable.profile_image ).into ( activityProfileBinding.imageViewProfile );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void loadFavBooks() {
        pdfArrayList = new ArrayList<> ( );
        // load favorite books from database: Users > userId > Favorites
        databaseReferenceUsers.child ( firebaseAuth.getUid ( ) ).child ( "Favorites" ).addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear list before adding data
                pdfArrayList.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    String bookId = "" + dataSnapshot.child ( "bookId" ).getValue ( );
                    // set it to  model
                    ModelPdf modelPdf = new ModelPdf ( );
                    modelPdf.setId ( bookId );
                    // add model to list
                    pdfArrayList.add ( modelPdf );
                }
                // set number of favorite books
                activityProfileBinding.textViewFavBookCount.setText ( "" + pdfArrayList.size ( ) );
                // setup adapter
                pdfFavoriteAdapter = new PdfFavoriteAdapter ( ProfileActivity.this, pdfArrayList );
                // set adapter to recycler view
                activityProfileBinding.recyclerViewFavBooks.setAdapter ( pdfFavoriteAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public void onBackPressed() {
        Intent intentDashboardAdminActivity = new Intent ( ProfileActivity.this, DashboardAdminActivity.class );
        intentDashboardAdminActivity.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity ( intentDashboardAdminActivity );
        finish ( );
    }
}