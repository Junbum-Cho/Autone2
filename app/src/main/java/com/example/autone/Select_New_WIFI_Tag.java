package com.example.autone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import android.net.wifi.ScanResult;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class Select_New_WIFI_Tag extends Fragment {

    private Context mContext;
    private ImageButton btnConnect;
    private PopupWindow mPopupWindow;

    private int mCurrentX = Gravity.CENTER_HORIZONTAL;
    private int mCurrentY = Gravity.CENTER_VERTICAL;

    private WifiManager wifiManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public String wifi_ssid;
    public String wifi_pw ;


    public Select_New_WIFI_Tag(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getContext();
        //Event Bus
        try{ EventBus.getDefault().register(this); }catch (Exception e){}
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_wifi_list_adapter,container,false);

        //wifi 버튼
        btnConnect = view.findViewById(R.id.button16);
        //권한에 대한 자동 허가 요청 및 설명
        AutoPermissions.Companion.loadAllPermissions((Activity) mContext,101);

        return view;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstancdState){
//        super.onViewCreated(view,savedInstancdState);
//
//        btnConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //가운데 버튼 클릭 시 wifi list를 보여줄 popupwindow를 띄운다.
//                setMyPopupWindow();
//            }
//        });
//
//
//        //get access to location permission
//        //wifi scan을 위해 위치 허가를 꼭 받아야함.
//        if ( Build.VERSION.SDK_INT >= 23){
//            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
//                    PackageManager.PERMISSION_GRANTED  ){
//                requestPermissions(new String[]{
//                                android.Manifest.permission.ACCESS_FINE_LOCATION},
//                        REQUEST_CODE_ASK_PERMISSIONS);
//                return ;
//            }
//        }
//
//
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CODE_ASK_PERMISSIONS:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    wifiScan();
//                    //Log.d("wifi", "In this");
//
//                } else {
//                    // Permission Denied
//                    //Log.d("wifi", "permission denied");
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

    //popup window
    private void setMyPopupWindow(){

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View popupLayout = inflater.inflate(R.layout.activity_select_new_wifi_tag, null);

        recyclerView = popupLayout.findViewById(R.id.WIFIListRecyclerView);

        popupLayout.setOnTouchListener(new View.OnTouchListener() {

            private float mDx;
            private float mDy;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mDx = mCurrentX - motionEvent.getRawX();
                        mDy = mCurrentY - motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurrentX = (int) (motionEvent.getRawX() + mDx);
                        mCurrentY = (int) (motionEvent.getRawY() + mDy);
                        mPopupWindow.update(mCurrentX, mCurrentY, -1, -1);
                        break;
                }
                return true;
            }
        });


        mPopupWindow = new PopupWindow(popupLayout, 800, 900, true);
        mPopupWindow.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Log.i("POP-UP", "onDismiss: ");
            }

        });


        wifiScan(); //popupwindow에서 wifi scan을 시작한다.

    }

    public void wifiScan(){
        startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));

//        wifiManager = (WifiManager)
//                mContext.getSystemService(WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }





    private void scanSuccess() {

        // 스캔 성공시 저장된 list를 recycler view를 통해 보여줌
        List<ScanResult> results = wifiManager.getScanResults();
        mAdapter = new WIFI_List_Adapter(results);
        recyclerView.setAdapter(mAdapter);



        Log.d("wifi", "scan success");
        StringBuffer st = new StringBuffer();
        for(ScanResult r : results ){
            Log.d("wifi",""+r);
            st.append(r.SSID);
            st.append("\n");
        }

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        Log.d("wifi", "scanFailure");
        Toast.makeText(mContext,"wifi scan에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        // consider using old scan results: these are the OLD results!
        //List<ScanResult> results = wifiManager.getScanResults();
        // potentially use older scan results ...

    }


    //wifidialog가 끝나면 eventbus를 통해 전달된 ssid와 pw 저장
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void enterPWEvent(WifiData event){
        //wifi_ssid = event.ssid;
        //wifi_pw = event.pw;

        Log.d("wifi","setting\nssid : " + wifi_ssid + "   pw : " + wifi_pw);
        mPopupWindow.dismiss();

    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}