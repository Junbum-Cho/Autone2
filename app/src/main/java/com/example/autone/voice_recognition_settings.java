package com.example.autone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.ProgressDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class voice_recognition_settings extends AppCompatActivity {

    BottomSheetDialog dialog;
    ImageView show;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog mDialog;
    private CustomChoiceListViewAdapter adapter;
    private ArrayList<String> selectedSignals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognition_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        selectedSignals = new ArrayList<>();

        ImageView previousbtn = findViewById(R.id.imageButton4);
        previousbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Button next = findViewById(R.id.button18);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viw) {
                mDialog = new ProgressDialog(voice_recognition_settings.this);
                mDialog.setMessage("처리중...");
                mDialog.show();
                saveSignalsToFirebase();
                mDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), Main_screen.class);
                startActivity(intent);
            }
        });

        //하단바 코드
        show = findViewById (R.id.imageView52);
        dialog = new BottomSheetDialog(this);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showsignal();
                dialog.show();
            }
        });
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void showsignal() {
        View view = getLayoutInflater().inflate(R.layout.signal_bottom_sheet, null, false);
        ListView listView = view.findViewById(R.id.listview1);
        adapter = new CustomChoiceListViewAdapter();
        listView.setAdapter(adapter);

        // 아이템 추가.
        //adapter.addItem(ContextCompat.getDrawable(this, R.drawable.shouting_icon), "'알겠습니다'를 세 번 말하기") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.shouting_icon), "'죄송합니다'를 세 번 말하기") ;
        //adapter.addItem(ContextCompat.getDrawable(this, R.drawable.shouting_icon), "'알아들었습니다'를 세 번 말하기") ;

        adapter.notifyDataSetChanged();

        dialog.setContentView(view);
    }

    private void saveSignalsToFirebase() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference signalRef = databaseReference.child("Report Signal").child(userId).child("start signal");

        selectedSignals.clear();
        for (int i = 0; i < adapter.getCount(); i++) {
            ListViewItem item = (ListViewItem) adapter.getItem(i);
            if (adapter.getItem(i).isChecked()) {
                selectedSignals.add(adapter.getItem(i).getText());
            }
        }

        signalRef.setValue(selectedSignals).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(voice_recognition_settings.this, "선택된 시그널(들)을 성공적으로 저장했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(voice_recognition_settings.this, "시그널 저장 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), Main_screen.class);
        startActivity(intent);
    }
}