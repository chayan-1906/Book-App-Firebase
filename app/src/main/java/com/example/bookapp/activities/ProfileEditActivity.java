package com.example.bookapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TaskInfo;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;
import java.util.Objects;

public class ProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "ProfileEditActivity";

    // view binding
    private ActivityProfileEditBinding activityProfileEditBinding;

    private ProgressDialog progressDialog;

    // firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private StorageReference storageReferenceProfileImages;

    private Uri imageUri;

    String fullName;
    String profileImage;
    String fileNameAndPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        activityProfileEditBinding = ActivityProfileEditBinding.inflate ( getLayoutInflater ( ) );
        setContentView ( activityProfileEditBinding.getRoot ( ) );

        progressDialog = new ProgressDialog ( this );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );

        // firebase
        firebaseAuth = FirebaseAuth.getInstance ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );

        loadUserInfo ( );

        // handle click, go back
        activityProfileEditBinding.imageButtonBack.setOnClickListener ( v -> onBackPressed ( ) );

        // handle click, pick image
        activityProfileEditBinding.imageViewProfile.setOnClickListener ( v -> showImageAttachMenu ( ) );

        // handle click, pick image
        activityProfileEditBinding.btnUpdate.setOnClickListener ( v -> validateData ( ) );
    }

    private void loadUserInfo() {
        Log.i ( TAG, "loadUserInfo: started" );
        databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get all info of user
                fullName = "" + snapshot.child ( "name" ).getValue ( );
                profileImage = "" + snapshot.child ( "profileImage" ).getValue ( );
                // set data to ui
                Objects.requireNonNull ( activityProfileEditBinding.textInputLayoutFullName.getEditText ( ) ).setText ( fullName );
                // set image using glide
                Glide.with ( ProfileEditActivity.this ).load ( profileImage ).placeholder ( R.drawable.profile_image ).into ( activityProfileEditBinding.imageViewProfile );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void validateData() {
        // get data
        fullName = Objects.requireNonNull ( activityProfileEditBinding.textInputLayoutFullName.getEditText ( ) ).getText ( ).toString ( ).trim ( );
        // validate data
        if (TextUtils.isEmpty ( fullName )) {
            FancyToast.makeText ( getApplicationContext ( ), "Enter your name", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false ).show ( );
            return;
        }
        if (imageUri == null) {
            // need to update without image
            updateProfile ( "" );
        } else {
            // need to update with image
            uploadImage ( );
        }
    }

    private void uploadImage() {
        Log.i ( TAG, "uploadImage: started" );
        progressDialog.setMessage ( "Updating profile photo..." );
        progressDialog.show ( );
        // image path and name, use uid to replace previous
        fileNameAndPath = "ProfileImages/" + firebaseAuth.getUid ( );
        // storage reference
        storageReferenceProfileImages = FirebaseStorage.getInstance ( ).getReference ( fileNameAndPath );
        storageReferenceProfileImages.putFile ( imageUri ).addOnSuccessListener ( taskSnapshot -> {
            Log.i ( TAG, "onSuccess: Profile Photo uploaded" );
            FancyToast.makeText ( getApplicationContext ( ), "Profile Photo uploaded successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false ).show ( );
            Log.i ( TAG, "onSuccess: Getting url of the uploaded image" );
            Task<Uri> uriTask = taskSnapshot.getStorage ( ).getDownloadUrl ( );
            while (!uriTask.isSuccessful ( )) ;
            String uploadedImageUrl = String.valueOf ( uriTask.getResult ( ) );
            Log.i ( TAG, "uploadImage: Uploaded Image URL: " + uploadedImageUrl );
            updateProfile ( uploadedImageUrl );
        } ).addOnFailureListener ( e -> {
            progressDialog.dismiss ( );
            Log.i ( TAG, "onFailure: Failed to upload Profile Photo" );
            FancyToast.makeText ( getApplicationContext ( ), "Failed to upload Profile Photo " + e, FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void updateProfile(String imageUri) {
        Log.i ( TAG, "updateProfile: Updating User Profile" );
        progressDialog.setMessage ( "Updating User Profile" );
        progressDialog.show ( );
        // set data
        HashMap<String, Object> hashMap = new HashMap<> ( );
        hashMap.put ( "name", fullName );
        if (imageUri != null) {
            hashMap.put ( "profileImage", imageUri );
        }
        // update data to db
        databaseReferenceUsers.child ( Objects.requireNonNull ( firebaseAuth.getUid ( ) ) ).updateChildren ( hashMap ).addOnSuccessListener ( aVoid -> {
            progressDialog.dismiss ( );
            Log.i ( TAG, "onSuccess: Profile Updated successfully" );
            // navigating to DashboardAdminActivity.class
            Intent intentProfileActivity = new Intent ( ProfileEditActivity.this, ProfileActivity.class );
            intentProfileActivity.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( intentProfileActivity );
            FancyToast.makeText ( getApplicationContext ( ), "Profile Updated successfully", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false ).show ( );
        } ).addOnFailureListener ( e -> {
            Log.i ( TAG, "onFailure: Failed to update database: " + e );
            progressDialog.dismiss ( );
            FancyToast.makeText ( getApplicationContext ( ), "Failed to update database " + e, FancyToast.LENGTH_LONG, FancyToast.ERROR, false ).show ( );
        } );
    }

    private void showImageAttachMenu() {
        // init/setup popup menu
        PopupMenu popupMenu = new PopupMenu ( this, activityProfileEditBinding.imageViewProfile );
        popupMenu.getMenu ( ).add ( Menu.NONE, 0, 0, "Camera" );
        popupMenu.getMenu ( ).add ( Menu.NONE, 1, 1, "Gallery" );
        popupMenu.show ( );
        // handle menu item click
        popupMenu.setOnMenuItemClickListener ( item -> {
            // get id of the item is clicked
            int which = item.getItemId ( );
            if (which == 0) {
                // Camera clicked
                pickImageCamera ( );
            } else if (which == 1) {
                // Gallery clicked
                pickImageGallery ( );
            }
            return false;
        } );
    }

    private void pickImageCamera() {
        // intent to pick image from Camera
        ContentValues contentValues = new ContentValues ( );
        contentValues.put ( MediaStore.Images.Media.TITLE, "New Pick" );    // image title
        contentValues.put ( MediaStore.Images.Media.DESCRIPTION, "Sample Image Description" );
        imageUri = getContentResolver ( ).insert ( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues );

        Intent intentCamera = new Intent ( MediaStore.ACTION_IMAGE_CAPTURE );
        intentCamera.putExtra ( MediaStore.EXTRA_OUTPUT, imageUri );
        cameraActivityResultLauncher.launch ( intentCamera );
    }

    private void pickImageGallery() {
        // intent to pick image from Gallery
        Intent intentGallery = new Intent ( Intent.ACTION_PICK );
        intentGallery.setType ( "image/*" );
        galleryActivityResultLauncher.launch ( intentGallery );
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult ( new ActivityResultContracts.StartActivityForResult ( ), result -> {
        // used to handle result of camera intent
        // get uri of image
        if (result.getResultCode ( ) == Activity.RESULT_OK) {
            Log.i ( TAG, "onActivityResult: Picked from Camera" + imageUri );
            activityProfileEditBinding.imageViewProfile.setImageURI ( imageUri );
        } else {
            FancyToast.makeText ( getApplicationContext ( ), "Cancelled", FancyToast.LENGTH_SHORT, FancyToast.INFO, false ).show ( );
        }
    } );

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult ( new ActivityResultContracts.StartActivityForResult ( ), result -> {
        // used to handle result of gallery intent
        // get uri of image
        if (result.getResultCode ( ) == Activity.RESULT_OK) {
            Log.i ( TAG, "onActivityResult: Picked from Gallery" + imageUri );
            Intent data = result.getData ( );
            imageUri = Objects.requireNonNull ( data ).getData ( );
            Log.i ( TAG, "onActivityResult: " + imageUri );
            activityProfileEditBinding.imageViewProfile.setImageURI ( imageUri );
        }
    } );
}