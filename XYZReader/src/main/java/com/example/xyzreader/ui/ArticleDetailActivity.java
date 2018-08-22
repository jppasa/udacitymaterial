package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
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
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapters.ArticleParagraphAdapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.models.ArticleInfo;
import com.example.xyzreader.utils.ArticleUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_ARTICLE = "extra_article";
    public static final String EXTRA_PHOTO = "extra_photo";
    private static final String APP_BAR_EXPANDED = "app_bar_expanded";
    private static final float SUPPOSED_ACTION_BAR_HEIGHT_DP = 56f;

    private ArticleInfo articleInfo;

    @BindView(R.id.appbar) AppBarLayout appbar;
    @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.meta_bar) LinearLayout metaBar;
    @BindView(R.id.imgThumbnail) ImageView imgThumbnail;
    @BindView(R.id.txtArticleTitle) TextView txtArticleTitle;
    @BindView(R.id.txtArticleByline) TextView txtArticleByline;
    @BindView(R.id.share_fab) FloatingActionButton shareFab;
    @BindView(R.id.up_fab) FloatingActionButton upFab;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private Cursor mCursor;
    private int scrimHeightTrigger;
    private ViewPropertyAnimator mShowToolbarAnimation;
    private ViewPropertyAnimator mHideToolbarAnimation;

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

        setSupportActionBar(toolbar);
        toolbar.setTitle(" ");

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_ARTICLE)) {

            articleInfo = extras.getParcelable(EXTRA_ARTICLE);
            Bitmap photo = extras.getParcelable(EXTRA_PHOTO);

            if (articleInfo != null) {
                populatePhoto(articleInfo, photo);
                populateTitles(articleInfo);
                setShareButton();

                loadArticle();
            }
        }

        if (savedInstanceState != null) {
            boolean expand = savedInstanceState.getBoolean(APP_BAR_EXPANDED);
            appbar.setExpanded(expand, false);
            onEnterAnimationComplete();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean expanded = appbar.getBottom() >= scrimHeightTrigger;

        outState.putBoolean(APP_BAR_EXPANDED, expanded);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void loadArticle() {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void populateTitles(ArticleInfo articleInfo) {
        txtArticleTitle.setText(articleInfo.getTitle());
        txtArticleByline.setText(ArticleUtils.dateFrom(articleInfo.getDate(), articleInfo.getAuthor(), true));

        metaBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                metaBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                scrimHeightTrigger = appbar.getTotalScrollRange() - (metaBar.getHeight() + getStatusBarHeight());

                collapsingToolbar.setScrimVisibleHeightTrigger(scrimHeightTrigger);
            }
        });

        final int actionBarSize = getActionBarSize();
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (actionBarSize > 0) {
                    int scroll = appBarLayout.getTotalScrollRange() + verticalOffset;

                    Log.d("TOOLBAR_ANIMATION", scroll + " <= " + actionBarSize);

                    if (scroll <= actionBarSize) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                }
            }
        });
    }

    private void hideToolbar() {
        if (mHideToolbarAnimation == null) {
            mHideToolbarAnimation = toolbar.animate()
                    .alpha(0)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mShowToolbarAnimation = null;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mHideToolbarAnimation = null;
                        }
                    });
            mHideToolbarAnimation.start();
        }
    }

    private void showToolbar() {
        if (mShowToolbarAnimation == null) {
            mShowToolbarAnimation = toolbar.animate()
                    .alpha(1)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mHideToolbarAnimation = null;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mShowToolbarAnimation = null;
                        }
                    });
            mShowToolbarAnimation.start();
        }
    }

    private int getActionBarSize() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        return (int) dpToPixel(SUPPOSED_ACTION_BAR_HEIGHT_DP);
    }

    private float dpToPixel(float dp){
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setShareButton() {
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(
                        ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
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
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
                        }
                    });
        }
    }

    private void showShareFab() {
        shareFab.animate()
                .alpha(1)
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(500)
                .start();
    }

    public void setToolbarColor(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(@NonNull Palette p) {
                Context context = ArticleDetailActivity.this;

                int backgroundColor = ContextCompat.getColor(context, R.color.theme_primary);
                int statusBarColor = ContextCompat.getColor(context, android.R.color.transparent);
                int fabColor = ContextCompat.getColor(context, R.color.theme_accent);

                collapsingToolbar.setBackgroundColor(backgroundColor);

                backgroundColor = p.getMutedColor(backgroundColor);
                fabColor = p.getLightVibrantColor(fabColor);

                collapsingToolbar.setBackgroundColor(backgroundColor);

                collapsingToolbar.setStatusBarScrimColor(statusBarColor);
                collapsingToolbar.setContentScrimColor(backgroundColor);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(statusBarColor);
                }

                shareFab.setBackgroundTintList(ColorStateList.valueOf(fabColor));
            }
        });
    }

    @Override
    public void onEnterAnimationComplete() {
        metaBar.animate().alpha(1).setStartDelay(250).start();
        showShareFab();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        shareFab.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, articleInfo.getId());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        onEnterAnimationComplete();

        String[] paragraphs = new String[]{ "N/A" };

        mCursor = cursor;
        if (mCursor != null && mCursor.moveToFirst()) {
            paragraphs = mCursor.getString(ArticleLoader.Query.BODY).split("(\r\n\r\n)|(\n\n)");
        } else {
            if (mCursor != null) {
                mCursor.close();
            }

            mCursor = null;
        }

        if (recyclerView.getAdapter() != null) {
            ArticleParagraphAdapter adapter = (ArticleParagraphAdapter) recyclerView.getAdapter();
            adapter.setParagraphs(paragraphs);
        } else {
            recyclerView.setAdapter(new ArticleParagraphAdapter(this, paragraphs));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            setUpButton();
        }
    }

    private void setUpButton() {
        upFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appbar.setExpanded(true);
                recyclerView.smoothScrollToPosition(0);
            }
        });

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                    upFab.animate()
                            .alpha(1)
                            .translationY(0)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();

                } else {
                    float translationY = getResources().getDimension(R.dimen.up_fab_translationY);

                    upFab.animate()
                            .alpha(0)
                            .translationY(translationY)
                            .setInterpolator(new AccelerateInterpolator())
                            .start();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mCursor = null;
    }
}
