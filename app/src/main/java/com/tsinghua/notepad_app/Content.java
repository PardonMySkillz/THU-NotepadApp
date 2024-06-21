package com.tsinghua.notepad_app;

import android.net.Uri;

public class Content {
    public Content(String text) {
        this.type = ContentType.TEXT;
    }

    public Content(Uri uri, ContentType type) {
        this.type = type;
        this.uri = uri;

    }
    public Content() {

    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public enum ContentType {
        TEXT,
        IMAGE,
        RECORDING

    }

    private ContentType type;
    private String text;
    private Uri uri;
    private int order;

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public android.net.Uri getUri() {
        return this.uri;
    }

    public void setUri(android.net.Uri uri) {
        this.uri = uri;
    }

    private Uri Uri;

}

