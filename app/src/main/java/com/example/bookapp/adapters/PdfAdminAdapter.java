package com.example.bookapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.activities.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filters.FilterPdfAdmin;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class PdfAdminAdapter extends RecyclerView.Adapter<PdfAdminAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "PdfAdminAdapter";

    // context
    private final Context context;

    // arraylist to hold list of data of type ModelPdf
    public static ArrayList<ModelPdf> pdfArrayList;
    public static ArrayList<ModelPdf> filterList;

    // viewBinding of row_pdf_admin.xml
    @SuppressLint("StaticFieldLeak")
    static RowPdfAdminBinding rowPdfAdminBinding;

    private FilterPdfAdmin filterPdfAdmin;

    // constructor
    public PdfAdminAdapter(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        PdfAdminAdapter.pdfArrayList = pdfArrayList;
        PdfAdminAdapter.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind layout using view binding
        rowPdfAdminBinding = RowPdfAdminBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( rowPdfAdminBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull PdfAdminAdapter.ViewHolder holder, int position) {
        // get data
        ModelPdf modelPdf = pdfArrayList.get ( position );
        String pdfId = modelPdf.getId ( );
        String categoryId = modelPdf.getCategoryId ( );
        String title = modelPdf.getTitle ( );
        String description = modelPdf.getDescription ( );
        String pdfUrl = modelPdf.getUploadedPdfUrl ( );
        String timestamp = modelPdf.getTimestamp ( );
        // convert timestamp into dd/MM/yyyy format
        String formattedDate = MyApplication.formatTimestamp ( timestamp );
        // set data
        holder.textViewBookName.setText ( title );
        holder.textViewBookDescription.setText ( description );
        holder.textViewBookDate.setText ( formattedDate );
        // load further details like category, pdf from url, pdf size in separate function
        MyApplication.loadCategory ( "" + categoryId, holder.textViewBookCategory );
        MyApplication.loadPdfFromUrlFromSinglePage ( context, "" + pdfUrl, "" + title, holder.pdfView, holder.progressBar, null );
        MyApplication.loadPdfSize ( context, "" + pdfUrl, "" + title, holder.textViewBookSize );
        // handle click, show dialog with options 1) Edit, 2) Delete
        holder.imageButtonMore.setOnClickListener ( v -> moreOptionsDialog ( modelPdf, holder ) );
        // handle click, open PdfDetailActivity, pass pdf/book id to get details of the pdf
        holder.itemView.setOnClickListener ( v -> {
            Intent intentPdfDetailsActivity = new Intent ( context, PdfDetailActivity.class );
            intentPdfDetailsActivity.putExtra ( "bookId", pdfId );
            context.startActivity ( intentPdfDetailsActivity );
        } );
        holder.itemView.setOnClickListener ( v -> {
            Intent intentPdfDetailsActivity = new Intent ( context, PdfDetailActivity.class );
            intentPdfDetailsActivity.putExtra ( "bookId", pdfId );
            context.startActivity ( intentPdfDetailsActivity );
        } );

        animate ( holder );
    }

    private void moreOptionsDialog(ModelPdf modelPdf, ViewHolder holder) {
        String bookId = modelPdf.getId ( );
        String bookUrl = modelPdf.getUploadedPdfUrl ( );
        String bookTitle = modelPdf.getTitle ( );
        // options to show in dialog
        String[] options = {"Edit", "Delete"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder ( context );
        builder.setTitle ( "Choose Options" )
                .setItems ( options, (dialog, which) -> {
                    // handle dialog option click
                    if (which == 0) {
                        // edit clicked, open PdfEditActivity to edit the book info
                        Intent intentPdfEditActivity = new Intent ( context, PdfEditActivity.class );
                        intentPdfEditActivity.putExtra ( "bookId", bookId );
                        context.startActivity ( intentPdfEditActivity );
                    } else if (which == 1) {
                        // delete clicked
                        MyApplication.deleteBook ( context, "" + bookId, "" + bookUrl, "" + bookTitle );
                    }
                } ).show ( );
    }

    @Override
    public int getItemCount() {
        // returns number of pdfs or size of list
        return pdfArrayList.size ( );
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation ( context, R.anim.bounce_interpolator );
        viewHolder.itemView.setAnimation ( animAnticipateOvershoot );
    }

    @Override
    public Filter getFilter() {
        if (filterPdfAdmin == null) {
            filterPdfAdmin = new FilterPdfAdmin ( filterList, this );
        }
        return filterPdfAdmin;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views of row_pdf_admin.xml
        RelativeLayout relativeLayoutPdfDetails;
        private final PDFView pdfView;
        private final ProgressBar progressBar;
        private final TextView textViewBookName;
        private final TextView textViewBookDescription;
        private final TextView textViewBookSize;
        private final TextView textViewBookDate;
        private final TextView textViewBookCategory;
        private final ImageButton imageButtonMore;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            relativeLayoutPdfDetails = rowPdfAdminBinding.relativeLayoutPdfDetails;
            pdfView = rowPdfAdminBinding.pdfView;
            progressBar = rowPdfAdminBinding.progressBar;
            textViewBookName = rowPdfAdminBinding.textViewBookName;
            textViewBookDescription = rowPdfAdminBinding.textViewBookDescription;
            textViewBookSize = rowPdfAdminBinding.textViewBookSize;
            textViewBookDate = rowPdfAdminBinding.textViewBookDate;
            textViewBookCategory = rowPdfAdminBinding.textViewBookCategory;
            imageButtonMore = rowPdfAdminBinding.imageButtonMore;
        }
    }
}