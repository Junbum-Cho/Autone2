package com.example.autone;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public interface mCurrentLocationEventListener extends MapView.CurrentLocationEventListener {
    @Override
    default void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    default void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    default void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    default void onCurrentLocationUpdateCancelled(MapView mapView) {

    }
}
