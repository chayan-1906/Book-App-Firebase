package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityAddPdfAdminBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AddPdfAdminActivity extends AppCompatActivity {

    private static final String TAG = "AddPdfAdminActivity";

    // view binding
    private ActivityAddPdfAdminBinding activityAddPdfAdminBinding;

    // progress dialog
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceCategories;
    private DatabaseReference databaseReferenceBooks;
    private StorageReference storageReferenceBooks;

    // arraylist to hold pdf categories
    private ArrayList<String> categoryTitleArrayList;
    private ArrayList<String> categoryIdArrayList;

    // uri of picked pdf
    private Uri pdfUri = null;

    private String bookName;
    private String bookDescription;
    private String selectedCategoryId;
    private String selectedCategoryTitle;

    public static final int PDF_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityAddPdfAdminBinding = ActivityAddPdfAdminBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityAddPdfAdminBinding.getRoot ( ) );

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceCategories = FirebaseDatabase.getInstance ( ).getReference ( "Categories" );
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );

        loadCategories ( );

        // setup progress dialog
        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setCancelable ( false );

        // handle click, go back
        activityAddPdfAdminBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, attach pdf
        activityAddPdfAdminBinding.imageButtonAttachFile.setOnClickListener ( v -> pickIntentPdf ( ) );

        // handle click, pick category
        activityAddPdfAdminBinding.textViewCategoryTitle.setOnClickListener ( v -> categoryPickDialog ( ) );

        // handle click, upload pdf
        activityAddPdfAdminBinding.btnUploadPdf.setOnClickListener ( v -> {
            // validate data
            validateData ( );
        } );
    }

    private void validateData() {
        // Step 1: Validate data

        // get data
        bookName = Objects.requireNonNull ( activityAddPdfAdminBinding.textInputLayoutBookName.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        bookDescription = Objects.requireNonNull ( activityAddPdfAdminBinding.textInputLayoutBookDescription.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        // validate data
        if (TextUtils.isEmpty ( bookName )) {
            activityAddPdfAdminBinding.textInputLayoutBookName.setError ( "" );
            activityAddPdfAdminBinding.textInputLayoutBookName.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter book name", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (TextUtils.isEmpty ( bookDescription )) {
            activityAddPdfAdminBinding.textInputLayoutBookDescription.setError ( "" );
            activityAddPdfAdminBinding.textInputLayoutBookDescription.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Enter book description", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (TextUtils.isEmpty ( selectedCategoryTitle )) {
            activityAddPdfAdminBinding.textViewCategoryTitle.setError ( "" );
            activityAddPdfAdminBinding.textViewCategoryTitle.requestFocus ( );
            FancyToast.makeText ( getApplicationContext ( ), "Choose Book Category", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
            return;
        } else if (pdfUri == null) {
            FancyToast.makeText ( getApplicationContext ( ), "Pick pdf to upload", FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
            return;
        }
        uploadPdfToStorage ( );
    }

    private void uploadPdfToStorage() {
        // Step 2: Upload pdf to firebase storage
        // showing progress dialog
        progressDialog.setMessage ( "Uploading pdf..." );
        progressDialog.show ( );
        // timestamp
        String timestamp = String.valueOf ( System.currentTimeMillis ( ) );

        // path of pdf in firebase storage
        String filePathAndName = "Books/" + timestamp;
        storageReferenceBooks = FirebaseStorage.getInstance ( ).getReference ( filePathAndName );
        storageReferenceBooks.putFile ( pdfUri ).addOnSuccessListener ( taskSnapshot -> {
            // upload successful, get the url of uploaded file
            Task<Uri> uriTask = taskSnapshot.getStorage ( ).getDownloadUrl ( );
            while (!uriTask.isSuccessful ( )) ;
            String uploadedPdfUrl = "" + uriTask.getResult ( );
            // set the link to firebase  realtime database
            uploadPdfIntoDatabase ( uploadedPdfUrl, timestamp );
        } ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Failed to upload pdf " + e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void uploadPdfIntoDatabase(String uploadedPdfUrl, String timestamp) {
        // Step 3: Set the link to firebase  realtime database
        progressDialog.setMessage ( "Uploading pdf info..." );
        String uid = firebaseAuth.getUid ( );
        // setup data to upload, also add view count, download count while adding pdf/book
        HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
        stringObjectHashMap.put ( "uid", "" + uid );
        stringObjectHashMap.put ( "id", "" + timestamp );
        stringObjectHashMap.put ( "title", "" + bookName );
        stringObjectHashMap.put ( "description", "" + bookDescription );
        stringObjectHashMap.put ( "categoryId", "" + selectedCategoryId );
        stringObjectHashMap.put ( "url", "" + uploadedPdfUrl );
        stringObjectHashMap.put ( "timestamp", "" + timestamp );
        stringObjectHashMap.put ( "viewsCount", "0" );
        stringObjectHashMap.put ( "downloadsCount", "0" );
        // db reference: database root -> Books
        databaseReferenceBooks.child ( timestamp ).setValue ( stringObjectHashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss ( );
                FancyToast.makeText ( getApplicationContext ( ), "Book uploaded successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false ).show ( );
            }
        } ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Failed to upload in realtime database due to " + e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void loadCategories() {
        // first we need to categories from firebase
        categoryTitleArrayList = new ArrayList<> ( );
        categoryIdArrayList = new ArrayList<> ( );
        databaseReferenceCategories.addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear ( );
                categoryIdArrayList.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    // get id & title of category
                    String categoryId = "" + dataSnapshot.child ( "id" ).getValue ( );
                    String categoryTitle = "" + dataSnapshot.child ( "category" ).getValue (  );
                    // add to respective arraylist
                    categoryTitleArrayList.add ( categoryTitle );
                    categoryIdArrayList.add ( categoryId );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void categoryPickDialog() {
        // get array of categories from arraylist
        String[] categoriesArray = new String[ categoryTitleArrayList.size ( ) ];
        for (int i = 0; i < categoryTitleArrayList.size ( ); i++) {
            categoriesArray[ i ] = categoryTitleArrayList.get ( i );
        }
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder ( this );
        builder.setTitle ( "Pick Category" );
        builder.setItems ( categoriesArray, (dialog, which) -> {
            // handle item click
            // get clicked item from array list
            selectedCategoryTitle = categoryTitleArrayList.get ( which );
            selectedCategoryId = categoryIdArrayList.get ( which );
            // set it to category textview
            activityAddPdfAdminBinding.textViewCategoryTitle.setText ( selectedCategoryTitle );
        } );
        builder.show ( );
    }

    private void pickIntentPdf() {
        Log.d ( TAG, "pickIntentPdf: started" );
        Intent intentPdfPick = new Intent ( );
        intentPdfPick.setType ( "application/pdf" );
        intentPdfPick.setAction ( Intent.ACTION_GET_CONTENT );
        startActivityForResult ( Intent.createChooser ( intentPdfPick, "Select Pdf" ), PDF_PICK_CODE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult ( requestCode, resultCode, data );
        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                pdfUri = Objects.requireNonNull ( data ).getData ( );
            }
        } else {
            FancyToast.makeText ( getApplicationContext ( ), "Cancelled picking pdf", FancyToast.LENGTH_SHORT, FancyToast.INFO, false ).show ( );
        }
    }
}