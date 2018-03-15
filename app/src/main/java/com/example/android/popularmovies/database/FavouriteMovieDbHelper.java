package com.example.android.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Baraa Hesham on 3/10/2018.
 */

public class FavouriteMovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favouriteMovies.db";
    private static final int DATABASE_VERSION = 1;

    public FavouriteMovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);


    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVOURITE_MOVIES_TABLE = "CREATE TABLE " + FavouriteMoviesContract.FavouriteMovie.TABLENAME + " ("+
        FavouriteMoviesContract.FavouriteMovie.MOVIE_ID + " INTEGER PRIMARY KEY, " +
                FavouriteMoviesContract.FavouriteMovie.ORIGINAL_TITLE + " TEXT, " +
                FavouriteMoviesContract.FavouriteMovie.POSTER_PATH + " TEXT, " +
                FavouriteMoviesContract.FavouriteMovie.OVERVIEW + " TEXT, " +
                FavouriteMoviesContract.FavouriteMovie.RATING + " REAL, " +
                FavouriteMoviesContract.FavouriteMovie.RELEASE_DATE+" DATE, "+
                FavouriteMoviesContract.FavouriteMovie.TITLE+" TEXT, "+
                FavouriteMoviesContract.FavouriteMovie.ORIGINAL_LANGUAGE+" TEXT, "+
                FavouriteMoviesContract.FavouriteMovie.GENRE+" TEXT, "+
                FavouriteMoviesContract.FavouriteMovie.TRAILER_PATH+" TEXT, "+
                FavouriteMoviesContract.FavouriteMovie.REVIEW_AUTHORS+" TEXT, "+
                FavouriteMoviesContract.FavouriteMovie.REVIEW_RESPONSE+" INTEGER, "+
                FavouriteMoviesContract.FavouriteMovie.TIME_STAMP+" TIMESTAMP DEFAULT CURRENT_TIMESTAMP "+
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITE_MOVIES_TABLE);





    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ FavouriteMoviesContract.FavouriteMovie.TABLENAME);
        onCreate(sqLiteDatabase);
    }
}
