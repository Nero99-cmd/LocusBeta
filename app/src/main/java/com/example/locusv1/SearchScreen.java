package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class SearchScreen extends AppCompatActivity {

    Button btnSearch;
    EditText etUserName;
    RecyclerView recyclerView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        btnSearch = findViewById(R.id.btnSearchUsers);
        etUserName = findViewById(R.id.searchUsersName);
        recyclerView = findViewById(R.id.usersRecyclerView);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUserName.getText().toString();
                findUsers(userName);
            }
        });

    }

    private void findUsers(String userName){

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();
        Query serachUsersQuery = databaseReference.orderByChild("UserName")
                .startAt(userName).endAt(userName + "\uf8ff");

        FirebaseRecyclerAdapter<Findusers,FindusersViewHolder> firebaseRecyclerAdapter
                 = new FirebaseRecyclerAdapter<Findusers, FindusersViewHolder>(
                         Findusers.class,
                            R.layout.searchuser,
                            FindusersViewHolder.class,
                serachUsersQuery) {
            @Override
            protected void populateViewHolder(FindusersViewHolder findusersViewHolder, Findusers findusers, final int i) {
                findusersViewHolder.setName(findusers.getName());
                findusersViewHolder.setUserName(findusers.getUserName());

                findusersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserID = getRef(i).getKey();
                        Intent i = new Intent(getApplicationContext(),SendRequestScreen.class);
                        i.putExtra("visitUserID",visitUserID);
                        startActivity(i);
                    }
                });
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindusersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindusersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView myname = (TextView) mView.findViewById(R.id.allUsersName);
            myname.setText(name);
        }

        public void setUserName(String userName) {
            TextView myUserName = (TextView) mView.findViewById(R.id.allUsersUsername);
            myUserName.setText(userName);
        }
    }
}