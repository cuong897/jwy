package com.library.Controller;

import com.google.api.services.books.v1.model.Volume;
import com.library.Models.GoogleBookAPI;
import javafx.application.Platform;
import javafx.concurrent.Task;
import com.library.Models.Book;
import com.library.Models.Customer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

import java.net.URL;
import java.util.*;

public class CustomerHomeController implements Initializable {

    @FXML
    private AnchorPane homeRoot;

    @FXML
    private ImageView mainBannerImageView;

    @FXML
    private Label customerWelcomeLabel;


    @FXML
    private HBox forYouBooksContainer;

    @FXML
    private VBox bookCard1;
    @FXML
    private ImageView bookImage1;
    @FXML
    private Label bookTitleLabel1;
    @FXML
    private Button viewButton1;

    @FXML
    private VBox bookCard2;
    @FXML
    private ImageView bookImage2;
    @FXML
    private Label bookTitleLabel2;
    @FXML
    private Button viewButton2;

    @FXML
    private VBox bookCard3;
    @FXML
    private ImageView bookImage3;
    @FXML
    private Label bookTitleLabel3;
    @FXML
    private Button viewButton3;

    private CustomerDashboardController parentController;
    private List<Book> recommendedBooks;

    public CustomerDashboardController getParentController() {
        return parentController;
    }

    public void setParentController(CustomerDashboardController parentController) {
        this.parentController = parentController;
        Customer currentCustomer = this.parentController.getCurrentCustomer();
        customerWelcomeLabel.setText("Welcome " + currentCustomer.getName() + "!");
        mainBannerImageView.setImage(new Image("/Image/tai-anh-meo-ngau-thumbnail.jpg"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadRecommendedBooksFromAPI();
        displayRecommendedBooks();
    }

    private void loadRecommendedBooksFromAPI() {
        Task<List<Book>> fetchBooksTask = new Task<>() {
            @Override
            protected List<Book> call() throws Exception {
                List<Book> fetchedRecommendedBooks = new ArrayList<>();
                try {
                    String[] queries = {"a", "t", "pa", "j", "v"};
                    String randomQuery = queries[new Random().nextInt(queries.length)];
                    List<Volume> volumes = GoogleBookAPI.searchBooks(randomQuery);

                    if (volumes != null && !volumes.isEmpty()) {
                        Collections.shuffle(volumes);
                        int numberOfBooksToSelect = Math.min(3, volumes.size());
                        for (int i = 0; i < numberOfBooksToSelect; i++) {
                            Book book = convertVolumeToBook(volumes.get(i));
                            if (book != null) {
                                fetchedRecommendedBooks.add(book);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi khi tải sách gợi ý từ Google Books API: " + e.getMessage());
                }
                return fetchedRecommendedBooks;
            }
        };

        fetchBooksTask.setOnSucceeded(event -> {
            recommendedBooks.clear();
            recommendedBooks.addAll(fetchBooksTask.getValue());
            Platform.runLater(() -> {
                displayRecommendedBooks();
            });
        });

        fetchBooksTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                fetchBooksTask.getException().printStackTrace();
                displayRecommendedBooks(); // Hiển thị giao diện trống hoặc thông báo lỗi
            });
        });

        new Thread(fetchBooksTask).start();
    }
    private void displayRecommendedBooks() {
        if (recommendedBooks == null || recommendedBooks.isEmpty()) {
            bookCard1.setVisible(false);
            bookCard2.setVisible(false);
            bookCard3.setVisible(false);
            return;
        }

        if (recommendedBooks.size() > 0) {
            populateBookCard(bookCard1, bookImage1, bookTitleLabel1, viewButton1, recommendedBooks.get(0));
            bookCard1.setVisible(true);
        } else {
            bookCard1.setVisible(false);
        }

        if (recommendedBooks.size() > 1) {
            populateBookCard(bookCard2, bookImage2, bookTitleLabel2, viewButton2, recommendedBooks.get(1));
            bookCard2.setVisible(true);
        } else {
            bookCard2.setVisible(false);
        }

        if (recommendedBooks.size() > 2) {
            populateBookCard(bookCard3, bookImage3, bookTitleLabel3, viewButton3, recommendedBooks.get(2));
            bookCard3.setVisible(true);
        } else {
            bookCard3.setVisible(false);
        }
    }

    private void populateBookCard(VBox card, ImageView imageView, Label titleLabel, Button viewbutton, Book book) {
        titleLabel.setText(book.getTitle());
        try {
            String imagePath = book.getThumbnailUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imageView.setImage(image);
            } else {
                Image placeholder = new Image(getClass().getResourceAsStream("/Image/icons8-bookshelf-50.png"));
                imageView.setImage(placeholder);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + book.getThumbnailUrl() + " - " + e.getMessage());
            try {
                Image placeholder = new Image(getClass().getResourceAsStream("/Image/icons8-bookshelf-50.png")); // Cần có ảnh này
                imageView.setImage(placeholder);
            } catch (Exception ex) {
                imageView.setImage(null);
            }
        }


        viewbutton.setOnAction(event -> handleViewBookAction(book));
    }


    @FXML
    private void handleViewButton1() {
        if (recommendedBooks != null && recommendedBooks.size() > 0) {
            handleViewBookAction(recommendedBooks.get(0));
        }
    }

    @FXML
    private void handleViewButton2() {
        if (recommendedBooks != null && recommendedBooks.size() > 1) {
            handleViewBookAction(recommendedBooks.get(1));
        }
    }

    @FXML
    private void handleViewButton3() {
        if (recommendedBooks != null && recommendedBooks.size() > 2) {
            handleViewBookAction(recommendedBooks.get(2));
        }
    }


    private void handleViewBookAction(Book book) {
        if (book == null || parentController == null) {
            System.err.println("handleViewBookAction: Sách hoặc parentController là null.");
            return;
        }
        String searchQuery = book.getIsbn();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            searchQuery = book.getTitle();
        }
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            System.err.println("handleViewBookAction: Không có thông tin (ISBN/Tiêu đề) để tìm kiếm sách.");
            return;
        }
        parentController.gotoBorrowBook();
        String finalSearchQuery = searchQuery;
        Platform.runLater(() -> {
            parentController.searchBooks(finalSearchQuery);

        });
    }

    private Book convertVolumeToBook(Volume volume) {
        if (volume == null || volume.getVolumeInfo() == null) {
            return null;
        }

        Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
        String title = volumeInfo.getTitle() != null ? volumeInfo.getTitle() : "Unknown";

        String author = "Unknown";
        if (volumeInfo.getAuthors() != null && !volumeInfo.getAuthors().isEmpty()) {
            author = String.join(", ", volumeInfo.getAuthors());
        }

        String imagePath = null;
        if (volumeInfo.getImageLinks() != null) {
            if (volumeInfo.getImageLinks().getThumbnail() != null) {
                imagePath = volumeInfo.getImageLinks().getThumbnail();
            } else if (volumeInfo.getImageLinks().getSmallThumbnail() != null) {
                imagePath = volumeInfo.getImageLinks().getSmallThumbnail();
            }
            if (imagePath != null && imagePath.startsWith("http://")) {
                imagePath = imagePath.replaceFirst("http://", "https://");
            }
        }
        String description = volumeInfo.getDescription() != null ? volumeInfo.getDescription() : "No Description";
        String isbn = GoogleBookAPI.getISBN(volumeInfo);
        return new Book(title, author, description, isbn, imagePath);
    }
}

