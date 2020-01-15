package com.example.carpool;

import com.google.firebase.firestore.GeoPoint;

public class Users {
    String name;
    GeoPoint position;
    String nationalId;
    String schoolId;
    String phone;
    Boolean haveCar;
    String uid;
    boolean check;
    public Users(String name,GeoPoint position,String nationalId,String schoolId,Boolean haveCar,String pho,String uid)
    {
        this.name=name;
        this.haveCar=haveCar;
        this.schoolId=schoolId;
        this.uid=uid;
        this.position=position;
        this.phone=pho;
        this.nationalId=nationalId;
    }

}
