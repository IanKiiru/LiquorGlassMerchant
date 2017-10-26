package com.liquor.kiiru.liquorglassmerchant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.liquor.kiiru.liquorglassmerchant.Common.Common;

import java.util.List;

import static com.liquor.kiiru.liquorglassmerchant.MainActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    private DatabaseReference locationRef;
    Location lastLocation;
    private GoogleMap mMap;
    private TextView txtFullName, mCustomerName, mCustomerPhone, mCustomerDestination;
    private String customerId = "";
    private Boolean isLoggingOut = false;
    private LinearLayout mCustomerInfo;

    private ImageView mCustomerProfileImage;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    mapFragment.getMapAsync(this);
                    if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission to access your location denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home Merchant");
        setSupportActionBar(toolbar);

        locationRef = FirebaseDatabase.getInstance().getReference();
        mapFragment = SupportMapFragment.newInstance();
        android.support.v4.app.FragmentManager sFm = getSupportFragmentManager();
        sFm.beginTransaction().add(R.id.mapMerchant, mapFragment).commit();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            mapFragment.getMapAsync(this);
        }

        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);

        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);

        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        txtFullName = (TextView) headerLayout.findViewById(R.id.textFullName);
        txtFullName.setText(Common.currentUser.getfName());


    }

    private void getAssignedCustomer(){
        String merchantId = Common.currentUser.getPhone();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Merchants").child(merchantId).child("customerOrderId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();

                } else {
                    customerId = "";
                    if (deliveryLocationMarker != null) {
                        deliveryLocationMarker.remove();
                    }
                    if (assignedCustomerDeliveryLocationRefListener != null){
                        assignedCustomerDeliveryLocationRef.removeEventListener(assignedCustomerDeliveryLocationRefListener);
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    Marker deliveryLocationMarker;
    private DatabaseReference assignedCustomerDeliveryLocationRef;
    private ValueEventListener assignedCustomerDeliveryLocationRefListener;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerDeliveryLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerDeliveryLocationRefListener = assignedCustomerDeliveryLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng merchantLatLng = new LatLng(locationLat,locationLng);
                    deliveryLocationMarker = mMap.addMarker(new MarkerOptions().position(merchantLatLng).title("Delivery location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_customer)));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem itemSwitch = menu.findItem(R.id.my_switch);
        itemSwitch.setActionView(R.layout.user_switch);
        final SwitchCompat userSwitch = (SwitchCompat) menu.findItem(R.id.my_switch).getActionView().findViewById(R.id.online_switch);
        userSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(getApplicationContext()!=null){
                    userSwitch.setText("Online  ");
                    String userId = Common.currentUser.getPhone();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("merchantsAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("merchantsWorking");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (customerId) {
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                            break;
                    }


                } else if (isChecked == false) {
                    userSwitch.setText("Offline  ");
                    disconnectMerchant();
                }
            }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_orders) {
            // Handle the camera action
        } else if (id == R.id.nav_account) {
            Intent profileIntent = new Intent(Home.this, UserProfile.class);
            startActivity(profileIntent);

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_logout) {
            disconnectMerchant();
            Intent signOut_intent = new Intent(Home.this, MerchantLoginActivity.class);
            signOut_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signOut_intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);


        }
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
        getAssignedCustomer();

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }



    private void disconnectMerchant(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userId = Common.currentUser.getPhone();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("merchantsAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            lastLocation = location;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_merchant)));
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, 15);
            mMap.animateCamera(update);
        }



}
