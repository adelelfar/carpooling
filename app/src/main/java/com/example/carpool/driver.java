package com.example.carpool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class driver extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    GeoPoint loc;
    Users user;
    String bdan;
    String last;
    RoundKornerRelativeLayout requestlay;
    boolean fristtime=true;
    Handler mHandler = new Handler();
    Runnable runnable;
    int mInterval = 10*1000; //Delay for 15 seconds.  One second = 1000 milliseconds.

    AlertDialog dialog;
    List<Users> driversList=new ArrayList<>();


    List<Users> requesters=new ArrayList<>();
    List<Users> trips=new ArrayList<>();

    boolean isRequest=false;
    boolean see_all;

    Button req,cancel ;
    TextView phoreq,userreq,locreq,requesteridd;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference requestRef = db.collection("Requests");
    CollectionReference tripsRef = db.collection("Trips");
    String country;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver);
        progressBar=findViewById(R.id.progressBar);
        Button accreq=(Button)findViewById(R.id.Acceptreq);
        Button rfreq=(Button)findViewById(R.id.refusereq);
        accreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AcceptRequest(mAuth.getUid(),requesteridd.getText().toString(),"CA");
                requestlay.setVisibility(View.GONE);

            }
        });
        rfreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteRequest(mAuth.getUid(),requesteridd.getText().toString());
                requestlay.setVisibility(View.GONE);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        req=findViewById(R.id.button4);

        phoreq=(TextView)findViewById(R.id.phoneclient);
        userreq=(TextView)findViewById(R.id.nameclient);
        locreq=(TextView)findViewById(R.id.locationclient);
        requesteridd=(TextView)findViewById(R.id.clientid);
        requestlay=(RoundKornerRelativeLayout) findViewById(R.id.roundKornerRelativeLayout);
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
    private void deteailsrequester(final String Uid)
    {
        db.collection("Users")
                .whereEqualTo("Uid",Uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String phone = (String) document.getData().get("phone");
                                String name = (String) document.getData().get("username");
                                GeoPoint l =  ((GeoPoint) document.getData().get("location"));
                                phoreq.setText(phone);
                                userreq.setText(name);
                                requesteridd.setText(Uid);
                                locreq.setText((String.valueOf(l.getLatitude())));
                                requestlay.setVisibility(View.VISIBLE);


                            }
                        }
                    }

                });

    }

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
                                //see_all=(boolean) document.getData().get("access all");
                                boolean isDriver = (boolean) document.getData().get("is driver");
                                country=(String) document.getData().get("country");
                                user = new Users(name, l, id, schoolId, isDriver,phone,uid);
                                //progressBar.setVisibility(View.GONE);
                                break;

                            }
                        }
                        // if driver mode
                        RetrieveRequesters(mAuth.getUid());
                        RetrieveTrips(mAuth.getUid());

                        //show_user_location();

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
                                                        trips.add(user);
                                                        if(counter==0)
                                                        {
                                                            show_requests_location();

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













    public void RetrieveTrips(String requestedId)
    {
        db.collection("Trips")
                .whereEqualTo("CaptainId",requestedId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String uid = (String) document.getData().get("ClientId");
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
                                                        trips.add(user);
                                                        if(counter==0)
                                                        {
                                                            show_trips_location();

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

            }

        });
    }








    public void listenToDocument() {


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
        String type=(String) marker.getTag();
        bdan=(String)infos[2];
        cancel=mview.findViewById(R.id.button6);

        req=mview.findViewById(R.id.button4);
        cancel.setVisibility(View.VISIBLE);
        //Toast.makeText(this,bdan.id,Toast.LENGTH_LONG).show();
        mbulder.setView(mview);
        dialog=mbulder.create();

        if(type=="trip")
        {
            cancel.setText("cancel trip");
            req.setVisibility(View.GONE);

        }
        if(type=="request")
        {
            req.setVisibility(View.VISIBLE);
            req.setText("accept the request");
            cancel.setText("cancel request");
        }
        //checkRequest(mAuth.getUid(),bdan);
        dialog.show();
        return false;
    }


    boolean second=false;

    DocumentChange dc;
    void func()
    {

        Toast.makeText(driver.this,"hi",Toast.LENGTH_LONG).show();

        db.collection("Requests")
                .whereEqualTo("requestedId", mAuth.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            //     Log.w(TAG, "listen:error", e);
                            return;
                        }
                        //   if(fristtime)
                        {

                            // fristtime=!fristtime;
                        }
                        if (snapshots.getDocumentChanges().size() <= 0) {
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            switch (dc.getType()) {
                                case ADDED:
                                    deteailsrequester(dc.getDocument().get("requesterId").toString());
                                    //   dc= snapshots.getDocumentChanges().remove(0);
                                    //Toast.makeText(driver.this,"added "+dc.getDocument().get("requesterId").toString(),Toast.LENGTH_LONG).show();
                                    break;
                                case MODIFIED:
                                    //   snapshots.getDocumentChanges().clear();
                                    Toast.makeText(driver.this, "modifed " + dc.getDocument().getData().toString(), Toast.LENGTH_LONG).show();
                                    break;
                                case REMOVED:
                                    //   snapshots.getDocumentChanges().clear();
                                    requestlay.setVisibility(View.GONE);
                                    Toast.makeText(driver.this, "removed " + dc.getDocument().getData().toString(), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    }

                });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        RetrieveUser(mAuth.getUid());
        startRepeatingTask();
//        mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title("Marker in Sydney"));
        //      mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                func();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }



    void show_user_location()
    {
        //  mMap.addMarker(new MarkerOptions().position( new LatLng(user.position.getLatitude(),user.position.getLongitude())).title(user.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(user.position.getLatitude(),user.position.getLongitude())));

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



                        }

                    //    show_drivers_location();

                    }
                });

    }

    public void setting(View view) {
        Intent i = new Intent(driver.this, SettingsActivity.class);
        i.putExtra("mAuth", mAuth.getUid());
        startActivity(i);
    }

    void show_requests_location()
    {
        for (Users requester:requesters)
        {

            mMap.addMarker(new MarkerOptions().position( new LatLng(requester.position.getLatitude(),requester.position.getLongitude())).title(requester.name+","+requester.phone+" school requestttt"+","+requester.uid)).setTag("request");

            //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(driver.position.getLatitude(),driver.position.getLongitude())));
            mMap.setOnMarkerClickListener(this);
        }
    }
    void show_trips_location()
    {
        for (Users trip:trips)
        {

            mMap.addMarker(new MarkerOptions().position( new LatLng(trip.position.getLatitude(),trip.position.getLongitude())).title(trip.name+","+trip.phone+" school tripppp"+","+trip.uid)).setTag("trip");

            //       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(driver.position.getLatitude(),driver.position.getLongitude())));
            mMap.setOnMarkerClickListener(this);
        }
    }


}
