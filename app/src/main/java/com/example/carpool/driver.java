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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class driver extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    GeoPoint loc;
    Users user;
    String bdan;

    AlertDialog dialog;



    List<Users> requesters=new ArrayList<>();
    boolean isRequest=false;

    Button req,cancel ;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference requestRef = db.collection("Requests");
    CollectionReference tripsRef = db.collection("Trips");

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver);
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

                                user = new Users(name, l, id, schoolId, false,phone,uid);
                                progressBar.setVisibility(View.GONE);
                                break;

                            }
                        }
                        show_user_location();
                    }

                });

    }

    int counter=0;
    public void RetrieveRequesters(String requestedId)
    {
        db.collection("Requests")
                .whereEqualTo("requestedId",requestedId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String uid = (String) document.getData().get("requesterId");
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

                                                        user = new Users(name, l, id, schoolId, false,phone,uid);
                                                        requesters.add(user);
                                                        if(counter==0)
                                                        {
                                                            show_requesters_location();
                                                            break;
                                                        }
                                                        break;

                                                    }
                                                }
                                            }

                                        });



                                //Toast.makeText(context, document.getData().get("requester"),Toast.LENGTH_LONG).show();

                            }
                        }
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
                    req.setVisibility(View.VISIBLE);
                    req.setText("accept");
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







    public void AcceptRequest(String requestedId, String requesterId,String schoolId) {
        Map<String, Object> trip = new HashMap<>();

        trip.put("CaptainId", requestedId);

        trip.put("ClientId", requesterId);
        trip.put("schoolId", schoolId);



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




        requestRef.whereEqualTo("requestedId",requestedId ).whereEqualTo("requesterId",requesterId)
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
        AcceptRequest(mAuth.getUid(),bdan,"FF");
        dialog.hide();

    }
    public void CancelRequest(View view) {
        DeleteRequest(mAuth.getUid(),bdan);
        dialog.hide();

    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        AlertDialog.Builder mbulder=new AlertDialog.Builder(driver.this);
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
        checkRequest(mAuth.getUid(),bdan);
        //dialog.show();
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera


        RetrieveRequesters(mAuth.getUid());


//        mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title("Marker in Sydney"));
        //      mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));
    }
    void show_user_location()
    {
        //  mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title(user.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));

    }


    void show_requesters_location()
    {
        for (Users requester:requesters)
        {

            mMap.addMarker(new MarkerOptions().position( new LatLng(requester.position.getLatitude(),requester.position.getLongitude())).title(requester.name+","+requester.phone+" school")).setTag(requester.uid);

            //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(driver.position.getLatitude(),driver.position.getLongitude())));
            mMap.setOnMarkerClickListener(this);
        }
    }



}
