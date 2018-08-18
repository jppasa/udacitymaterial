package com.example.xyzreader.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

import static com.example.xyzreader.adapters.ArticleParagraphAdapter.ViewType.BOTTOM;
import static com.example.xyzreader.adapters.ArticleParagraphAdapter.ViewType.MIDDLE;
import static com.example.xyzreader.adapters.ArticleParagraphAdapter.ViewType.TOP;

public class ArticleParagraphAdapter extends RecyclerView.Adapter<ArticleParagraphAdapter.ParagraphViewHolder> {

    private final Typeface typeface;
    private String[] paragraphs;

    public void setParagraphs(String[] paragraphs) {
        this.paragraphs = paragraphs;
        notifyDataSetChanged();
    }

    enum ViewType {
        TOP, BOTTOM, MIDDLE
    }

    public ArticleParagraphAdapter(Context context, String[] paragraphs) {
        this.paragraphs = paragraphs;

        this.typeface = Typeface.createFromAsset(context.getResources().getAssets(), "Rosario-Regular.ttf");
    }

    @NonNull
    @Override
    public ParagraphViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        int layoutRes = R.layout.article_paragraph_item;

        if (viewType == TOP.ordinal()) {
            layoutRes = R.layout.article_paragraph_item_top;
        } else if (viewType == BOTTOM.ordinal()) {
            layoutRes = R.layout.article_paragraph_item_bottom;
        }

        View view = inflater.inflate(layoutRes, parent, false);

        return new ParagraphViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParagraphViewHolder holder, int position) {
        String paragraph = paragraphs[position];

        holder.txtParagraph.setText(Html.fromHtml(paragraph));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TOP.ordinal();
        } else if (position == paragraphs.length - 1) {
            return BOTTOM.ordinal();
        } else {
            return MIDDLE.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        if (paragraphs != null) {
            return paragraphs.length;
        }
        return 0;
    }

    class ParagraphViewHolder extends RecyclerView.ViewHolder {
        TextView txtParagraph;

        ParagraphViewHolder(View itemView) {
            super(itemView);

            txtParagraph = itemView.findViewById(R.id.article_paragraph);
            txtParagraph.setTypeface(typeface);
        }
    }
}
