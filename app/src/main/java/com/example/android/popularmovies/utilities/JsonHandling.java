package com.example.android.popularmovies.utilities;

import android.content.Context;
import android.widget.Toast;

import com.example.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Baraa Hesham on 2/24/2018, parsing Json.
 */

public class JsonHandling {

    public static final String Movie_ID_KEY = "id";
    public static final String ORIGINAL_TITLE_KEY = "original_title";
    public static final String POSTER_PATH_KEY = "poster_path";
    public static final String OVERVIEW_KEY = "overview";
    public static final String RELEASE_DATE_KEY = "release_date";
    public static final String RATING_KEY = "vote_average";
    public static final String TITLE_KEY = "title";
    public static final String ORIGINAL_LANGUAGE_KEY = "original_language";
    public static final String GENRE_ID = "genre_ids";
    public static ArrayList<String> genreList;

    public static Movie movieJsonHandling(JSONObject jsonMovie) throws JSONException {

        String id = jsonMovie.getString(Movie_ID_KEY);
        String originalTitle = jsonMovie.getString(ORIGINAL_TITLE_KEY);
        String posterPath = jsonMovie.getString(POSTER_PATH_KEY);
        String overview = jsonMovie.getString(OVERVIEW_KEY);
        String releaseDate = jsonMovie.getString(RELEASE_DATE_KEY);
        float rating = Float.valueOf(jsonMovie.getString(RATING_KEY));
        String title = jsonMovie.getString(TITLE_KEY);
        String originalLanguage = jsonMovie.getString(ORIGINAL_LANGUAGE_KEY);



        return new Movie(id, originalTitle, posterPath, overview, rating, releaseDate, title,
                originalLanguage);
    }
    public static Movie movieWithGenre  (JSONObject movieJson) throws JSONException {
        JSONArray genreArray = movieJson.getJSONArray(GENRE_ID);

        genreList = new ArrayList<>();

        for (int i = 0; i < genreArray.length(); i++) {
            int genre = genreArray.getInt(i);
            String genreString = null;
            switch (genre) {
                case 28:
                    genreString = "Action";
                    break;
                case 12:
                    genreString = "Adventure";
                    break;
                case 16:
                    genreString = "Animation";
                    break;
                case 35:
                    genreString = "Comedy";
                    break;
                case 80:
                    genreString = "Crime";
                    break;
                case 99:
                    genreString = "Documentary";
                    break;
                case 18:
                    genreString = "Drama";
                    break;
                case 10751:
                    genreString = "Family";
                    break;
                case 14:
                    genreString = "Fantasy";
                    break;
                case 36:
                    genreString = "History";
                    break;
                case 27:
                    genreString = "Horror";
                    break;
                case 10402:
                    genreString = "Music";
                    break;
                case 9648:
                    genreString = "Mystery";
                    break;
                case 10749:
                    genreString = "Romance";
                    break;
                case 878:
                    genreString = "Science Fiction";
                    break;
                case 10770:
                    genreString = "TV Movie";
                    break;
                case 53:
                    genreString = "Thriller";
                    break;
                case 10752:
                    genreString = "War";
                    break;
                case 37:
                    genreString = "Western";
                    break;
            }
            genreList.add(genreString);
        }

        return new Movie(genreList);
    }

}
