package com.example.locusv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button lLogin, lSignin;
    EditText lEmail, lPassword;
    ProgressBar lprogressBar;
    DatabaseReference reference;
    FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lLogin = findViewById(R.id.loginLoginButton);
        lSignin = findViewById(R.id.loginSigninButton);
        lEmail = findViewById(R.id.loginEmail);
        lPassword = findViewById(R.id.loginPassword);
        lprogressBar = findViewById(R.id.loginPB);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();

        lLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = lEmail.getText().toString().trim();
                String password = lPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(MainActivity.this, "Email can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    Toast.makeText(MainActivity.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6){
                    Toast.makeText(MainActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                    return;
                }

                lprogressBar.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                lprogressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    finish();
                                    Toast.makeText(MainActivity.this, "LogIn Successful, WELCOME", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                } else {
                                    Toast.makeText(MainActivity.this, "ERROR: Please try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        lSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(),SigninActivity.class));
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(),MainScreen.class));
        }
    }
}