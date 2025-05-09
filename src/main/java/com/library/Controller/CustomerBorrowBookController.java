package com.library.Controller;

import com.google.api.services.books.v1.model.Volume;
import com.library.Models.Book;
import com.library.Models.Customer;
import com.library.Models.DatabaseConnection;
import com.library.Models.GoogleBookAPI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CustomerBorrowBookController implements Initializable {
    @FXML private AnchorPane rootPane;
    @FXML private Label statusLabel;

    @FXML private ImageView coverImageView;
    @FXML private Label titleLabel;
    @FXML private Button borrowButton;
    @FXML private Label authorLabel;
    @FXML private Label isbnLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private ListView<Book> booksListView;

    @FXML private Pane detailViewPane;

    private CustomerDashboardController parentDashboardController;
    private Customer currentCustomer;
    private Book selectedBookForDetail;

    private ObservableList<Book> bookObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        booksListView.setCellFactory(new Callback<ListView<Book>, ListCell<Book>>() {
            @Override
            public ListCell<Book> call(ListView<Book> listView) {
                return new ListCell<Book>() {
                    private ImageView imageView = new ImageView();
                    private Label label = new Label();
                    private HBox hbox = new HBox(10);
                    {
                        imageView.setPreserveRatio(true);
                        imageView.setFitHeight(60);
                        hbox.getChildren().addAll(imageView, label);

                    }

                    @Override
                    protected void updateItem(Book book, boolean empty) {
                        super.updateItem(book, empty);
                        if (empty || book == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String bookDisplayText = book.getTitle() + (book.getAuthor() != null ? " - " + book.getAuthor() : "");
                            label.setText(bookDisplayText);
                            label.setWrapText(true);

                            String thumbnailUrl = book.getThumbnailUrl();
                            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                                try {
                                    Image image = new Image(thumbnailUrl, true);
                                    imageView.setImage(image);
                                } catch (Exception e) {
                                    System.err.println("Không thể tải thumbnail: " + thumbnailUrl + " Lỗi: " + e.getMessage());
                                    imageView.setImage(null);
                                }
                            } else {
                                imageView.setImage(null);
                            }
                            setGraphic(hbox);
                            setText(null);
                        }
                    }
                };
            }
        });
        booksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                fetchFullBookDetailsAndDisplay(newValue.getIsbn(), newValue);
            } else {
                if (detailViewPane != null) detailViewPane.setVisible(false);
            }
        });

        if (detailViewPane != null) detailViewPane.setVisible(false);

        borrowButton.setOnAction(event -> {
            if (selectedBookForDetail != null && selectedBookForDetail.getAvailableNumber() > 0) {
                processBorrowing(selectedBookForDetail);
            } else {
                showAlert(Alert.AlertType.WARNING, "Không thể mượn", "Sách này không có sẵn hoặc chưa chọn sách.");
            }
        });
    }

    public void setParentDashboardController(CustomerDashboardController parentController) {
        this.parentDashboardController = parentController;
        if (this.parentDashboardController != null) {
            this.currentCustomer = this.parentDashboardController.getCurrentCustomer();
        }
    }

    public void updateSearchBooksResult(String query) {
        if (query == null || query.trim().isEmpty()) {
            bookObservableList.clear();
            if (detailViewPane != null) detailViewPane.setVisible(false);
            return;
        }
        if (detailViewPane != null) detailViewPane.setVisible(false);

        Task<List<Book>> searchTask = new Task<>() {
            @Override
            protected List<Book> call() throws Exception {
                List<Volume> volumes = GoogleBookAPI.searchBooks(query);
                if (volumes != null) {
                    return volumes.stream()
                            .map(CustomerBorrowBookController::convertVolumeToBook)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
                return new ArrayList<>();
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<Book> books = searchTask.getValue();
            bookObservableList.setAll(books);
            booksListView.setItems(bookObservableList);
            if (books.isEmpty()) {
            }
        });

        searchTask.setOnFailed(event -> {
            bookObservableList.clear();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tìm Kiếm API", "Đã có lỗi xảy ra khi tìm sách qua Google Books API.");
            try {
                if (searchTask.getException() != null) throw searchTask.getException();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        new Thread(searchTask).start();
    }

    private static Book convertVolumeToBook(Volume volume) {
        if (volume == null || volume.getVolumeInfo() == null) return null;
        Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
        Book book = new Book();
        book.setIsbn(GoogleBookAPI.getISBN(volumeInfo));
        if (book.getIsbn() == null) return null;
        book.setTitle(volumeInfo.getTitle());
        book.setAuthor(volumeInfo.getAuthors() != null ? String.join(", ", volumeInfo.getAuthors()) : "N/A");
        book.setDescription(volumeInfo.getDescription());

        String thumbnailUrl = null;
        if (volumeInfo.getImageLinks() != null) {
            thumbnailUrl = volumeInfo.getImageLinks().getThumbnail();
            if (thumbnailUrl == null) {
                thumbnailUrl = volumeInfo.getImageLinks().getSmallThumbnail();
            }
            if (thumbnailUrl != null && thumbnailUrl.startsWith("http://")) {
                thumbnailUrl = thumbnailUrl.replaceFirst("http://", "https://");
            }
        }
        book.setThumbnailUrl(thumbnailUrl);
        book.setAvailableNumber(0);
        return book;
    }

    private void fetchFullBookDetailsAndDisplay(String isbn, Book apiBookInfo) {
        if (isbn == null) {
            if (detailViewPane != null) detailViewPane.setVisible(false);
            return;
        }

        Task<Book> fetchDbTask = new Task<>() {
            @Override
            protected Book call() throws Exception {
                Book bookFromDb = getBookDetailsFromLocalDBIncludingAvailability(isbn);
                if (bookFromDb != null) {
                    apiBookInfo.setAvailableNumber(bookFromDb.getAvailableNumber());
                    if (bookFromDb.getTitle() != null) apiBookInfo.setTitle(bookFromDb.getTitle());
                    if (bookFromDb.getAuthor() != null) apiBookInfo.setAuthor(bookFromDb.getAuthor());
                    if (bookFromDb.getThumbnailUrl() != null) {
                        apiBookInfo.setThumbnailUrl(bookFromDb.getThumbnailUrl());
                    }
                    return apiBookInfo;
                } else {
                    apiBookInfo.setAvailableNumber(0);
                    return apiBookInfo;
                }
            }
        };

        fetchDbTask.setOnSucceeded(event -> {
            selectedBookForDetail = fetchDbTask.getValue();
            if (selectedBookForDetail != null) {
                displayBookDetails(selectedBookForDetail);
            } else {
                if (detailViewPane != null) detailViewPane.setVisible(false);
            }
        });

        fetchDbTask.setOnFailed(event -> {
            if (detailViewPane != null) detailViewPane.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy chi tiết sách từ thư viện.");
            if (fetchDbTask.getException() != null) fetchDbTask.getException().printStackTrace();
        });
        new Thread(fetchDbTask).start();
    }
    private Book getBookDetailsFromLocalDBIncludingAvailability(String isbn) {
        String sql = "SELECT title, author, description, availableNumber, thumbnail FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Book book = new Book();
                book.setIsbn(isbn);
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setDescription(rs.getString("description"));
                book.setAvailableNumber(rs.getInt("availableNumber"));
                book.setThumbnailUrl(rs.getString("thumbnail"));
                return book;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void displayBookDetails(Book book) {
        if (book == null) {
            if(detailViewPane != null) detailViewPane.setVisible(false);
            return;
        }

        if(detailViewPane != null) detailViewPane.setVisible(true);
        titleLabel.setText(book.getTitle() != null ? book.getTitle() : "N/A");
        authorLabel.setText("Tác giả: " + (book.getAuthor() != null ? book.getAuthor() : "N/A"));
        isbnLabel.setText("ISBN: " + (book.getIsbn() != null ? book.getIsbn() : "N/A"));
        descriptionTextArea.setText(book.getDescription() != null ? book.getDescription() : "Không có mô tả.");
        statusLabel.setText( book.getAvailableNumber() > 0 ? "Có sẵn" : "Không có sẵn" );
        try {
            String imageUrl = book.getThumbnailUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image img = new Image(imageUrl, true);
                coverImageView.setImage(img);
            } else {
                coverImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/default_book_cover.png"))));
            }
        } catch (Exception e) {
            coverImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/default_book_cover.png"))));
        }

        Book bookFromDb = getBookDetailsFromLocalDBIncludingAvailability(book.getIsbn());
        int availableCount = 0;
        boolean inLibrary = false;
        if (bookFromDb != null) {
            availableCount = bookFromDb.getAvailableNumber();
            inLibrary = true;
        }


        if (availableCount > 0) {
            borrowButton.setDisable(false);
            borrowButton.setText("Mượn");
        } else {
            if (!inLibrary) {
                borrowButton.setText("Không có ở thư viện");
            } else {
                borrowButton.setText("Đã hết");
            }
            borrowButton.setDisable(true);
        }
        if(selectedBookForDetail != null && bookFromDb != null && selectedBookForDetail.getIsbn().equals(bookFromDb.getIsbn())){
            selectedBookForDetail.setAvailableNumber(availableCount);
        } else if (selectedBookForDetail != null && !inLibrary) {
            selectedBookForDetail.setAvailableNumber(0);
        }
    }

    private void processBorrowing(Book bookToBorrow) {
        if (bookToBorrow == null || currentCustomer == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thông tin không đủ để mượn sách (thiếu sách hoặc người dùng).");
            return;
        }
        if (bookToBorrow.getIsbn() == null || bookToBorrow.getIsbn().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Sách", "Sách không có mã ISBN hợp lệ.");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            String checkAvailabilitySql = "SELECT availableNumber FROM books WHERE isbn = ? FOR UPDATE";
            int currentAvailable = -1;
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkAvailabilitySql)) {
                pstmtCheck.setString(1, bookToBorrow.getIsbn());
                ResultSet rsCheck = pstmtCheck.executeQuery();
                if (rsCheck.next()) {
                    currentAvailable = rsCheck.getInt("availableNumber");
                    if (currentAvailable <= 0) {
                        conn.rollback();
                        showAlert(Alert.AlertType.WARNING, "Hết sách", "Xin lỗi, sách này đã hết.");
                        if (selectedBookForDetail != null && selectedBookForDetail.getIsbn().equals(bookToBorrow.getIsbn())) {
                            selectedBookForDetail.setAvailableNumber(0);
                            displayBookDetails(selectedBookForDetail);
                        }
                        return;
                    }
                } else {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Sách không tồn tại trong hệ thống thư viện.");
                    return;
                }
            }

            String updateBookSql = "UPDATE books SET availableNumber = availableNumber - 1 WHERE isbn = ?";
            try (PreparedStatement pstmtUpdateBook = conn.prepareStatement(updateBookSql)) {
                pstmtUpdateBook.setString(1, bookToBorrow.getIsbn());
                int affectedRows = pstmtUpdateBook.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật", "Không thể cập nhật số lượng sách.");
                    return;
                }
            }
            String updateBorrowCountSql = "UPDATE books SET borrowCount = borrowCount + 1 WHERE isbn = ?";
            try (PreparedStatement pstmtUpdateBorrowCount = conn.prepareStatement(updateBorrowCountSql)) {
                pstmtUpdateBorrowCount.setString(1, bookToBorrow.getIsbn());
                pstmtUpdateBorrowCount.executeUpdate();
            }
            String insertTransactionSql = "INSERT INTO transactions (userID, isbn) VALUES (?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertTransactionSql)) {
                pstmtInsert.setInt(1, currentCustomer.getUserID());
                pstmtInsert.setString(2, bookToBorrow.getIsbn());
                pstmtInsert.executeUpdate();
            }


            conn.commit();
            if (parentDashboardController != null) {
                parentDashboardController.notifyRankingNeedsRefresh();
            }
            Book borrowedBookEntry = new Book();
            borrowedBookEntry.setIsbn(bookToBorrow.getIsbn());
            borrowedBookEntry.setTitle(bookToBorrow.getTitle());
            borrowedBookEntry.setAuthor(bookToBorrow.getAuthor());
            borrowedBookEntry.setThumbnailUrl(bookToBorrow.getThumbnailUrl());
            currentCustomer.getCurrentlyBorrowedBookList().add(borrowedBookEntry);

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mượn sách '" + bookToBorrow.getTitle() + "' thành công!");
            int newAvailableCount = currentAvailable - 1;
            if (selectedBookForDetail != null && selectedBookForDetail.getIsbn().equals(bookToBorrow.getIsbn())) {
                selectedBookForDetail.setAvailableNumber(newAvailableCount);
                displayBookDetails(selectedBookForDetail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Xảy ra lỗi khi mượn sách: " + e.getMessage());
            if (conn != null) {
                try {
                    if (!conn.getAutoCommit()) conn.rollback();
                } catch (SQLException exRollback) { exRollback.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}