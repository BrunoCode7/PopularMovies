package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.popularmovies.database.FavouriteMoviesContract;
import com.example.android.popularmovies.databinding.ActivityDetailsBinding;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.utilities.GlideApp;
import com.example.android.popularmovies.utilities.MySingleton;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.example.android.popularmovies.MainActivity.MOVIES_LOADER_ID;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.CONTENT_URI;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.MOVIE_ID;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.ORIGINAL_TITLE;


public class DetailsActivity extends AppCompatActivity {
    ActivityDetailsBinding detailsBinding;
    private String movieId;
    ArrayList<String> genre;
    private static final String API_KEY = BuildConfig.API_KEY;
    private String movieReviewURL;
    private String movieVideoURL;
    private String author;
    private String review;
    private FirebaseJobDispatcher dispatcher;
    private Job job;
    Context context = this;
    private String JOP_TAG_DETAILS = "jopDetails";
    private Uri.Builder trailerBuilder;
    Intent intenttrailer;
    static JsonObjectRequest reviewJson;
    static JsonObjectRequest trailerJson;
    Movie movie;
    Uri uri;
    private ArrayList<String> authors = new ArrayList<>();
    private ArrayList<String> Review = new ArrayList<>();
    private Cursor dCursor;

    JSONArray reviewJsonArray;
    int totalResponse;
    String trailerKey;
//    private boolean isFavourite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);


