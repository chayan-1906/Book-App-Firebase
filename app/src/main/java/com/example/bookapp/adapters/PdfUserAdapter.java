package com.example.bookapp.adapters;

import android.annotation.SuppressLint;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.databinding.RowPdfUserBinding;
import com.example.bookapp.filters.FilterPdfUser;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class PdfUserAdapter extends RecyclerView.Adapter<PdfUserAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "PdfUserAdapter";

    // context
    private final Context context;

    // arraylist to hold list of data of type ModelPdf
    public static ArrayList<ModelPdf> pdfArrayList;
    public static ArrayList<ModelPdf> filterList;

    // viewBinding of row_pdf_User.xml
    @SuppressLint("StaticFieldLeak")
    static RowPdfUserBinding rowPdfUserBinding;

    private FilterPdfUser filterPdfUser;

    // constructor
    public PdfUserAdapter(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        PdfUserAdapter.pdfArrayList = pdfArrayList;
        PdfUserAdapter.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind layout using view binding
        rowPdfUserBinding = RowPdfUserBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( rowPdfUserBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull PdfUserAdapter.ViewHolder holder, int position) {
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
        // handle click, open PdfDetailUserActivity, pass pdf/book id to get details of the pdf
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
        if (filterPdfUser == null) {
            filterPdfUser = new FilterPdfUser ( filterList, this );
        }
        return filterPdfUser;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views of row_pdf_User.xml
        RelativeLayout relativeLayoutPdfDetails;
        private final PDFView pdfView;
        private final ProgressBar progressBar;
        private final TextView textViewBookName;
        private final TextView textViewBookDescription;
        private final TextView textViewBookSize;
        private final TextView textViewBookDate;
        private final TextView textViewBookCategory;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            relativeLayoutPdfDetails = rowPdfUserBinding.relativeLayoutPdfDetails;
            pdfView = rowPdfUserBinding.pdfView;
            progressBar = rowPdfUserBinding.progressBar;
            textViewBookName = rowPdfUserBinding.textViewBookName;
            textViewBookDescription = rowPdfUserBinding.textViewBookDescription;
            textViewBookSize = rowPdfUserBinding.textViewBookSize;
            textViewBookDate = rowPdfUserBinding.textViewBookDate;
            textViewBookCategory = rowPdfUserBinding.textViewBookCategory;
        }
    }
}