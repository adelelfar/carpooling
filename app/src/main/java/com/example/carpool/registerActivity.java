package com.example.carpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class registerActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    GeoPoint loc;
    GoogleSignInClient mGoogleSignInClient;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "1000";

    CollectionReference userRef = db.collection("Users");
    EditText nid;
    Boolean x = false;
    EditText u, pho;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);









        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        final SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(registerActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(registerActivity.this, "please enable GPS", Toast.LENGTH_LONG).show();
                } else {
                    client.getLastLocation().addOnSuccessListener(registerActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                loc = new GeoPoint(location.getLatitude(), location.getLongitude());
                            }
                        }
                    });


                    switch (v.getId()) {
                        case R.id.sign_in_button:
                            signIn();
                            break;
                        // ...
                    }
                }
            }
        });







        mAuth = FirebaseAuth.getInstance();

        requestPermission();
        client= LocationServices.getFusedLocationProviderClient(this);
//on button click
        final TextView v1 =(TextView)findViewById(R.id.textView);
        final TextView v2 =(TextView)findViewById(R.id.pas);
        final TextView v3 =(TextView)findViewById(R.id.con);
        final  TextView v4 =(TextView)findViewById(R.id.na);

        final TextView v5 =(TextView)findViewById(R.id.bir);
        final TextView v6 =(TextView)findViewById(R.id.Ge);
        final TextView v7 =(TextView)findViewById(R.id.add);

        nid =(EditText) findViewById(R.id.birthdate);

        u = (EditText) findViewById(R.id.username);
        final EditText p = (EditText) findViewById(R.id.paswordd);
        final EditText p1 = (EditText) findViewById(R.id.editText6);
        pho = (EditText) findViewById(R.id.phone);

        final ImageButton cont=(ImageButton)findViewById(R.id.continue11);
        final ImageButton bak=(ImageButton)findViewById(R.id.back11);
        final ImageButton lo=(ImageButton)findViewById(R.id.lo);

        final RadioButton mal=(RadioButton)findViewById(R.id.radioButton);
        final RadioButton fe=(RadioButton)findViewById(R.id.radioButton2);
        final Button done=(Button)findViewById(R.id.button2);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((u.getText().toString().equals("") && p.getText().toString().equals("") && p1.getText().toString().equals("") && pho.getText().toString().equals("")&&(!mal.isChecked()||!fe.isChecked())))
                {
                    Toast.makeText(getApplicationContext(),"please fill the requirement ",Toast.LENGTH_LONG).show();
                }
                else if(loc==null)
                {
                    Toast.makeText(getApplicationContext(),"please dos button el location elly odamak da ",Toast.LENGTH_LONG).show();

                }
                else {
                    if ((p.getText().toString()).equals(p1.getText().toString())) {
                        if (mal.isChecked()) {
                            x = true;
                        }
                        String email = u.getText().toString();
                        String password = p.getText().toString();
                      //  savedata(email, password);

                    }
                }
            }
        });
        lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        bak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  v1.setVisibility(View.VISIBLE);
               // v2.setVisibility(View.VISIBLE);
               // v3.setVisibility(View.VISIBLE);
                v4.setVisibility(View.VISIBLE);
                cont.setVisibility(View.VISIBLE);
                //
                // .setVisibility(View.VISIBLE);
              //  p.setVisibility(View.VISIBLE);
            //    p1.setVisibility(View.VISIBLE);
                pho.setVisibility(View.VISIBLE);
                v5.setVisibility(View.GONE);
                v6.setVisibility(View.GONE);
                v7.setVisibility(View.GONE);
                mal.setVisibility(View.GONE);
                fe.setVisibility(View.GONE);
                nid.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                bak.setVisibility(View.GONE);
                lo.setVisibility(View.GONE);
                signInButton.setVisibility(View.GONE);

            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.GONE);
                v3.setVisibility(View.GONE);
                v4.setVisibility(View.GONE);
                signInButton.setVisibility(View.VISIBLE);

                cont.setVisibility(View.GONE);

                v5.setVisibility(View.VISIBLE);
                v6.setVisibility(View.VISIBLE);
                nid.setVisibility(View.VISIBLE);
               // done.setVisibility(View.VISIBLE);
                bak.setVisibility(View.VISIBLE);
                mal.setVisibility(View.VISIBLE);
                fe.setVisibility(View.VISIBLE);
                //lo.setVisibility(View.VISIBLE);

                u.setVisibility(View.GONE);
                p.setVisibility(View.GONE);
                p1.setVisibility(View.GONE);
                pho.setVisibility(View.GONE);


            }
        });


    }




    boolean isDriver;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplication());

            if (acct != null) {
                savedata(acct);
            }
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("2", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this,"signInResult:failed code=" + e.getStatusCode(),Toast.LENGTH_LONG).show();

        }
    }

    public void retive(String Uid)
    {
        db.collection("Users")
                .whereEqualTo("Uid", Uid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {


                        String id = (String) document.getData().get("Uid");
                        isDriver = (boolean) document.getData().get("is driver");

                        break;
                    }
                }
                if(!isDriver) {
                    Intent i = new Intent(registerActivity.this, MapActivity.class);
                    startActivity(i);
                    finish();

                }else {

                    Intent i = new Intent(registerActivity.this, driver.class);
                    startActivity(i);
                    finish();

                }

            }
        });
    }

    private void savedata(final GoogleSignInAccount acct)
    {
        mAuth.createUserWithEmailAndPassword(acct.getEmail(), acct.getId())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            // Sign in success, update UI with the signed-in user's information
                            addData(acct.getDisplayName(), nid.getText().toString(),mAuth.getUid(), pho.getText().toString(), loc, nid.getText().toString(), x);
                            retive(mAuth.getUid());

                        } else {
                           // retive(mAuth.getUid());
                            Toast.makeText(registerActivity.this, "error happened , please try again later or try to login", Toast.LENGTH_LONG).show();
                            // If sign in fails, display a message to the user.

                        }

                    }
                });
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }

    void addData(String username, String NationalId,String Uid, String phone, GeoPoint loc, String school, boolean driver)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("username",username);
        user.put("Uid",Uid);
        user.put("id","3243");
        user.put("phone",phone);
        user.put("location",loc);
        user.put("school",school);
        user.put("country","Egypt");
        user.put("access non-drives",true);
        user.put("is driver",driver);
        if(driver)
            user.put("plate","xyz 123");
        else
            user.put("plate","has no car");

        addUser("Users",user);
    }
    void addUser(String collection,Map<String, Object> user)
    {


        db.collection(collection).document() .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });


    }



}
