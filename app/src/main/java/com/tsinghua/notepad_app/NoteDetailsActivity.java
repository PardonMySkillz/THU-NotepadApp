package com.tsinghua.notepad_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class NoteDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_AUDIO_RECORD = 3;
    private static final int REQUEST_AUDIO_PICK = 4;
    EditText titleEditText;
    ImageButton saveNoteBtn, addTextBtn, addImageBtn, addAudioBtn;
    ImageButton deleteNoteBtn;
    String title, docId;
    RecyclerView contentRecyclerView;
    FirebaseAuth firebaseAuth;
    ArrayList<Content> contentList = new ArrayList<Content>();
    ContentAdapter contentAdapter;
    boolean isEditMode;
    ProgressBar progressBar;

    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userId = currentUser.getUid();
        titleEditText = findViewById(R.id.titleText);
        saveNoteBtn = findViewById(R.id.saveNoteButton);
        addTextBtn = findViewById(R.id.addTextButton);
        addImageBtn = findViewById(R.id.addImageButton);
        addAudioBtn = findViewById(R.id.addAudioButton);
        deleteNoteBtn = findViewById(R.id.deleteNoteButton);
        progressBar = findViewById(R.id.progressBar);
        contentRecyclerView = findViewById(R.id.NoteContentRecycleView);
        contentAdapter = new ContentAdapter(contentList, this);
        contentRecyclerView.setAdapter(contentAdapter);






        title = getIntent().getStringExtra("title");
        docId = getIntent().getStringExtra("docId");
        if (docId != null && !docId.isEmpty()) {
            deleteNoteBtn.setVisibility(View.VISIBLE);
            deleteNoteBtn.setEnabled(true);
            isEditMode = true;
            fetchContentFromFirebase();

            FirestoreRecyclerOptions<Content> options = new FirestoreRecyclerOptions.Builder<Content>()
                    .setQuery(Utility.getCollectionReferenceForNotes().document(docId).collection("contents"), Content.class)
                    .build();
        }
        else {
            isEditMode = false;
            deleteNoteBtn.setVisibility(View.INVISIBLE);
        }
        titleEditText.setText(title);



        deleteNoteBtn.setOnClickListener(v -> deleteNoteFromFirebase());
        saveNoteBtn.setOnClickListener(v -> saveNote());
        addTextBtn.setOnClickListener(v -> addTextContent());
        addImageBtn.setOnClickListener(v -> pickImage());
        addAudioBtn.setOnClickListener(v -> pickAudio());


    }


    void saveNote() {
        String noteTitle = titleEditText.getText().toString();

        if (noteTitle == null || noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        Note note = new Note();
        note.setTitle(noteTitle);
        note.setTimestamp(Timestamp.now());
        note.setContents(contentList);

        if (isEditMode) {
            // Update the existing note
            DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document(docId);
            documentReference.set(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(NoteDetailsActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NoteDetailsActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                }
            });
            saveContentsToFirebase(contentList);
        } else {
            // Save the new note
            saveContentsToFirebase(contentList);
        }
    }

    void deleteNoteFromFirebase() {
        DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(NoteDetailsActivity.this, "Note Deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(NoteDetailsActivity.this, "Note Deletion failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchContentFromFirebase() {
        Utility.getCollectionReferenceForNotes().document(docId).collection("contents")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            contentList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Content content = new Content();
                                content.setType(Content.ContentType.valueOf(document.getString("type")));
                                content.setOrder(document.getLong("order").intValue());
                                if (content.getType() == Content.ContentType.TEXT) {
                                    content.setText(document.getString("text"));
                                } else if (content.getType() == Content.ContentType.RECORDING || content.getType() == Content.ContentType.IMAGE) {
                                    content.setUri(Uri.parse(document.getString("uri")));
                                }
                                Log.e("TAG", content.getType().toString());
                                contentList.add(content);
                            }
                            setupRecyclerView(contentList);

//                            
                        } else {
                            // Handle the error
                        }
                    }
                });

    }

    private void addTextContent() {
        // Prompt the user to enter text
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add Text")
                .setView(input)
                .setPositiveButton("Add", (dialog, whichButton) -> {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        Content content = new Content();
                        content.setType(Content.ContentType.TEXT);
                        content.setText(text);
                        contentList.add(content);
                        contentAdapter.notifyItemInserted(contentList.size() - 1); // Notify the adapter
                        setupRecyclerView(contentList);
                    }
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss())
                .show();
    }

    private void saveContentsToFirebase(ArrayList<Content> contentList) {
        DocumentReference docRef;
        if (isEditMode) {
            docRef = Utility.getCollectionReferenceForNotes().document(docId);
        } else {
            docRef = Utility.getCollectionReferenceForNotes().document();
            // Save the new note metadata
            Note note = new Note();
            note.setTitle(titleEditText.getText().toString());
            note.setTimestamp(Timestamp.now());
            docId = docRef.getId(); // Save the new document id for further use
            docRef.set(note);
        }

        progressBar.setVisibility(View.VISIBLE);
        saveNoteBtn.setVisibility(View.INVISIBLE);

        // Delete all existing content entries in the 'contents' sub-collection
        docRef.collection("contents").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
            } else {
                Log.d("ACTIVITY: ", "Error getting documents: ", task.getException());
            }

            // Add new content entries
            for (int i = 0; i < contentList.size(); i++) {
                Content content = contentList.get(i);
                content.setOrder(i);
                docRef.collection("contents").add(content).addOnCompleteListener(addTask -> {
                    if (addTask.isSuccessful()) {
                        Toast.makeText(NoteDetailsActivity.this, "Content added successfully", Toast.LENGTH_SHORT).show();
                        contentAdapter.notifyItemInserted(contentList.size() - 1); // Notify the adapter
                        setupRecyclerView(contentList);
                    } else {
                        Toast.makeText(NoteDetailsActivity.this, "Failed to add content", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Toast.makeText(NoteDetailsActivity.this, "Saving process Complete", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            saveNoteBtn.setVisibility(View.VISIBLE);
            saveNoteBtn.setEnabled(true);
            finish();
        });
    }


    private void pickImage() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Take photo with camera
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else if (which == 1) {
                        // Pick image from gallery
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
                    }
                })
                .show();
    }

    private void pickAudio() {
        String[] options = {"Record Audio", "Choose from Device"};
        new AlertDialog.Builder(this)
                .setTitle("Select Audio")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Record audio
                        Intent recordAudioIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                        startActivityForResult(recordAudioIntent, REQUEST_AUDIO_RECORD);
                    } else if (which == 1) {
                        // Pick audio file from device
                        Intent pickAudioIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickAudioIntent, REQUEST_AUDIO_PICK);
                    }
                })
                .show();
    }




    private void setupRecyclerView(ArrayList<Content> contentList) {
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ContentAdapter adapter = new ContentAdapter(contentList, this);
        contentRecyclerView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                uploadData(selectedUri, requestCode);
            }
        }
    }

    private void uploadData(Uri dataUri, int requestCode) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("FirebaseStorage", "User is not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance("gs://mobileapplicationdevelop-8b4b4.appspot.com")
                .getReference(userId + "/" + System.currentTimeMillis());
        showProgressBar();

        storageRef.putFile(dataUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Uri downloadUrl = uri;
                    Log.d("FirebaseStorage", "File uploaded successfully, URL: " + downloadUrl);
                    Content content = new Content();

                    if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_PICK) {
                        content.setType(Content.ContentType.IMAGE);
                    } else if (requestCode == REQUEST_AUDIO_PICK || requestCode == REQUEST_AUDIO_RECORD) {
                        content.setType(Content.ContentType.RECORDING);
                    }

                    content.setUri(uri);
                    contentList.add(content);
                    contentAdapter.notifyItemInserted(contentList.size() - 1); // Notify the adapter
                    setupRecyclerView(contentList);
                    hideProgressBar();

                }).addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Error getting download URL", e);
                    hideProgressBar();
                }))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Error uploading file", e);
                    hideProgressBar();
                });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}