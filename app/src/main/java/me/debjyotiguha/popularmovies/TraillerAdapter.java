package me.debjyotiguha.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.debjyotiguha.popularmovies.data.Trailler;

@SuppressWarnings("ConstantConditions")
public class TraillerAdapter extends RecyclerView.Adapter<TraillerAdapter.TraillerAdapterViewHolder>{

    private GridItemClickListener mOnClickListener;

    /**
     * Defines a method signature for treat click events
     */
    public interface GridItemClickListener {
        void onGridTraillerItemClick(int clickedItemIndex);
    }

    /**
     * Receives method within GridItemClickListener to treat on click events
     */
    TraillerAdapter(GridItemClickListener listener) {
        mOnClickListener = listener;
    }

    private Trailler[] mTraillers;

    //method used outside from this class
    public Trailler getTrailler(int index){
        return mTraillers[index];
    }

    /**
     * Set new content of movies for MovieAdapter;
     */
    public void setTraillersData(Trailler[] traillers) {
        mTraillers = traillers; //updates traillers list
        notifyDataSetChanged();
    }

    /**
     * Inflates a ViewHolder
     */
    @Override
    public TraillerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        int layoutIdForGridItem = R.layout.trailler_grid_item;

        View view = inflater.inflate(layoutIdForGridItem, parent, shouldAttachToParentImmediately);

        return new TraillerAdapterViewHolder(view);
    }

    /**
     * Binds desired content to viewHolder, according to its position
     */
    @Override
    public void onBindViewHolder(TraillerAdapterViewHolder holder, int position) {
        //access method bind from viewHolder
        holder.bind(mTraillers[position]);
    }

    @Override
    public int getItemCount() {
        if(mTraillers != null)
            return mTraillers.length;
        else
            return 0;
    }

    /**
     * class for the grid activity
     */
    public class TraillerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mPlayButton;
        private TextView mTraillerTitle;
        Context context;

        TraillerAdapterViewHolder(View itemView) {
            super(itemView);

            mPlayButton = (ImageView) itemView.findViewById(R.id.play);
            mTraillerTitle = (TextView) itemView.findViewById(R.id.trailler_title);
            context = itemView.getContext();

            //attach an external handler from Adapter into this viewHolder
            itemView.setOnClickListener(this);//set the OnclickListener event
        }

        /**
         * Invokes mOnClickListener from adapter class.
         */
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onGridTraillerItemClick(clickedPosition);
        }

        /**
         * This method is called in adapter to bind specific values into viewHolder
         */
        void bind(Trailler trailler) {
            mTraillerTitle.setText(trailler.getName());
        }

    }

}
