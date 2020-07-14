package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileScreen extends AppCompatActivity {

    TextView displayName, displayUserName;
    Button btnSignOut, btnfriendsList;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        displayName = findViewById(R.id.profileName);
        displayUserName = findViewById(R.id.profileUserName);
        btnSignOut = findViewById(R.id.btnSIGNOUT);
        btnfriendsList = findViewById(R.id.btnFRIENDSLIST);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("Users").child(uid).child("UserName").getValue(String.class);
                String name = snapshot.child("Users").child(uid).child("Name").getValue(String.class);

                displayName.setText(name);
                displayUserName.setText(username);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        btnfriendsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),FriendsListActivity.class));
            }
        });
    }
}