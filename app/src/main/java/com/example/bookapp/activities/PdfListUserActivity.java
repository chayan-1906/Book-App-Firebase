package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapters.PdfUserAdapter;
import com.example.bookapp.databinding.ActivityPdfListUserBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListUserActivity extends AppCompatActivity {

    private static final String TAG = "PdfListUserActivity";

    // view binding
    private ActivityPdfListUserBinding activityPdfListUserBinding;

    // arraylist to hold list of data of type ModelPdf
    private ArrayList<ModelPdf> modelPdfArrayList;

    // adapter
    private PdfUserAdapter pdfUserAdapter;

    // firebase
    private DatabaseReference databaseReferenceBooks;

    private String categoryId;
    private String categoryTitle;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityPdfListUserBinding = ActivityPdfListUserBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityPdfListUserBinding.getRoot ( ) );

        // get data from intent
        Intent intentCategoryAdapter = getIntent ( );
        categoryId = intentCategoryAdapter.getStringExtra ( "categoryId" );
        categoryTitle = intentCategoryAdapter.getStringExtra ( "categoryTitle" );

        activityPdfListUserBinding.textViewSubtitle.setText ( " (" + categoryTitle + ")" );

        loadPdfList ( );

        // search
        activityPdfListUserBinding.textInputEditTextSearch.addTextChangedListener ( new TextWatcher ( ) {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // called as and when type each letter
                try {
                    pdfUserAdapter.getFilter ( ).filter ( s );
                } catch (Exception e) {
                    e.printStackTrace ( );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );

        // handle click, go back
        activityPdfListUserBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );
    }

    private void loadPdfList() {
        modelPdfArrayList = new ArrayList<> ( );
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );
        databaseReferenceBooks.orderByChild ( "categoryId" ).equalTo ( categoryId ).addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelPdfArrayList.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    // get data
                    String categoryId = dataSnapshot.child ( "categoryId" ).getValue ( String.class );
                    String description = dataSnapshot.child ( "description" ).getValue ( String.class );
                    String id = dataSnapshot.child ( "id" ).getValue ( String.class );
                    String timestamp = dataSnapshot.child ( "timestamp" ).getValue ( String.class );
                    String title = dataSnapshot.child ( "title" ).getValue ( String.class );
                    String uid = dataSnapshot.child ( "uid" ).getValue ( String.class );
                    String uploadedPdfUrl = dataSnapshot.child ( "url" ).getValue ( String.class );
                    String viewsCount = dataSnapshot.child ( "viewsCount" ).getValue ( String.class );
                    String downloadsCount = dataSnapshot.child ( "downloadsCount" ).getValue ( String.class );
                    // add to list
                    modelPdfArrayList.add ( new ModelPdf ( id, uid, title, description, categoryId, uploadedPdfUrl, timestamp, viewsCount, downloadsCount, false ) );
                }
                // setup adapter
                pdfUserAdapter = new PdfUserAdapter ( PdfListUserActivity.this, modelPdfArrayList );
                activityPdfListUserBinding.recyclerViewBooks.setAdapter ( pdfUserAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
}