package com.example.znews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class FragmentHome extends Fragment {
    protected ArrayList<News> all_news;

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
            return new ViewHolder(v);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            holder.news_item.setOnClickListener(v -> showDetail(all_news.get(position)));

            String title = (String) all_news.get(position).getTitle();
            if (title != null) {
                if (title.length() > 20) title = title.substring(0, 20) + "...";
            } else {
                title = "";
            }
            holder.news_title.setText(title);

            String picUrl = all_news.get(position).getPicUrl();
            Glide.with(holder.itemView)
                    .load(picUrl)
                    .centerCrop()
                    .dontTransform()
                    .dontAnimate()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(holder.news_pic);

            holder.favor_button.setOnClickListener(v -> {
                boolean favor = all_news.get(position).getFav();
                if (favor) {
                    holder.favor_button.setImageDrawable(getResources().getDrawable(R.drawable.fav_grey_foreground));
                    all_news.get(position).setFav(false);
                }
                else {
                    holder.favor_button.setImageDrawable(getResources().getDrawable(R.drawable.fav_foreground));
                    all_news.get(position).setFav(true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return all_news.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout news_item;
            TextView news_title;
            ImageView news_pic;
            ImageButton favor_button;

            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                news_item = itemView.findViewById(R.id.news_item);
                news_title = itemView.findViewById(R.id.news_title);
                news_pic = itemView.findViewById(R.id.news_pic);
                favor_button = itemView.findViewById(R.id.favor_button);
            }
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
        RefreshLayout srl = view.findViewById(R.id.smart_refresh);
        srl.setRefreshHeader(new ClassicsHeader(requireContext()));
        srl.setRefreshFooter(new ClassicsFooter(requireContext()));
        srl.setOnRefreshListener(refreshLayout -> {
            try {
                initData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            refreshLayout.finishRefresh(3000);
        });
        srl.setOnLoadMoreListener(refreshLayout -> {
            try {
                Log.e("LoadMoreListener", "entering moreData");
                moreData(all_news.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            refreshLayout.finishLoadMore(3000);
        });

        try {
            initData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RecyclerView rv_list = view.findViewById(R.id.rv_list);
        rv_list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv_list.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_list.setAdapter(new MyAdapter());

        super.onViewCreated(view, savedInstanceState);
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
        cnt += 10;
        String head = "https://olympics.com/tokyo-2020/zh/library/editorial/country/all/sport/all/order/desc/skip/";
        String tail = "/limit/10/morenews-grid";
        String root = head + cnt + tail;
        Log.e("SubThread/getNews", root);
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
                Log.e("Cards_Item/url", href);
                Element article = a.children().select("article").first();
                Element img = article.children().select("figure").first().children().select("picture").first().children().select("img").first();
                String picUrl = img.attr("data-src");
                if (picUrl.equals("")) continue;
                news.setPicUrl(picUrl);
                Log.e("Cards_Item/picUrl", picUrl);
                Element h3 = article.children().select("div").first().children().select("header").first().children().select("div").first().children().select("h3").first();
                String title = h3.attr("title");
                if (title.equals("")) continue;
                news.setTitle(title);
                Log.e("Cards_Item/title", title);
                all_news.add(news);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}