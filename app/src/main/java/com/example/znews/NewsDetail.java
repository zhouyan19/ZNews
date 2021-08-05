package com.example.znews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class NewsDetail extends AppCompatActivity {
    private News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.news_detail);
        initData();

        ImageButton back_home = findViewById(R.id.detail_back_home);
        back_home.setOnClickListener(v -> NewsDetail.this.finish());

        WebView web = (WebView) findViewById(R.id.detail_web);
        String web_url = news.getUrl();
        web.loadUrl(web_url);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void initData () {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        news = bundle.getParcelable("news");
    }
}