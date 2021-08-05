package com.example.znews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class FragmentHome extends Fragment {
    private int news_cnt = 0;
    protected ArrayList<News> all_news;

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return all_news.size();
        }

        @Override
        public Object getItem(int position) {
            return all_news.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"InflateParams", "UseCompatLoadingForDrawables"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // sx: 这里通过 parent.getContext() 得到父对象的上下文，以填充 food_item。
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, null);
            } else {
                view = convertView;
            }

            LinearLayout news_item = view.findViewById(R.id.news_item);
            news_item.setOnClickListener(v -> showDetail(all_news.get(position)));

            TextView news_title = view.findViewById(R.id.news_title);
            String title = (String) all_news.get(position).getTitle();
            if (title != null) {
                if (title.length() > 20) title = title.substring(0, 20) + "...";
            } else {
                title = "";
            }
            news_title.setText(title);

            ImageView news_pic = view.findViewById(R.id.news_pic);
            String picUrl = all_news.get(position).getPicUrl();
            Glide.with(view)
                    .load(picUrl)
                    .centerCrop()
                    .dontTransform()
                    .dontAnimate()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(news_pic);

            ImageButton favor_button = view.findViewById(R.id.favor_button);
            favor_button.setOnClickListener(v -> {
                boolean favor = all_news.get(position).getFav();
                if (favor) {
                    favor_button.setImageDrawable(getResources().getDrawable(R.drawable.fav_grey_foreground));
                    all_news.get(position).setFav(false);
                }
                else {
                    favor_button.setImageDrawable(getResources().getDrawable(R.drawable.fav_foreground));
                    all_news.get(position).setFav(true);
                }
            });

            return view;
        }
    }

    public void showDetail(News news) {
        Intent intent = new Intent();
        intent.setClass(getContext(), NewsDetail.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("news", news);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout srl = view.findViewById(R.id.swipe_refresh);
        srl.setColorSchemeResources(R.color.royal_blue);
        srl.setOnRefreshListener(() -> {
            try {
                news_cnt = 0;
                initData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            srl.setRefreshing(false);
        });

        srl.setRefreshing(true);
        try {
            news_cnt = 0;
            initData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        srl.setRefreshing(false);

        ListView lv_list = view.findViewById(R.id.lv_list);
        lv_list.setAdapter(new MyAdapter());
        Log.e("MainTread", "Adapter set");

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void initData () throws InterruptedException {
        Thread net_conn_thread = new Thread(() -> {
            try {
                all_news = new ArrayList<>();
                getAllNews(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
    }

    public void moreData(int cnt) throws InterruptedException {
        Thread net_conn_thread = new Thread(() -> {
            try {
                getAllNews(cnt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
    }

    private void getAllNews (int cnt) throws IOException {
        String head = "https://olympics.com/tokyo-2020/zh/library/editorial/country/all/sport/all/order/desc/skip/";
        String tail = "/limit/10/morenews-grid";
        String root = head + cnt + tail;
        Log.e("SubThread/getNews", "Trying to get the html...");
        Document document = Jsoup.parse(new URL(root).openStream(), "UTF-8", root);
        Log.e("SubThread/getNews", "Document!");
        Element cards_group = document.getElementsByClass("tk-cardsgroup__sequence row").select("ul").first();
        Elements cards_items = cards_group.children().select("li");
        for (Element i : cards_items) {
            News news = new News();
            try {
                Element a = i.children().select("a").first();
                String href = a.attr("href");
                if (href.equals("")) continue;
                news.setUrl(href);
                Element article = a.children().select("article").first();
                Element img = article.children().select("figure").first().children().select("picture").first().children().select("img").first();
                String picUrl = img.attr("data-src");
                if (picUrl.equals("")) continue;
                news.setPicUrl(picUrl);
                Element h3 = article.children().select("div").first().children().select("header").first().children().select("div").first().children().select("h3").first();
                String title = h3.attr("title");
                if (title.equals("")) continue;
                news.setTitle(title);
                all_news.add(news);
                news_cnt++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}