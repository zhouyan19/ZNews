package com.example.znews;

import android.net.NetworkInfo;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.io.Serializable;

public class News implements Parcelable {
    private CharSequence title;
    private CharSequence des;
    private CharSequence content;
    private String picUrl;
    private Boolean fav;

    public News () {
        title = "";
        des = "";
        content = "";
        picUrl = "";
        fav = false;
    }

    public News (String _t, String _d, String _c, String _p, Boolean _f) {
        title = _t;
        des = _d;
        content = _c;
        picUrl = _p;
        fav = _f;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected News(Parcel in) {
        title = in.readString();
        des = in.readString();
        content = in.readString();
        picUrl = in.readString();
        fav = in.readBoolean();
    }

    CharSequence getTitle () { return title; }
    CharSequence getDes () { return des; }
    CharSequence getContent () { return content; }
    String getPicUrl () { return picUrl; }
    Boolean getFav () { return fav; }

    void setTitle (CharSequence _t) { title = _t; }
    void setDes (CharSequence _d) { des = _d; }
    void setContent (CharSequence _c) { content = _c; }
    void setPicUrl (String _p) { picUrl = _p; }
    void setFav (Boolean _f) { fav = _f; }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title.toString());
        dest.writeString(des.toString());
        dest.writeString(content.toString());
        dest.writeString(picUrl);
        dest.writeBoolean(fav);
    }
}
