package com.example.carpool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SettingsActivity extends AppCompatActivity {
    EditText school,phone,plate,home;
    Spinner yourcountrySpinnerobj;
    SwitchCompat have_car,see_all;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    QueryDocumentSnapshot doc;
    ArrayAdapter<String> countryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        yourcountrySpinnerobj=findViewById(R.id.yourcountrySpinnerobj);




        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }

        Collections.sort(countries);
        for (String country : countries) {
            System.out.println(country);
        }

        countryAdapter = new ArrayAdapter<String>(getApplication(),
                android.R.layout.simple_spinner_item, countries);

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the your spinner
        yourcountrySpinnerobj.setAdapter(countryAdapter);

        school=findViewById(R.id.school);
        plate=findViewById(R.id.plate);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String mAuth = extras.getString("mAuth");


        home=findViewById(R.id.home);
        phone=findViewById(R.id.phone);

        have_car=findViewById(R.id.haveCar);


        retrieve(mAuth);

    }

    void addData()
    {
        Map<String, Object> user = new HashMap<>();
        user.put("plate",plate.getText().toString());
        user.put("phone",phone.getText().toString());
        user.put("school",school.getText().toString());
        user.put("is driver",have_car.isChecked());
        user.put("country",String.valueOf(yourcountrySpinnerobj.getSelectedItem()));

        addUser("Users",user);
    }


    public void retrieve(String Uid)
    {

        db.collection("Users")
                .whereEqualTo("Uid", Uid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        doc=document;
                        have_car.setChecked((boolean) document.getData().get("is driver"));
                        //home.setText(String.valueOf(document.getData().get("location")));
                        school.setText(String.valueOf(document.getData().get("school")));
                        plate.setText(String.valueOf(document.getData().get("plate")));
                        phone.setText(String.valueOf(document.getData().get("phone")));
                        yourcountrySpinnerobj.setSelection(countryAdapter.getPosition(String.valueOf(document.getData().get("country"))));

                    }
                }
            }
        });
    }


    void addUser(String collection,Map<String, Object> user)
    {
        String x=doc.getId();
        String y;

        db.collection(collection).document(doc.getId()) .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });


    }

    public void save_settings(View view) {
        addData();
        Intent i;
        if(have_car.isChecked()) {
             i = new Intent(SettingsActivity.this, driver.class);
        }
        else {
             i = new Intent(SettingsActivity.this, MapActivity.class);
        }
        startActivity(i);
        finish();
    }
}   