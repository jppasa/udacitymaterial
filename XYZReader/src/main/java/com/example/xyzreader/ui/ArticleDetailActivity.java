package com.example.xyzreader.ui;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapters.ArticleParagraphAdapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.models.ArticleInfo;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_ARTICLE = "extra_article";
    public static final String EXTRA_PHOTO = "extra_photo";
    private ArticleInfo articleInfo;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.US);
    // Use default locale format
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

//    private Cursor mCursor;
//    private long mStartId;

//    private long mSelectedItemId;
//    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
//    private int mTopInset;

//    private ViewPager mPager;
//    private MyPagerAdapter mPagerAdapter;
//    private View mUpButtonContainer;
//    private View mUpButton;

    @BindView(R.id.imgThumbnail) ImageView imgThumbnail;
    @BindView(R.id.txtArticleTitle) TextView txtArticleTitle;
    @BindView(R.id.txtArticleByline) TextView txtArticleByline;
    @BindView(R.id.share_fab) FloatingActionButton shareFab;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
//    @BindView(R.id.article_body) TextView txtArticleBody;

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

//            getWindow().setEnterTransition(null);
        }

        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_ARTICLE)) {

            articleInfo = extras.getParcelable(EXTRA_ARTICLE);
            Bitmap photo = extras.getParcelable(EXTRA_PHOTO);
//            String body = extras.getString(EXTRA_BODY);

            if (articleInfo != null) {
                populatePhoto(articleInfo, photo);
                populateTitles(articleInfo);
                setShareButton();
            }
        }


        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
//                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
//                mSelectedItemId = mStartId;
            }
        }

        loadArticle(false);
    }

    private void loadArticle(boolean forceReload) {
        if (forceReload) {
            getSupportLoaderManager().restartLoader(0, null, this);
        } else {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onEnterAnimationComplete() {
//        showFab();
    }

    private void populateTitles(ArticleInfo articleInfo) {
        txtArticleTitle.setText(articleInfo.getTitle());

        Date publishedDate = articleInfo.getDate(); //parsePublishedDate();

        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            txtArticleByline.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + articleInfo.getAuthor()
                            + "</font>"));

        } else {
            // If date is before 1902, just show the string
            txtArticleByline.setText(Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + articleInfo.getAuthor()
                            + "</font>"));

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setShareButton() {
        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
    }

    private void populatePhoto(ArticleInfo articleInfo, Bitmap photo) {
        if (photo != null) {
            imgThumbnail.setImageBitmap(photo);
            setToolbarColor(photo);
        } else {
            supportPostponeEnterTransition();

            Picasso.with(this)
                    .load(articleInfo.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(imgThumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) imgThumbnail.getDrawable()).getBitmap();
                            setToolbarColor(bitmap);

                            supportStartPostponedEnterTransition();
//                            scheduleStartPostponedTransition(imgThumbnail);
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
//                            scheduleStartPostponedTransition(imgThumbnail);
                        }
                    });
        }
    }

    private void showFab() {
        shareFab.animate()
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public void setToolbarColor(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(@NonNull Palette p) {
                CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
                FloatingActionButton fab = findViewById(R.id.share_fab);

                int backgroundColor = ContextCompat.getColor(ArticleDetailActivity.this, R.color.theme_primary);
                int statusBarColor = ContextCompat.getColor(ArticleDetailActivity.this, android.R.color.transparent);
                int fabColor = ContextCompat.getColor(ArticleDetailActivity.this, R.color.theme_accent);

                collapsingToolbar.setBackgroundColor(backgroundColor);

                backgroundColor = p.getMutedColor(backgroundColor);
//                statusBarColor = p.getDarkMutedColor(statusBarColor);
                fabColor = p.getLightVibrantColor(fabColor);

                collapsingToolbar.setBackgroundColor(backgroundColor);

                collapsingToolbar.setStatusBarScrimColor(statusBarColor);
                collapsingToolbar.setContentScrimColor(backgroundColor);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(statusBarColor);
                }

                fab.setBackgroundTintList(ColorStateList.valueOf(fabColor));
            }
        });
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        shareFab.animate()
                .alpha(0)
                .scaleX(0)
                .scaleY(0)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        shareFab.setVisibility(View.GONE);
                        ArticleDetailActivity.super.onBackPressed();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        shareFab.setVisibility(View.GONE);
                        ArticleDetailActivity.super.onBackPressed();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                })
                .start();

//        super.onBackPressed();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, articleInfo.getId());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        showFab();
//        String body = "N/A";
        String[] paragraphs = new String[]{ "N/A" };

        mCursor = cursor;
        if (mCursor != null && mCursor.moveToFirst()) {
            paragraphs = mCursor.getString(ArticleLoader.Query.BODY).split("(\r\n\r\n)|(\n\n)");
//            body = mCursor.getString(ArticleLoader.Query.BODY)
//                    .replaceAll("(\r\n|\n)", "<br />");
        } else {
            if (mCursor != null) {
                mCursor.close();
            }

            mCursor = null;
        }

        recyclerView.setAdapter(new ArticleParagraphAdapter(this, paragraphs));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

}
