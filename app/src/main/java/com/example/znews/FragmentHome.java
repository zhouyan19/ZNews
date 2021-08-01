package com.example.znews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class FragmentHome extends Fragment {

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout srl = view.findViewById(R.id.swipe_refresh);
        srl.setColorSchemeResources(R.color.royal_blue);
        srl.setOnRefreshListener(() -> {
            try {
                initData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            srl.setRefreshing(false);
        });

        srl.setRefreshing(true);
        try {
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void initData () throws InterruptedException {
        Thread net_conn_thread = new Thread(() -> {
            ArrayList<String> url_list = getUrlList();
            assert url_list != null;
            if (url_list.size() == 0) {
                // TODO
            } else {
                Log.e("SubThread/url_list", String.valueOf(url_list.size()));
                try {
                    getNews(url_list);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        net_conn_thread.start();
        net_conn_thread.join();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ArrayList<String> getUrlList () {
        ArrayList<String> url_list  = new ArrayList<>();
        String root = "https://olympics.com/tokyo-2020/zh/news/";
        try {
            Log.e("SubThread/getUrlList", "Trying to get the html...");
            Document document = Jsoup.parse(new URL(root).openStream(), "UTF-8", root);
            Log.e("SubThread/getUrlList", "Document!");
            Elements cards_group = document.getElementsByClass("tk-cardsgroup__item-link");
            Elements es = cards_group.select("a");
            for (Element e : es) {
                String s = e.attr("href");
                Log.e("", s);
                if (s != null) url_list.add(s);
            }
            for (String url : url_list) {
                Log.e("SubThread/getUrlList", url);
            }
            return url_list;
        } catch (Exception e) {
            Log.e("SubThread/getUrlList", e.toString());
            return new ArrayList<>();
        }
    }

    public void getNews (ArrayList<String> url_list) throws InterruptedException {
        all_news = new ArrayList<>();
        for (String url : url_list) {
            News news = getNewsInfo(url);
            all_news.add(news);
        }
        Log.e("SubThread/getNews", "News got");
    }

    public News getNewsInfo (String url) throws InterruptedException {
        News news = new News();
        Thread news_info_thread = new Thread(() -> {
            try {
                Document document = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
                Log.e("SubSub/getNewsInfo", "Document!");
                Elements article_title = document.getElementsByClass("tk-article__title");
                Element title = article_title.select("h1").first();
                news.setTitle(title.text());
                Log.e("getNewsInfo/setTitle", title.text());
                Elements article_des = document.getElementsByClass("tk-article__summary");
                Element des = article_des.select("p").first();
                news.setDes("【" + des.text() + "】");
                Log.e("getNewsInfo/setDes", des.text());
                Element article_content = document.getElementsByClass("tk-article__part markdown").select("div").first();
                Log.e("(Content)", "article_content");
                Elements paragraphs = article_content.children().select("p");
                StringBuilder content = new StringBuilder();
                for (Element p : paragraphs) {
                    content.append(p.text());
                }
                news.setContent(content);
                Log.e("getNewsInfo/setContent", String.valueOf(content.length()));
                Element picture = document.getElementsByClass("tk-lead-block__picture").first();
                Element img = picture.children().select("img").first();
                String picUrl = img.attr("src");
                news.setPicUrl(picUrl);
                Log.e("getNewsInfo/setPicUrl", picUrl);
            } catch (Exception e) {
                Log.e("SubSub/getNewsInfo", e.toString());
                news.setTitle("");
                news.setContent("");
                news.setDes("");
                news.setPicUrl("");
            }
        });
        news_info_thread.start();
        news_info_thread.join();
        return news;
    }
}