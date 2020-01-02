package com.example.carpool;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener, RoutingListener {

    private GoogleMap mMap;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    GeoPoint loc;
    Users user;
    List<Users> driversList=new ArrayList<>();
    String bdan;
    String country;
    AlertDialog dialog;



    List<Users> customersList=new ArrayList<>();

    List<Users> requesters=new ArrayList<>();
    boolean isRequest=false;

    List<Users> requesteds=new ArrayList<>();

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



        //RetrieveUser(mAuth.getUid());
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


    public void RetrieveUser(String Uid)
    {
        db.collection("Users")
                .whereEqualTo("Uid",Uid)
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
                                boolean isDriver = (boolean) document.getData().get("is driver");
                                country= (String) document.getData().get("country");

                                user = new Users(name, l, id, schoolId, false,phone,uid);
                                progressBar.setVisibility(View.GONE);
                                break;

                            }
                        }
                        RetrieveAll();
                        show_user_location();
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


                                driversList.add(users);

                            }



                        }
                        if(driversList.size()<=0)
                        {
                            Toast.makeText(MapActivity.this,"No driver yet",Toast.LENGTH_LONG).show();
                        }
                        show_drivers_location();

                    }
                });

    }



    public void checkRequest(String requestedId,String requesterId)
    {
        isRequest=false;
        requestRef.whereEqualTo("requestedId", requestedId).whereEqualTo("requesterId", requesterId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        isRequest=true;
                        break;
                    }

                }
                if(isRequest)
                {
                    cancel.setVisibility(View.VISIBLE);
                    req.setVisibility(View.GONE);
                }
                else
                {
                    req.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.GONE);

                }
                dialog.show();
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




    public void AcceptRequest(Users requested,Users requester) {
        Map<String, Object> trip = new HashMap<>();

        trip.put("requestedNationalId", requested.nationalId);
        trip.put("requestedId", requested.uid);
        trip.put("requestedName", requested.name);
        trip.put("requestedLocation", requested.position);


        trip.put("requesterId", requester.uid);
        trip.put("requesterNationalId", requester.nationalId);
        trip.put("requesterName", requester.name);
        trip.put("requesterLocation", requester.position);
        trip.put("requesterSchoolId", requester.schoolId);



        tripsRef.document().set(trip, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("W", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("E", "Error writing document", e);
                    }
                });




        requestRef.whereEqualTo("requestedId", requested.uid).whereEqualTo("requesterId", requester.uid)
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


    }
    //yasta enta hna ?
    // msh sha3'ala :'( :' ana gyt  wrini tab  ndatha azay

    //RetrieveTripsForCustomer deh bos 3laha a7na kna 3mlnha swa deh nnyl eh b2a ana 7tat feha deh bas
    //hadef data be edy fel server ba3den negrb
    //ne call el function awel ma yefta7 bas ek moshkla en mafesh requests el data etmasa7t
//tab mt3mlha mn 3ndk b2a w ab2a oli a3bal md5ol al 7mam
    public void RetrieveTripsForCustomer(final String requesterId)
    {

        tripsRef.whereEqualTo("ClientId",requesterId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                GeoPoint clientloc= (GeoPoint) document.getData().get("ClientLoc");
                                GeoPoint captainloc= (GeoPoint) document.getData().get("captainLoc");
                                String requestedid = (String) document.getData().get("CaptainId");
                                getroutetodriver(clientloc,captainloc);
                                break;

                            }
                        }

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

    }




    // on button request click
    public void MakeRequest(View view) {
        SendRequest(bdan,mAuth.getUid(),"FF");
        dialog.hide();

    }
    public void CancelRequest(View view) {
        ereaspolilines();
        DeleteRequest(bdan,mAuth.getUid());
        dialog.hide();

    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        AlertDialog.Builder mbulder=new AlertDialog.Builder(MapActivity.this);
        View mview=getLayoutInflater().inflate(R.layout.driverinfo,null);
        String info=marker.getTitle();
        String infos[]=info.split(",");

        TextView e=(TextView) mview.findViewById(R.id.textView5);
        e.setText(infos[0]);
        TextView e1=(TextView) mview.findViewById(R.id.textView6);
        e1.setText(infos[1]);
        bdan=(String)marker.getTag();
        cancel=mview.findViewById(R.id.button6);

        req=mview.findViewById(R.id.button4);
        cancel.setVisibility(View.VISIBLE);
        //Toast.makeText(this,bdan.id,Toast.LENGTH_LONG).show();
        mbulder.setView(mview);
        dialog=mbulder.create();
        checkRequest(bdan,mAuth.getUid());
        //dialog.show();
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        RetrieveUser(mAuth.getUid());

        //RetrieveAll();
        RetrieveTripsForCustomer(mAuth.getUid());

        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera




// da el account bta3y ..ana customer wwenta el driver   astana 3awz a2olk 7aga astana hklmk oka aklmk phone ?





//        mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title("Marker in Sydney"));
        //      mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));
    }
    void show_user_location()
    {
        //  mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title(user.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));

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

    void show_drivers_location()
    {
        for (Users driver:driversList)
        {

            mMap.addMarker(new MarkerOptions().position( new LatLng(driver.position.getLatitude(),driver.position.getLongitude())).title(driver.name+","+driver.phone)).setTag(driver.uid);

            //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(driver.position.getLatitude(),driver.position.getLongitude())));
            mMap.setOnMarkerClickListener(this);
        }
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
    //ana 5last bas run ma mbnadish  3l fun asln bos
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
}