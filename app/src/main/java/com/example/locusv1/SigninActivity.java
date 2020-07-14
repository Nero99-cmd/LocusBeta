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

public class SigninActivity extends AppCompatActivity {

    private Button sSignIn, sLogIn;
    private EditText sName, sUserName, sEmail, sPassword;
    private ProgressBar sProgressBar;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        sSignIn = findViewById(R.id.signinSigninButton);
        sLogIn = findViewById(R.id.signinLoginButton);
        sName = findViewById(R.id.signinName);
        sEmail = findViewById(R.id.signinEmail);
        sPassword = findViewById(R.id.signinPassword);
        sUserName = findViewById(R.id.signinUserName);
        sProgressBar = findViewById(R.id.signinPB);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();

        sSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = sEmail.getText().toString();
                String password = sPassword.getText().toString();
                final String name = sName.getText().toString();
                final String userName = sUserName.getText().toString();
                final double lat = 0;
                final double lang = 0;
                final int status = 0;

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(SigninActivity.this, "Email can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    Toast.makeText(SigninActivity.this, "Password can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(name)){
                    Toast.makeText(SigninActivity.this, "Name can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(userName)){
                    Toast.makeText(SigninActivity.this, "UserName can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6){
                    Toast.makeText(SigninActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                    return;
                }

                sProgressBar.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    finish();
                                    sProgressBar.setVisibility(View.GONE);
                                    users data = new users(
                                            name,
                                            userName,
                                            email,
                                            lat,
                                            lang,
                                            status
                                    );

                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance()
                                            .getCurrentUser().getUid()).setValue(data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(SigninActivity.this, "Sign-In Succesful", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(getApplicationContext(),MainScreen.class));
                                        }
                                    });
                                } else {
                                    sProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(SigninActivity.this, "ERROR: Please try again!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        sLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(),MainScreen.class));
        }
    }
}