package com.example.autone;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class Map_main_screen_Adapter extends RecyclerView.Adapter<Map_main_screen_Adapter.ViewHolder> {

    public ArrayList<Map_main_screen_Adapter.Item> localDataSet = new ArrayList<>();
    private Map_main_screen_Adapter.OnItemClickListener onItemClickListener;
    private Location userLocation;

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_emergency_center_address_list, parent, false);

        return new Map_main_screen_Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = localDataSet.get(position);
        holder.address.setText(item.address);
        holder.coord.setText(calculateDistance(item.latitude, item.longitude) + " m");
        holder.type.setText(item.type);
        holder.response.setText(item.response);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(Map_main_screen_Adapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView address, coord, type, response;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.textView104);
            coord = itemView.findViewById(R.id.textView105);
            type = itemView.findViewById(R.id.textView101);
            response = itemView.findViewById((R.id.textView103));
        }
    }
    public static class Item{
        String address;
        double latitude;
        double longitude;
        String type;
        String response;

        public Item(String address, double latitude, double longitude, String type, String response){
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
            this.response = response;
        }
    }

    private String calculateDistance(double crimeLat, double crimeLon) {
        if (userLocation == null) return "N/A";
        Location crimeLocation = new Location("");
        crimeLocation.setLatitude(crimeLat);
        crimeLocation.setLongitude(crimeLon);
        float distance = userLocation.distanceTo(crimeLocation);
        return String.format("%d", (int) distance); // Convert distance to an integer
    }
}
