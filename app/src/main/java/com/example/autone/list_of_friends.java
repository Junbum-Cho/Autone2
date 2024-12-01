package com.example.autone;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link list_of_friends#newInstance} factory method to
 * create an instance of this fragment.
 */

public class list_of_friends extends Fragment {

    private ArrayList<Friends_List_Adapter.Item> dataSet = new ArrayList<>(); // Assuming you want an ArrayList. Initialize it properly.


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public list_of_friends() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment list_of_friends.
     */
    // TODO: Rename and change types and number of parameters
    public static list_of_friends newInstance(String param1, String param2) {
        list_of_friends fragment = new list_of_friends();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Friends_List_Adapter adapter = new Friends_List_Adapter(dataSet);
        adapter.setOnImageClickListener(new Friends_List_Adapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                // Launch your desired Activity:
                Intent intent = new Intent(getContext(), Add_family_member.class);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                //TODO make delete
            }

            @Override
            public void onRemoveClick (int position) {

            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView3);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_of_friends, container, false);
    }
}