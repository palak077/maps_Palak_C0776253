package com.example.route4cities_overlay_exercise;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMapLongClickListener ,  GoogleMap.OnMapClickListener{

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;

    Polygon shape;

    private static final int POLYGON_SIDES = 4;

    List<Marker> markers = new ArrayList();
    List<Marker> distanceMarkers = new ArrayList<>();

    ArrayList<Polyline> listOfPolylines = new ArrayList<>();
    List<Marker> tagCity = new ArrayList<>();

    ArrayList<Character> charactersABCD = new ArrayList<>();
    HashMap<LatLng, Character> markerLabelMap = new HashMap<>();

    Marker dragMarker;

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        locationListener = new LocationListener()
        {
            @Override
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
        else{
            startUpdateLocation();
            LatLng canadaCenterLatLong = new LatLng( 43.651070,-79.347015);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canadaCenterLatLong, 5));}


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                System.out.println("marker Clicked"+marker.isInfoWindowShown());
                if(marker.isInfoWindowShown()){
                    marker.hideInfoWindow();
                }
                else{
                    marker.showInfoWindow();
                }
                return true;
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
        {
            @Override
            public void onMarkerDragStart(Marker marker) {

                dragMarker = marker;

            }
            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (markers.size() == POLYGON_SIDES) {
                    for (Polyline line : listOfPolylines) {
                        line.remove();
                    }
                    listOfPolylines.clear();

                    shape.remove();
                    shape = null;

                    for (Marker currMarker : distanceMarkers) {
                        currMarker.remove();
                    }
                    distanceMarkers.clear();
                    drawShape();
                }
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarker(latLng);



            }




        });

        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    private void setMarker (LatLng latLng){

        Geocoder geoCoder = new Geocoder(this);
        Address address = null;

        try
        {
            List<Address> matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = (matches.isEmpty() ? null : matches.get(0));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String title = "";
        String snippet = "";

        ArrayList<String> titleString = new ArrayList<>();
        ArrayList<String> snippetString = new ArrayList<>();

        if(address != null){
            if(address.getSubThoroughfare() != null)
            {
                titleString.add(address.getSubThoroughfare());

            }
            if(address.getThoroughfare() != null)
            {

                titleString.add(address.getThoroughfare());

            }
            if(address.getPostalCode() != null)
            {

                titleString.add(address.getPostalCode());

            }
            if(titleString.isEmpty())
            {
                titleString.add("Unknown Location");
            }
            if(address.getLocality() != null)
            {
                snippetString.add(address.getLocality());

            }
            if(address.getAdminArea() != null)
            {
                snippetString.add(address.getAdminArea());
            }

        }

        title = TextUtils.join(", ",titleString);
        title = (title.equals("") ? "  " : title);

        snippet = TextUtils.join(", ",snippetString);

        MarkerOptions options = new MarkerOptions().position(latLng)
                .draggable(true)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                //   .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .snippet(snippet);

        // check if there are already the same number of markers, we clear the map
        if (markers.size() == POLYGON_SIDES)
        {
            clearMap();
        }

        Marker mm = mMap.addMarker(options);
        markers.add(mm);

        if (markers.size() == POLYGON_SIDES) {
            drawShape();
        }

        System.out.println("mmmmmm");
        // Add city Label Marker

        Character cityLetters = 'A';
        Character[] arr = {'A','B','C','D'};
        for(Character letter: arr){
            if(charactersABCD.contains(letter)){
                continue;
            }
            cityLetters = letter;
            break;
        }

        LatLng labelLatLng = new LatLng(latLng.latitude - 0.55,latLng.longitude);
        MarkerOptions optionsCityLabel = new MarkerOptions().position(labelLatLng)
                .draggable(false)
                //.icon(displayText(cityLetters.toString()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .snippet(snippet);
        Marker letterMarker = mMap.addMarker(optionsCityLabel);

        tagCity.add(letterMarker);
        charactersABCD.add(cityLetters);
        markerLabelMap.put(letterMarker.getPosition(),cityLetters);
    }

    private void clearMap() {



        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();

        for(Polyline line: listOfPolylines){
            line.remove();
        }
        listOfPolylines.clear();

        shape.remove();
        shape = null;

        for (Marker marker : distanceMarkers) {
            marker.remove();
        }
        distanceMarkers.clear();

        for( Marker marker: tagCity){
            marker.remove();
        }
        tagCity.clear();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        if(markers.size() == 0){
            return;
        }
        double minDistance = Double.MAX_VALUE;
        Marker nearestMarker = null;

        for(Marker marker: markers){
            double currDistance = distance(marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    latLng.latitude,
                    latLng.longitude);
            if(currDistance < minDistance){
                minDistance = currDistance;
                nearestMarker = marker;
            }
        }

        if(nearestMarker != null){
            final Marker finalNearestMarker = nearestMarker;
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
            deleteDialog
                    .setTitle("remove marker ?")
                    .setMessage("do you want to remove the marker?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            finalNearestMarker.remove();
                            markers.remove(finalNearestMarker);

                            charactersABCD.remove(markerLabelMap.get(finalNearestMarker.getPosition()));
                            markerLabelMap.remove(finalNearestMarker);

                            for(Polyline polyline: listOfPolylines){
                                polyline.remove();
                            }
                            listOfPolylines.clear();

                            if(shape != null){
                                shape.remove();
                                shape = null;
                            }

                            for(Marker currMarker: distanceMarkers){
                                currMarker.remove();
                            }
                            distanceMarkers.clear();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            finalNearestMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            // finalNearestMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

                        }
                    });
            AlertDialog dialog = deleteDialog.create();
            dialog.show();
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
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

    private String getAddress(Location location){

        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> locationAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(locationAddress != null && locationAddress.size() > 0){

                if(locationAddress.get(0).getSubThoroughfare() != null)
                    address += locationAddress.get(0).getSubThoroughfare() + " ";
                if(locationAddress.get(0).getThoroughfare() != null)
                    address += locationAddress.get(0).getThoroughfare() + " ";
                if(locationAddress.get(0).getPostalCode() != null)
                    address += locationAddress.get(0).getLocality();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        return address;

    }



    private String getCity(Location location){

        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> locationAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(locationAddress != null && locationAddress.size() > 0){

                if(locationAddress.get(0).getAdminArea() != null)
                    address += locationAddress.get(0).getAddressLine(0) + " ";
                if(locationAddress.get(0).getThoroughfare() != null)
                    address += locationAddress.get(0).getAddressLine(1) + " ";

            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        return address;

    }


    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        /*Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setHomeMarker(lastKnownLocation);*/
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        mMap.addMarker(options);

//        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
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
    public void onPolygonClick(Polygon polygon) {
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

        System.out.println("Distance - polygon." + distance);

        Toast.makeText(this, "Total Distance= " + distance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        System.out.println("poly line click");
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
        System.out.println(" Distance of the polyline" + distance);
        Toast.makeText(this, "Distance= " + distance, Toast.LENGTH_SHORT).show();

    }

    private void drawShape()
    {
        PolygonOptions options = new PolygonOptions()
                .fillColor(Color.argb(12, 0, 255, 0))
                .strokeColor(Color.GREEN);

        LatLng[] markersConvex = new LatLng[POLYGON_SIDES];
        for (int i = 0; i < POLYGON_SIDES; i++) {
            markersConvex[i] = new LatLng(markers.get(i).getPosition().latitude,
                    markers.get(i).getPosition().longitude);
        }

        Vector<LatLng> sortedLatLong = createLoc.convexHull(markersConvex, POLYGON_SIDES);
        Vector<LatLng> sortedLatLong2 =  new Vector<>();

        int leftM = 0;
        for (int i = 0; i < markers.size(); i++)
            if (markers.get(i).getPosition().latitude < markers.get(leftM).getPosition().latitude)
                leftM = i;

        Marker currentMarker = markers.get(leftM);
        sortedLatLong2.add(currentMarker.getPosition());
        System.out.println(currentMarker.getPosition());
        while(sortedLatLong2.size() != POLYGON_SIDES){
            double minDistance = Double.MAX_VALUE;
            Marker nearestMarker  = null;
            for(Marker marker: markers){
                if(sortedLatLong2.contains(marker.getPosition())){
                    continue;
                }

                double curDistance = distance(currentMarker.getPosition().latitude,
                        currentMarker.getPosition().longitude,
                        marker.getPosition().latitude,
                        marker.getPosition().longitude);

                if(curDistance < minDistance){
                    minDistance = curDistance;
                    nearestMarker = marker;
                }
            }

            if(nearestMarker != null){
                sortedLatLong2.add(nearestMarker.getPosition());
                currentMarker = nearestMarker;
            }
        }
        System.out.println(sortedLatLong);

        // add polygon as per convex hull lat long
        options.addAll(sortedLatLong);
        shape = mMap.addPolygon(options);
        shape.setClickable(true);

        // draw the polyline too
        LatLng[] pointsOnLine = new LatLng[sortedLatLong.size() + 1];
        int index = 0;
        for (LatLng x : sortedLatLong) {
            pointsOnLine[index] = x;

            index++;
            if (index == sortedLatLong.size()) {

                pointsOnLine[index] = sortedLatLong.elementAt(0);
            }
        }

        for(int i =0 ; i<pointsOnLine.length -1 ; i++){

            LatLng[] tempArr = {pointsOnLine[i], pointsOnLine[i+1] };
            Polyline currentPolyline =  mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(tempArr)
                    .color(Color.RED));
            currentPolyline.setClickable(true);
            listOfPolylines.add(currentPolyline);
        }
    }
    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}