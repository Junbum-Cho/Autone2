package com.example.autone;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import net.daum.mf.map.api.MapView;

import java.lang.ref.WeakReference;

public class MapViewSingleton {
    private static MapView instance;
    private static WeakReference<Activity> activityReference;

    private MapViewSingleton() {}

    public static MapView getInstance(Activity activity) {
        if (instance == null) {
            activityReference = new WeakReference<>(activity);
            instance = new MapView(activityReference.get());
        }
        return instance;
    }

    public static void removeMapView(ViewGroup container) {
        if (instance != null) {
            container.removeView(instance);
            instance = null;
            activityReference.clear();
            activityReference = null;
        }
    }
}
