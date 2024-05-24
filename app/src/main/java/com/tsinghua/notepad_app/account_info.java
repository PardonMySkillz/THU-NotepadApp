package com.tsinghua.notepad_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class account_info extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUserRef;
    private TextView textViewUsername, textViewEmail;
    private Button btnChangeUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userId = currentUser.getUid();

        databaseUserRef = FirebaseDatabase.getInstance("https://mobileapplicationdevelop-8b4b4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        btnChangeUsername = findViewById(R.id.btnChangeUsername);

        retrieveAccountInfo(userId);

        btnChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to the "Change Username" option
                Intent intent = new Intent(account_info.this, ChangeUsernameActivity.class);
                startActivity(intent);
                finish();
            }
        });





    }
    private void retrieveAccountInfo(String userId) {
        databaseUserRef.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        textViewUsername.setText(username);
                        textViewEmail.setText(email);
                    }
                    else {
                        // User data not found
                        Toast.makeText(account_info.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        Toast.makeText(account_info.this, "Error retreiving user data: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(account_info.this, "Unknown error retreiving user data", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }
}