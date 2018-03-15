package com.example.android.popularmovies.database;

import android.net.Uri;
import android.webkit.WebView;

import java.net.URI;

/**
 * Created by Baraa Hesham on 3/10/2018.
 */

public class FavouriteMoviesContract {


    public static final String AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "favouriteMovies";
    public static final String PATH_AMOVIE = "favouriteMovies/#";

    public static final class FavouriteMovie {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();



        public static final String TABLENAME = "favouriteMovies";
        public static final String MOVIE_ID = "id";
        public static final String ORIGINAL_TITLE = "originalTitle";
        public static final String POSTER_PATH = "posterPath";
        public static final String OVERVIEW = "overview";
        public static final String RATING = "rating";
        public static final String RELEASE_DATE = "releaseDate";
        public static final String TITLE = "title";
        public static final String ORIGINAL_LANGUAGE = "originalLanguage";
        public static final String GENRE = "genre";
        public static final String TIME_STAMP = "timeStamp";
        public static final String TRAILER_PATH = "trailerpath";
        public static final String REVIEW_AUTHORS = "reviewAuthor";
        public static final String REVIEW_RESPONSE = "reviews";



    }
}
