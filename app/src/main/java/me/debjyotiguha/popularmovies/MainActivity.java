package me.debjyotiguha.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import me.debjyotiguha.popularmovies.data.Movie;
import me.debjyotiguha.popularmovies.data.PopularMoviesContract;
import me.debjyotiguha.popularmovies.utilities.MoviesJsonUtils;
import me.debjyotiguha.popularmovies.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import static me.debjyotiguha.popularmovies.utilities.NetworkUtils.SORT_BY_FAVORITE;
import static me.debjyotiguha.popularmovies.utilities.NetworkUtils.SORT_BY_POPULARITY;
import static me.debjyotiguha.popularmovies.utilities.NetworkUtils.SORT_BY_RATING;

/**
 * Main Class. implements MovieAdapter.GridItemClickListener in order to treat click events
 * */
public class MainActivity extends AppCompatActivity implements
        MovieAdapter.GridItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private final String TAG = MainActivity.class.toString();
    private static final int MOVIES_LOADER_ID = 0;

    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorView;
    private ProgressBar mProgressBar;
    private static Movie[] mMovies; // saves movies list temporally
    private static boolean isErrorViewVisible = false;
    private static int mSortType = SORT_BY_POPULARITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorView = (TextView) findViewById(R.id.tv_error_display);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);

        setRecyclerView();

        if(mMovies != null){
            if(isErrorViewVisible)
                showErrorView();
            else
                if(mSortType != SORT_BY_FAVORITE)
                    mMovieAdapter.setMoviesData(mMovies);
        }
        else{
            loadMovies(SORT_BY_POPULARITY);
        }
    }

    /**
     * This method is called after this activity has been paused or restarted.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(mSortType == SORT_BY_FAVORITE)
            getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
    }

    /**
     * This method do a parser in a cursor in current position to a Movie
     */
    private Movie parseMovie(Cursor cursor)
    {
        Movie movie = null;

        try
        {
            int idIndex = cursor.getColumnIndexOrThrow(PopularMoviesContract.Movies.COLUMN_MOVIE_ID);
            int titleIndex = cursor.getColumnIndexOrThrow(PopularMoviesContract.Movies.COLUMN_TITLE);
            int posterUrlIndex = cursor.getColumnIndexOrThrow(PopularMoviesContract.Movies.COLUMN_POSTER_URL);
            int sysnopsisIndex = cursor.getColumnIndexOrThrow(PopularMoviesContract.Movies.COLUMN_SYNOPSIS);
            int ratingIndex = cursor.getColumnIndexOrThrow(PopularMoviesContract.Movies.COLUMN_RATING);
            int releaseDateIndex = cursor.getColumnIndexOrThrow(PopularMoviesContract.Movies.COLUMN_RELEASE_DATE);
            long id = cursor.getLong(idIndex);
            String title = cursor.getString(titleIndex);
            String posterUrl = cursor.getString(posterUrlIndex);
            String sysnopsis = cursor.getString(sysnopsisIndex);
            Double rating = cursor.getDouble(ratingIndex);
            String releaseDate = cursor.getString(releaseDateIndex);
            movie = new Movie(id, title, posterUrl, sysnopsis, rating, releaseDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return movie;
    }

    /**
     * parser from Cursor to Movie[]
     * */
    private Movie[] populateMovies(Cursor cursor){
        if(cursor ==null || cursor.getCount() == 0)
            return null;

        Movie[] movies = new Movie[cursor.getCount()];

        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++){
            Movie movie = parseMovie(cursor);
            movies[i] = movie;
            cursor.moveToNext();
        }

        return movies;
    }

    /**
     * Defines recycler view configuration
     */
    private void setRecyclerView() {

        final boolean reverseLayout = false;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(
                this, setNumberOfColumns(), LinearLayoutManager.VERTICAL, reverseLayout);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mMoviesRecyclerView.setAdapter(mMovieAdapter);
    }

    public void onGridItemClick(int clickedItemIndex){
        Log.d(TAG, "click item index" + clickedItemIndex);

        Movie movie = mMovieAdapter.getMovie(clickedItemIndex);
        Intent intent = new Intent(this, MovieDetail.class);
        intent.putExtra(Movie.class.toString(), movie);
        startActivity(intent);
    }

    /**
     * Inflates Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    /**
     * Handles selected items from menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        mMoviesRecyclerView.smoothScrollToPosition(0); // resets position

        switch (id) {
            case R.id.sort_by_popularity:
                mSortType = SORT_BY_POPULARITY;
                Log.d(TAG, "Select by popularity");
                loadMovies(SORT_BY_POPULARITY);
                break;
            case R.id.sort_by_highest_rate:
                mSortType = SORT_BY_RATING;
                Log.d(TAG, "Select by rating");
                loadMovies(SORT_BY_RATING);
                break;
            case R.id.sort_by_favorites:
                mSortType = SORT_BY_FAVORITE;
                Log.d(TAG, "Select by favorites");
                loadMoviesFromDB();
                break;
            default:
                Log.d(TAG, "Invalid option");
                break;
        }

        return true;
    }

    /**
     * Shows error view and hides recycler view;
     */
    private void showErrorView() {
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.VISIBLE);
        isErrorViewVisible = true;
    }

    /**
     * Shows recycler view and hides error view;
     */
    private void showRecyclerView() {
        mErrorView.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
        isErrorViewVisible = false;
    }

    /**
     * Calls network to fetch movies by popularity or by average rating.
     * Otherwise, this method is going to be sorted by popularity.
     */
    private void loadMovies(int sortedBy) {

        URL urlPostersQuery;

        if (sortedBy == SORT_BY_POPULARITY || sortedBy == SORT_BY_RATING) {
            urlPostersQuery = NetworkUtils.buildUrlWithFilter(sortedBy);
        } else {
            urlPostersQuery = NetworkUtils.buildUrlWithFilter(SORT_BY_POPULARITY);
        }
        new MoviesQueryTask().execute(urlPostersQuery);

    }

    /**
     * retrieves movies from DB
     * */
    private void loadMoviesFromDB(){
        getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, this);
    }

    /**
     * returns the number of Columns based on width screen size
     */
    private int setNumberOfColumns() {

        //from https://stackoverflow.com/questions/4743116/get-screen-width-and-height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int dpi = displayMetrics.densityDpi;
        float posterSizeDp = 180;

        Log.d(TAG, "width: " + width);
        Log.d(TAG, "dpi: " + dpi);
        Log.d(TAG, "dp: " + (width / (dpi / 160)) / posterSizeDp);

        //px = dp*(dpi/160)
        return (int) ((width / (dpi / 160)) / posterSizeDp);
    }

    /**
     * This method implements AsyncTaskLoader for database
     * */
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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
                // Will implement to load data

                try {
                    return getContentResolver().query(
                            PopularMoviesContract.Movies.CONTENT_URI,
                            null,
                            null,
                            null,
                            PopularMoviesContract.Movies.COLUMN_TITLE);

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
        Movie[] movies = populateMovies(data);
        data.close(); // close this cursor in order to avoid memory leak;

        if(movies != null){
            mMovies = movies;
            mMovieAdapter.setMoviesData(movies);
            showRecyclerView();
        }else{
            Toast.makeText(this, R.string.favorites_empty, Toast.LENGTH_SHORT).show();
        }
        mMovies = movies;
        mMovieAdapter.setMoviesData(movies);
    }

    /**
     * This method implements AsyncTaskLoader for database
     * */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.setMoviesData(null);
    }

    @SuppressLint("StaticFieldLeak")
    private class MoviesQueryTask extends AsyncTask<URL, Void, Movie[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(URL... urls) {

            URL url;
            Movie[] movies = null;

            if (urls.length > 0) {
                url = urls[0];

                String moviesJsonString = null;
                try {
                    moviesJsonString = NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(MainActivity.this.getClass().toString(), "moviesJsonString: " + moviesJsonString);

                if (moviesJsonString != null) {
                    try {
                        movies = MoviesJsonUtils.
                                getMoviesFromJson(moviesJsonString);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (movies != null)
                        return movies;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (movies != null) {
                Log.d("Test","full!");
                mMovies = movies;//updates moviesList var
                mMovieAdapter.setMoviesData(movies);
                showRecyclerView();
            } else {
                Log.d("Test","empty");
                showErrorView();
            }
        }
    }

}
