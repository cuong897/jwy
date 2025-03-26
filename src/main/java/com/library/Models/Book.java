package com.library.Models;

public class Book {
    private int totalNumber;
    private int availableNumber;
    private String title;
    private String author;
    private String publisher;
    private String publishDate;
    private String description;
    private double price;
    public Book(){

    }
    public Book(Book other){
        this.totalNumber = other.totalNumber;
        this.availableNumber = other.availableNumber;
        this.title = other.title;
        this.author = other.author;
        this.publisher = other.publisher;
        this.publishDate = other.publishDate;
        this.description = this.description;
        this.price = other.price;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        this.totalNumber = totalNumber;
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Book{" +
                "totalNumber=" + totalNumber +
                ", availableNumber=" + availableNumber +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publishDate='" + publishDate + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
