package com.example.autone;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.HashMap;
import java.util.List;

public class WIFI_List_Adapter extends RecyclerView.Adapter<WIFI_List_Adapter.MyViewHolder> {

    private List<ScanResult> items;
    private Context mContext;

    public WIFI_List_Adapter(List<ScanResult> items){

        this.items=items;
    }

    @NonNull
    @Override
    public WIFI_List_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_wifi_list_adapter , parent, false);

        mContext = parent.getContext();

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvWifiName;
        public MyViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)
                    {
                        // click event
                        // ssid 저장
                        String ssid = items.get(pos).SSID;

                        // pw 입력 다이얼로그를 호출한다.
                        // 입력된 pw을 저장한다.
                        New_WIFI_adding customDialog = new New_WIFI_adding(mContext);
                        customDialog.callFunction(ssid);
                    }
                }
            });


            tvWifiName=itemView.findViewById(R.id.tv_wifiName);

        }
        public void setItem(ScanResult item){
            tvWifiName.setText(item.SSID);

        }
    }
}

//public class WIFI_List_Adapter extends RecyclerView.Adapter<WIFI_List_Adapter.MyViewHolder> {
//
//    String password;
//    String ssid;
//
//    private List<ScanResult> items;
//    private Context mContext;
//    private DatabaseReference mDatabase;
//
//    public WIFI_List_Adapter(List<ScanResult> items){
//        this.items = items;
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//    }
//
//    @NonNull
//    @Override
//    public WIFI_List_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_wifi_list_adapter, parent, false);
//
//        mContext = parent.getContext();
//
//        return new MyViewHolder(itemView);
//    }
//
//
//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        holder.setItem(items.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return items.size();
//    }
//
//    public class MyViewHolder extends RecyclerView.ViewHolder {
//        public TextView WifiName;
//        public MyViewHolder(View itemView) {
//            super(itemView);
//            WifiName = itemView.findViewById(R.id.tv_wifiName);
//
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int pos = getAdapterPosition();
//                    if (pos != RecyclerView.NO_POSITION) {
//                        ScanResult selectedResult = items.get(pos);
//                        final String ssid = selectedResult.SSID;
//
//                        wifiDialog customDialog = new wifiDialog(mContext);
//                        customDialog.callFunction(ssid, new wifiDialog.DialogCallback() {
//
//                            @Override
//                            public void onCallback(String password) {
//                                // Create a new object to hold SSID and Password
//                                HashMap<String, String> wifiInfo = new HashMap<>();
//                                wifiInfo.put("SSID", ssid);
//                                wifiInfo.put("Password", password);
//
//                                // Save this object to Firebase
//                                mDatabase.child("WIFI List").child("WIFI Tag 1").setValue(ssid);
//                                mDatabase.child("WIFI List").child("WIFI Tag 1").child("WIFI Password").setValue(password);
//                            }
//                        });
//                    }
//                }
//            });
//        }
//        public void setItem(ScanResult item){
//            WifiName.setText(item.SSID);
//        }
//    }
//}