        ////////////////////////////////////////////

        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        job = dispatcher.newJobBuilder().setService(MainActivity.MyJobService.class).setTag(JOP_TAG_DETAILS).setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(Constraint.ON_ANY_NETWORK).setReplaceCurrent(false)
                .setTrigger(Trigger.executionWindow(0, 3))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).build();
        Intent intent = getIntent();
        movie = intent.getParcelableExtra("movie");

        genre = intent.getStringArrayListExtra("genres");
        GlideApp.with(this).load("http://image.tmdb.org/t/p/w342/" + movie.getPosterPath())
                .error(R.drawable.error).into(detailsBinding.detailsPoster);

        detailsBinding.detailsRating.setText(String.valueOf(movie.getRating()));
        detailsBinding.detailsOverview.setText("Genre: " + genre.toString().replace("[", "").replace("]", "") + "." + "\n" + "Release Date: " + movie.getReleaseDate() + "\n" + "Original Language: " + movie.getOriginalLanguage() + "\n" +
                "\n" + "Overview:" + "\n" + movie.getOverview());

        if (movie.getOriginalTitle().equals(movie.getTitle())) {
            detailsBinding.detailsOriginalTitle.setText(movie.getOriginalTitle());
        } else {
            detailsBinding.detailsOriginalTitle.setText(movie.getOriginalTitle() + "\n" + movie.getTitle());

        }
        movieId = movie.getId();


        trailerBuilder = new Uri.Builder();
        trailerBuilder.scheme("https").authority("api.themoviedb.org").appendPath("3")
                .appendPath("movie")
                .appendPath(movieId)
                .appendPath("videos").appendQueryParameter("api_key", API_KEY);

        if (isOnline()) {
            fetchReview();
            fetchTrailer();

        } else {

            dCursor = getContentResolver().query(CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build(), null, null, null, null);
            if (dCursor != null && dCursor.moveToFirst()) {
//                isFavourite = true;
                int trailerPath = dCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.TRAILER_PATH);
                int reviewAuthors = dCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.REVIEW_AUTHORS);
                int tResponse = dCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.REVIEW_RESPONSE);

                int reviewRespons = dCursor.getInt(tResponse);
                int orientation = DetailsActivity.this.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    try {
                        detailsBinding.full.setVisibility(View.VISIBLE);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);

                    }
                } else {
                    detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);
                }

                String trailerKey = dCursor.getString(trailerPath);
                String trailerURL = "https://www.youtube.com/watch?v=" + trailerKey;
                String thumbnailUrl = "https://img.youtube.com/vi/" + trailerKey + "/sddefault.jpg";
                Uri trailerUri = Uri.parse(trailerURL);


                GlideApp.with(getApplicationContext()).load(thumbnailUrl)
                        .into(detailsBinding.trailerImage);
                intenttrailer = new Intent(Intent.ACTION_VIEW, trailerUri);
                String title = getResources().getString(R.string.trailer_intent);
                final Intent chooser = Intent.createChooser(intenttrailer, title);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intenttrailer, 0);
                boolean isIntentSafe = activities.size() > 0;
                detailsBinding.imageButton.setVisibility(View.VISIBLE);
                if (isIntentSafe) {
                    detailsBinding.imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(chooser);
                        }
                    });

                }

                String JAO = dCursor.getString(reviewAuthors);
                try {
                    JSONArray usersReview = new JSONArray(JAO);
                    for (int i = 0; i < usersReview.length(); i++) {
                        JSONObject usersReviewJSONObject = usersReview.getJSONObject(i);
                        String fAuthor = usersReviewJSONObject.getString("author");
                        String fReview = usersReviewJSONObject.getString("content");
                        detailsBinding.revo.append("Review Number (" + (i + 1) + ") \n" + "Author: " + fAuthor + "\n\n" +
                                fReview + "\n\n-------------------\n\n");
                    }
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        try {
                            detailsBinding.full.setVisibility(View.VISIBLE);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);

                        }
                        if (reviewRespons != 0) {
                            try {
                                detailsBinding.revoScroll.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                detailsBinding.revo.setVisibility(View.VISIBLE);

                            }

                        } else {
                            try {
                                detailsBinding.revoScroll.setVisibility(View.GONE);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                detailsBinding.revo.setVisibility(View.GONE);

                            }


                        }

                    } else {
                        try {
                            detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            detailsBinding.full.setVisibility(View.VISIBLE);

                        }
                    }
                    if (reviewRespons != 0) {
                        try {
                            detailsBinding.revoScroll.setVisibility(View.VISIBLE);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            detailsBinding.revo.setVisibility(View.VISIBLE);

                        }
                    } else {
                        try {
                            detailsBinding.revoScroll.setVisibility(View.GONE);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            detailsBinding.revo.setVisibility(View.GONE);

                        }
                        ;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "exception", Toast.LENGTH_LONG).show();

                }


            } else {
//                isFavourite = false;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dispatcher != null) {
            dispatcher.cancel(JOP_TAG_DETAILS);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //FirebaseJobDispatcher used to detect change in connectivity and react to it.
        if (!isOnline()) {
            dispatcher.schedule(job);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.add_remove_favourite:


                dCursor = getContentResolver().query(CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build(), null, null, null, null);
                if (dCursor != null && dCursor.moveToFirst()) {

                    getContentResolver().delete(CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build(), null, null);
                    item.setIcon(R.drawable.notfavourit);
                    Toast.makeText(getBaseContext(), "Movie Removed From Favourites List", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    uri = getContentResolver().insert(CONTENT_URI, addFavouriteMovie(movie, genre));
                    if (uri != null) {
                        Toast.makeText(getBaseContext(), "Movie Added to Favourites List", Toast.LENGTH_SHORT).show();
                        item.setIcon(R.drawable.favourit);
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_2, menu);
        dCursor = getContentResolver().query(CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build(), null, null, null, null);
        if (dCursor != null && dCursor.moveToFirst()) {
            menu.getItem(0).setIcon(R.drawable.favourit);
        } else {
            menu.getItem(0).setIcon(R.drawable.notfavourit);
        }

        return true;
    }


    private void fetchReview() {
        Cursor asCursor = getContentResolver().query(CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build(), null, null, null, null);
//        if (asCursor != null) {
//            isFavourite = true;
//            asCursor.close();
//        } else {
//            isFavourite = false;
//        }
        Uri.Builder reviewBuilder = new Uri.Builder();
        reviewBuilder.scheme("https").authority("api.themoviedb.org").appendPath("3")
                .appendPath("movie")
                .appendPath(movieId)
                .appendPath("reviews").appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("language", "en-US")
                .appendQueryParameter("page", "1");
        movieReviewURL = reviewBuilder.build().toString();

        reviewJson = new JsonObjectRequest(Request.Method.GET, movieReviewURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            totalResponse = response.getInt("total_results");
                            reviewJsonArray = response.getJSONArray("results");
                            detailsBinding.revo.setText("Movie Reviews List \n\n");
                            for (int i = 0; i < reviewJsonArray.length(); i++) {
                                JSONObject reviewJson = reviewJsonArray.getJSONObject(i);
                                author = reviewJson.getString("author");
                                review = reviewJson.getString("content");
                                authors.add(author);
                                Review.add(review);
                                detailsBinding.revo.append("Review Number (" + (i + 1) + ") \n" + "Author: " + author + "\n\n" +
                                        review + "\n\n-------------------\n\n");
                            }
                            int orientation = DetailsActivity.this.getResources().getConfiguration().orientation;
                            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                                try {
                                    detailsBinding.full.setVisibility(View.VISIBLE);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);

                                }
                                if (totalResponse != 0) {
                                    try {
                                        detailsBinding.revoScroll.setVisibility(View.VISIBLE);
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                        detailsBinding.revo.setVisibility(View.VISIBLE);

                                    }

                                } else {
                                    try {
                                        detailsBinding.revoScroll.setVisibility(View.GONE);
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                        detailsBinding.revo.setVisibility(View.GONE);

                                    }


                                }

                            } else {
                                try {
                                    detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    detailsBinding.full.setVisibility(View.VISIBLE);

                                }
                            }
                            if (totalResponse != 0) {
                                try {
                                    detailsBinding.revoScroll.setVisibility(View.VISIBLE);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    detailsBinding.revo.setVisibility(View.VISIBLE);

                                }
                            } else {
                                try {
                                    detailsBinding.revoScroll.setVisibility(View.GONE);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    detailsBinding.revo.setVisibility(View.GONE);

                                }
                                ;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int orientation = DetailsActivity.this.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    try {
                        detailsBinding.full.setVisibility(View.INVISIBLE);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        detailsBinding.fullDisplayLinear.setVisibility(View.INVISIBLE);
                    }
                } else {
                    detailsBinding.fullDisplayLinear.setVisibility(View.INVISIBLE);
                }

                // error response from server
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    Toast.makeText(getApplicationContext(), "Error Code =" + networkResponse
                            .statusCode, Toast.LENGTH_SHORT).show();

                } else {
                    // can't connect to the server(internet connection error)
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    dispatcher.schedule(job);
                }
            }
        });
        reviewJson.setShouldCache(false);
        MySingleton.getInstance(getApplicationContext()).addToRequestQue(reviewJson);

    }

    private void fetchTrailer() {
        movieVideoURL = trailerBuilder.build().toString();

        trailerJson = new JsonObjectRequest
                (Request.Method.GET, movieVideoURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int orientation = DetailsActivity.this.getResources().getConfiguration().orientation;
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            try {
                                detailsBinding.full.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);

                            }
                        } else {
                            detailsBinding.fullDisplayLinear.setVisibility(View.VISIBLE);
                        }


                        try {
                            JSONArray trailerJsonArray = response.getJSONArray("results");
                            JSONObject trailerJsonObject = trailerJsonArray.getJSONObject(0);
                            trailerKey = trailerJsonObject.getString("key");
                            String trailerURL = "https://www.youtube.com/watch?v=" + trailerKey;
                            String thumbnailUrl = "https://img.youtube.com/vi/" + trailerKey + "/sddefault.jpg";
                            Uri trailerUri = Uri.parse(trailerURL);


                            GlideApp.with(getApplicationContext()).load(thumbnailUrl)
                                    .into(detailsBinding.trailerImage);
                            intenttrailer = new Intent(Intent.ACTION_VIEW, trailerUri);
                            String title = getResources().getString(R.string.trailer_intent);
                            final Intent chooser = Intent.createChooser(intenttrailer, title);
                            PackageManager packageManager = getPackageManager();
                            List<ResolveInfo> activities = packageManager.queryIntentActivities(intenttrailer, 0);
                            boolean isIntentSafe = activities.size() > 0;
                            detailsBinding.imageButton.setVisibility(View.VISIBLE);
                            if (isIntentSafe) {
                                detailsBinding.imageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startActivity(chooser);
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int orientation = DetailsActivity.this.getResources().getConfiguration().orientation;
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            try {
                                detailsBinding.full.setVisibility(View.INVISIBLE);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                detailsBinding.fullDisplayLinear.setVisibility(View.INVISIBLE);
                            }

                        } else {

                            detailsBinding.fullDisplayLinear.setVisibility(View.INVISIBLE);
                        }

                        // error response from server
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            Toast.makeText(getApplicationContext(), "Error Code =" + networkResponse
                                    .statusCode, Toast.LENGTH_SHORT).show();

                        } else {
                            // can't connect to the server(internet connection error)
                            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                            dispatcher.schedule(job);
                        }
                    }
                });
        trailerJson.setShouldCache(false);
        MySingleton.getInstance(getApplicationContext()).addToRequestQue(trailerJson);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    ContentValues addFavouriteMovie(Movie favouriteMovie, ArrayList<String> genre) {

        ContentValues cv = new ContentValues();
        cv.put(FavouriteMoviesContract.FavouriteMovie.MOVIE_ID, favouriteMovie.getId());
        cv.put(FavouriteMoviesContract.FavouriteMovie.ORIGINAL_TITLE, favouriteMovie.getOriginalTitle());
        cv.put(FavouriteMoviesContract.FavouriteMovie.POSTER_PATH, favouriteMovie.getPosterPath());
        cv.put(FavouriteMoviesContract.FavouriteMovie.OVERVIEW, favouriteMovie.getOverview());
        cv.put(FavouriteMoviesContract.FavouriteMovie.RATING, favouriteMovie.getRating());
        cv.put(FavouriteMoviesContract.FavouriteMovie.RELEASE_DATE, favouriteMovie.getReleaseDate());
        cv.put(FavouriteMoviesContract.FavouriteMovie.TITLE, favouriteMovie.getTitle());
        cv.put(FavouriteMoviesContract.FavouriteMovie.ORIGINAL_LANGUAGE, favouriteMovie.getOriginalTitle());
        cv.put(FavouriteMoviesContract.FavouriteMovie.GENRE, genre.toString());
        cv.put(FavouriteMoviesContract.FavouriteMovie.TRAILER_PATH, trailerKey);
        cv.put(FavouriteMoviesContract.FavouriteMovie.REVIEW_AUTHORS, reviewJsonArray.toString());
        cv.put(FavouriteMoviesContract.FavouriteMovie.REVIEW_RESPONSE, totalResponse);
        return cv;
    }


}



