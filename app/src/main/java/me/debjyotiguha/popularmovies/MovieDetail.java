package me.debjyotiguha.popularmovies;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import me.debjyotiguha.popularmovies.data.Movie;
import me.debjyotiguha.popularmovies.data.PopularMoviesContract;
import me.debjyotiguha.popularmovies.data.Review;
import me.debjyotiguha.popularmovies.data.Trailler;
import me.debjyotiguha.popularmovies.utilities.MoviesJsonUtils;
import me.debjyotiguha.popularmovies.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MovieDetail extends AppCompatActivity
        implements TraillerAdapter.GridItemClickListener, ReviewAdapter.GridItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private final String TAG = MovieDetail.class.toString();
    private final String MOVIE_ID = "movie_id"; //key for bundle in asyncTaskLoader
    private static final int MOVIES_LOADER_ID = 0;

    private Movie mMovie;

    private ProgressBar mTraillerProgressBar;
    private TextView mTraillerErrorView;
    private RecyclerView mTraillerRecyclerView;
    private TraillerAdapter mTraillerAdapter;
    private ProgressBar mReviewProgressBar;
    private TextView mReviewErrorView;
    private RecyclerView mReviewRecyclerView;
    private ReviewAdapter mReviewAdapter;
    private ImageView mStartImageView;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView mTitleTextView;
        TextView mYearTextView;
        TextView mRatingTextView;
        TextView mSynopsisTextView;
        ImageView mPosterImageView;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mTitleTextView = (TextView) findViewById(R.id.tv_movie_title);
        mYearTextView = (TextView) findViewById(R.id.tv_year);
        mRatingTextView = (TextView) findViewById(R.id.tv_rating);
        mSynopsisTextView = (TextView) findViewById(R.id.tv_synopsis);
        mPosterImageView = (ImageView) findViewById(R.id.iv_poster);
        mStartImageView = (ImageView) findViewById(R.id.iv_start);

        mTraillerProgressBar = (ProgressBar) findViewById(R.id.pb_trailler_loading_indicator);
        mTraillerErrorView = (TextView) findViewById(R.id.tv_trailler_error_display);
        mTraillerRecyclerView = (RecyclerView) findViewById(R.id.rc_traillers);

        mReviewProgressBar = (ProgressBar) findViewById(R.id.pb_review_loading_indicator);
        mReviewErrorView = (TextView) findViewById(R.id.tv_review_error_display);
        mReviewRecyclerView = (RecyclerView) findViewById(R.id.rc_reviews);

        Intent intent = getIntent();

        mMovie = intent.getParcelableExtra(Movie.class.toString());

        mTitleTextView.setText(mMovie.getTitle());
        String year = mMovie.getReleaseDate().substring(0,
                mMovie.getReleaseDate().lastIndexOf("-"));
        mYearTextView.setText(year);
        String rating = mMovie.getRating() + "/10";
        mRatingTextView.setText(rating);
        mSynopsisTextView.setText(mMovie.getSynopsis());

        String posterUrl = NetworkUtils.POSTER_URL_BASE + NetworkUtils.POSTER_SIZE_PATH_URL +
                mMovie.getPosterUrl();

        NetworkUtils.setImage(this, posterUrl, mPosterImageView);

        setRecyclerViews(); // inflates traillers and reviews lists;

        //check if this Movie is a favorite one;
        hasMovieInDb(mMovie.getId());
    }

    /**
     * Defines recycler view configuration
     */
    private void setRecyclerViews() {

        final boolean reverseLayout = false;

        //Trailler recyclerView
        LinearLayoutManager linearTraillerLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, reverseLayout);
        mTraillerRecyclerView.setLayoutManager(linearTraillerLayoutManager);
        mTraillerRecyclerView.setHasFixedSize(true);
        //add Adapter
        mTraillerAdapter = new TraillerAdapter(this);
        mTraillerRecyclerView.setAdapter(mTraillerAdapter);

        //Review recyclerView
        LinearLayoutManager linearReviewLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, reverseLayout);
        mReviewRecyclerView.setLayoutManager(linearReviewLayoutManager);
        mReviewRecyclerView.setHasFixedSize(true);
        //add Adapter
        mReviewAdapter = new ReviewAdapter(this);
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        loadTraillers(mMovie.getId());
        loadReviews(mMovie.getId());
    }

    /**
     * Handler for TraillerAdapter
     */
    public void onGridTraillerItemClick(int clickedItemIndex){
        Log.d(TAG, "click item index" + clickedItemIndex);
        Trailler trailler = mTraillerAdapter.getTrailler(clickedItemIndex);

        //start an intent which call youtube url
        Uri webpage = Uri.parse(trailler.getUrlString());
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Handler for ReviewAdapter
     */
    public void onGridReviewItemClick(int clickedItemIndex){
        Log.d(TAG, "click item index" + clickedItemIndex);
        Review review = mReviewAdapter.getReview(clickedItemIndex);

        //start an intent which call youtube url
        Uri webpage = Uri.parse(review.getUrlString());
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * retrieve trailler url information
     */
    private void loadTraillers(long movie_id){
        URL urlTraillerQuery;
        urlTraillerQuery = NetworkUtils.buildUrlWithParam(
                NetworkUtils.MOVIE_TRAILLER, movie_id);
        Log.d("trailler", urlTraillerQuery.toString());

        //execute out from main thread
        new TraillerQueryTask().execute(urlTraillerQuery);
    }

    /**
     * retrieve review url information
     */
    private void loadReviews(long movie_id){
        URL urlReviewQuery;
        urlReviewQuery = NetworkUtils.buildUrlWithParam(
                NetworkUtils.MOVIE_REVIEW, movie_id);
        Log.d("review", urlReviewQuery.toString());

        //execute out from main thread
        new ReviewQueryTask().execute(urlReviewQuery);
    }

    private void showRecyclerView(int id){
        switch (id){
            case R.id.rc_traillers:
                mTraillerRecyclerView.setVisibility(View.VISIBLE);
                break;
            case R.id.rc_reviews:
                mReviewRecyclerView.setVisibility(View.VISIBLE);
                break;
            default:
                Log.d(MovieDetail.class.toString(),"invalid recyclerView");
                break;
        }
    }

    private void showErrorView(int id){
        switch (id){
            case R.id.rc_traillers:
                mTraillerErrorView.setVisibility(View.VISIBLE);
                break;
            case R.id.rc_reviews:
                mReviewErrorView.setVisibility(View.VISIBLE);
                break;
            default:
                Log.d(MovieDetail.class.toString(),"invalid errorView");
                break;
        }
    }

    /**
      * handler for btn_favorites. Set this movie as favorite movie
      */
    public void setFavoriteMovie(View view) {
        isFavorite = !isFavorite;

        if(isFavorite) {
            mStartImageView.setImageResource(android.R.drawable.btn_star_big_on);
            boolean res = addNewMovie(mMovie);
            if(res)
                Toast.makeText(this, R.string.movie_saved, Toast.LENGTH_SHORT).show();
            else
                Log.d(TAG, "movie not saved! It wasn't already saved?"); // debug
        } else {
            mStartImageView.setImageResource(android.R.drawable.btn_star_big_off);
            removeMovie(mMovie);
        }
    }

    /**
     * Insert a movie in Data base
     * */
    private boolean addNewMovie(Movie movie){
//        movie.setIsFavorite(FAVORITE_ON);

        ContentValues cv = new ContentValues();
        cv.put(PopularMoviesContract.Movies.COLUMN_MOVIE_ID, movie.getId());
        cv.put(PopularMoviesContract.Movies.COLUMN_TITLE, movie.getTitle());
        cv.put(PopularMoviesContract.Movies.COLUMN_POSTER_URL, movie.getPosterUrl());
        cv.put(PopularMoviesContract.Movies.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        cv.put(PopularMoviesContract.Movies.COLUMN_RATING, movie.getRating());
        cv.put(PopularMoviesContract.Movies.COLUMN_SYNOPSIS, movie.getSynopsis());
//        cv.put(PopularMoviesContract.Movies.COLUMN_IS_FAVORITE, movie.getIsFavorite());

        Uri uri = getContentResolver().insert(PopularMoviesContract.Movies.CONTENT_URI, cv);
        //return mDb.insert(PopularMoviesContract.Movies.TABLE_NAME, null, cv);

        return uri != null;
    }

    /**
     * Insert a movie in Data base
     * */
    private void removeMovie(Movie movie){
//        movie.setIsFavorite(FAVORITE_OFF);
        long movie_id = movie.getId();

        String whereClause = PopularMoviesContract.Movies.COLUMN_MOVIE_ID + "=?";
        String[] whereArgs = new String[1];
        whereArgs[0] = Long.toString(movie_id);

        int res = getContentResolver().delete(
                PopularMoviesContract.Movies.CONTENT_URI,
                whereClause,whereArgs);

        if(res>0)
            Toast.makeText(this, R.string.movie_removed, Toast.LENGTH_SHORT).show();
        else
            Log.d(TAG,"error during remove movie from db!!!!!!");
    }

    /**
     * Check whether has Movie in Data base. If has, then it's a favorite Movie
     * */
    private void hasMovieInDb(long movie_id){
        Bundle args = new Bundle();
        args.putString(MOVIE_ID,String.valueOf(movie_id));
        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, args, this);
    }

    /**
     * This method implements AsyncTaskLoader for database
     * */
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        return new android.support.v4.content.AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mMoviesData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mMoviesData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mMoviesData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                try {
                    String movie_id;

                    if(args != null && args.containsKey(MOVIE_ID))
                        movie_id = args.getString(MOVIE_ID);
                    else{
                        Log.e(TAG, "no movie id was passed!");
                        return null;
                    }

                    return getContentResolver().query(
                            Uri.withAppendedPath(
                                    PopularMoviesContract.Movies.CONTENT_URI,
                                    movie_id),
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mMoviesData = data;
                super.deliverResult(data);
            }
        };
    }

    /**
     * This method implements AsyncTaskLoader for database
     * */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null){
            isFavorite = data.getCount() > 0;
            data.close();
        }

        updateFavoriteMovieStar();
    }

    /**
     * Update favorite movie star based in isFavorite tag
     * */
    private void updateFavoriteMovieStar(){
        if(isFavorite) {
            mStartImageView.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            mStartImageView.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    /**
     * This method implements AsyncTaskLoader for database
     * */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @SuppressLint("StaticFieldLeak")
    private class TraillerQueryTask extends AsyncTask<URL, Void, Trailler[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTraillerProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Trailler[] doInBackground(URL... urls) {
            URL url;
            Trailler[] traillers = null;

            if (urls.length > 0) {
                url = urls[0];

                String traillersJsonString = null;
                try {
                    traillersJsonString = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(MovieDetail.this.getClass().toString(), "traillersJsonString: " + traillersJsonString);

                if (traillersJsonString != null) {
                    try {
                        traillers = MoviesJsonUtils.
                                getTraillersFromJson(traillersJsonString);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (traillers != null)
                        return traillers;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Trailler[] traillers) {
            mTraillerProgressBar.setVisibility(View.INVISIBLE);
            if (traillers != null) {

                mMovie.setTraillers(traillers);
                mTraillerAdapter.setTraillersData(traillers); //updates recycler view
                showRecyclerView(R.id.rc_traillers);
            } else {
                Log.d("traillers","vazio");
                showErrorView(R.id.rc_traillers);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ReviewQueryTask extends AsyncTask<URL, Void, Review[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mReviewProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Review[] doInBackground(URL... urls) {
            URL url;
            Review[] reviews = null;

            if (urls.length > 0) {
                url = urls[0];

                String reviewsJsonString = null;
                try {
                    reviewsJsonString = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(MovieDetail.this.getClass().toString(), "reviewsJsonString: " + reviewsJsonString);

                if (reviewsJsonString != null) {
                    try {
                        reviews = MoviesJsonUtils.
                                getReviewsFromJson(reviewsJsonString);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (reviews != null)
                        return reviews;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Review[] reviews) {
            mReviewProgressBar.setVisibility(View.INVISIBLE);
            if (reviews != null) {
                mMovie.setReviews(reviews);
                mReviewAdapter.setReviewsData(reviews); //updates recycler view
                showRecyclerView(R.id.rc_reviews);
            } else {
                Log.d("reviews","vazio");
                showErrorView(R.id.rc_reviews);
            }
        }
    }
}
