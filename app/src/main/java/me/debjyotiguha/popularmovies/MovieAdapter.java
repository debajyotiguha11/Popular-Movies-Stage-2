package me.debjyotiguha.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import me.debjyotiguha.popularmovies.data.Movie;
import me.debjyotiguha.popularmovies.utilities.NetworkUtils;

@SuppressWarnings("ConstantConditions")
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private GridItemClickListener mOnClickListener;
    private Movie[] mMovies;

    public Movie getMovie(int index){
        return mMovies[index];
    }

    /**
     * Receives method within GridItemClickListener to treat on click events
     */
    MovieAdapter(GridItemClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        int layoutIdForGridItem = R.layout.movie_grid_item;

        View view = inflater.inflate(layoutIdForGridItem, parent, shouldAttachToParentImmediately);

        return  new MovieAdapterViewHolder(view);
    }

    /**
     * Binds desired content to viewHolder, according to its position
     */
    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {

        //debug
        //String posterUrl = "http://image.tmdb.org/t/p/w342//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg";
        String posterUrl = NetworkUtils.POSTER_URL_BASE + NetworkUtils.POSTER_SIZE_PATH_URL +
                mMovies[position].getPosterUrl();
        //Log.d("viewHolder","posterUrl: " + posterUrl);

        holder.bind(posterUrl);
    }

    @Override
    public int getItemCount() {
        if (mMovies != null)
            return mMovies.length;
        else
            return 0;
    }

    /**
     * Set new content of movies for MovieAdapter;
     */
    public void setMoviesData(Movie[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    /**
     * Defines a method signature for treat click events
     */
    public interface GridItemClickListener {
        void onGridItemClick(int clickedItemIndex);
    }

    /**
     * MovieAdapterViewHolder is the inner class ViewHolder for this Adapter
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mMoviePoster;
        Context context;

        MovieAdapterViewHolder(View itemView) {
            super(itemView);

            mMoviePoster = (ImageView) itemView.findViewById(R.id.iv_poster);
            context = itemView.getContext();
            itemView.setOnClickListener(this);//set the OnclickListener event
        }

        /**
         * Invokes mOnClickListener from adapter class.
         */
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onGridItemClick(clickedPosition);
        }

        void bind(String posterUrl) {
            NetworkUtils.setImage(context, posterUrl, mMoviePoster);
        }
    }

}
