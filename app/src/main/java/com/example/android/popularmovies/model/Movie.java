package com.example.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Baraa Hesham on 2/23/2018.
 */

public class Movie implements Parcelable {

    private String id;
    private String originalTitle;
    private String posterPath;
    private String overview;
    private float rating;
    private String releaseDate;
    private String title;
    private String originalLanguage;
    private ArrayList<String> genre;
    private String trailerPath;
    private String movieReview;
    private String mAuthor;
    public Movie(){

    }
    public Movie(ArrayList<String> genre) {
        this.genre=genre;
    }

    public Movie(String id, String originalTitle, String posterPath, String overview, float rating,
    String releaseDate, String title, String originalLanguage,String movieReview,String mAuthor){
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.title = title;
        this.originalLanguage = originalLanguage;
        this.movieReview=movieReview;
        this.mAuthor=mAuthor;
    }

    // Main constructor
    public Movie(String id, String originalTitle, String posterPath, String overview, float rating,
                 String releaseDate, String title, String originalLanguage) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.title = title;
        this.originalLanguage = originalLanguage;
    }

    protected Movie(Parcel in) {
        id = in.readString();
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        rating = in.readFloat();
        releaseDate = in.readString();
        title = in.readString();
        originalLanguage = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };


    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }



    public void setMovieReview(String movieReview) {
        this.movieReview = movieReview;
    }

    public void setTrailerPath(String trailerPath) {
        this.trailerPath = trailerPath;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setGenre(ArrayList<String> genre) {
        this.genre = genre;
    }

    //Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public float getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getMovieReview() {
        return movieReview;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getTrailerPath() {
        return trailerPath;
    }

    public ArrayList<String> getGenre() {
        return genre;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(originalTitle);
        parcel.writeString(posterPath);
        parcel.writeString(overview);
        parcel.writeFloat(rating);
        parcel.writeString(releaseDate);
        parcel.writeString(title);
        parcel.writeString(originalLanguage);
    }
}