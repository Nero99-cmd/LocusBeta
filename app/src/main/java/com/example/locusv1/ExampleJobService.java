package com.example.locusv1;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
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
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.util.concurrent.BlockingDeque;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.example.locusv1.App.CHANNEL_ID;

public class ExampleJobService extends JobService  {

    DatabaseReference currentUserRef, friendUserRef;
    FirebaseUser currentUser;
    String currentUserID, friendsID;
    final List<String> allFriendsLIST = new ArrayList<>();

    interface Datasize{
        List<String> LISTTTT = new ArrayList<>();
    }

    private LocationManager locationManager;
    private LocationListener locationListener = new MyLocationListener();
    private String lat, log;
    private boolean gps_enable = false;
    private boolean network = false;

    private NotificationManagerCompat notificationManager;

    private Geocoder geocoder;
    private List<Address> addresses;

    private double LATITUDE;
    private double LONGITUDE;

    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;

    
    @Override
    public boolean onStartJob(JobParameters params) {

        currentUserRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = currentUser.getUid();
        friendUserRef = currentUserRef.child("Friends").child(currentUserID);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        notificationManager = NotificationManagerCompat.from(this);

        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(final JobParameters params){

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                everythingFunction();
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job Cancelled before complte");
        jobCancelled = true;
        return true;
    }

    public void everythingFunction(){
        allFriendsLIST.clear();
        friendUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final MainScreen mainScreen = new MainScreen();
                for (DataSnapshot ds: snapshot.getChildren()){
                    friendsID = ds.getKey();
                    allFriendsLIST.add(friendsID);

                }

                int size = allFriendsLIST.size();
                for (int i = 0; i < size; i ++){
                    ExampleThread thread = new ExampleThread(allFriendsLIST.get(i));
                    thread.start();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        int s = Datasize.LISTTTT.size();
        Log.d(TAG, "ID: " + s);

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

                currentUserRef.child("Users").child(currentUserID).child("Lat").setValue(LATITUDE);
                currentUserRef.child("Users").child(currentUserID).child("Lang").setValue(LONGITUDE);
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

    public void getMyLocation() {
        try {
            gps_enable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Toast.makeText(ExampleJobService.this, "Please Enable Location and try again", Toast.LENGTH_LONG).show();
        }
        try {
            network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Toast.makeText(ExampleJobService.this, "Please Enable Internet and try again", Toast.LENGTH_LONG).show();
        }
        if (!gps_enable && !network) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ExampleJobService.this);
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

    public void sendNotification(String name, int D){
        Intent intent = new Intent(getApplicationContext(),CallActivity.class);
        String v = "80865";
        intent.putExtra("Phone",v);

        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("LOCUS")
                .setContentText(name + " is nearby! " + D + " KM away" )
                .setSmallIcon(R.drawable.ic_home)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(1,notification);
    }

    class ExampleThread extends Thread{
        String fID;
        ExampleThread(String fID){
            this.fID = fID;
        }

        @Override
        public void run() {
            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    getMyLocation();
                    int STATUS = snapshot.child("Users").child(fID).child("Status").getValue(int.class);
                    double LAT1 = snapshot.child("Users").child(currentUserID).child("Lat").getValue(double.class);
                    double LANG1 = snapshot.child("Users").child(currentUserID).child("Lang").getValue(double.class);
                    double LAT2 = snapshot.child("Users").child(fID).child("Lat").getValue(double.class);
                    double LANG2 = snapshot.child("Users").child(fID).child("Lang").getValue(double.class);
                    String friendName = snapshot.child("Users").child(fID).child("Name").getValue(String.class);
                    if (STATUS == 1){
                       ExampleThreadtwo threadtwo = new ExampleThreadtwo(LAT1, LANG1, LAT2, LANG2, friendName);
                       threadtwo.start();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Log.d(TAG, "T1 complte");
        }
    }

    class ExampleThreadtwo extends Thread{
        double lt1,  lg1, lt2, lg2;
        String name;
        ExampleThreadtwo(double lt1, double lg1, double lt2, double lg2, String name ){
            this.lg1 = lg1;
            this.lg2 = lg2;
            this.lt1 = lt1;
            this.lt2 = lt2;
            this.name = name;
        }

        @Override
        public void run() {
            double distance = calcDist(lt1,lg1,lt2,lg2);
            int dist = (int) distance;
            if (dist < 10){
                sendNotification(name, dist);
            }
        }
    }

    public static double calcDist(double lat1, double lon1, double lat2, double lon2){
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
}
