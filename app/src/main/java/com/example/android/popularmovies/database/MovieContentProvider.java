package com.example.android.popularmovies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import static com.example.android.popularmovies.database.FavouriteMoviesContract.AUTHORITY;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.CONTENT_URI;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.TABLENAME;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.TIME_STAMP;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.PATH_AMOVIE;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.PATH_MOVIES;

/**
 * Created by Baraa Hesham on 3/10/2018.
 */

public class MovieContentProvider extends ContentProvider {
    public static final int MOVIES = 40;
    public static final int MOVIES_WITH_ID = 1;
    private static final UriMatcher mUriMatcher = buildUriMatcher();


    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_MOVIES, MOVIES);
        uriMatcher.addURI(AUTHORITY, PATH_AMOVIE , MOVIES_WITH_ID);
        return uriMatcher;
    }

    private FavouriteMovieDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new FavouriteMovieDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        int match = mUriMatcher.match(uri);
        Cursor mCursor;
        switch (match) {

            case MOVIES:
                mCursor = database.query(TABLENAME, null, null, null,
                        null, null, TIME_STAMP);
                break;

            case MOVIES_WITH_ID:

                String id=uri.getPathSegments().get(1);
                String mSelection = "id=?";
                String[] mSelectionArgs = new String[]{id};
                mCursor = database.query(TABLENAME, null, mSelection, mSelectionArgs,
                        null, null, null);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        mCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return mCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);


        Uri returnUri = null;

        switch (match) {
            case MOVIES:
                try {
                    Long id = database.insert(TABLENAME, null, contentValues);
                    if (id > 0) {
                        //success
                        returnUri = ContentUris.withAppendedId(CONTENT_URI, id);

                    } else {
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Movie is Already Favourite", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                throw new UnsupportedOperationException("unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionsArgs) {

        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        int selectedRow;
        switch (match) {

            case MOVIES:
                selectedRow = database.delete(TABLENAME, null, null);
                break;

            case MOVIES_WITH_ID:

                String id=uri.getPathSegments().get(1);
                String mSelection = "id=?";
                String[] mSelectionArgs = new String[]{id};
                selectedRow = database.delete(TABLENAME,mSelection,mSelectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (selectedRow!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return selectedRow;


    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
