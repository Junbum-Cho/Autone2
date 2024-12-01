package com.example.autone;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Notification_Center_Requests_Adapter extends RecyclerView.Adapter<Notification_Center_Requests_Adapter.ViewHolder> {

    public ArrayList<Item> localDataSet;
    private OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     Provide a reference to the type of views that you are using
     (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView dataView, dataView2;
        public final ImageView dataView3, dataView4;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View

            dataView = view.findViewById(R.id.textView87);
            dataView2 = view.findViewById(R.id.textView100);
            dataView3 = view.findViewById(R.id.imageView101);
            dataView4 = view.findViewById(R.id.imageView102);
            dataView3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*//Log.d("AdapterClick", "ImageView clicked!");*/
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        /*if (listener != null && "edit_family_false".equals(v.getTag())) {*/
                            listener.onItemClick(position);
/*                        }*/
                    }
                }
            });
            dataView4.setOnClickListener(new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public Notification_Center_Requests_Adapter(ArrayList<Item> dataSet) {
        localDataSet = dataSet;
    }
    public void setLocalDataSet(ArrayList<Item> dataSet) {
        localDataSet = dataSet;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_notification_friend_request, viewGroup, false);
        return new ViewHolder(view, onItemClickListener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Item item = localDataSet.get(position);
        viewHolder.dataView.setText(String.format("%s",item.username));
        viewHolder.dataView2.setText(String.format("%s",item.friend_or_family));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
    static class Item{
        String username;
        String friend_or_family;
        String userkey;
        String identity;

        public Item(String username, String friend_or_family, String userkey, String identity) {
            this.username = username;
            this.friend_or_family = friend_or_family;
            this.userkey = userkey;
            this.identity = identity;
        }
    }
}

