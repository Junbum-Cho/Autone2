package com.example.autone;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Statistics_and_Files_Adapter extends RecyclerView.Adapter<Statistics_and_Files_Adapter.ViewHolder> {

    private ArrayList<Item> localDataSet;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView dateView, addressView, promptView;

        public ViewHolder(View view) {
            super(view);
            dateView = view.findViewById(R.id.textView97);
            addressView = view.findViewById(R.id.textView93);
            promptView = view.findViewById(R.id.textView95);
            view.setOnClickListener(this);  // Set the ViewHolder's View to listen for clicks.
        }


        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position);
                }
            }
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */

    public Statistics_and_Files_Adapter(ArrayList<Item> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.file_and_statistics, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Item currentItem = localDataSet.get(position);
        viewHolder.dateView.setText(currentItem.timestamp);
        viewHolder.addressView.setText(currentItem.location);
        //viewHolder.promptView.setText(currentItem.prompt);
        viewHolder.promptView.setText(String.format("파일 삭제까지 %s달 남음",4));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static class Item{
        public String reportID;
        public String timestamp;
        public String location;
        public String prompt;

        public Item() {}

        public Item(String reportID, String timestamp, String location, String prompt) {
            this.reportID = reportID;
            this.timestamp = timestamp;
            this.location = location;
            this.prompt = prompt;
        }
    }
}

