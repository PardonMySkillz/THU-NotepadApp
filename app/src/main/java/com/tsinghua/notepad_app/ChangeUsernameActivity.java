package com.tsinghua.notepad_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeUsernameActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUserRef;
    private EditText editTextNewUsername;
    private Button btnSaveUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_username);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userId = currentUser.getUid();

        databaseUserRef = FirebaseDatabase.getInstance("https://mobileapplicationdevelop-8b4b4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        editTextNewUsername = findViewById(R.id.editTextNewUsername);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);

        btnSaveUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editTextNewUsername.getText().toString().trim();
                if (!newUsername.isEmpty()) {
                    databaseUserRef.child("Users").child(userId).child("username").setValue(newUsername).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ChangeUsernameActivity.this, "Username Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ChangeUsernameActivity.this, MainActivity.class);
                            intent.putExtra("newUsername", newUsername);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChangeUsernameActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();

                        }
                    });
                    finish();
                } else {
                    Toast.makeText(ChangeUsernameActivity.this, "Please enter a new username.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}