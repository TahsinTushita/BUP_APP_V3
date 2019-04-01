package com.sust.bup_app_v3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = "";
    private boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 5f;

    private EditText mSearchText,cropsSearch;
    private ImageView mGps;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Marker marker;
    ArrayList<Address> addresses = new ArrayList<Address>();

    private HashMap<Marker, String> hs = new HashMap<>();
    String searchString,cropsSearchString;
    Geocoder geocoder,geocoder2;
    List<Address> list;
    DatabaseReference cropsDatabase,placesDatabase;
    Query cropsDatabaseQuery,placesDatabaseQuery;
    ArrayList<Crops> cropsArrayList;
    ArrayList<Places> placesArrayList;



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            //getDeviceLocation();
            //getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //mMap.getUiSettings().setMyLocationButtonEnabled(false);


            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker marker) {

                    marker.showInfoWindow();
                    return false;
                }
            });

//            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                @Override
//                public void onInfoWindowClick(Marker marker) {
//                    Intent intent = new Intent(MapActivity.this,Profile.class);
//                    intent.putExtra("profileID",hs.get(marker));
//                    if(hs.get(marker).equals(LoginActivity.user)) {
//                        intent.putExtra("from", "");
//                    }
//                    else intent.putExtra("from", "MapActivity");
//                    startActivity(intent);
////
//                }
//            });



            init();
            getCurrentLocation();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = (EditText) findViewById(R.id.inputsearch);
        mGps = (ImageView) findViewById(R.id.gpsid);
        cropsSearch = findViewById(R.id.inputsearch2);
        placesArrayList = new ArrayList<>();

        getLocationPermission();


    }

    private void init() {
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute method for searching
                    geolocate();

                }
                return false;
            }
        });

        cropsSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute method for searching
                    searchCrops();

                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDeviceLocation();
                getCurrentLocation();
            }
        });



        hideSoftKeyboard(MapActivity.this);
        drawNearby();
    }

    private void searchCrops(){
        cropsSearchString = cropsSearch.getText().toString().toLowerCase();
        cropsArrayList = new ArrayList<>();
        geocoder2 = new Geocoder(this);

        cropsDatabase = FirebaseDatabase.getInstance().getReference("Crops");
        cropsDatabaseQuery = cropsDatabase.orderByKey().equalTo(cropsSearchString);
        cropsDatabaseQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                cropsArrayList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Crops crops = snapshot.getValue(Crops.class);
                    cropsArrayList.add(crops);
                }
                try {
                    geocoder2 = new Geocoder(MapActivity.this);
                    addresses.clear();
                    mMap.clear();
                    myMarker = mMap.addMarker(userMarker);
                    String address;

                    for (int i = 0; i < cropsArrayList.size(); i++) {
                        address = cropsArrayList.get(i).getPlace();
                        addresses = (ArrayList<Address>) geocoder2.getFromLocationName(address, 1);

                        for(Address add: addresses){
                            double longitude = add.getLongitude();
                            double latitude = add.getLatitude();
                            MarkerOptions options = new MarkerOptions().position(new LatLng(latitude,longitude)).title(cropsArrayList.get(i).getPlace());
                            Marker marker = mMap.addMarker(options);
                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
//20.8103,90.4125
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        moveCam(new LatLng(20.8103,90.4125),5f);
    }

    private void geolocate() {
        searchString = mSearchText.getText().toString().toLowerCase();
        geocoder = new Geocoder(MapActivity.this);
        list = new ArrayList<>();
        placesDatabase = FirebaseDatabase.getInstance().getReference("Places");
        placesDatabaseQuery = placesDatabase.orderByKey().equalTo(searchString);
        placesDatabaseQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                placesArrayList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Places places = snapshot.getValue(Places.class);
                    placesArrayList.add(places);
                }

                try {
                    mMap.clear();
                    myMarker = mMap.addMarker(userMarker);
                    list = geocoder.getFromLocationName(searchString, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (list.size() > 0) {
                    Address address = list.get(0);
                    //Toast.makeText(MapActivity.this, address.toString(), Toast.LENGTH_SHORT).show();
                    moveCamera(placesArrayList,new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



    private void moveCamera(ArrayList places,LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (!title.equals("my location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            InfoWindowAdapter adapter = new InfoWindowAdapter(this);
            mMap.setInfoWindowAdapter(adapter);
            marker = mMap.addMarker(options);
            marker.setTag(places);
            marker.showInfoWindow();

        }
        hideSoftKeyboard(MapActivity.this);
    }



    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (!title.equals("my location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            marker = mMap.addMarker(options);
        }
        hideSoftKeyboard(MapActivity.this);
    }
    public void moveCam(LatLng latLng,float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        hideSoftKeyboard(MapActivity.this);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }

                    mLocationPermissionGranted = true;

                    //initialize the map

                    initMap();
                }
            }
        }
    }


    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    double latitude;
    double longitude;

    private MarkerOptions userMarker;
    private Marker myMarker;

    private void drawNearby() {

        ArrayList<Address> addresses = new ArrayList<Address>();
        String address = null;


    }

    private void getCurrentLocation(){

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission
                        (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {


        }
        else {

            // enable location buttons

            // fetch last location if any from provider - GPS.
            final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            final Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //if last known location is not available
            if (loc == null) {

                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {

                        // getting location of user
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        //do something with Lat and Lng
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        //when user enables the GPS setting, this method is triggered.
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        //when no provider is available in this case GPS provider, trigger your gpsDialog here.
                    }
                };

                //update location every 10sec in 500m radius with both provider GPS and Network.

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10*1000, 500, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 500, locationListener);
            }
            else {
                //do something with last known location.
                // getting location of user
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }
        }

        if(userMarker == null) {
            //mMap.addCircle(new CircleOptions().center(new LatLng(latitude,longitude)).radius(10000).strokeWidth(0f).fillColor(0xE6FFBEBE));

            userMarker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Current Location");
            myMarker = mMap.addMarker(userMarker);
            myMarker.showInfoWindow();
            //hs.put(myMarker,LoginActivity.user);
        }
        else {
            myMarker.remove();

            userMarker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Current Location");
            mMap.clear();
            //mMap.addCircle(new CircleOptions().center(new LatLng(latitude,longitude)).radius(10000).strokeWidth(0f).fillColor(0xE6FFBEBE));

            myMarker = mMap.addMarker(userMarker);
            myMarker.showInfoWindow();

            //hs.put(myMarker,LoginActivity.user);
        }
        //Toast.makeText(this,"lat:"+latitude+" long:"+longitude,Toast.LENGTH_SHORT).show();
        moveCamera(new LatLng(latitude, longitude),DEFAULT_ZOOM
                , "my location");

        drawNearby();
    }
}
