package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.PdfAdminAdapter;
import com.example.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    // arraylist in which we want to search
    ArrayList<ModelPdf> filterList;
    PdfAdminAdapter pdfAdminAdapter;
    // adapter in which filter need to be implemented

    // constructor
    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, PdfAdminAdapter pdfAdminAdapter) {
        this.filterList = filterList;
        this.pdfAdminAdapter = pdfAdminAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults ( );
        // value should not be null and empty
        if (constraint != null && constraint.length ( ) > 0) {
            // change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString ( ).toLowerCase ( );
            ArrayList<ModelPdf> filterModels = new ArrayList<> ( );
            for (int i = 0; i < filterList.size ( ); i++) {
                // validate
                if (filterList.get ( i ).getTitle ( ).toLowerCase ( ).contains ( constraint )) {
                    // add to filtered list
                    filterModels.add ( filterList.get ( i ) );
                }
            }
            filterResults.count = filterModels.size ( );
            filterResults.values = filterModels;
        } else {
            filterResults.count = filterList.size ( );
            filterResults.values = filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        // apply filter changes
        PdfAdminAdapter.pdfArrayList = (ArrayList<ModelPdf>) filterResults.values;
        pdfAdminAdapter.notifyDataSetChanged ( );
    }
}
