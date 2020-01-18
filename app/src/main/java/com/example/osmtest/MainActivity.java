package com.example.osmtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//Draw point on map
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;



import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener, MapboxMap.OnMapClickListener {

    //Map and location
    MapView mapView;
    MapboxMap myMap;
    PermissionsManager pm;
    LocationComponent locationComponent;

    //Navigation
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navMapRoute;
    private static final String TAG = "DirectionsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(getApplicationContext(),
                getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_main);



        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {


                myMap = mapboxMap;
                myMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded(){

                    @Override
                    public void onStyleLoaded(@NonNull Style style){

                        addDestinationIconSymbolLayer(style);
                        enableLocationComponent(style);

                        myMap.addOnMapClickListener(MainActivity.this);


                    }
                });
            }
            });
    }




    public void startNav(View v) {
        boolean simulateRoute = false;
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(simulateRoute)
                .build();
// Call this method with Context from within an Activity
        NavigationLauncher.startNavigation(MainActivity.this, options);
    }


    private void addDestinationIconSymbolLayer(@NonNull Style currentStyle){

        Bitmap destinationMarker = BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default);
        GeoJsonSource gjSource = new GeoJsonSource("destination-source-id");
        SymbolLayer dsl = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");


        currentStyle.addImage("destination-icon-id",destinationMarker);

        currentStyle.addSource(gjSource);

        dsl.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );

        currentStyle.addLayer(dsl);
    }


    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng clickSpot){

      Point destination = Point.fromLngLat(clickSpot.getLongitude(), clickSpot.getLatitude());

      Point origin = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude() );

      GeoJsonSource source = myMap.getStyle().getSourceAs("destination-source-id");

      getRoute(origin, destination);

      if(source != null){
          source.setGeoJson(Feature.fromGeometry(destination));
      }

      return true;
    }


    private void getRoute(Point origin, Point destination) {

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                              @Override
                              public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                                  if(response.body()==null){
                                      System.out.println("No routes found. Something might be wrong with accesstoken");
                                  } else if(response.body().routes().size() <1){

                                      System.out.println("No routes");
                                      return;
                                  }


                                  currentRoute = response.body().routes().get(0);


                                  if(navMapRoute != null) navMapRoute.removeRoute();
                                  else navMapRoute = new NavigationMapRoute(null, mapView, myMap,
                                          R.style.NavigationMapRoute);


                                  navMapRoute.addRoute(currentRoute);

                              }

                              @Override
                              public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                                System.out.println(t.getMessage());
                              }
                          }

                );


    }

    @SuppressWarnings({"Missingpermission"})
    private void enableLocationComponent(@NonNull Style readyMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            locationComponent = myMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, readyMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);

            Location userPos = locationComponent.getLastKnownLocation();

            CameraPosition userCameraPos = new CameraPosition.Builder()
                    .target(new LatLng(userPos.getLatitude(), userPos.getLongitude()))
                    .zoom(17)
                    .bearing(180)
                    .tilt(0)
                    .build();

            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(userCameraPos), 1000);



        } else {

            pm = new PermissionsManager(this);
            pm.requestLocationPermissions(this);

        }
    }

    public void centerOnUserLocation(Double zoomLevel) {

        Location oldUserPos = locationComponent.getLastKnownLocation();
        locationComponent.forceLocationUpdate(oldUserPos);
        Location current = locationComponent.getLastKnownLocation();

        CameraPosition userCameraPos = new CameraPosition.Builder()
                .target(new LatLng(current.getLatitude(), current.getLongitude()))
                .zoom(zoomLevel)
                .bearing(0)
                .tilt(0)
                .build();

        myMap.animateCamera(CameraUpdateFactory.newCameraPosition(userCameraPos), 1000);

    }

    public void togglemapStyle(View view) {

        String satellite = Style.SATELLITE_STREETS;
        String normalMap = Style.MAPBOX_STREETS;
        String currentStyle = myMap.getStyle().getUri();

        System.out.println(satellite);
        System.out.println(currentStyle);

        if(currentStyle.equals(satellite)){
            System.out.println("Tyylit täsmää");
            myMap.setStyle(new Style.Builder().fromUri(normalMap), new Style.OnStyleLoaded(){

                @Override
                public void onStyleLoaded(@NonNull Style style){
                    System.out.println("Tyyli vaihdettu");
                    addDestinationIconSymbolLayer(style);
                    enableLocationComponent(style);

                }
            });



        }else{

            myMap.setStyle(new Style.Builder().fromUri(satellite), new Style.OnStyleLoaded(){

                @Override
                public void onStyleLoaded(@NonNull Style style){

                    enableLocationComponent(style);
                    addDestinationIconSymbolLayer(style);
                }
            });

        }


    }

    public void locButtonClick(View view){

        centerOnUserLocation(17.0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        pm.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(myMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    public void goToList(View view){

        Intent goToRouteMenu = new Intent(this, SelectionScreenActivity.class);
        startActivity(goToRouteMenu);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
