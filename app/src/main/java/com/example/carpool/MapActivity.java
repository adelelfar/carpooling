package com.example.carpool;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener, RoutingListener {

    private GoogleMap mMap;
    int zoom =10;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    GeoPoint loc;
    Users user;
    List<Users> driversList=new ArrayList<>();
    List<Users> trips=new ArrayList<>();

    Map<String, String>  user_map = new HashMap<>();

    String bdan;

    String country;
    AlertDialog dialog;

    List<Users> requests=new ArrayList<>();


    Button req,cancel ;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference requestRef = db.collection("Requests");
    CollectionReference tripsRef = db.collection("Trips");

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        polylines=new ArrayList<>();

        progressBar=findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        req=findViewById(R.id.button4);

        client= LocationServices.getFusedLocationProviderClient(this);

        /**/

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
    int counter=0;
    public void RetrieveRequests()
    {
        db.collection("Requests")
                .whereEqualTo("requesterId",mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String uid = (String) document.getData().get("requestedId");
                                counter+=1;
                                db.collection("Users")
                                        .whereEqualTo("Uid",uid)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        counter-=1;
                                                        String id = (String) document.getData().get("id");
                                                        String uid = (String) document.getData().get("Uid");
                                                        GeoPoint l =  ((GeoPoint) document.getData().get("location"));
                                                        String schoolId = (String) document.getData().get("school");
                                                        String name = (String) document.getData().get("username");
                                                        String phone = (String) document.getData().get("phone");
                                                        boolean isDriver = (boolean) document.getData().get("is driver");
                                                        user = new Users(name, l, id, schoolId, isDriver,phone,uid);
                                                        user_map.put(uid,"request");
                                                        requests.add(user);
                                                        if(counter==0)
                                                        {
                                                            RetrieveTrips();
                                                            //   show_requests_location();
                                                            break;

                                                        }
                                                        break;

                                                    }
                                                }

                                            }

                                        });


                                //Toast.makeText(context, document.getData().get("requester"),Toast.LENGTH_LONG).show();

                            }
                            if(counter==0)
                            {
                                RetrieveTrips();
                            }
                        }
                    }
                });
    }


    public void RetrieveAll() {
        // c.showLoading();
        db.collection("Users").whereEqualTo("country",country)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Users users = new Users(String.valueOf(document.getData().get("username"))
                                        , ((GeoPoint) document.getData().get("location"))
                                        , String.valueOf(document.getData().get("id"))
                                        , String.valueOf(document.getData().get("school"))
                                        , (boolean) document.getData().get("is driver")
                                        ,String.valueOf(document.getData().get("phone"))
                                        , String.valueOf(document.getData().get("Uid")));
                                if(!users.uid.equals(mAuth.getUid())) {
                                    driversList.add(users);
                                }
                            }
                            show_all_location();


                        }

                    }
                });

    }


    public void RetrieveTrips()
    {
        db.collection("Trips")
                .whereEqualTo("ClientId",mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String uid = (String) document.getData().get("CaptainId");
                                counter += 1;
                                db.collection("Users")
                                        .whereEqualTo("Uid", uid)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        counter -= 1;
                                                        String id = (String) document.getData().get("id");
                                                        String uid = (String) document.getData().get("Uid");

                                                        GeoPoint l = ((GeoPoint) document.getData().get("location"));
                                                        String schoolId = (String) document.getData().get("school");
                                                        String name = (String) document.getData().get("username");
                                                        String phone = (String) document.getData().get("phone");
                                                        boolean isDriver = (boolean) document.getData().get("is driver");
                                                        user = new Users(name, l, id, schoolId, isDriver, phone, uid);
                                                        user_map.put(uid, "trip");
                                                        trips.add(user);
                                                        if (counter == 0) {
                                                            //   show_trips_location();
                                                            RetrieveAll();
                                                            break;
                                                        }
                                                        break;

                                                    }
                                                }

                                            }

                                        });

                            }
                                //Toast.makeText(context, document.getData().get("requester"),Toast.LENGTH_LONG).show();
                                if(counter==0)
                                {
                                    RetrieveAll();
                                }

                        }

                    }
                });
    }




    public void RetrieveUser()
    {
        db.collection("Users")
                .whereEqualTo("Uid",mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {


                                String id = (String) document.getData().get("id");
                                String uid = (String) document.getData().get("uid");

                                GeoPoint l =  ((GeoPoint) document.getData().get("location"));
                                String schoolId = (String) document.getData().get("school");
                                String name = (String) document.getData().get("username");
                                String phone = (String) document.getData().get("phone");
                            //    see_all = (boolean)document.getData().get("access all");
                                boolean isDriver = (boolean) document.getData().get("is driver");
                                country= (String) document.getData().get("country");

                                user = new Users(name, l, id, schoolId, false,phone,uid);
                                progressBar.setVisibility(View.GONE);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(user.position.getLatitude(),user.position.getLongitude() ),zoom ));
                                break;

                            }
                        }
                        RetrieveRequests();


                    }

                });

    }


    public void SendRequest(String requestedId, String requesterId,String schoolId) {


        Map<String, Object> request = new HashMap<>();

        request.put("requestedId",requestedId);
        request.put("schoolId",schoolId);
        request.put("requesterId",requesterId);

        requestRef.document().set(request, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("@", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("$", "Error writing document", e);
                    }
                });
    }





    public void DeleteRequest(String requestedId,String requesterId) {

        requestRef.whereEqualTo("requestedId", requestedId).whereEqualTo("requesterId", requesterId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        requestRef.document(document.getId()).delete();
                    }


                }
            }

        });
        tripsRef.whereEqualTo("CaptainId", requestedId).whereEqualTo("ClientId", requesterId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        tripsRef.document(document.getId()).delete();
                    }


                }
            }

        });

    }




    // on button request click
    public void MakeRequest(View view) {
        SendRequest(bdan,mAuth.getUid(),"FF");
        dialog.hide();
        mMap.clear();
        RetrieveUser();

    }
    public void CancelRequest(View view) {
        ereaspolilines();
        DeleteRequest(bdan,mAuth.getUid());
        dialog.hide();
        mMap.clear();
        user_map.remove(bdan);
        requests.clear();
        trips.clear();
        driversList.clear();
        RetrieveUser();

    }



    @Override
    public boolean onMarkerClick(Marker marker) {

        AlertDialog.Builder mbulder=new AlertDialog.Builder(MapActivity.this);
        View mview=getLayoutInflater().inflate(R.layout.driverinfo,null);
        String type=marker.getTitle();
        Toast.makeText(this,type,Toast.LENGTH_LONG).show();

        Users u = (Users) marker.getTag();

        TextView e=(TextView) mview.findViewById(R.id.textViewUsername);
        e.setText(u.name);
        TextView e1=(TextView) mview.findViewById(R.id.textViewSchool);
        e1.setText(u.schoolId);
        TextView e2=(TextView) mview.findViewById(R.id.textViewPhone);
        e2.setText(u.phone);

        bdan=u.uid;

        boolean have_car=u.haveCar;
        cancel=mview.findViewById(R.id.button6);

        req=mview.findViewById(R.id.button4);
        cancel.setVisibility(View.VISIBLE);
        //marker.setTitle(type+" from "+infos[0]);
        //Toast.makeText(this,bdan.id,Toast.LENGTH_LONG).show();
        mbulder.setView(mview);
        dialog=mbulder.create();

        if(type.equals("trip"))
        {
            cancel.setVisibility(View.VISIBLE);
            cancel.setText("cancel trip");
            req.setVisibility(View.INVISIBLE);

        }
        else if(type.equals("request"))
        {
            cancel.setVisibility(View.VISIBLE);
            req.setVisibility(View.INVISIBLE);
            cancel.setText("cancel request");
        }
       else if(type.equals("user"))
        {
            req.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.INVISIBLE);

        }
        if(!have_car)
        {
            req.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);
        }
      //  Toast.makeText(MapActivity.this,type,Toast.LENGTH_SHORT).show();
        //checkRequest(mAuth.getUid(),bdan);
        dialog.show();
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        RetrieveUser();


        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera





