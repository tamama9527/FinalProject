package devlight.io.sample;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import dalvik.annotation.TestTargetClass;
import devlight.io.sample.GPSTracker;
import devlight.io.sample.R;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class googlemap extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private String group_name;
    private GoogleMap mMap;
    private LatLngBounds.Builder builder;
    private FirebaseUser user;
    private double[] lat;
    private double[] lng;
    private int count;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myRef = database.getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        group_name=getIntent().getExtras().getString("group");
        user = FirebaseAuth.getInstance().getCurrentUser();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            public void onMapLoaded() {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i=0;
                        count=(int) dataSnapshot.child("group").child(group_name).child("member").getChildrenCount()-1;
                        lat=new double[count];
                        lng=new double[count];
                        for(DataSnapshot user_token:dataSnapshot.child("group").child(group_name).child("member").getChildren()){
                            if(user_token.getValue().toString().equals(user.getUid().toString())==false){
                                lat[i]=(double)dataSnapshot.child("member").child(user_token.getValue().toString()).child("location").child("lat").getValue();
                                lng[i]=(double)dataSnapshot.child("member").child(user_token.getValue().toString()).child("location").child("lng").getValue();
                                i++;
                            }
                        }
                        start(mMap);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                start(mMap);
            }
        });
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    public void start(final GoogleMap map) {
        builder = new LatLngBounds.Builder();
        for(int i=0;i<count;i++){
            MarkerOptions loc = new MarkerOptions().position(new LatLng(lat[i], lng[i]));
            map.addMarker(loc);
            builder.include(loc.getPosition());
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation != null) {
            builder.include(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
            Log.d("my_lat",String.valueOf(myLocation.getLatitude()));
            Log.d("my_lng",String.valueOf(myLocation.getLongitude()));
            myRef.child("member").child(user.getUid()).child("location").child("lat").setValue(myLocation.getLatitude());
            myRef.child("member").child(user.getUid()).child("location").child("lng").setValue(myLocation.getLongitude());
            LatLngBounds bounds = builder.build();
            int padding = 150; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener(){

                @Override
                public void onMyLocationChange(Location location) {
                    myRef.child("member").child(user.getUid()).child("location").child("lat").setValue(location.getLatitude());
                    myRef.child("member").child(user.getUid()).child("location").child("lng").setValue(location.getLongitude());
                }
            });
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        this.finish();
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}