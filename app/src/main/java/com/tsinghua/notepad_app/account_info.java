package com.tsinghua.notepad_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class account_info extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button changeUsernameBtn;
    private DatabaseReference databaseUserRef;
    private FirebaseAuth firebaseAuth;
    private TextView textViewUsername, textViewEmail;

    private Button updateProfilePicBtn;

    private ImageView profilePicImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userId = currentUser.getUid();
        databaseUserRef = FirebaseDatabase.getInstance("https://mobileapplicationdevelop-8b4b4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        textViewUsername = findViewById(R.id.usernameView);
        textViewEmail = findViewById(R.id.EmailTextView);
        changeUsernameBtn = findViewById(R.id.changeUsernameButton);
        updateProfilePicBtn = findViewById(R.id.updateProfilePicBtn);
        profilePicImageView = findViewById(R.id.profilePictureView);

        retrieveAccountInfo(userId);

        changeUsernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChangeUsernameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        updateProfilePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });





    }

    private void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }
    private void uploadProfilePicture(Uri imageUri) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userId = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance("gs://mobileapplicationdevelop-8b4b4.appspot.com").getReference("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseUserRef.child("Users").child(userId).child("profilePicUrl").setValue(uri.toString());
                        Toast.makeText(account_info.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(account_info.this, "Error uploading profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        String profilePicUrl = snapshot.child("profilePicUrl").getValue(String.class);

                        textViewUsername.setText(username);
                        textViewEmail.setText(email);

                        if (!TextUtils.isEmpty(profilePicUrl)) {
                            Glide.with(account_info.this)
                                    .load(profilePicUrl)
                                    .centerCrop()
                                    .override(500,500)
                                    .into(profilePicImageView);

                        }
                         else {
                            // Display a default profile picture
                            profilePicImageView.setImageResource(R.drawable.ic_launcher_background);

                        }
                    }
                    else {
                        // User data not found
                        Toast.makeText(account_info.this, "User data not found.", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        Toast.makeText(account_info.this, "Error retreiving user data: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(account_info.this, "Unknown error retreiving user data", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data != null && data.getData() !=null) {
            Uri imageUri = data.getData();

            uploadProfilePicture(imageUri);
        }
    }
}