package com.example.xyzreader.utils;

import android.database.Cursor;

import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.models.ArticleInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ArticleUtils {

    public static ArticleInfo retrieveArticle(Cursor cursor, int position) {
        cursor.moveToPosition(position);

        long id = cursor.getLong(ArticleLoader.Query._ID);
        String title = cursor.getString(ArticleLoader.Query.TITLE);
        Date date = parsePublishedDate(cursor, position);
        String author = cursor.getString(ArticleLoader.Query.AUTHOR);
        String thumbnailUrl = cursor.getString(ArticleLoader.Query.THUMB_URL);
        String photoUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);
        float aspectRatio = cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);

        return new ArticleInfo(id, title, date, author, thumbnailUrl, photoUrl, aspectRatio);
    }

    private static Date parsePublishedDate(Cursor cursor, int position) {
        cursor.moveToPosition(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());

        try {
            String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return new Date();
        }
    }
}
