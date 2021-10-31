package com.example.bookapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.CommentAdapter;
import com.example.bookapp.adapters.PdfFavoriteAdapter;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.DialogAddCommentBinding;
import com.example.bookapp.models.ModelComment;
import com.example.bookapp.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private static final String TAG = "PdfDetailActivity";

    // view binding
    private ActivityPdfDetailBinding activityPdfDetailBinding;

    // Progress Dialog
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceBooks;
    private DatabaseReference databaseReferenceUsers;

    // book id, get from intent
    private String bookId;
    private String bookName;
    private String bookUrl;
    private String comment;

    // arraylist to hold comments
    private ArrayList<ModelComment> commentArrayList;

    // adapter to set to recycler view
    private CommentAdapter commentAdapter;

    private boolean isMyFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityPdfDetailBinding = ActivityPdfDetailBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityPdfDetailBinding.getRoot ( ) );

        // get data from PdfAdminAdapter
        try {
            bookId = getIntent ( ).getStringExtra ( "bookId" );
            Log.d ( TAG, "onCreate: " + bookId );
        } catch (Exception e) {
            e.printStackTrace ( );
        }

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );

        if (firebaseAuth.getCurrentUser ( ) != null) {
            checkIsFavorite ( );
        }

        // at the beginning of the activity, we need book url that we will load later in loadBookDetails(0
        activityPdfDetailBinding.buttonDownloadBook.setVisibility ( View.GONE );
        loadBookDetails ( );
        loadComments ( );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );

        // increment book view count, whenever the page starts
        MyApplication.incrementBookViewCount ( bookId );

        // handle click, go back
        activityPdfDetailBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, open to view pdf
        activityPdfDetailBinding.buttonReadBook.setOnClickListener ( v -> {
            Intent intentViewPdfActivity = new Intent ( PdfDetailActivity.this, ViewPdfActivity.class );
            intentViewPdfActivity.putExtra ( "bookId", bookId );
            startActivity ( intentViewPdfActivity );
        } );

        // handle click, open to download pdf
        activityPdfDetailBinding.buttonDownloadBook.setOnClickListener ( v -> {
            if (ContextCompat.checkSelfPermission ( PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED) {
                MyApplication.downloadBook ( this, "" + bookId, "" + bookName, "" + bookUrl );
            } else {
                requestPermissionLauncher.launch ( Manifest.permission.WRITE_EXTERNAL_STORAGE );
            }
        } );

        // handle click, add/remove favorite
        activityPdfDetailBinding.btnFavourite.setOnClickListener ( v -> {
            if (firebaseAuth.getCurrentUser ( ) == null) {
                FancyToast.makeText ( PdfDetailActivity.this, "You are not logged in", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false ).show ( );
            } else {
                if (isMyFavorite) {
                    // in favorite, remove from favorite
                    MyApplication.removeFromFavorites ( PdfDetailActivity.this, bookId );
                } else {
                    // not in favorite, add to favorite
                    MyApplication.addToFavourite ( PdfDetailActivity.this, bookId );
                }
            }
        } );

        // handle click, show comment
        activityPdfDetailBinding.imageButtonAddComment.setOnClickListener ( v -> {
            /* user must be logged in */
            if (firebaseAuth.getCurrentUser ( ) == null) {
                FancyToast.makeText ( getApplicationContext ( ), "Please login to comment", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
                return;
            }
            addCommentDialog ( );
        } );

    }

    private void loadComments() {
        commentArrayList = new ArrayList<> ( );
        databaseReferenceBooks.child ( bookId ).child ( "Comments" ).addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist
                commentArrayList.clear ( );
                for (DataSnapshot dataSnapshot : snapshot.getChildren ( )) {
                    String id = "" + dataSnapshot.child ( "id" ).getValue ( );
                    String bookId = "" + dataSnapshot.child ( "bookId" ).getValue ( );
                    String timestamp = "" + dataSnapshot.child ( "timestamp" ).getValue ( );
                    String comment = "" + dataSnapshot.child ( "comment" ).getValue ( );
                    String uid = "" + dataSnapshot.child ( "uid" ).getValue ( );
                    commentArrayList.add ( new ModelComment ( id, bookId, timestamp, comment, uid ) );
                }
                Log.i ( TAG, "onDataChange: " + commentArrayList );
                // setup adapter
                commentAdapter = new CommentAdapter ( PdfDetailActivity.this, commentArrayList );
                // set adapter to recycler view
                activityPdfDetailBinding.recyclerViewComments.setAdapter ( commentAdapter );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void addCommentDialog() {
        // inflate bind view for dialog
        DialogAddCommentBinding dialogAddCommentBinding = DialogAddCommentBinding.inflate ( LayoutInflater.from ( this ) );
        // setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder ( PdfDetailActivity.this, R.style.custom_dialog );
        builder.setView ( dialogAddCommentBinding.getRoot ( ) );
        // create and show alert dialog
        AlertDialog alertDialog = builder.create ( );
        alertDialog.show ( );
        // handle click, dismiss dialog
        dialogAddCommentBinding.imageButtonBack.setOnClickListener ( v -> alertDialog.dismiss ( ) );

        // handle click, add comment
        dialogAddCommentBinding.btnSubmitComment.setOnClickListener ( v -> {
            // get data
            comment = dialogAddCommentBinding.textInputLayoutComment.getEditText ( ).getText ( ).toString ( ).trim ( );
            if (TextUtils.isEmpty ( comment )) {
                FancyToast.makeText ( getApplicationContext ( ), "Enter your comment", FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
                return;
            }
            alertDialog.dismiss ( );
            addComment ( );
        } );
    }

    private void addComment() {
        progressDialog.setMessage ( "Adding Comment..." );
        progressDialog.show ( );
        // timestamp for comment
        String timestamp = "" + System.currentTimeMillis ( );
        // setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<> ( );
        hashMap.put ( "id", timestamp );
        hashMap.put ( "bookId", bookId );
        hashMap.put ( "timestamp", timestamp );
        hashMap.put ( "comment", comment );
        hashMap.put ( "uid", firebaseAuth.getUid ( ) );
        // set data to db
        databaseReferenceBooks.child ( bookId ).child ( "Comments" ).child ( timestamp ).setValue ( hashMap ).addOnSuccessListener ( aVoid -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Comment added successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( );
        } ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Failed to add comment " + e, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
        } );
    }

    // request storage permission
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult ( new ActivityResultContracts.RequestPermission ( ), isGranted -> {
        if (isGranted) {
            MyApplication.downloadBook ( this, "" + bookId, "" + bookName, "" + bookUrl );
        } else {
            FancyToast.makeText ( getApplicationContext ( ), "Please allow storage permission", FancyToast.LENGTH_LONG, FancyToast.WARNING, false ).show ( );
        }
    } );

    private void loadBookDetails() {
        databaseReferenceBooks.child ( bookId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get data
                bookName = "" + snapshot.child ( "title" ).getValue ( );
                String bookDescription = "" + snapshot.child ( "description" ).getValue ( );
                String categoryId = "" + snapshot.child ( "categoryId" ).getValue ( );
                String viewsCount = "" + snapshot.child ( "viewsCount" ).getValue ( );
                String downloadsCount = "" + snapshot.child ( "downloadsCount" ).getValue ( );
                bookUrl = "" + snapshot.child ( "url" ).getValue ( );
                String timestamp = "" + snapshot.child ( "timestamp" ).getValue ( );
                String date = MyApplication.formatTimestamp ( timestamp );
                // required data is loaded, show download button
                activityPdfDetailBinding.buttonDownloadBook.setVisibility ( View.VISIBLE );
                MyApplication.loadCategory ( "" + categoryId, activityPdfDetailBinding.textViewCategory );
                MyApplication.loadPdfFromUrlFromSinglePage ( PdfDetailActivity.this, "" + bookUrl, bookName, activityPdfDetailBinding.pdfView, activityPdfDetailBinding.progressBar, activityPdfDetailBinding.textViewPagesCount );
                MyApplication.loadPdfSize ( PdfDetailActivity.this, bookUrl, bookName, activityPdfDetailBinding.textViewBookSize );
                // set data
                activityPdfDetailBinding.textViewTitle.setText ( bookName );
                activityPdfDetailBinding.textViewBookName.setText ( bookName );
                activityPdfDetailBinding.textViewBookDate.setText ( date );
                activityPdfDetailBinding.textViewBookViewsCount.setText ( viewsCount.replace ( "null", "N/A" ) );
                activityPdfDetailBinding.textViewBookDownloadsCount.setText ( downloadsCount.replace ( "null", "N/A" ) );
                activityPdfDetailBinding.textViewBookDescription.setText ( bookDescription );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void checkIsFavorite() {
        // logged in, check if its in favorite list or not
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceUsers.child ( firebaseAuth.getUid ( ) ).child ( "Favorites" ).child ( bookId ).addValueEventListener ( new ValueEventListener ( ) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isMyFavorite = snapshot.exists ( ); // true: if exists, false: if not exists
                if (isMyFavorite) {
                    // exists in favorite
                    activityPdfDetailBinding.btnFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds ( 0, R.drawable.ic_favorite_white, 0, 0 );
                    activityPdfDetailBinding.btnFavourite.setText ( "Remove from Favorites" );
                } else {
                    // not exists in favorite
                    activityPdfDetailBinding.btnFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds ( 0, R.drawable.ic_favorite_border_white, 0, 0 );
                    activityPdfDetailBinding.btnFavourite.setText ( "Add to Favorites" );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
}