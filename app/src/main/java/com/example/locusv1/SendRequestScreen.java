package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SendRequestScreen extends AppCompatActivity {

    TextView displayRName, displayRUserName;
    Button sendRequest, cancelRequest;
    DatabaseReference FriendRequestRef, usersREF, FriendsREF;
    FirebaseAuth auth;
    String senderUserId, receiverUserID, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request_screen);

        displayRName = findViewById(R.id.requestName);
        displayRUserName = findViewById(R.id.requestUserName);
        sendRequest = findViewById(R.id.requestSend);
        cancelRequest = findViewById(R.id.requestCancel);

        CURRENT_STATE = "not_friends";

        auth = FirebaseAuth.getInstance();
        senderUserId = auth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        usersREF = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsREF = FirebaseDatabase.getInstance().getReference().child("Friends");

        usersREF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child(receiverUserID).child("UserName").getValue().toString();
                String name = snapshot.child(receiverUserID).child("Name").getValue().toString();

                displayRName.setText(name);
                displayRUserName.setText(username);

                MaintainBUttons();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cancelRequest.setVisibility(View.INVISIBLE);
        cancelRequest.setEnabled(false);

        if (!(senderUserId.equals(receiverUserID))){
            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequest.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){

                        SendFriendRequestTOPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent")){
                        CancelFreindRequest();
                    }

                    if (CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")){
                        UnfriendExistingUser();
                    }
                }

                private void SendFriendRequestTOPerson() {
                    FriendRequestRef.child(senderUserId).child(receiverUserID).child("Request_Type").setValue("SENT").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                FriendRequestRef.child(receiverUserID).child(senderUserId).child("Request_Type").setValue("RECEIVED").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            sendRequest.setEnabled(true);
                                            CURRENT_STATE = "request_received";
                                            sendRequest.setText("Usend Request");
                                            cancelRequest.setVisibility(View.INVISIBLE);
                                            cancelRequest.setEnabled(false);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } else {
            cancelRequest.setVisibility(View.INVISIBLE);
            sendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void UnfriendExistingUser() {
        FriendsREF.child(senderUserId).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendsREF.child(receiverUserID).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendRequest.setText("Send Request");
                                                cancelRequest.setVisibility(View.INVISIBLE);
                                                cancelRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsREF.child(senderUserId).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendsREF.child(receiverUserID).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                FriendRequestRef.child(senderUserId).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    FriendRequestRef.child(receiverUserID).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        sendRequest.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        sendRequest.setText("UNFRIEND");
                                                                                        cancelRequest.setVisibility(View.INVISIBLE);
                                                                                        cancelRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintainBUttons() {
        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)){
                    String request_type = snapshot.child(receiverUserID).child("Request_Type").getValue(String.class);
                    if (request_type.equals("SENT")){
                        sendRequest.setText("Unsend Request");
                        CURRENT_STATE = "request_sent";
                        cancelRequest.setVisibility(View.INVISIBLE);
                        cancelRequest.setEnabled(false);
                    }else if (request_type.equals("RECEIVED")){
                        CURRENT_STATE = "request_received";
                        sendRequest.setText("ACCEPT REQUEST");
                        cancelRequest.setVisibility(View.VISIBLE);
                        cancelRequest.setEnabled(true);
                        cancelRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFreindRequest();
                            }
                        });
                    }
                } else {
                    FriendsREF.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserID)){
                                CURRENT_STATE = "friends";
                                sendRequest.setText("UNFRIEND");
                                cancelRequest.setVisibility(View.INVISIBLE);
                                cancelRequest.setEnabled(false);
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

    private void CancelFreindRequest() {
        FriendRequestRef.child(senderUserId).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FriendRequestRef.child(receiverUserID).child(senderUserId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendRequest.setEnabled(true);
                                CURRENT_STATE = "not_friends";
                                sendRequest.setText("Send Request");
                                cancelRequest.setVisibility(View.INVISIBLE);
                                cancelRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }
}