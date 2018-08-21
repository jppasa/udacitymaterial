package com.example.xyzreader.ui;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.view.MenuItem;
import android.view.View;
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
    private ArticleInfo articleInfo;

    @BindView(R.id.appbar) AppBarLayout appbar;
    @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.meta_bar) LinearLayout metaBar;
    @BindView(R.id.imgThumbnail) ImageView imgThumbnail;
    @BindView(R.id.txtArticleTitle) TextView txtArticleTitle;
    @BindView(R.id.txtArticleByline) TextView txtArticleByline;
    @BindView(R.id.share_fab) FloatingActionButton shareFab;
    @BindView(R.id.up_fab) FloatingActionButton upFab;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private Cursor mCursor;
    private int scrimHeightTrigger;

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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
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
        showShareFab();

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
