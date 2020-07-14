package com.example.locusv1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class CallActivity extends AppCompatActivity {

    TextView phoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        phoneNo = findViewById(R.id.tvPhoneNo);

        Intent intent = getIntent();
        String i = intent.getStringExtra("Phone");
        phoneNo.setText(i);

    }
}