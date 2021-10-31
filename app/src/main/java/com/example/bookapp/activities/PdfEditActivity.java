package com.example.bookapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class PdfEditActivity extends AppCompatActivity {

    private static final String TAG = "PdfEditActivity";

    // view binding
    private ActivityPdfEditBinding activityPdfEditBinding;

    // book id get from intent started from PdfAdminAdapter
    private String bookId;

    // progress dialog
    private ProgressDialog progressDialog;

    // firebase
    private DatabaseReference databaseReferenceCategories;
    private DatabaseReference databaseReferenceBooks;

    private ArrayList<String> arrayListCategoryId;
    private ArrayList<String> arrayListCategoryTitle;

    private String selectedCategoryId;
    private String selectedCategoryTitle;
    private String description;
    private String title;

    private String bookName;
    private String bookDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityPdfEditBinding = ActivityPdfEditBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityPdfEditBinding.getRoot ( ) );

        // book id get from intent started from PdfAdminAdapter
        bookId = getIntent ( ).getStringExtra ( "bookId" );

        // firebase
        databaseReferenceCategories = FirebaseDatabase.getInstance ( ).getReference ( "Categories" );
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );

        // progress dialog
        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );

        loadCategories ( );
        loadBookInfo ( );

        // handle click, pick category
        activityPdfEditBinding.textViewCategoryTitle.setOnClickListener ( v -> categoryDialog ( ) );

        // handle click, go to previous screen
        activityPdfEditBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, begin upload
        activityPdfEditBinding.btnUploadPdf.setOnClickListener ( v -> validateData ( ) );

    }

    private void loadBookInfo() {
        databaseReferenceBooks.child ( bookId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get book info
                selectedCategoryId = "" + snapshot.child ( "categoryId" ).getValue ( );
                description = "" + snapshot.child ( "description" ).getValue ( );
                title = "" + snapshot.child ( "title" ).getValue ( );
                // set to views
                activityPdfEditBinding.textInputLayoutBookName.getEditText ( ).setText ( title );
                Objects.requireNonNull ( activityPdfEditBinding.textInputLayoutBookDescription.getEditText ( ) ).setText ( description );
                // updating to db
                databaseReferenceCategories.child ( selectedCategoryId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // get category
                        String category = "" + snapshot.child ( "category" ).getValue ( );
                        // set to category text view
                        activityPdfEditBinding.textViewCategoryTitle.setText ( category );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void validateData() {
        // Step 1: Validate data
        // get data
        bookName = Objects.requireNonNull ( activityPdfEditBinding.textInputLayoutBookName.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        bookDescription = Objects.requireNonNull ( activityPdfEditBinding.textInputLayoutBookDescription.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        // validate data
        if (TextUtils.isEmpty ( bookName )) {
            activityPdfEditBinding.textInputLayoutBookName.setError ( "" );
            activityPdfEditBinding.textInputLayoutBookName.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter book name", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (TextUtils.isEmpty ( bookDescription )) {
            activityPdfEditBinding.textInputLayoutBookDescription.setError ( "" );
            activityPdfEditBinding.textInputLayoutBookDescription.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter book description", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (TextUtils.isEmpty ( selectedCategoryId )) {
            activityPdfEditBinding.textViewCategoryTitle.setError ( "" );
            activityPdfEditBinding.textViewCategoryTitle.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Choose Book Category", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        }
        updatePdf ( );
    }

    private void updatePdf() {
        // show progress dialog
        progressDialog.setMessage ( "Updating book info..." );
        progressDialog.show ( );
        // setup data to update db
        HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
        stringObjectHashMap.put ( "title", "" + bookName );
        stringObjectHashMap.put ( "description", "" + bookDescription );
        stringObjectHashMap.put ( "categoryId", "" + selectedCategoryId );
        // db reference: database root -> Books
        databaseReferenceBooks.child ( bookId ).updateChildren ( stringObjectHashMap ).addOnSuccessListener ( unused -> {
            progressDialog.dismiss ( );
            onBackPressed ( );
            FancyToast.makeText ( getApplicationContext ( ), "Updated successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false ).show ( );
        } ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Failed to update due to " + e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void categoryDialog() {
        // make string array from arraylist of string
        String[] categoriesArray = new String[ arrayListCategoryTitle.size ( ) ];
        for (int i = 0; i < arrayListCategoryTitle.size ( ); i++) {
            categoriesArray[ i ] = arrayListCategoryTitle.get ( i );
        }
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder ( this );
        builder.setTitle ( "Choose Category" )
                .setItems ( categoriesArray, (dialog, which) -> {
                    selectedCategoryId = arrayListCategoryId.get ( which );
                    selectedCategoryTitle = arrayListCategoryTitle.get ( which );
                    // set to textview
                    activityPdfEditBinding.textViewCategoryTitle.setText ( selectedCategoryTitle );
                } ).show ( );
    }

    private void loadCategories() {
        arrayListCategoryId = new ArrayList<> ( );
        arrayListCategoryTitle = new ArrayList<> ( );
        databaseReferenceCategories.addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayListCategoryId.clear ( );
                arrayListCategoryTitle.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    // get id & title of category
                    String id = "" + dataSnapshot.child ( "id" ).getValue ( );
                    String category = "" + dataSnapshot.child ( "category" ).getValue ( );
                    // add to respective arraylist
                    arrayListCategoryId.add ( id );
                    arrayListCategoryTitle.add ( category );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
}