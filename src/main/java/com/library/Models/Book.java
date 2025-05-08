package com.library.Models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Book {
    private int availableNumber;
    private String title;
    private String author;

    private String description;
    private String isbn;
    String thumbnailUrl;
    private transient BooleanProperty selectedForReturn = new SimpleBooleanProperty(false);
    public boolean isSelectedForReturn() {
        if (this.selectedForReturn == null) {
            this.selectedForReturn = new SimpleBooleanProperty(false);
        }
        return this.selectedForReturn.get();
    }

    public void setSelectedForReturn(boolean selected) {
        if (this.selectedForReturn == null) {
            this.selectedForReturn = new SimpleBooleanProperty(false);
        }
        this.selectedForReturn.set(selected);
    }

    public BooleanProperty selectedForReturnProperty() {
        // Đảm bảo selectedForReturn không bao giờ null khi được truy cập
        if (this.selectedForReturn == null) {
            this.selectedForReturn = new SimpleBooleanProperty(false);
        }
        return this.selectedForReturn;
    }

    public Book(){

    }
    public Book(Book other){

        this.availableNumber = other.availableNumber;
        this.title = other.title;
        this.author = other.author;

        this.description = this.description;
        this.isbn = other.isbn;
    }



    public int getAvailableNumber() {
        return availableNumber;
    }

    public void setAvailableNumber(int availableNumber) {
        this.availableNumber = availableNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public Book(String title, String author, String description, String isbn, String thumbnailUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
    }
}
