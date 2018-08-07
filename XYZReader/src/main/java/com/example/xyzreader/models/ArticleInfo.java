package com.example.xyzreader.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ArticleInfo implements Parcelable {
    private long id;
    private String title;
    private Date date;
    private String author;
    private String thumbnailUrl;
    private String photoUrl;
    private float aspectRatio;

    public ArticleInfo(long id, String title, Date date, String author, String thumbnailUrl, String photoUrl, float aspectRatio) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
        this.photoUrl = photoUrl;
        this.aspectRatio = aspectRatio;
    }

    private ArticleInfo(Parcel in) {
        id = in.readLong();
        title = in.readString();
        date = new Date(in.readLong());
        author = in.readString();
        thumbnailUrl = in.readString();
        photoUrl = in.readString();
        aspectRatio = in.readFloat();
    }

    public static final Creator<ArticleInfo> CREATOR = new Creator<ArticleInfo>() {
        @Override
        public ArticleInfo createFromParcel(Parcel in) {
            return new ArticleInfo(in);
        }

        @Override
        public ArticleInfo[] newArray(int size) {
            return new ArticleInfo[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeLong(date.getTime());
        dest.writeString(author);
        dest.writeString(thumbnailUrl);
        dest.writeString(photoUrl);
        dest.writeFloat(aspectRatio);
    }
}
