package com.example.bookapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfFavoriteBinding;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfFavoriteAdapter extends RecyclerView.Adapter<PdfFavoriteAdapter.ViewHolder> {

    private static final String TAG = "PdfFavoriteAdapter";

    // view binding for row_pdf _favorite.xml
    @SuppressLint("StaticFieldLeak")
    static RowPdfFavoriteBinding rowPdfFavoriteBinding;

    // context
    private final Context context;

    // arraylist to hold list of data of type ModelPdf
    public static ArrayList<ModelPdf> pdfArrayList;

    // firebase
    private DatabaseReference databaseReferenceBooks;

    public PdfFavoriteAdapter(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        PdfFavoriteAdapter.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        rowPdfFavoriteBinding = RowPdfFavoriteBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( rowPdfFavoriteBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull PdfFavoriteAdapter.ViewHolder holder, int position) {
        /* Get data, set data, handle click */
        ModelPdf modelPdf = pdfArrayList.get ( position );
        loadBookDetails ( modelPdf, holder );
        holder.itemView.setOnClickListener ( v -> {
            Intent intentPdfDetailActivity = new Intent ( context, PdfDetailActivity.class );
            intentPdfDetailActivity.putExtra ( "bookId", modelPdf.getId ( ) );
            context.startActivity ( intentPdfDetailActivity );
        } );

        // handle click, remove from favorite
        holder.imageButtonRemoveFav.setOnClickListener ( v -> MyApplication.removeFromFavorites ( context, modelPdf.getId ( ) ) );

        animate ( holder );
    }

    private void loadBookDetails(ModelPdf modelPdf, ViewHolder holder) {
        String bookId = modelPdf.getId ( );
        Log.i ( TAG, "loadBookDetails: Book Details of book id: " + bookId );
        databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );
        databaseReferenceBooks.child ( bookId ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get book info
                String bookTitle = "" + snapshot.child ( "title" ).getValue ( );
                String bookDescription = "" + snapshot.child ( "description" ).getValue ( );
                String categoryId = "" + snapshot.child ( "categoryId" ).getValue ( );
                String bookUrl = "" + snapshot.child ( "url" ).getValue ( );
                String timestamp = "" + snapshot.child ( "timestamp" ).getValue ( );
                String uid = "" + snapshot.child ( "uid" ).getValue ( );

                // set data
                modelPdf.setFavorite ( true );
                modelPdf.setTitle ( bookTitle );
                modelPdf.setDescription ( bookDescription );
                modelPdf.setCategoryId ( categoryId );
                modelPdf.setUploadedPdfUrl ( bookUrl );
                modelPdf.setTimestamp ( timestamp );
                modelPdf.setUid ( uid );
                // format date
                String date = MyApplication.formatTimestamp ( timestamp );
                MyApplication.loadCategory ( categoryId, holder.textViewBookCategory );
                MyApplication.loadPdfFromUrlFromSinglePage ( context, bookUrl, bookTitle, holder.pdfView, holder.progressBar, null );
                MyApplication.loadPdfSize ( context, bookUrl, bookTitle, holder.textViewBookSize );
                // set data to views
                holder.textViewBookName.setText ( bookTitle );
                holder.textViewBookDescription.setText ( bookDescription );
                holder.textViewBookDate.setText ( date );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size ( );
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation ( context, R.anim.bounce_interpolator );
        viewHolder.itemView.setAnimation ( animAnticipateOvershoot );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views of row_pdf_favorite.xml
        private final PDFView pdfView;
        private final ProgressBar progressBar;
        private final TextView textViewBookName;
        private final ImageView imageButtonRemoveFav;
        private final TextView textViewBookDescription;
        private final TextView textViewBookSize;
        private final TextView textViewBookDate;
        private final TextView textViewBookCategory;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            pdfView = rowPdfFavoriteBinding.pdfView;
            progressBar = rowPdfFavoriteBinding.progressBar;
            textViewBookName = rowPdfFavoriteBinding.textViewBookName;
            imageButtonRemoveFav = rowPdfFavoriteBinding.imageButtonRemoveFav;
            textViewBookDescription = rowPdfFavoriteBinding.textViewBookDescription;
            textViewBookSize = rowPdfFavoriteBinding.textViewBookSize;
            textViewBookDate = rowPdfFavoriteBinding.textViewBookDate;
            textViewBookCategory = rowPdfFavoriteBinding.textViewBookCategory;
        }
    }
}