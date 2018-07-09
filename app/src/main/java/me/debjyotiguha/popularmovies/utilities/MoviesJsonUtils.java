package me.debjyotiguha.popularmovies.utilities;

import android.util.Log;

import me.debjyotiguha.popularmovies.data.Movie;
import me.debjyotiguha.popularmovies.data.Review;
import me.debjyotiguha.popularmovies.data.Trailler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class MoviesJsonUtils {

    private static final String TAG = MoviesJsonUtils.class.getName();

    /**
     * This method parses JSON from a web response and returns an array of Movie objects
     */
    public static Movie[] getMoviesFromJson(String moviesJsonStr)
            throws JSONException {

        JSONObject moviesJson = new JSONObject(moviesJsonStr);//create a JSON Object
        JSONArray resultsArray = moviesJson.getJSONArray("results");
        Movie[] movies = new Movie[resultsArray.length()];
        for (int i = 0; i < resultsArray.length(); i++) {

            JSONObject movieJson = resultsArray.getJSONObject(i);
            movies[i] = new Movie(
                    movieJson.getLong("id"),
                    movieJson.getString("title"),
                    movieJson.getString("poster_path"),
                    movieJson.getString("overview"),
                    movieJson.getDouble("vote_average"),
                    movieJson.getString("release_date"));

            Log.d(TAG, movies[i].toString());
        }

        return movies;
    }

    /**
     * This method parses JSON from a web response and returns an array of Trailler objects
     */
    public static Trailler[] getTraillersFromJson(String traillersJsonStr)
            throws JSONException {

        JSONObject traillersJson = new JSONObject(traillersJsonStr);//create a JSON Object
        JSONArray resultsArray = traillersJson.getJSONArray("results");
        Trailler[] traillers = new Trailler[resultsArray.length()];
        for (int i = 0; i < resultsArray.length(); i++) {

            JSONObject traillerJson = resultsArray.getJSONObject(i);
            traillers[i] = new Trailler(
                    traillerJson.getString("key"),
                    traillerJson.getString("name"));

            Log.d(TAG, traillers[i].toString());
        }

        return traillers;
    }

    /**
     * This method parses JSON from a web response and returns an array of Review objects
     */
    public static Review[] getReviewsFromJson(String reviewsJsonStr)
            throws JSONException {

        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);//create a JSON Object
        JSONArray resultsArray = reviewsJson.getJSONArray("results");
        Review[] reviews = new Review[resultsArray.length()];
        for (int i = 0; i < resultsArray.length(); i++) {

            JSONObject reviewJson = resultsArray.getJSONObject(i);
            reviews[i] = new Review(
                    reviewJson.getString("author"),
                    reviewJson.getString("content"),
                    reviewJson.getString("url"));

            Log.d(TAG, reviews[i].toString());
        }

        return reviews;
    }


}
