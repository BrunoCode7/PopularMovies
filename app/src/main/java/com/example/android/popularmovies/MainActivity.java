package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.popularmovies.database.FavouriteMovieDbHelper;
import com.example.android.popularmovies.database.FavouriteMoviesContract;
import com.example.android.popularmovies.databinding.ActivityMainBinding;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.utilities.MovieAdapter;
import com.example.android.popularmovies.utilities.MovieCursorAdapter;
import com.example.android.popularmovies.utilities.MySingleton;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Scanner;

import static com.example.android.popularmovies.DetailsActivity.reviewJson;
import static com.example.android.popularmovies.DetailsActivity.trailerJson;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.CONTENT_URI;
import static com.example.android.popularmovies.database.FavouriteMoviesContract.FavouriteMovie.TIME_STAMP;
import static com.example.android.popularmovies.utilities.JsonHandling.movieJsonHandling;
import static com.example.android.popularmovies.utilities.JsonHandling.movieWithGenre;

public class MainActivity extends AppCompatActivity implements MovieCursorAdapter.favouriteClickListener, MovieAdapter.ItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{
    private static final String API_KEY = BuildConfig.API_KEY;
    private final String URL_POPULAR_DATA =
            "https://api.themoviedb.org/3/movie/popular?api_key="+API_KEY;
    private final String URL_TOP_RATED_DATA =
            "https://api.themoviedb.org/3/movie/top_rated?api_key="+API_KEY;
    private final String URL_FAVOURITE_MOVIES="favourit movies";
    private final String KEY_STATE_RV_POSITION="rv_position";
    private final String JOP_TAG = "jop";
    private String PREFERRED_URL;
    static JsonObjectRequest objectRequest;
    JSONArray moviesJsonArray;
    private static SharedPreferences userPreferences;
    private ArrayList<Movie> moviesList;
    private ArrayList<Movie> moviesWithGenre;
    MovieAdapter movieAdapter;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    Context context = this;
    ActivityMainBinding mainBinding;
    private FirebaseJobDispatcher dispatcher;
    private Job job;
    SQLiteDatabase database;
    Cursor mCursor;
    private Movie chosenMovie;
    private Movie chosenMovieWithGenre;
    public final static int MOVIES_LOADER_ID=0;
    MovieCursorAdapter movieCursorAdapter;
    RecyclerView.LayoutManager mLayoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        movieCursorAdapter = new MovieCursorAdapter(context,this);
        mLayoutManager=new GridLayoutManager(getApplicationContext(), getSpanCount());
        mainBinding.moviesRv.setLayoutManager(mLayoutManager);
        getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, this);


        // DataBase code
        FavouriteMovieDbHelper dbHelper = new FavouriteMovieDbHelper(this);
        database = dbHelper.getWritableDatabase();


        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        job = dispatcher.newJobBuilder().setService(MyJobService.class).setTag(JOP_TAG).setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(Constraint.ON_ANY_NETWORK).setReplaceCurrent(false)
                .setTrigger(Trigger.executionWindow(0, 3))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).build();

        // sharedPreference is used to save the user choice the last time he/she used the app

        userPreferences = getPreferences(MODE_PRIVATE);
        PREFERRED_URL = userPreferences.getString(getString(R.string.user_sorting_Preference_key), URL_POPULAR_DATA);
        if(PREFERRED_URL.equals(URL_FAVOURITE_MOVIES)){
            mainBinding.moviesRv.setAdapter(movieCursorAdapter);
        }else {
        fetchData();}


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                PREFERRED_URL = userPreferences.getString(getString(R.string.user_sorting_Preference_key), URL_POPULAR_DATA);
                if(PREFERRED_URL.equals(URL_FAVOURITE_MOVIES)){
                    mainBinding.moviesRv.setVisibility(View.VISIBLE);
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, MainActivity.this);
                    mainBinding.moviesRv.setAdapter(movieCursorAdapter);
                }else {
                    fetchData();}
            }
        };
        userPreferences.registerOnSharedPreferenceChangeListener(listener);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_1, menu);
        switch (PREFERRED_URL) {

            case URL_POPULAR_DATA:
                menu.findItem(R.id.popular).setChecked(true);
                mainBinding.viewTitleTv.setText(R.string.pup);
                break;
            case URL_TOP_RATED_DATA:
                menu.findItem(R.id.top_rating).setChecked(true);
                mainBinding.viewTitleTv.setText(R.string.top);
                break;
            case URL_FAVOURITE_MOVIES:
                menu.findItem(R.id.favourite).setChecked(true);
                mainBinding.viewTitleTv.setText(R.string.fav);
                break;
        }
        return true;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
             if(PREFERRED_URL.equals(URL_FAVOURITE_MOVIES)){
                getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
                mainBinding.moviesRv.setAdapter(movieCursorAdapter);
            }else if (mainBinding.moviesRv.getVisibility()==View.INVISIBLE){
                fetchData();}else {
            }
        }


    @Override
    protected void onStop() {
        super.onStop();
        if (dispatcher != null) {
            dispatcher.cancel(JOP_TAG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the sharedPreference Listener
        userPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }


    //Helper Method to fetch the Json data (Volley Library)
    public void fetchData() {

        objectRequest = new JsonObjectRequest(Request.Method.GET, PREFERRED_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mainBinding.moviesRv.setVisibility(View.VISIBLE);
                        try {
                            moviesJsonArray = response.getJSONArray("results");
                            moviesList = new ArrayList<>();
                            moviesWithGenre = new ArrayList<>();
                            for (int i = 0; i < moviesJsonArray.length(); i++) {
                                JSONObject jsonMovieObject = moviesJsonArray.getJSONObject(i);
                                Movie movie = movieJsonHandling(jsonMovieObject);
                                Movie movie1 = movieWithGenre(jsonMovieObject);
                                moviesList.add(movie);
                                moviesWithGenre.add(movie1);
                            }


                            movieAdapter = new MovieAdapter(context, moviesList, MainActivity.this);
                            mainBinding.moviesRv.setAdapter(movieAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainBinding.moviesRv.setVisibility(View.INVISIBLE);

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
        //cancel caching (to get realtime Data)
        objectRequest.setShouldCache(false);
        MySingleton.getInstance(getApplicationContext()).addToRequestQue(objectRequest);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //FirebaseJobDispatcher used to detect change in connectivity and react to it.

        if ((!isOnline()) && (PREFERRED_URL.equals(URL_POPULAR_DATA)||PREFERRED_URL.equals(URL_TOP_RATED_DATA))) {
            mainBinding.moviesRv.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "No Internet Connection!!", Toast.LENGTH_SHORT).show();
            dispatcher.schedule(job);

    }}

    @Override
    public void onItemClickListener(int clickedItemIndex, ImageView imageView) {
        chosenMovie = moviesList.get(clickedItemIndex);
        chosenMovieWithGenre = moviesWithGenre.get(clickedItemIndex);
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("movie", chosenMovie);
        intent.putStringArrayListExtra("genres", chosenMovieWithGenre.getGenre());

        // ImageView Transition
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                imageView, "123");
        startActivity(intent, optionsCompat.toBundle());}



    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new  AsyncTaskLoader<Cursor>(this) {
            Cursor moviesData=null;

            @Override
            protected void onStartLoading() {
                if (moviesData!=null){
                    deliverResult(moviesData);
                }else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(CONTENT_URI,null
                    ,null,null,TIME_STAMP);
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
    }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                moviesData=data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        movieCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            movieCursorAdapter.swapCursor(null);
    }


    @Override
    public void onListItemClick(int movieId, ImageView sharedImageView) {

        mCursor=getContentResolver().query(CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build(),null,null,null,null);
        if (mCursor!= null && mCursor.moveToFirst()) {
                int idIndex = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.MOVIE_ID);
                int originalTitle = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.ORIGINAL_TITLE);
                int posterPath = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.POSTER_PATH);
                int overview = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.OVERVIEW);
                int rating = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.RATING);
                int releaseDate = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.RELEASE_DATE);
                int title = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.TITLE);
                int originalLanguage = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.ORIGINAL_LANGUAGE);
                int genre = mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.GENRE);
                int mId = mCursor.getInt(idIndex);
                int Authors=mCursor.getColumnIndex(FavouriteMoviesContract.FavouriteMovie.REVIEW_AUTHORS);
                String mAuthors= mCursor.getString(Authors);
                String mOriginalTitle = mCursor.getString(originalTitle);
                String mPosterPath = mCursor.getString(posterPath);
                String mOverview = mCursor.getString(overview);
                float mRating = mCursor.getFloat(rating);
                String mDate = mCursor.getString(releaseDate);
                String mTitle = mCursor.getString(title);
                String mOriginalLanguage = mCursor.getString(originalLanguage);
                String mGenre = mCursor.getString(genre);
                Scanner s = new Scanner(mGenre).useDelimiter(",");
                ArrayList<String> moGenre = new ArrayList<>();
                while (s.hasNext()) {
                    moGenre.add(s.next());
                }


                Movie favouriteMovie = new Movie(String.valueOf(mId), mOriginalTitle, mPosterPath, mOverview, mRating, mDate, mTitle
                        , mOriginalLanguage);

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("movie", favouriteMovie);
                intent.putStringArrayListExtra("genres", moGenre);

                // ImageView Transition
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        sharedImageView, "123");
                startActivity(intent, optionsCompat.toBundle());
            }mCursor.close();
    }




    static public class MyJobService extends JobService {


        @Override
        public boolean onStartJob(JobParameters job) {
            if (!userPreferences.getString(getString(R.string.user_sorting_Preference_key),"").equals("favourit movies")){
            if (objectRequest != null) {
                Toast.makeText(getApplicationContext(), "Loading Data ....", Toast.LENGTH_LONG).show();
                objectRequest.setShouldCache(false);
                MySingleton.getInstance(getApplicationContext()).addToRequestQue(objectRequest);
                jobFinished(job, false);

            }
            if (trailerJson != null) {
                Toast.makeText(getApplicationContext(), "Loading Data ....", Toast.LENGTH_LONG).show();
                trailerJson.setShouldCache(false);
                MySingleton.getInstance(getApplicationContext()).addToRequestQue(trailerJson);
                jobFinished(job, false);
            }
            if (reviewJson != null) {
                reviewJson.setShouldCache(false);
                MySingleton.getInstance(getApplicationContext()).addToRequestQue(reviewJson);
                jobFinished(job, false);
            }}else {
                jobFinished(job, false);
            }

            return false; // Answers the question: "Is there still work going on?"
        }

        @Override
        public boolean onStopJob(JobParameters job) {
            Toast.makeText(getApplicationContext(), "Connection Lost!", Toast.LENGTH_SHORT).show();
            return false; // Answers the question: "Should this job be retried?"
        }
    }

    public boolean isOnline() {
       ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);

        SharedPreferences.Editor editor = userPreferences.edit();

        switch (item.getItemId()) {
            case R.id.popular:
                editor.clear();
                editor.putString(getString(R.string.user_sorting_Preference_key), URL_POPULAR_DATA);
                editor.apply();
                mainBinding.viewTitleTv.setText(R.string.pup);
                return true;
            case R.id.top_rating:
                editor.clear();
                editor.putString(getString(R.string.user_sorting_Preference_key), URL_TOP_RATED_DATA);
                editor.apply();
                mainBinding.viewTitleTv.setText(R.string.top);
                return true;
            case R.id.favourite:
                editor.clear();
                editor.putString(getString(R.string.user_sorting_Preference_key), URL_FAVOURITE_MOVIES);
                editor.apply();
                mainBinding.viewTitleTv.setText(R.string.fav);
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }

private int getSpanCount(){

    int orientation = MainActivity.this.getResources().getConfiguration().orientation;
    int spanCount;

    //if device in Portrait
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        //view 2 posters
        spanCount = 2;
    } else {
        //else view 4 posters
        spanCount = 4;
    }
    return spanCount;
}



}


