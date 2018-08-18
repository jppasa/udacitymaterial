package com.example.xyzreader.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.models.ArticleInfo;
import com.example.xyzreader.utils.ArticleUtils;
import com.squareup.picasso.Picasso;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private ArticleClickListener mArticleClickListener;

    public ArticleAdapter(Context context, Cursor cursor, ArticleClickListener listener) {
        mContext = context;
        mCursor = cursor;
        mArticleClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_article, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ArticleInfo articleInfo = ArticleUtils.retrieveArticle(mCursor, position);

        holder.titleView.setText(articleInfo.getTitle());
        holder.subtitleView.setText(ArticleUtils.dateFrom(articleInfo.getDate(), articleInfo.getAuthor()));

        Picasso.with(mContext)
                .load(articleInfo.getThumbnailUrl())
                .into(holder.thumbnailView);

        final int finalPosition = position;
        final ImageView finalThumbnail = holder.thumbnailView;
        final TextView finalTitle = holder.titleView;
        final TextView finalSubtitle = holder.subtitleView;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mArticleClickListener != null) {
                    mArticleClickListener.onArticleClicked(
                            finalThumbnail,
                            finalTitle,
                            finalSubtitle,
                            finalPosition,
                            articleInfo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailView;
        TextView titleView;
        TextView subtitleView;

        ViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);
        }
    }

    public interface ArticleClickListener {
        void onArticleClicked(ImageView imageView,
                              TextView titleView,
                              TextView subtitleView,
                              int position,
                              ArticleInfo articleInfo);
    }
}
