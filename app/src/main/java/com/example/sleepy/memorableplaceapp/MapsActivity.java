package com.example.sleepy.memorableplaceapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.sleepy.memorableplaceapp.R.id.date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = this.getSharedPreferences("com.example.sleepy.memorableplaceapp", Context.MODE_PRIVATE);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(getIntent().getIntExtra("position", Integer.MAX_VALUE) ==0){
            mMap.setOnMapLongClickListener(this);
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerOnMap(location, "Location Change, YOU ARE HERE");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if(Build.VERSION.SDK_INT < 23){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }else{
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                }else{
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                    try {
                        Location lastKnownLoca  = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        centerOnMap(lastKnownLoca, "Last Location");
                    }catch(Exception e) {
                        System.out.println("lastknownlocation error");
                    }
                }
            }
        }
        else if(getIntent().getIntExtra("position", Integer.MAX_VALUE) >0){
            int position = getIntent().getIntExtra("position", Integer.MAX_VALUE);
            LatLng latLng =  new LatLng(MainActivity.coordList.get(position).latitude,   MainActivity.coordList.get(position).longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(MainActivity.placesList.get(position)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,locationListener);
                try {
                    Location lastKnownLoca  = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerOnMap(lastKnownLoca, "Last Location");
                }catch(Exception e) {
                    System.out.println("lastknownlocation error");
                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String result = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if(addressList!=null && addressList.size() >0){
                System.out.println(addressList.get(0).getThoroughfare());
                System.out.println(addressList.get(0).getSubThoroughfare());

                if(addressList.get(0).getThoroughfare() != null){
                    if(addressList.get(0).getSubThoroughfare() != null){
                        result += addressList.get(0).getSubThoroughfare() + " ";
                    }
                    result += addressList.get(0).getThoroughfare() + " ";
                }else{
                    String timeDate = DateFormat.getDateTimeInstance().format(new Date());

                    result = timeDate;
                }

                mMap.addMarker(new MarkerOptions().position(latLng).title(result));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                MainActivity.coordList.add(latLng);
                MainActivity.placesList.add(result);
                MainActivity.arrayAdapter.notifyDataSetChanged();
                Toast.makeText(MapsActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();

                ArrayList<String> longList = new ArrayList<String>();
                ArrayList<String> latList = new ArrayList<String>();

                for(LatLng temp:MainActivity.coordList){
                    longList.add(Double.toString(temp.latitude));
                    latList.add(Double.toString(temp.latitude));
                }

                sharedPreferences.edit().putString("placesList", ObjectSerializer.serialize(MainActivity.placesList)).apply();
                sharedPreferences.edit().putString("longList", ObjectSerializer.serialize(longList)).apply();
                sharedPreferences.edit().putString("latList", ObjectSerializer.serialize(latList)).apply();


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            System.out.println("error with the shared preferences");
        }


    }

    public void centerOnMap (Location location, String title){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("title"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
        System.out.println(title);
    }

}
