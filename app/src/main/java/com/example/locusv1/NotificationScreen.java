package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationScreen extends AppCompatActivity {

    private RecyclerView notiRecyclerView;
    private DatabaseReference notiRef, dataRef;
    private FirebaseAuth nAuth;
    private String nID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);

        nAuth = FirebaseAuth.getInstance();
        nID = nAuth.getCurrentUser().getUid();
        notiRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(nID);
        dataRef = FirebaseDatabase.getInstance().getReference().child("Users");

        notiRecyclerView = findViewById(R.id.notificationList);
        notiRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        notiRecyclerView.setLayoutManager(linearLayoutManager);

        DislayAllNotification();

    }

    private void DislayAllNotification() {
        FirebaseRecyclerAdapter<Notification,NotificationViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Notification, NotificationViewHolder>(
                        Notification.class,
                        R.layout.notificationlist,
                        NotificationViewHolder.class,
                        notiRef
        ) {
            @Override
            protected void populateViewHolder(final NotificationViewHolder notificationViewHolder, Notification notification, final int i) {
                notiRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String userIDs = getRef(i).getKey();
                        if (snapshot.child(userIDs).child("Request_Type").getValue().toString() == "RECEIVED"){
                            dataRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        final String userNameee = snapshot.child("Name").getValue().toString();

                                        notificationViewHolder.setName(userNameee);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        notiRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{

        View nView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            nView = itemView;
        }
        public void setName(String name) {
            TextView myname = nView.findViewById(R.id.requestSenderName);
            myname.setText(name);
        }
    }
}