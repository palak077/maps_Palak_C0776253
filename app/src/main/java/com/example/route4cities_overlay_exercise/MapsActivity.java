package com.example.route4cities_overlay_exercise;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final int REQUEST_CODE = 1;

    Polygon shape;

    private static final int POLYGON_SIDES = 4;

    List<Marker> markers = new ArrayList();
    List<Marker> distanceMarkers = new ArrayList<>();

    ArrayList<Polyline> listOfPolyline = new ArrayList<>();

    Marker dragMarker;

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        //mMap.setOnCameraMoveListener(this);

        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            //the above method gets called automatically once your location has been changed..
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            {
            startUpdateLocation();
            LatLng initialLocToronto = new LatLng(43.651070, -79.347015);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocToronto, 5));

            //to add the marker at the default location of user given here - Toronto
//                MarkerOptions options = new MarkerOptions().position(initialLocToronto)
//                        .draggable(true)
//                        .title("Current Location , By Default")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
//                        //   .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
//                        .snippet("User is here");
//                Marker marker = mMap.addMarker(options);
//                markers.add(marker);
        }

        //implement map drag listener
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                dragMarker = marker;
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            //as marker is no longer part of polygon - erase the data
            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (markers.size() == POLYGON_SIDES) {
                    for (Polyline line : listOfPolyline)
                    {
                        line.remove();
                    }
                    listOfPolyline.clear();
                    shape.remove();
                    shape = null;
                    for (Marker currMarker : distanceMarkers)
                    {
                        currMarker.remove();
                    }
                    distanceMarkers.clear();
                    drawShape();
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng)
            {
                setMarker(latLng);
            }
        });
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    private void setMarker(LatLng latLng)
    {
        MarkerOptions options = new MarkerOptions().position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        //   .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
               // .snippet("Snippet here");
             //   .title("title");

        // check if there are already the same number of markers, we clear the map
        if (markers.size() == POLYGON_SIDES)
        {
            clearMap();
        }
        Marker marker = mMap.addMarker(options);
        markers.add(marker);

        if(markers.size() == 1)
        {
            marker.setTitle("A");
        }
        else if(markers.size() == 2)
        {
            marker.setTitle("B");
        }
        else if(markers.size() == 3)
        {
            marker.setTitle("C");
        }
        else if (markers.size() == 4)
        {
            marker.setTitle("D");
        }

        System.out.println("After the marker is added");
        if (markers.size() == POLYGON_SIDES)
        {
            drawShape();
        }
    }

    private void clearMap()
    {
        for (Marker marker : markers)
        {
            marker.remove();
        }
        markers.clear();
        for (Polyline line : listOfPolyline)
        {
            line.remove();
        }
        listOfPolyline.clear();

        shape.remove();
        shape = null;
        for (Marker marker : distanceMarkers)
        {
            marker.remove();
        }
        distanceMarkers.clear();
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        System.out.println("Long click on the map");

//        if (markers.size() == 0)
//        {
//            return;
//        }
//        double minDistance = Double.MAX_VALUE;
//        Marker nearestMarker = null;
//
//        for (Marker marker : markers) {
//            double currDistance = distance(marker.getPosition().latitude,
//                    marker.getPosition().longitude,
//                    latLng.latitude,
//                    latLng.longitude);
//            if (currDistance < minDistance) {
//                minDistance = currDistance;
//                nearestMarker = marker;
//            }
//        }
//
//        if (nearestMarker != null) {
//            final Marker finalNearestMarker = nearestMarker;
//            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
//            deleteDialog
//                    .setTitle("remove marker ?")
//                    .setMessage("do you want to remove the marker?")
//
//                    // Specifying a listener allows you to take an action before dismissing the dialog.
//                    // The dialog is automatically dismissed when a dialog button is clicked.
//                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // Continue with delete operation
//                            finalNearestMarker.remove();
//                            markers.remove(finalNearestMarker);
//
//                            charactersABCD.remove(markerLabelMap.get(finalNearestMarker.getPosition()));
//                            markerLabelMap.remove(finalNearestMarker);
//
//                            for (Polyline polyline : listOfPolyline) {
//                                polyline.remove();
//                            }
//                            listOfPolyline.clear();
//
//                            if (shape != null) {
//                                shape.remove();
//                                shape = null;
//                            }
//
//                            for (Marker currMarker : distanceMarkers) {
//                                currMarker.remove();
//                            }
//                            distanceMarkers.clear();
//
//                        }
//                    })
//                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            finalNearestMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                            // finalNearestMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
//
//                        }
//                    });
//            AlertDialog dialog = deleteDialog.create();
//            dialog.show();
//        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2)
    {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void startUpdateLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location)
    {
        currentLocation = location;
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("CURRENT LOCATION")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_image))
                .snippet("User is here");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }
    //distanceFromUser()
    private void distanceFromUser(LatLng StartP, LatLng EndP)
    {
        Location loc1 = new Location("");

        loc1.setLatitude(StartP.latitude);
        loc1.setLongitude(StartP.longitude);

        Location loc2 = new Location("");

        loc2.setLatitude(EndP.latitude);
        loc2.setLongitude(EndP.longitude);

        float distance = loc1.distanceTo(loc2);

        System.out.println(" Distance between marker and user is:" + distance + "\n");
        Toast.makeText(this, "Distance between marker and user is:" + distance, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon)
    {
        List points = polygon.getPoints();

        Location loc1 = new Location("");
        LatLng l1 = (LatLng) points.get(0);
        loc1.setLatitude(l1.latitude);
        loc1.setLongitude(l1.longitude);

        Location loc2 = new Location("");
        LatLng l2 = (LatLng) points.get(1);
        loc2.setLatitude(l2.latitude);
        loc2.setLongitude(l2.longitude);

        Location loc3 = new Location("");
        LatLng l3 = (LatLng) points.get(2);
        loc2.setLatitude(l3.latitude);
        loc2.setLongitude(l3.longitude);

        Location loc4 = new Location("");
        LatLng l4 = (LatLng) points.get(3);
        loc2.setLatitude(l4.latitude);
        loc2.setLongitude(l4.longitude);

        float distance = loc1.distanceTo(loc2) + loc2.distanceTo(loc3) + loc3.distanceTo(loc4) + loc4.distanceTo(loc1);

        System.out.println("Distance covered by polygon:" + distance + "\n");
        Toast.makeText(this, "Distance covered by polygon:" + distance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolylineClick(Polyline polyline)
    {
        System.out.println("poly line clicked ");
        List points = polyline.getPoints();

        Location loc1 = new Location("");
        LatLng l1 = (LatLng) points.get(0);

        loc1.setLatitude(l1.latitude);
        loc1.setLongitude(l1.longitude);

        Location loc2 = new Location("");
        LatLng l2 = (LatLng) points.get(1);

        loc2.setLatitude(l2.latitude);
        loc2.setLongitude(l2.longitude);

        float distance = loc1.distanceTo(loc2);
        System.out.println(" Distance between two cities:" + distance + "\n");
        Toast.makeText(this, "Distance between two cities:" + distance, Toast.LENGTH_SHORT).show();
    }

    private void drawShape()
    {
        PolygonOptions options = new PolygonOptions()
                //shape fill is green color with 35% transparency
                .fillColor(Color.argb(59, 0, 128, 0))
                .strokeColor(Color.RED);

        LatLng[] markersConvex = new LatLng[POLYGON_SIDES];
        for (int i = 0; i < POLYGON_SIDES; i++) {
            markersConvex[i] = new LatLng(markers.get(i).getPosition().latitude,
                    markers.get(i).getPosition().longitude);
        }

        Vector<LatLng> sortedLatLong = createLoc.convexHull(markersConvex, POLYGON_SIDES);
        Vector<LatLng> sortedLatLong2 = new Vector<>();

        int leftM = 0;
        for (int i = 0; i < markers.size(); i++)
            if (markers.get(i).getPosition().latitude < markers.get(leftM).getPosition().latitude)
                leftM = i;

        Marker currentMarker = markers.get(leftM);
        sortedLatLong2.add(currentMarker.getPosition());

        // Printing the first available marker
        System.out.println(currentMarker.getPosition());

        while (sortedLatLong2.size() != POLYGON_SIDES)
        {
            double minDistance = Double.MAX_VALUE;
            Marker nearestMarker = null;
            for (Marker marker : markers)
            {
                if (sortedLatLong2.contains(marker.getPosition()))
                {
                    continue;
                }
                double curDistance = distance(currentMarker.getPosition().latitude,
                        currentMarker.getPosition().longitude,
                        marker.getPosition().latitude,
                        marker.getPosition().longitude);

                if (curDistance < minDistance)
                {
                    minDistance = curDistance;
                    nearestMarker = marker;
                }
            }

            if (nearestMarker != null) {
                sortedLatLong2.add(nearestMarker.getPosition());
                currentMarker = nearestMarker;
            }
        }
        //printing the sorted list of markers
        System.out.println(sortedLatLong);

        options.addAll(sortedLatLong);
        shape = mMap.addPolygon(options);
        shape.setClickable(true);

        LatLng[] pointsOnLine = new LatLng[sortedLatLong.size() + 1];
        int index = 0;
        for (LatLng x : sortedLatLong)
        {
            pointsOnLine[index] = x;
            index++;
            if (index == sortedLatLong.size())
            {
                pointsOnLine[index] = sortedLatLong.elementAt(0);
            }
        }
        for (int i = 0; i < pointsOnLine.length - 1; i++)
        {
            LatLng[] tempArr = {pointsOnLine[i], pointsOnLine[i + 1]};
            Polyline currentPolyline = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(tempArr)
                    .color(Color.RED));
            currentPolyline.setClickable(true);
            listOfPolyline.add(currentPolyline);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(this, "Marker is dragged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }

    //this should not be called in main UI thread
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onMarkerClick(Marker markerIcon)
    {
        System.out.println("Marker is being  Clicked" + markerIcon.isInfoWindowShown() + "\n");
        if (markerIcon.isInfoWindowShown())
        {
            markerIcon.hideInfoWindow();
        }
        else
            {
                markerIcon.showInfoWindow();
        }

        Geocoder geoCoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        Address addressOfMarker = null;
        //have to use list for many markers
        //List<Address> addresses;

        LatLng isLatitudeAndLongitude = markerIcon.getPosition();

//        try
//        {
//            List<Address> matches = geoCoder.getFromLocation(isLatitudeAndLongitude.latitude, isLatitudeAndLongitude.longitude, 1);
//            addressOfMarker = (matches.isEmpty() ? null : matches.get(0));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        ArrayList<String> addressString = new ArrayList<>();
//
//        if (addressOfMarker != null)
//        {
//            if (addressOfMarker.getThoroughfare() != null)
//            {
//                addressString.add(addressOfMarker.getThoroughfare());
//            }
//            if (addressOfMarker.getPostalCode() != null) {
//                addressString.add(addressOfMarker.getPostalCode());
//            }
//            if (addressString.isEmpty()) {
//                addressString.add("Unknown Location");
//            }
//            if (addressOfMarker.getLocality() != null) {
//                addressString.add(addressOfMarker.getLocality());
//            }
//            if (addressOfMarker.getAdminArea() != null) {
//                addressString.add(addressOfMarker.getAdminArea());
//            }
//        }
//        System.out.println("ADDRESS OF MARKER  IS" + "  " +  addressOfMarker.getThoroughfare() + "  " + addressOfMarker.getLocality() + "  " + addressOfMarker.getCountryName() + "  " + addressOfMarker.getPostalCode() + "\n");
//        Toast.makeText(MapsActivity.this,"ADDRESS OF MARKER IS:" + "  " +  addressOfMarker.getThoroughfare() + "  " +  addressOfMarker.getLocality() + "  " + addressOfMarker.getCountryName() + "  " +  addressOfMarker.getPostalCode() ,Toast.LENGTH_LONG).show();

        if(currentLocation != null)
        {
            Location location = new Location(LocationManager.GPS_PROVIDER);

            location.setLatitude( isLatitudeAndLongitude.latitude);
            location.setLongitude(isLatitudeAndLongitude.longitude);

            double distance = currentLocation.distanceTo(location);
            markerIcon.setSnippet("Distance b/w marker and user :  " + distance );
        }

        return  false;
    }
}


// marker.setDraggable(true);