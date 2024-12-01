package com.example.autone;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public interface mMapViewEventListener extends MapView.MapViewEventListener {
    @Override
     default void onMapViewInitialized(MapView mapView) {

    }

    @Override
    default void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    default void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    default void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    default void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    default void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    default void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    default void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    default void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
}
