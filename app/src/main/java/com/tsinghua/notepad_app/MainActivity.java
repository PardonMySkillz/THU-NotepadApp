package com.tsinghua.notepad_app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    DatabaseReference databaseUserRef;

    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logoutButton);
        textView = findViewById((R.id.SplashScreen));
        user = auth.getCurrentUser();
        databaseUserRef = FirebaseDatabase.getInstance("https://mobileapplicationdevelop-8b4b4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        String userId = user.getUid();
        //get the username of userId
        databaseUserRef.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                if (task.isSuccessful()) {
                    if (snapshot.exists()) {
                        username = snapshot.child("username").getValue(String.class);
                        //updateTextview with username
                        textView.setText(getString(R.string.welcome_msg, username));
                    } else {
                        //User data Not found
                        throw new RuntimeException("User data not found in the database.");
                    }
                }
                else {
                    throw new RuntimeException("Unknown error occurred while retrieving user data.");
                }
            }
        });

//        if (user == null) {
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }
//        else {
//            String name = user.getEmail();
//            String WelcomeMessage = getString(R.string.welcome_msg, String.valueOf(user.getEmail()));
//            textView.setText(WelcomeMessage);
//        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }
}
