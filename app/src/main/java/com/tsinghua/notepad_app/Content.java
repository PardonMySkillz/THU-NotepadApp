package com.tsinghua.notepad_app;

import android.net.Uri;

public class Content {
    public enum ContentType {
        TEXT,
        IMAGE,
        RECORDING

    }

    private ContentType type;
    private String text;
    private Uri imageUri;

    private Uri recordingUri;


    // Constructors for different content types
    public Content(String text) {
        this.type = ContentType.TEXT;
        this.text = text;
    }
    public Content(Uri source, ContentType type) {
        if (type == ContentType.IMAGE) {
            this.imageUri = source;
            this.type = ContentType.IMAGE;
        }
        else if (type == ContentType.RECORDING) {
            this.recordingUri = source;
            this.type = ContentType.RECORDING;
        }
    }

    // Getters
    public ContentType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Uri getImageUri() {
        return imageUri;
    }
    public Uri getRecordingUri() {
        return recordingUri;
    }
}

