package com.tsinghua.notepad_app;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Query;
import androidx.appcompat.widget.SearchView;



public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button LogOutbutton;
    Button accountInfoButton;
    FloatingActionButton addNoteButton;

    String title, content, docId;


    RecyclerView recyclerView;
    FirebaseUser user;
    DatabaseReference databaseUserRef;
    NoteAdapter noteAdapter;
    ImageView profilePic;
    TextView splashScreen;
    SearchView searchView;


    String username;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        auth = FirebaseAuth.getInstance();
        LogOutbutton = findViewById(R.id.logoutButton);
        accountInfoButton = findViewById(R.id.AccountInfoButton);
        addNoteButton =findViewById(R.id.add_note_button);
        profilePic = findViewById(R.id.profile_pic);
        splashScreen = findViewById(R.id.splashScreen);
        searchView = findViewById(R.id.searchView);


        recyclerView = findViewById(R.id.recycler_view);

        user = auth.getCurrentUser();
        databaseUserRef = FirebaseDatabase.getInstance("https://mobileapplicationdevelop-8b4b4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        setupRecyclerView();

        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");




        String userId = user.getUid();
        //get the username of userId
        databaseUserRef.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                if (task.isSuccessful()) {
                    if (snapshot.exists()) {
                        username = snapshot.child("username").getValue(String.class);
                        splashScreen.setText(getString(R.string.welcome_msg, username));
                        String profilePicUrl = snapshot.child("profilePicUrl").getValue(String.class);
                        if (!TextUtils.isEmpty(profilePicUrl)) {
                            Glide.with(MainActivity.this)
                                    .load(profilePicUrl)
                                    .centerCrop()
                                    .override(50,50)
                                    .into(profilePic);
                        }
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

        LogOutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });
        accountInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), account_info.class);
                startActivity(intent);

            }
        });

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewNoteActivity.class);
                startActivity(intent);

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });




    }

    private void filterNotes(String searchText) {
        Query query = Utility.getCollectionReferenceForNotes()
                .orderBy("title")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        noteAdapter.updateOptions(options);
        noteAdapter.notifyDataSetChanged();
    }

    void setupRecyclerView(){
        Query query  = Utility.getCollectionReferenceForNotes().orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(options,this);
        recyclerView.setAdapter(noteAdapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }




}
