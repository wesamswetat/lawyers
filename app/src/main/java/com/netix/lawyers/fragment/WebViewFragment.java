package com.netix.lawyers.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.netix.lawyers.R;


public class WebViewFragment extends Fragment {


    public WebViewFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View webView = inflater.inflate(R.layout.fragment_web_view, container, false);

        WebView myWebView = webView.findViewById(R.id.webview);

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl("file:///android_asset/webView/home.html");
        WebSettings webSettings = myWebView.getSettings();

        WebSettings ws = myWebView.getSettings();
        ws.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new WebViewClient() {

        });

        return webView;
    }


}
