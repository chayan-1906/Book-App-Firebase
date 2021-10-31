package com.example.bookapp.models;

public class ModelCategory {

    // variables
    String id;
    String uid;
    String category;
    String timestamp;

    // constructor for firebase
    public ModelCategory() { }

    // parameterized constructor
    public ModelCategory(String id, String uid, String category, String timestamp) {
        this.id = id;
        this.uid = uid;
        this.category = category;
        this.timestamp = timestamp;
    }

    /* Getter/Setter */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
