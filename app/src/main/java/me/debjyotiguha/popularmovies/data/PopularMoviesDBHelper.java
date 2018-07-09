package me.debjyotiguha.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PopularMoviesDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "popularMovies.db";
    private static final int DATABASE_VERSION = 9;

    PopularMoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " +
                PopularMoviesContract.Movies.TABLE_NAME + " (" +
                PopularMoviesContract.Movies._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularMoviesContract.Movies.COLUMN_MOVIE_ID + " LONG NOT NULL UNIQUE," +
                PopularMoviesContract.Movies.COLUMN_TITLE + " TEXT NOT NULL," +
                PopularMoviesContract.Movies.COLUMN_RATING + " TEXT NOT NULL," +
                PopularMoviesContract.Movies.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                PopularMoviesContract.Movies.COLUMN_SYNOPSIS + " TEXT NOT NULL," +
                PopularMoviesContract.Movies.COLUMN_POSTER_URL + " TEXT NOT NULL" +
                ")";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " +
                PopularMoviesContract.Reviews.TABLE_NAME + " (" +
                PopularMoviesContract.Reviews._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularMoviesContract.Reviews.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                PopularMoviesContract.Reviews.COLUMN_AUTHOR + " LONG NOT NULL," +
                PopularMoviesContract.Reviews.COLUMN_CONTENT + " TEXT NOT NULL," +
                PopularMoviesContract.Reviews.COLUMN_URL_STRING + " TEXT NOT NULL" +
                ")";

        final String SQL_CREATE_TRAILLERS_TABLE = "CREATE TABLE " +
                PopularMoviesContract.Traillers.TABLE_NAME + " (" +
                PopularMoviesContract.Traillers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PopularMoviesContract.Traillers.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                PopularMoviesContract.Traillers.COLUMN_KEY + " LONG NOT NULL UNIQUE," +
                PopularMoviesContract.Traillers.COLUMN_NAME + " TEXT NOT NULL," +
                PopularMoviesContract.Traillers.COLUMN_URL_STRING + " TEXT NOT NULL" +
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILLERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.Movies.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.Reviews.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PopularMoviesContract.Traillers.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
