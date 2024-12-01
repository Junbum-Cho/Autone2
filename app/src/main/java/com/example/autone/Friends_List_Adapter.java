package com.example.autone;


import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Friends_List_Adapter extends RecyclerView.Adapter<Friends_List_Adapter.ViewHolder> {

    public ArrayList<Item> localDataSet;
    private OnImageClickListener onImageClickListener; // Step 2
    Boolean editMode = false;

    // Step 1
    public interface OnImageClickListener {
        void onImageClick(int position);
        void onDeleteClick(int position);
        void onRemoveClick (int position);
    }

    public void setOnImageClickListener(OnImageClickListener listener) { // Step 3
        this.onImageClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView dataView;
        public final ImageView dataView2;
        public final ImageView dataView3;
        public final ImageView dataView4;
        public final Drawable dataView3_family;
        public final Drawable dataView3_edit_family;
        public final Drawable dataView3_edit_family_false;

        public ViewHolder(View view, final OnImageClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View

            dataView = (TextView) view.findViewById(R.id.textView30);
            dataView2 = (ImageView) view.findViewById(R.id.imageView39);
            dataView3 = (ImageView) view.findViewById(R.id.imageView40);
            dataView4 = (ImageView) view.findViewById(R.id.imageView41);
            dataView3_edit_family = AppCompatResources.getDrawable(view.getContext()
                    , R.drawable.delete_from_family);
            dataView3_edit_family_false = AppCompatResources.getDrawable(view.getContext()
                    , R.drawable.add_family);
            dataView3_family = AppCompatResources.getDrawable(view.getContext()
                    , R.drawable.friend_icon);


            dataView3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AdapterClick", "ImageView clicked!");
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        if ( "edit_family_false".equals(v.getTag())) {
                            listener.onImageClick(position);
                        } else if ("edit_family".equals(v.getTag())){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            dataView4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                            listener.onRemoveClick(position);
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
    public Friends_List_Adapter(ArrayList<Item> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_list_of_friends, viewGroup, false);

        return new ViewHolder(view, onImageClickListener); // Pass the listener to the ViewHolder
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Item item = localDataSet.get(position);
        viewHolder.dataView.setText(String.format("%s",item.user_name));
        if(editMode){
            viewHolder.dataView3.setVisibility(View.VISIBLE);
            if(item.family){
                viewHolder.dataView3.setImageDrawable(viewHolder.dataView3_edit_family);
                viewHolder.dataView3.setTag("edit_family");
            } else{
                viewHolder.dataView3.setImageDrawable(viewHolder.dataView3_edit_family_false);
                viewHolder.dataView3.setTag("edit_family_false");
            }
        } else{
            viewHolder.dataView3.setImageDrawable(viewHolder.dataView3_family);
            viewHolder.dataView3.setTag("family");
            if(item.family){
                viewHolder.dataView3.setVisibility(View.VISIBLE);
            } else{
                viewHolder.dataView3.setVisibility(View.INVISIBLE);
            }
        }

        if (item.online){
            viewHolder.dataView2.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.dataView2.setVisibility(View.INVISIBLE);
        }

        if (editMode) {
            viewHolder.dataView4.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.dataView4.setVisibility(View.GONE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
    public void setLocalDataSet(ArrayList<Item> localDataSet){
        this.localDataSet = localDataSet;
        notifyDataSetChanged();
    }
    static class Item{
        String user_name;
        String user_key;
        Boolean family;
        Boolean online;


        public Item(String user_name,String user_key, Boolean family, Boolean online) {
            this.user_name = user_name;
            this.family = family;
            this.online = online;
            this.user_key = user_key;
        }
    }
}

