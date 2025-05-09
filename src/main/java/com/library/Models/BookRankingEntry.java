package com.library.Models; // Hoặc package phù hợp với cấu trúc dự án của bạn

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BookRankingEntry {
    private final IntegerProperty rank;
    private final StringProperty title;
    private final StringProperty author;
    private final IntegerProperty borrowCount; // Giữ lại để sắp xếp, dù không hiển thị

    public BookRankingEntry(int rank, String title, String author, int borrowCount) {
        this.rank = new SimpleIntegerProperty(rank);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.borrowCount = new SimpleIntegerProperty(borrowCount);
    }

    // Getters for Properties (cần thiết cho TableView)
    public IntegerProperty rankProperty() {
        return rank;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty authorProperty() {
        return author;
    }

    public IntegerProperty borrowCountProperty() {
        return borrowCount;
    }

    // Getters for values (thuận tiện)
    public int getRank() {
        return rank.get();
    }

    public String getTitle() {
        return title.get();
    }

    public String getAuthor() {
        return author.get();
    }

    public int getBorrowCount() {
        return borrowCount.get();
    }
}