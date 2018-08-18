package com.example.xyzreader.utils;

import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.models.ArticleInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ArticleUtils {
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

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

    public static Spanned dateFrom(Date publishedDate, String author) {
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            return Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + author
                            + "</font>");

        } else {
            // If date is before 1902, just show the string
            return Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + author
                            + "</font>");

        }
    }
}
