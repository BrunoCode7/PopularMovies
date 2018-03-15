package com.example.android.popularmovies.utilities;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Movie;
import java.util.ArrayList;

/**
 * Created by Baraa Hesham on 2/25/2018, MovieAdapter.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private ArrayList<Movie> moviesList;
    private LayoutInflater layoutInflater;
    final private ItemClickListener clickListener;

    public interface ItemClickListener{
        void onItemClickListener(int clickedItemIndex,ImageView sharedImageView);
    }


    public MovieAdapter(Context context, ArrayList<Movie> moviesList,ItemClickListener listener) {
        this.context = context;
        clickListener=listener;
        this.moviesList = moviesList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.rv_item_layout, null);
        MovieViewHolder movieViewHolder = new MovieViewHolder(view);
        return movieViewHolder;
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        Movie movie = moviesList.get(position);
        GlideApp.with(context).load("http://image.tmdb.org/t/p/w342/" + movie.getPosterPath())
                .error(R.drawable.error).placeholder(R.drawable.loading).into(holder.poster);


        ViewCompat.setTransitionName(holder.poster,"123");

        holder.poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClickListener(holder.getAdapterPosition(),holder.poster);
            }
        });

    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }


    public class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView poster;

        public MovieViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster_image);
        }

    }
}
