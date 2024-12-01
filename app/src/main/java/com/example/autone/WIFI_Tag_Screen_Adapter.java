package com.example.autone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WIFI_Tag_Screen_Adapter extends RecyclerView.Adapter<WIFI_Tag_Screen_Adapter.CustomViewHolder> {

    private ArrayList<User_WIFI> arrayList;
    private Context context;

    public WIFI_Tag_Screen_Adapter(ArrayList<User_WIFI> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_wifi_tags, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        User_WIFI userWiFi = arrayList.get(position);
        holder.tv_wifiName.setText(userWiFi.getWifiName());
        holder.tv_wifiDetails.setText(userWiFi.getWifiDetails());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_wifiName;
        TextView tv_wifiDetails;
        ImageView deleteIcon;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_wifiName = itemView.findViewById(R.id.textView27);
            this.tv_wifiDetails = itemView.findViewById(R.id.tv_wifiDetails);
            this.deleteIcon = itemView.findViewById(R.id.imageView51);
        }
    }
}
