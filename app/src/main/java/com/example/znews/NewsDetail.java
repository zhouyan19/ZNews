package com.example.znews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

        TextView detail_title = findViewById(R.id.detail_title);
        detail_title.setText(news.getTitle());

        TextView detail_des = findViewById(R.id.detail_des);
        detail_des.setText(news.getDes());

        TextView detail_content = findViewById(R.id.detail_content);
        detail_content.setText(news.getContent());

        ImageView detail_pic = findViewById(R.id.detail_pic);
        String picUrl = news.getPicUrl();
        Thread get_pic_thread = new Thread(() -> {
            Bitmap bmp = getUrlImg(picUrl);
            detail_pic.setImageBitmap(bmp);
        });
        get_pic_thread.start();
        try {
            get_pic_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initData () {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        news = bundle.getParcelable("news");
    }

    private Bitmap getUrlImg (String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(3000); // 设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false); // 不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

}