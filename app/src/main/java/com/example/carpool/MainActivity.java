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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    Button login;
    EditText loginuser,loginpass;
    ProgressBar progressBar;
    boolean isDriver;
    private FusedLocationProviderClient client;

    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    GeoPoint loc;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Button btn_login=findViewById(R.id.sign_in_button);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        requestPermission();
        client= LocationServices.getFusedLocationProviderClient(this);


/*
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();


                        // ...
                }
            }
        });

        requestPermission();
        client= LocationServices.getFusedLocationProviderClient(this);

*/
        login=findViewById(R.id.button);

        loginuser=findViewById(R.id.loginuser);
        mAuth = FirebaseAuth.getInstance();
        loginpass=findViewById(R.id.loginpass);
        progressBar=findViewById(R.id.progressBar2);

        final Button register = (Button) findViewById(R.id.register);
        /*
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log(loginuser.getText().toString(),loginpass.getText().toString());
            }
        });
         */
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),l.get(0).get("status").toString(),Toast.LENGTH_LONG).show();
                Intent i = new Intent(MainActivity.this, registerActivity.class);
                startActivity(i);
                finish();

            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode ==1) {
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
                progressBar.setVisibility(View.INVISIBLE);
                log(acct.getEmail(),acct.getId());
            }
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            progressBar.setVisibility(View.INVISIBLE);
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("2", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this,"signInResult:failed code=" + e.getStatusCode(),Toast.LENGTH_LONG).show();

        }
    }




    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);

    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);

    }
    void log(String email,String pass)
    {
        mAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            retive(mAuth.getUid());

                            //access(user);

                        }
                        else
                        {
                            Toast.makeText(MainActivity.this,"please register first",Toast.LENGTH_LONG).show();

                        }

                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            //retive(mAuth.getUid());
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

                        progressBar.setVisibility(View.GONE);
                        break;
                    }
                }
                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "please enable GPS", Toast.LENGTH_LONG).show();
                } else {

                    client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                                loc = new GeoPoint(location.getLatitude(), location.getLongitude());
                            }
                        }
                    });
                    if(isDriver) {

                        Intent i = new Intent(MainActivity.this, driver.class);
                        startActivity(i);
                        finish();

                    }else {

                        Intent i = new Intent(MainActivity.this, MapActivity.class);
                        startActivity(i);
                        finish();

                    }





                }

            }
        });
    }

    public void settings(View view) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        i.putExtra("mAuth", mAuth.getUid());
        startActivity(i);
        finish();

    }
}