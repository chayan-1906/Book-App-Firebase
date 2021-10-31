package com.example.bookapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.example.bookapp.Constants.MAX_BYTES_PDF;

// Application class runs before launcher activity
public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    // firebase
    private static FirebaseAuth firebaseAuth;
    private static StorageReference storageReferencePdf;
    private static DatabaseReference databaseReferenceCategories;
    private static DatabaseReference databaseReferenceUsers;
    private static StorageReference storageReferenceBook;
    private static DatabaseReference databaseReferenceBooks;

    // progress dialog
    private static ProgressDialog progressDialog;

    @Override
    public void onCreate() {
        super.onCreate ( );
    }

    public static final String formatTimestamp(String timestamp) {
        Calendar calendar = Calendar.getInstance ( Locale.ENGLISH );
        calendar.setTimeInMillis ( Long.parseLong ( timestamp ) );
        // format timestamp to dd/MM/yyyy
        return DateFormat.format ( "dd/MM/yyyy", calendar ).toString ( );
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        progressDialog = new ProgressDialog ( context );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.setMessage ( "Deleting " + bookTitle + "..." );
        progressDialog.show ( );
        // delete book from firebase storage
        storageReferenceBook = FirebaseStorage.getInstance ( ).getReferenceFromUrl ( bookUrl );
        storageReferenceBook.delete ( ).addOnSuccessListener ( unused -> {
            databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );
            databaseReferenceBooks.child ( bookId ).removeValue ( ).addOnSuccessListener ( unused1 -> {
                progressDialog.dismiss ( );
                FancyToast.makeText ( context, bookTitle + " deleted successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( );
            } ).addOnFailureListener ( e -> {
                progressDialog.dismiss ( );
                FancyToast.makeText ( context, e.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
            } );
        } ).addOnFailureListener ( e -> FancyToast.makeText ( context, e.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( ) );
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public static void loadPdfSize(Context context, String bookUrl, String pdfTitle, TextView textViewBookSize) {
        // using url we can get file size and its metadata from firebase storage
        storageReferencePdf = FirebaseStorage.getInstance ( ).getReferenceFromUrl ( bookUrl );
        storageReferencePdf.getMetadata ( ).addOnSuccessListener ( storageMetadata -> {
            // get size in  bytes
            double bytes = storageMetadata.getSizeBytes ( );
            // convert bytes into KB, MB
            double kb = bytes / 1024;
            double mb = kb / 1024;
            if (mb >= 1) {
                textViewBookSize.setText ( String.format ( "%.2f", mb ) + " MB" );
            } else if (kb >= 1) {
                textViewBookSize.setText ( String.format ( "%.2f", kb ) + " KB" );
            } else {
                textViewBookSize.setText ( String.format ( "%.2f", bytes ) + " bytes" );
            }
        } ).addOnFailureListener ( e -> {
            // failed getting metadata
            FancyToast.makeText ( context, e.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
        } );
    }

    public static void loadPdfFromUrlFromSinglePage(Context context, String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView textViewPagesCount) {
        // using url we can get file and its metadata from firebase storage
        storageReferencePdf = FirebaseStorage.getInstance ( ).getReferenceFromUrl ( pdfUrl );
        storageReferencePdf.getBytes ( MAX_BYTES_PDF ).addOnSuccessListener ( bytes -> {
            // set to pdfView
            pdfView.fromBytes ( bytes ).pages ( 0 ).spacing ( 0 ).swipeHorizontal ( false ).enableSwipe ( false ).onError ( t -> {
                // hide progress bar
                progressBar.setVisibility ( View.GONE );
                FancyToast.makeText ( context, t.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
            } ).onPageError ( (page, t) -> {
                // hide progress bar
                progressBar.setVisibility ( View.GONE );
                FancyToast.makeText ( context, t.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
            } ).onLoad ( nbPages -> {
                // pdf loaded
                // hide progress bar
                progressBar.setVisibility ( View.GONE );
                // if pages param is not null, then set page numbers
                if (textViewPagesCount != null)
                    textViewPagesCount.setText ( String.valueOf ( nbPages ) );
            } ).load ( ); // show only first page
        } ).addOnFailureListener ( e -> {
            // hide progress bar
            progressBar.setVisibility ( View.GONE );
            FancyToast.makeText ( context, "Failed to get file", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
        } );
    }

    public static void loadCategory(String categoryId, TextView textViewBookCategory) {
        // get category using categoryId
        databaseReferenceCategories = FirebaseDatabase.getInstance ( ).getReference ( "Categories" );
        databaseReferenceCategories.child ( categoryId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = "" + snapshot.child ( "category" ).getValue ( );
                // set to category textview
                textViewBookCategory.setText ( category );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    public static void incrementBookViewCount(String bookId) {
        Log.d ( TAG, "incrementBookViewCount: " + bookId );
        // 1) Get the book views count
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );
        databaseReferenceBooks.child ( bookId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get views count
                String viewsCount = "" + snapshot.child ( "viewsCount" ).getValue ( );
                // in case of null, replace with 0
                if (viewsCount.equals ( "" ) || viewsCount.equals ( "null" )) {
                    viewsCount = "0";
                }
                // 2) Increment views count
                String newViewsCount = String.valueOf ( Integer.parseInt ( viewsCount ) + 1 );
                HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
                stringObjectHashMap.put ( "viewsCount", newViewsCount );
                databaseReferenceBooks.child ( bookId ).updateChildren ( stringObjectHashMap );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    public static void downloadBook(Context context, String bookId, String bookName, String bookUrl) {
        String nameWithExtension = bookName + ".pdf";
        // progress dialog
        ProgressDialog progressDialog = new ProgressDialog ( context );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setMessage ( "Downloading " + bookName );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );
        progressDialog.show ( );
        // download from firebase
        storageReferenceBook = FirebaseStorage.getInstance ( ).getReferenceFromUrl ( bookUrl );
        storageReferenceBook.getBytes ( MAX_BYTES_PDF ).addOnSuccessListener ( bytes -> saveDownloadedBook ( context, progressDialog, bytes, nameWithExtension, bookId ) ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            FancyToast.makeText ( context, "Failed to download: " + e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    public static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory ( Environment.DIRECTORY_DOWNLOADS );
            downloadsFolder.mkdirs ( );
            File bookAppFolder = new File ( downloadsFolder.getPath ( ) + "/BookApp" );
            bookAppFolder.mkdirs ( );
            String filePath = downloadsFolder.getPath ( ) + "/BookApp/" + nameWithExtension;
            FileOutputStream fileOutputStream = new FileOutputStream ( filePath );
            fileOutputStream.write ( bytes );
            fileOutputStream.close ( );
            progressDialog.dismiss ( );
            FancyToast.makeText ( context, "Downloaded Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( );
            incrementBookDownloadCount ( bookId );
        } catch (Exception e) {
            FancyToast.makeText ( context, "Failed to download: " + e.getMessage ( ), FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
            progressDialog.dismiss ( );
        }
    }

    public static void incrementBookDownloadCount(String bookId) {
        // Step 1: Get previous download count
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );
        databaseReferenceBooks.child ( bookId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get views count
                String downloadsCount = "" + snapshot.child ( "downloadsCount" ).getValue ( );
                // in case of null, replace with 0
                if (downloadsCount.equals ( "" ) || downloadsCount.equals ( "null" )) {
                    downloadsCount = "0";
                }
                // 2) Increment views count
                String newDownloadsCount = String.valueOf ( Integer.parseInt ( downloadsCount ) + 1 );
                HashMap<String, Object> stringObjectHashMap = new HashMap<> ( );
                stringObjectHashMap.put ( "downloadsCount", newDownloadsCount );
                databaseReferenceBooks.child ( bookId ).updateChildren ( stringObjectHashMap );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    /*public static void loadPdfPageCount(Context context, String bookUrl, PDFView pdfView, TextView textViewPagesCount) {
        Log.i ( TAG, "loadPdfPageCount: started" );
        // load pdf file from firebase storage using url
        storageReferenceBook = FirebaseStorage.getInstance ( ).getReferenceFromUrl ( bookUrl );
        Log.i ( TAG, "loadPdfPageCount: bookUrl: " + bookUrl );
        storageReferenceBook.getBytes ( MAX_BYTES_PDF ).addOnSuccessListener ( bytes -> {
            // file received
            // load book pages using PdfView library
            pdfView.fromBytes ( bytes ).onLoad ( nbPages -> {
                // pdf loaded from bytes we got from firebase storage, we can now show number of pages
                Log.i ( TAG, "loadPdfPageCount: " + nbPages );
                textViewPagesCount.setText ( String.valueOf ( nbPages ) );
            } ).load ( );
        } ).addOnFailureListener ( e -> {
            // failed to receive file
            Log.i ( TAG, "loadPdfPageCount: failed to get no. of pages: " + e );
        } );
    }*/

    public static void addToFavourite(Context context, String bookId) {
        // we can add only if user is logged in
        // 1) check if user is logged in
        firebaseAuth = FirebaseAuth.getInstance ( );
        if (firebaseAuth.getCurrentUser ( ) == null) {
            // not logged in, so can't add to fav
            FancyToast.makeText ( context, "You are not logged in", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false ).show ( );
        } else {
            long timestamp = System.currentTimeMillis ( );
            // setup data to add in firebase db of current user for fav book
            HashMap<String, Object> hashMap = new HashMap<> ( );
            hashMap.put ( "bookId", bookId );
            hashMap.put ( "timestamp", timestamp );
            // save to db
            databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
            databaseReferenceUsers.child ( firebaseAuth.getUid ( ) ).child ( "Favorites" ).child ( bookId ).setValue ( hashMap ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                @Override
                public void onSuccess(Void aVoid) {
                    FancyToast.makeText ( context, "Added to favorites list", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( );
                }
            } ).addOnFailureListener ( new OnFailureListener ( ) {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FancyToast.makeText ( context, "Failed to add to favorites list " + e, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
                }
            } );
        }
    }

    public static void removeFromFavorites(Context context, String bookId) {
        // we can remove only if user is logged in
        // 1) check if user is logged in
        firebaseAuth = FirebaseAuth.getInstance ( );
        if (firebaseAuth.getCurrentUser ( ) == null) {
            // not logged in, so can't remove from fav
            FancyToast.makeText ( context, "You are not logged in", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false ).show ( );
        } else {
            // remove from db
            databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
            databaseReferenceUsers.child ( firebaseAuth.getUid ( ) ).child ( "Favorites" ).child ( bookId ).removeValue ( ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                @Override
                public void onSuccess(Void aVoid) {
                    FancyToast.makeText ( context, "Removed from favorites list", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( );
                }
            } ).addOnFailureListener ( new OnFailureListener ( ) {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FancyToast.makeText ( context, "Failed to remove from favorites list " + e, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( );
                }
            } );
        }
    }

}