package com.example.xyzreader.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.text.Html;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_ARTICLE)) {

            articleInfo = extras.getParcelable(EXTRA_ARTICLE);
//            String body = extras.getString(EXTRA_BODY);

            if (articleInfo != null) {
                populatePhoto(articleInfo);
                populateTitles(articleInfo);
                setShareButton();
            }
        }

//        getSupportLoaderManager().initLoader(0, null, this);

//        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
//        mPager = findViewById(R.id.pager);
//        mPager.setAdapter(mPagerAdapter);
//        mPager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
//        mPager.setPageMarginDrawable(new ColorDrawable(0x00000000));
//
//        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                if (mCursor != null) {
//                    mCursor.moveToPosition(position);
//                    mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
//                }
//            }
//        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
//                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
//                mSelectedItemId = mStartId;
            }
        }

//        getSupportLoaderManager().initLoader(0, null, this);

//        supportPostponeEnterTransition();
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

    private void populatePhoto(ArticleInfo articleInfo) {

        Picasso.with(this)
                .load(articleInfo.getPhotoUrl())
                .fit()
                .centerCrop()
                .into(imgThumbnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) imgThumbnail.getDrawable()).getBitmap();
                        setToolbarColor(bitmap);

//                        scheduleStartPostponedTransition(photo);
                    }

                    @Override
                    public void onError() { }
                });
    }

    private void scheduleStartPostponedTransition(final ImageView photo) {
        photo.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        photo.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
//        supportStartPostponedEnterTransition();

//        mCursor = cursor;
//        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
//        if (mStartId > 0) {
//            mCursor.moveToFirst();
//            // TODO: optimize
//            while (!mCursor.isAfterLast()) {
//                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
////                    final int position = mCursor.getPosition();
////                    mPager.setCurrentItem(position, false);
//                    break;
//                }
//                mCursor.moveToNext();
//            }
//            mStartId = 0;
//        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
//        mCursor = null;
//        mPagerAdapter.notifyDataSetChanged();
    }

//    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
//        if (itemId == mSelectedItemId) {
//            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
////            updateUpButtonPosition();
//        }
//    }

//    private void updateUpButtonPosition() {
//        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
//        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
//    }

//    private class MyPagerAdapter extends FragmentStatePagerAdapter {
//        MyPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public void setPrimaryItem(ViewGroup container, int position, Object object) {
//            super.setPrimaryItem(container, position, object);
////            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
////            if (fragment != null) {
//////                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
//////                updateUpButtonPosition();
////            }
//        }
//
//
//        @Override
//        public Fragment getItem(int position) {
//            mCursor.moveToPosition(position);
//            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
//        }
//
//        @Override
//        public int getCount() {
//            return (mCursor != null) ? mCursor.getCount() : 0;
//        }
//    }
}
