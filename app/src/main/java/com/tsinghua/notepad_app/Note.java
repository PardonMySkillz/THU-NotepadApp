package com.tsinghua.notepad_app;

import com.google.firebase.Timestamp;

import java.util.ArrayList;


public class Note {

    String title;
    ArrayList<Content> contents;
    Timestamp timestamp;

    public Note(String title) {
        this.title = title;
    }

    public Note() {
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Content> getContent() {
        return contents;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(ArrayList<Content> content) {
        this.contents = contents;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
