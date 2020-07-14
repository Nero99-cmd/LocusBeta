package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.locusv1.App.CHANNEL_ID;

public class MainScreen extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    SupportMapFragment supportMapFragment;
    ProgressBar mapBar;

    private static final String TAG = "MainScreen";

    Button toProfile, toSearch, goOnline, goOffline, toNotification;
    TextView textViewStatus, textViewFFNB;

    private DatabaseReference databaseReference, listREf;
    private FirebaseUser firebaseUser;
    private String firebaseID;
    private NotificationManagerCompat notificationManager;

    public LocationManager locationManager;
    public LocationListener locationListener = new MyLocationListener();
    String lat, log, ed;
    private boolean gps_enable = false;
    private boolean network = false;

    Geocoder geocoder;
    List<Address> addresses;

    double LATITUDE;
    double LONGITUDE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        toProfile = findViewById(R.id.btnProfile);
        toSearch = findViewById(R.id.btnSearch);
        goOffline = findViewById(R.id.btnGoOffline);
        goOnline = findViewById(R.id.btnGoOnline);
        textViewStatus = findViewById(R.id.textViewSTATUS);
        textViewFFNB = findViewById(R.id.textViewfindingFriends);
        mapBar = findViewById(R.id.mapPB);
        toNotification = findViewById(R.id.btnNotification);

        notificationManager = NotificationManagerCompat.from(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseID = firebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        listREf = databaseReference.child("Friends").child(firebaseID);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        toSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchScreen.class));
            }
        });
        toNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),NotificationScreen.class));
            }
        });

        toProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(getApplicationContext(), ProfileScreen.class));
            }
        });

        goOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Users").child(firebaseID).child("Status").setValue(0);
                textViewStatus.setText("STATUS: OFFLINE");
                textViewFFNB.setVisibility(View.GONE);
                Toast.makeText(MainScreen.this, "You are OFFLINE", Toast.LENGTH_SHORT).show();
                stop();
            }
        });

        goOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Users").child(firebaseID).child("Status").setValue(1);
                textViewStatus.setText("STATUS: ONLINE");
                textViewFFNB.setVisibility(View.VISIBLE);
                Toast.makeText(MainScreen.this, "You are ONLINE", Toast.LENGTH_SHORT).show();
                getMyLocation();
                checkLocationPermission();
                start();

            }
        });

    }

    public void start(){

        ComponentName componentName = new ComponentName(this,ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "JOB SCHEDULED SUCCESFULLY");
        } else {
            Log.d(TAG, "JS FAiled");
        }
    }

    public void stop() {
    JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
    scheduler.cancel(123);
        Log.d(TAG, "JOB CANCELLED");
    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mapBar.setVisibility(View.VISIBLE);
        final List<String> allLIST = new ArrayList<>();
        listREf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ed = ds.getKey();
                    allLIST.add(ed);
                }

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (int i = 0; i < allLIST.size(); i++){
                            String neme = snapshot.child("Users").child(allLIST.get(i)).child("Name").getValue().toString();
                            double laxt = snapshot.child("Users").child(allLIST.get(i)).child("Lat").getValue(double.class);
                            double laxng = snapshot.child("Users").child(allLIST.get(i)).child("Lang").getValue(double.class);

                            LatLng frendPos = new LatLng(laxt,laxng);
                            map.addMarker(new MarkerOptions().position(frendPos).title(neme));
                        }

                        String myName = snapshot.child("Users").child(firebaseID).child("Name").getValue().toString();
                        double lxt = snapshot.child("Users").child(firebaseID).child("Lat").getValue(double.class);
                        double lxng = snapshot.child("Users").child(firebaseID).child("Lang").getValue(double.class);

                        map = googleMap;
                        LatLng currentUserPos = new LatLng(lxt,lxng);
                        map.addMarker(new MarkerOptions().position(currentUserPos).title("MEee"));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPos,15));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                map = googleMap;
                LatLng currentUserPos = new LatLng(13.1720715,77.6066401);
                map.addMarker(new MarkerOptions().position(currentUserPos).title("MEee"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPos,11));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mapBar.setVisibility(View.INVISIBLE);

    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                locationManager.removeUpdates(locationListener);
                lat = "" + location.getLatitude();
                log = "" + location.getLongitude();

                LATITUDE = Double.parseDouble(lat);
                LONGITUDE = Double.parseDouble(log);

                databaseReference.child("Users").child(firebaseID).child("Lat").setValue(LATITUDE);
                databaseReference.child("Users").child(firebaseID).child("Lang").setValue(LONGITUDE);

                geocoder = new Geocoder(MainScreen.this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String add = addresses.get(0).getAddressLine(0);
            }
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
    }

    public boolean checkLocationPermission() {
        int location1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int location2 = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissions = new ArrayList<>();

        if(location1 != PackageManager.PERMISSION_GRANTED){
            listPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(location2 != PackageManager.PERMISSION_GRANTED){
            listPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if(!listPermissions.isEmpty()){
            ActivityCompat.requestPermissions(this,listPermissions.toArray(new String[listPermissions.size()]),1);
        }

        return true;

    }

    public void getMyLocation() {
        try {
            gps_enable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Toast.makeText(MainScreen.this, "Please Enable Location and try again", Toast.LENGTH_LONG).show();
        }
        try {
            network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Toast.makeText(MainScreen.this, "Please Enable Internet and try again", Toast.LENGTH_LONG).show();
        }
        if (!gps_enable && !network) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainScreen.this);
            builder.setTitle("Attention");
            builder.setMessage("Please check if internet/location are active and TRY AGAIN");
            builder.create().show();
        }

        if (gps_enable) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }

        if (network){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }

    }

    public static double distance(double lat1, double lon1, double lat2, double lon2){
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;

            dist = dist * 1.609344;

            return (dist);
        }
    }

    public double sendDistance(double LAT1, double LANG1, double LAT2, double LANG2){
        double returnCode = distance(LAT1,LANG1,LAT2,LANG2);
        return returnCode;
    }
    //String id = "AIzaSyAQ-kM-jxQelt-N_7no71z0GClG7KjHB9k";

}