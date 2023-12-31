package com.cs407.badgerbash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Map extends AppCompatActivity {
private GoogleMap mmap;
    private DatabaseReference rootRef;
    FirebaseDatabase database;
    private SharedPreferences sharedPreferences;
    private int eventCount;

    EventInfo event;
    private LatLng eventDestination;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent=getIntent();
        String name=intent.getStringExtra("name");
        String lat=intent.getStringExtra("lat");
        String lon=intent.getStringExtra("lon");
        String createdBy=intent.getStringExtra("createdBy");
        String brief=intent.getStringExtra("brief");
        String full=intent.getStringExtra("full");
        String selectedTime= intent.getStringExtra("selectedTime");
        event=new EventInfo(name,lat,lon,createdBy,brief,full,selectedTime);

        eventDestination = new LatLng(Double.parseDouble(event.getLat()), Double.parseDouble(event.getLon()));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Map is ready to be used.
                setUpMap(googleMap);
            }
        });
        TextView mapTitle = findViewById(R.id.mapTitle);
        ImageView mapImage = findViewById(R.id.mapEventImage);
        TextView descriptionText = findViewById(R.id.mapDescription);
        descriptionText.setText(event.getBriefDescription());
        mapTitle.setText(event.getName());
        Button backButton = findViewById(R.id.mapBackButton);
        Button directionsButton = findViewById(R.id.mapDirectionsButton);
        Button signUp = findViewById(R.id.mapSignupButton);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Map.this, EventDescriptionPage.class);
                intent1.putExtra("name",event.getName());
                intent1.putExtra("lat",event.getLat());
                intent1.putExtra("lon", event.getLon());
                intent1.putExtra("createdBy",event.getCreatedBy());
                intent1.putExtra("brief",event.getBriefDescription());
                intent1.putExtra("full",event.getFullDescription());
                intent1.putExtra("selectedTime", event.getSelectedTime());
                startActivity(intent1);
            }
        });

        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "google.navigation:q=" + eventDestination.latitude+ "," + eventDestination.longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Map.this, MainActivity.class);
                addToSignedUp();
                startActivity(intent);
            }
        });


        getNumEvents();



    }
    private void setUpMap(GoogleMap googleMap) {
        // Add a marker at 'location' and move the camera
        googleMap.addMarker(new MarkerOptions().position(eventDestination).title("Marker Title"));
        LatLng cameraDestination = new LatLng(eventDestination.latitude+.003, eventDestination.longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cameraDestination));


        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraDestination, 14.5f)); // Zoom level is a float
    }
    private void addToSignedUp(){
        rootRef= FirebaseDatabase.getInstance().getReference().child("Users");
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email=sharedPreferences.getString("Username","defaultUsername");
        int dotIndex = email.indexOf('.');
        String username = email.substring(0, dotIndex);

        rootRef.child(username).child("SignedUp").child("Event" + eventCount).setValue(event);
        Intent intent = new Intent(Map.this, MainActivity.class);
        startActivity(intent);
    }
    private void getNumEvents(){
        rootRef= FirebaseDatabase.getInstance().getReference().child("Users");
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email=sharedPreferences.getString("Username","defaultUsername");
        int dotIndex = email.indexOf('.');
        String username = email.substring(0, dotIndex);
        DatabaseReference events = rootRef.child(username).child("SignedUp");
        events.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventCount = (int) snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}