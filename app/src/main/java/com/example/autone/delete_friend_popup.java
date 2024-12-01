package com.example.autone;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class delete_friend_popup extends Fragment {





    public delete_friend_popup() {
        // Required empty public constructor

    }

    View.OnClickListener denyListener;
    View.OnClickListener acceptListener;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_delete_friend_popup, container, false);
        // Inflate the layout for this fragment
        ImageView acceptDelete;
        ImageView denyDelete;

        acceptDelete = (ImageView) rootView.findViewById(R.id.imageView111);
        denyDelete = (ImageView) rootView.findViewById(R.id.imageView15);
        acceptDelete.setOnClickListener(acceptListener);
        denyDelete.setOnClickListener(denyListener);
        return rootView;
    }
}