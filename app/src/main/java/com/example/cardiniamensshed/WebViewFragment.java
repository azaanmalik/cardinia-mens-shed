package com.example.cardiniamensshed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WebViewFragment extends Fragment {

    private static final String ARG_URL = "url";

    private WebView webView;
    private String url;

    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
        }

        webView = view.findViewById(R.id.webView);

        // Set up WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript for the website
        webSettings.setDomStorageEnabled(true); // Enable DOM storage for modern websites

        // Handle navigation within the WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true; // Prevent opening links in an external browser
            }
        });

        // Load the initial URL
        webView.loadUrl(url);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Destroy the WebView to release resources
        if (webView != null) {
            webView.destroy();
        }
    }

    // Allow the parent activity to handle back navigation for the WebView
    public boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    public void goBack() {
        if (webView != null) {
            webView.goBack();
        }
    }
}