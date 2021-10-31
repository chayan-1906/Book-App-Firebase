package com.example.bookapp.models;

public class ModelPdf {

    // variables
    String id;
    String uid;
    String title;
    String description;
    String categoryId;
    String uploadedPdfUrl;
    String timestamp;
    String viewsCount;
    String downloadsCount;
    boolean favorite;

    // constructor for firebase
    public ModelPdf() {
    }

    // parameterized constructor


    public ModelPdf(String id, String uid, String title, String description, String categoryId, String uploadedPdfUrl, String timestamp, String viewsCount, String downloadsCount, boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.uploadedPdfUrl = uploadedPdfUrl;
        this.timestamp = timestamp;
        this.viewsCount = viewsCount;
        this.downloadsCount = downloadsCount;
        this.favorite = favorite;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUploadedPdfUrl() {
        return uploadedPdfUrl;
    }

    public void setUploadedPdfUrl(String uploadedPdfUrl) {
        this.uploadedPdfUrl = uploadedPdfUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(String viewsCount) {
        this.viewsCount = viewsCount;
    }

    public String getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(String downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