//        mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title("Marker in Sydney"));
        //      mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));
    }
    void show_user_location()
    {
        //  mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title(user.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));

    }

    void show_all_location()
    {
       // mMap.clear();

        for (Users request:requests)
        {
            mMap.addMarker(new MarkerOptions().position( new LatLng(request.position.getLatitude(),request.position.getLongitude())).title("request")).setTag(request);

            //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(driver.position.getLatitude(),driver.position.getLongitude())));
            mMap.setOnMarkerClickListener(this);
        }
        for (Users trip:trips)
        {
            mMap.addMarker(new MarkerOptions().position( new LatLng(trip.position.getLatitude(),trip.position.getLongitude())).title("trip")).setTag(trip);

            mMap.setOnMarkerClickListener(this);
        }
        for (Users driver:driversList)
        {
            if(!user_map.containsKey(driver.uid)) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(driver.position.getLatitude(), driver.position.getLongitude())).title("user")).setTag(driver);

                mMap.setOnMarkerClickListener(this);
            }

        }
    }
    void show_requests_location()
    {
       // mMap.clear();

    }
    void show_trips_location()
    {
       // mMap.clear();

    }
    private void getroutetodriver(GeoPoint clientloc, GeoPoint captainloc) {
        double lat = clientloc.getLatitude();
        double lng = clientloc.getLongitude ();
        LatLng start = new LatLng(lat, lng);
        lat = captainloc.getLatitude();
        lng = captainloc.getLongitude ();
        LatLng end = new LatLng(lat, lng);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build();
        routing.execute();
    }



    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRoutingCancelled() {

    }
    private void ereaspolilines()
    {
        for (Polyline polyline:polylines)
        {
            polyline.remove();
        }
        polylines.clear();
    }


    public void setting(View view) {
        Intent i = new Intent(MapActivity.this, SettingsActivity.class);
        i.putExtra("mAuth", mAuth.getUid());
        startActivity(i);
        finish();
    }
}