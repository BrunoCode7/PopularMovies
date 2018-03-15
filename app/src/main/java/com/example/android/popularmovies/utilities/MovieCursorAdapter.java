


package com.example.android.popularmovies.utilities;

        import android.content.Context;
        import android.database.Cursor;
        import android.support.annotation.NonNull;
        import android.support.v4.view.ViewCompat;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;


        import com.example.android.popularmovies.R;
        import com.example.android.popularmovies.database.FavouriteMoviesContract;

        import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.CONTENT_URI;

/**
 * Created by Baraa Hesham on 2/25/2018, MovieAdapter.
 */

public class MovieCursorAdapter extends RecyclerView.Adapter<MovieCursorAdapter.MovieCursorViewHolder> {

    public Cursor mCursor;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private final favouriteClickListener favouritClickListener;

    public interface favouriteClickListener{
        void onListItemClick(int movieId,ImageView sharedImageView);
    }

    public MovieCursorAdapter(Context context,favouriteClickListener favouriteClickListener) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
        this.favouritClickListener=favouriteClickListener;
    }

    @NonNull
    @Override
    public MovieCursorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater
                .inflate(R.layout.rv_item_layout,null);
        MovieCursorViewHolder movieViewHolder = new MovieCursorViewHolder(view);
        return movieViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieCursorViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.MOVIE_ID);
        int originalTitle = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.ORIGINAL_TITLE);
        int posterPath= mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.POSTER_PATH);
        int overview = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.OVERVIEW);
        int rating=mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.RATING);
        int releaseDate=mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.RELEASE_DATE);
        int title = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.TITLE);
        int originalLanguage = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.ORIGINAL_LANGUAGE);
        int genre = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.GENRE);
        int timeStamp=mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.TIME_STAMP);
            mCursor.moveToPosition(position);

            String posterLinkPath=mCursor.getString(posterPath);
            final int clickId=mCursor.getInt(idIndex);
        GlideApp.with(mContext).load("http://image.tmdb.org/t/p/w342/" + posterLinkPath)
                .error(R.drawable.error).placeholder(R.drawable.loading).into(holder.poster);

        ViewCompat.setTransitionName(holder.poster,"123");
        holder.poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favouritClickListener.onListItemClick(clickId,holder.poster);

            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();    }


    class MovieCursorViewHolder extends RecyclerView.ViewHolder {
        private ImageView poster;

        public MovieCursorViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster_image);

        }


    }
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }
}
