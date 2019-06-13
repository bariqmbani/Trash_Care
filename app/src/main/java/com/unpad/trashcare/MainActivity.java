package com.unpad.trashcare;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.unpad.trashcare.models.LokasiWarga;

import java.util.HashMap;
import java.util.Map;

import static com.unpad.trashcare.util.Constants.ERROR_DIALOG_REQUEST;
import static com.unpad.trashcare.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.unpad.trashcare.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "MainActivity";

    private Button btnFeedback, btnArtikel, btnAngkutSampah;
    private ImageView imgLogout;
    private TextView logout;


    String id;

    FirebaseFirestore db;

    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private LokasiWarga mLokasiWarga;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkMapServices();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        id = getIntent().getExtras().getString("ID");

        Log.d(TAG,id);

        checkMapServices();
        db = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnFeedback = findViewById(R.id.btnFeedback);
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
            }
        });

        btnArtikel = findViewById(R.id.btnArtikel);
        btnArtikel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ArtikelActivity.class));
            }
        });

        btnAngkutSampah = findViewById(R.id.btnAngkut);
        btnAngkutSampah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent i = new Intent(MainActivity.this, MapActivity.class);
                startActivity(i);*/
                requestConfirmation();
                Toast.makeText(MainActivity.this, "Tunggu", Toast.LENGTH_SHORT).show();
                getUserDetails();

            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        imgLogout = findViewById(R.id.imgLogout);
        logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profil) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getUserDetails() {

        if (mLokasiWarga == null) {
            mLokasiWarga = new LokasiWarga();

            DocumentReference userRef = db.collection("warga").document(id);

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user client.");

                        Warga warga = task.getResult().toObject(Warga.class);
                        mLokasiWarga.setWarga(warga);
                        ((UserClient)(getApplicationContext())).setWarga(warga);
                        getLastKnownLocation();
                    }
                }
            });
        }
        else {
            getLastKnownLocation();
        }

    }

    //get location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    mLokasiWarga.setGeo_point(geoPoint);
                    mLokasiWarga.setTimestamp(null);
                    saveUserLocation();
                }
            }
        });
    }
    //save location
    private void saveUserLocation() {

        if (mLokasiWarga != null) {

            DocumentReference locationRef = db.collection("Lokasi Warga").document(id);

            locationRef.set(mLokasiWarga).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database.");
                    }
                }
            });
        }

    }

    private void sendNotification(Warga warga) {
        String nama = warga.getNama();
        String alamat = warga.getAlamat();
        Map<String,Object> notification = new HashMap<>();
        notification.put("nama", nama);
        notification.put("alamat", alamat);
        notification.put("waktu", FieldValue.serverTimestamp());

        db.collection("pemberitahuan").document().set(notification);
    }

    private void requestConfirmation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah Anda ingin mengangkut sampah sekarang? ")
                .setCancelable(true)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Warga warga = ((UserClient)(getApplicationContext())).getWarga();
                        sendNotification(warga);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog confirmAlert = builder.create();
        confirmAlert.show();
    }


    //location permission
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Aplikasi TrashCare membutuhkan GPS agar bisa bekerja dengan baik. Ingin mengaktifkan GPS pada device Anda?")
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
;
            getUserDetails();
            return;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    //onNavigationItemSelected(item);
                    //onResume();
                    return;
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    private void logOut() {
        Toast.makeText(MainActivity.this, "Log Out", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        Intent out = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(out);
        finish();
    }

}
