package com.netix.lawyers.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.netix.lawyers.R;


public class PDFViewerFragment extends Fragment {

    PDFView pdfView;

    public PDFViewerFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View PDFViewer = inflater.inflate(R.layout.fragment_pdfviwer, container, false);
        pdfView =  PDFViewer.findViewById(R.id.pdfView);

        pdfView.fitToWidth();
        pdfView.documentFitsView();

        pdfView.fromAsset("pdf/exa.pdf")
                .enableSwipe(true)
                .enableDoubletap(true)
                .defaultPage(1)
                .load();


        return PDFViewer;
    }

}
