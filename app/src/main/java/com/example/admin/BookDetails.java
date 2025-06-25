package com.example.admin;

public class BookDetails {
    private String title;
    private String author;
    private String year;
    private String publisher;
    private String genre;
    private String description;
    private String tags;
    private String imageBase64;

    // Empty constructor (needed for Firestore)
    public BookDetails() {}

    // Full constructor
    public BookDetails(String title, String author, String year, String publisher,
                       String genre, String description, String tags, String imageBase64) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.publisher = publisher;
        this.genre = genre;
        this.description = description;
        this.tags = tags;
        this.imageBase64 = imageBase64;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getYear() {
        return year;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    // Setters (optional, but needed if Firestore sets data)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
