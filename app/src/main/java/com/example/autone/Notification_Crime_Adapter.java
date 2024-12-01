package com.example.autone;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Notification_Crime_Adapter extends RecyclerView.Adapter<Notification_Crime_Adapter.ViewHolder> {

    private ArrayList<Item> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView addressView;
        public final TextView distanceView;
        public final TextView typeView;
        public final TextView responseView;

        public ViewHolder(View view) {
            super(view);
            addressView = view.findViewById(R.id.textView104);
            distanceView = view.findViewById(R.id.textView105);
            typeView = view.findViewById(R.id.textView101);
            responseView = view.findViewById(R.id.textView103);
        }
    }

    public Notification_Crime_Adapter(ArrayList<Item> dataSet) {
        localDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_emergency_center_address_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Item item = localDataSet.get(position);
        viewHolder.addressView.setText(item.center_address);
        viewHolder.distanceView.setText(item.center_distance + " m");
        viewHolder.typeView.setText(item.crime_type);
        viewHolder.responseView.setText(item.response_coming);

        if (item.isFamily()) {
            viewHolder.addressView.setTextColor(Color.RED);
            viewHolder.distanceView.setTextColor(Color.RED);
            viewHolder.typeView.setTextColor(Color.RED);
            viewHolder.responseView.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static class Item {
        String center_address;
        String center_distance;
        String crime_type;
        String response_coming;
        private boolean isFamily;

        public Item(String center_address, String center_distance, String crime_type, String response_coming) {
            this.center_address = center_address;
            this.center_distance = center_distance;
            this.crime_type = crime_type;
            this.response_coming = response_coming;
            this.isFamily = false;
        }
        public boolean isFamily() {
            return isFamily;
        }

        public void setIsFamily(boolean isFamily) {
            this.isFamily = isFamily;
        }
    }
}
