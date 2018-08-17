package com.example.xyzreader.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapters.ArticleAdapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.models.ArticleInfo;
import com.example.xyzreader.utils.ArticleUtils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,ArticleAdapter.ArticleClickListener {
    private static final int LOADER_ID = 0;
    private static final String TAG = ArticleListActivity.class.toString();


    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.getDefault());
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = findViewById(R.id.toolbar);


//        final View toolbarContainerView = findViewById(R.id.toolbar_container);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = findViewById(R.id.recycler_view);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.theme_accent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadArticles(true);
            }
        });

//        if (savedInstanceState == null) {
//            refresh();
//        }

        loadArticles(false);
    }

    private void loadArticles(boolean forceRefresh) {
        if (forceRefresh) {
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (mRecyclerView.getAdapter() == null) {
            ArticleAdapter adapter = new ArticleAdapter(this, cursor, this);
            adapter.setHasStableIds(true);

            mRecyclerView.setAdapter(adapter);

            int count = getResources().getInteger(R.integer.list_column_count);

            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(count, StaggeredGridLayoutManager.VERTICAL));
        } else {
            ArticleAdapter adapter = (ArticleAdapter) mRecyclerView.getAdapter();

            adapter.setCursor(cursor);
        }

//        Adapter adapter = new Adapter(cursor);
//        adapter.setHasStableIds(true);
//        mRecyclerView.setAdapter(adapter);
//        int columnCount = getResources().getInteger(R.integer.list_column_count);
//        StaggeredGridLayoutManager sglm =
//                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onArticleClicked(ImageView imageView, TextView titleView, TextView subtitleView, int position, ArticleInfo articleInfo) {
        launchDetailActivity(articleInfo, imageView, titleView, subtitleView);
    }

    private void launchDetailActivity(ArticleInfo articleInfo, ImageView thumbnailView, TextView titleView, TextView subtitleView) {

        Uri uri = ItemsContract.Items.buildItemUri(articleInfo.getId());

        String thumbnailTransition = getString(R.string.transition_thumbnail);
        String titleTransition = getString(R.string.transition_title);
        String subtitleTransition = getString(R.string.transition_subtitle);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(ArticleDetailActivity.EXTRA_ARTICLE, articleInfo);

        List<Pair<View, String>> pairs = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View navigationBar = findViewById(android.R.id.navigationBarBackground);

            if (navigationBar != null) {
                pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
            }
        }

        pairs.add(Pair.create((View) thumbnailView, thumbnailTransition));
        pairs.add(Pair.create((View) titleView, titleTransition));
        pairs.add(Pair.create((View) subtitleView, subtitleTransition));

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        pairs.toArray(new Pair[pairs.size()]));

//        ActivityOptionsCompat options =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
//                        thumbnailView,          // Starting view
//                        transitionName      // The String
//                );

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

}
