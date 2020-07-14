package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView myFriendsList;
    private DatabaseReference friendsReference, usersRef;
    private FirebaseAuth mAuth;
    private String onlineUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendsList = findViewById(R.id.friendsList);
        myFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendsList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.searchuser,
                        FriendsViewHolder.class,
                        friendsReference

                ) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int i) {
                        friendsViewHolder.setDate(friends.getDate());
                        final String usersIDs = getRef(i).getKey();
                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    final String fullName = snapshot.child("Name").getValue().toString();

                                    friendsViewHolder.setName(fullName);
                                    friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent profileIntent = new Intent(getApplicationContext(),SendRequestScreen.class);
                                            profileIntent.putExtra("visitUserID",usersIDs);
                                            startActivity(profileIntent);
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
        myFriendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView myname = (TextView) mView.findViewById(R.id.allUsersName);
            myname.setText(name);
        }

        public void setDate(String date) {
            TextView myDate = (TextView) mView.findViewById(R.id.allUsersUsername);
            myDate.setText("Friends since:" + date);
        }
    }
}