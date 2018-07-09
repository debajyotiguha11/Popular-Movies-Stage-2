package me.debjyotiguha.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

@SuppressWarnings("ConstantConditions")
public class MoviesContentProvider extends ContentProvider {

    private PopularMoviesDBHelper dbHelper;

    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(
                PopularMoviesContract.AUTHORITY,
                PopularMoviesContract.PATH_MOVIES,
                MOVIES);

        uriMatcher.addURI(
                PopularMoviesContract.AUTHORITY,
                PopularMoviesContract.PATH_MOVIES + "/#",
                MOVIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new PopularMoviesDBHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match){
            case MOVIES:
                retCursor = db.query(
                        PopularMoviesContract.Movies.TABLE_NAME,
                        null,null,null,null,null,sortOrder);
                break;
            case MOVIES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "movie_id=?";
                String[] mSelectionArgs = new String[]{id};

                retCursor = db.query(
                        PopularMoviesContract.Movies.TABLE_NAME,
                        null, // columns
                        mSelection, // selection
                        mSelectionArgs, // selection args
                        null,
                        null,
                        null); // last param is sorted by
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }

        //update resolver
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Log.d("URL", uri.toString());

        Uri returnUri;

        switch (match){
            case MOVIES:
                long id = db.insert(PopularMoviesContract.Movies.TABLE_NAME,
                        null,
                        contentValues);
                if(id>0){
                    returnUri = ContentUris.withAppendedId(
                            PopularMoviesContract.Movies.CONTENT_URI, id);

                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }

        //update resolver
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String whereClause, @Nullable String[] whereArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int id;

        switch (match){
            case MOVIES:
                id = db.delete(PopularMoviesContract.Movies.TABLE_NAME,
                        whereClause,
                        whereArgs);
                if(id<0){
                    throw new android.database.SQLException("Failed to delete row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unkown uri: " + uri);
        }

        //update resolver
        try{
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return id;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
