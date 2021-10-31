package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.Constants;
import com.example.bookapp.databinding.ActivityViewPdfBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shashank.sony.fancytoastlib.FancyToast;

public class ViewPdfActivity extends AppCompatActivity {

    // view binding
    private ActivityViewPdfBinding activityViewPdfBinding;

    // Firebase
    private DatabaseReference databaseReferenceBooks;
    private StorageReference storageReferencePdf;

    // book id, get from intent
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityViewPdfBinding = ActivityViewPdfBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityViewPdfBinding.getRoot ( ) );

        // get book id from intent that we passed in intent
        try {
            bookId = getIntent ( ).getStringExtra ( "bookId" );
        } catch (Exception e) {
            e.printStackTrace ( );
        }

        // Firebase
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );

        loadBookDetails ( );

        // handle click, go back
        activityViewPdfBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );
    }

    private void loadBookDetails() {
        // database reference to get the book details, e.g. get book url using bookId
        // Step 1) Get book url using Book Id
        databaseReferenceBooks.child ( bookId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get data
                String bookName = "" + snapshot.child ( "title" ).getValue (  );
                activityViewPdfBinding.textViewTitle.setText ( bookName );
                String pdfUrl = "" + snapshot.child ( "url" ).getValue ( );
                // Step 2) Load pdf using that url from firebase storage
                loadBookFromUrl ( pdfUrl );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @SuppressLint("SetTextI18n")
    private void loadBookFromUrl(String pdfUrl) {
        // Step 2) Load pdf using that url from firebase storage
        storageReferencePdf = FirebaseStorage.getInstance ( ).getReferenceFromUrl ( pdfUrl );
        storageReferencePdf.getBytes ( Constants.MAX_BYTES_PDF ).addOnSuccessListener ( bytes -> {
            // load pdf using bytes
            activityViewPdfBinding.pdfView.fromBytes ( bytes ).swipeHorizontal ( false ).onPageChange ( (page, pageCount) -> {
                // set current and total pages at the bottom of the activity
                int currentPage = (page + 1); // +1 because, page starts from 0
                activityViewPdfBinding.textViewSubtitle.setText ( currentPage + "/" + pageCount );
            } ).onError ( t -> FancyToast.makeText ( getApplicationContext ( ), t.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( ) ).onPageError ( (page, t) -> FancyToast.makeText ( getApplicationContext ( ), "Error on " + page + ": " + t.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( ) ).load ( );
            activityViewPdfBinding.progressBar.setVisibility ( View.GONE );
        } ).addOnFailureListener ( e -> {
            // failed to load book
            activityViewPdfBinding.progressBar.setVisibility ( View.GONE );
        } );
    }
}