package com.library.Controller;


import com.google.api.services.books.v1.model.Volume;
import com.library.Models.Book;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class AdminAddBookController implements Initializable {


    @FXML private AnchorPane rootPane;
    @FXML private ImageView coverImageView;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label isbnLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private ListView<Book> booksListView;
    @FXML private Pane detailViewPane;


    @FXML private TextField quantityField;
    @FXML private Button addBookButton;


    private CustomerDashboardController dashboardController;
    private Book selectedBookFromAPI;
    private ObservableList<Book> bookObservableList = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        booksListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Book> call(ListView<Book> listView) {
                return new ListCell<>() {
                    private final ImageView imageView = new ImageView();
                    private final Label label = new Label();
                    private final HBox hbox = new HBox(10, imageView, label);
                    {
                        imageView.setPreserveRatio(true);
                        imageView.setFitHeight(60);
                        label.setWrapText(true);
                        label.prefWidthProperty().bind(hbox.widthProperty().subtract(imageView.fitWidthProperty()).subtract(20));
                    }


                    @Override
                    protected void updateItem(Book book, boolean empty) {
                        super.updateItem(book, empty);
                        if (empty || book == null || book.getTitle() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String bookDisplayText = book.getTitle() + (book.getAuthor() != null && !book.getAuthor().equals("N/A") ? " - " + book.getAuthor() : "");
                            label.setText(bookDisplayText);
                            String thumbnailUrl = book.getThumbnailUrl();
                            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                                try {
                                    Image image = new Image(thumbnailUrl, true);
                                    imageView.setImage(image);
                                } catch (Exception e) {
                                    imageView.setImage(null);
                                }
                            } else {
                                imageView.setImage(null);
                            }
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });


        booksListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedBookFromAPI = newValue;
                displayBookDetailsPanel(selectedBookFromAPI); // Chỉ hiển thị thông tin từ API
            } else {
                if (detailViewPane != null) detailViewPane.setVisible(false);
                selectedBookFromAPI = null;
            }
        });


        if (detailViewPane != null) detailViewPane.setVisible(false);
        if (addBookButton != null) {
            addBookButton.setOnAction(event -> handleAddBookToLibraryAction());
        }
        quantityField.setOnAction(event -> handleAddBookToLibraryAction());
        if (quantityField != null) {
            quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    quantityField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }
    }


    public void initializeSearchWithQuery(String query) {
        if (query != null && !query.trim().isEmpty()) {
            updateSearchBooksResult(query);
        }
    }


    public void setDashboardController(CustomerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
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
                            .map(AdminAddBookController::convertVolumeToLocalBook)
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
                booksListView.setPlaceholder(new Label("No books found for '" + query + "' on Google Books."));
            } else {
                booksListView.setPlaceholder(null);
            }
        });
        searchTask.setOnFailed(event -> { /* ... xử lý lỗi ... */ });
        new Thread(searchTask).start();
    }


    private static Book convertVolumeToLocalBook(Volume volume) {
        if (volume == null || volume.getVolumeInfo() == null) return null;
        Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
        String isbn = GoogleBookAPI.getISBN(volumeInfo);
        if (isbn == null) return null;


        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(volumeInfo.getTitle() != null ? volumeInfo.getTitle() : "N/A");
        book.setAuthor(volumeInfo.getAuthors() != null ? String.join(", ", volumeInfo.getAuthors()) : "N/A");
        book.setDescription(volumeInfo.getDescription());
        String thumbnailUrl = null;
        if (volumeInfo.getImageLinks() != null) {
            thumbnailUrl = volumeInfo.getImageLinks().getThumbnail();
            if (thumbnailUrl == null) thumbnailUrl = volumeInfo.getImageLinks().getSmallThumbnail();
            if (thumbnailUrl != null && thumbnailUrl.startsWith("http://")) {
                thumbnailUrl = thumbnailUrl.replaceFirst("http://", "https://");
            }
        }
        book.setThumbnailUrl(thumbnailUrl);
        return book;
    }


    private void displayBookDetailsPanel(Book apiBook) {
        if (apiBook == null) {
            if(detailViewPane != null) detailViewPane.setVisible(false);
            return;
        }
        if(detailViewPane != null) detailViewPane.setVisible(true);


        titleLabel.setText(apiBook.getTitle() != null ? apiBook.getTitle() : "N/A");
        authorLabel.setText("Tác giả: " + (apiBook.getAuthor() != null ? apiBook.getAuthor() : "N/A"));
        isbnLabel.setText("ISBN: " + (apiBook.getIsbn() != null ? apiBook.getIsbn() : "N/A"));
        descriptionTextArea.setText(apiBook.getDescription() != null ? apiBook.getDescription() : "Không có mô tả.");


        try {
            String imageUrl = apiBook.getThumbnailUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image img = new Image(imageUrl, true);
                coverImageView.setImage(img);
            } else {
                coverImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/default_book_cover.png"))));
            }
        } catch (Exception e) {
            coverImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/default_book_cover.png"))));
        }


        quantityField.clear();
        addBookButton.setDisable(false);
    }


    @FXML
    private void handleAddBookToLibraryAction() {
        if (selectedBookFromAPI == null || selectedBookFromAPI.getIsbn() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn sách", "Vui lòng chọn một sách từ danh sách kết quả.");
            return;
        }


        String quantityText = quantityField.getText();
        if (quantityText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu số lượng", "Vui lòng nhập số lượng sách muốn thêm.");
            return;
        }


        int quantityToAdd;
        try {
            quantityToAdd = Integer.parseInt(quantityText);
            if (quantityToAdd <= 0) {
                showAlert(Alert.AlertType.WARNING, "Số lượng không hợp lệ", "Số lượng sách phải là số dương.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Số lượng không hợp lệ", "Vui lòng nhập một số nguyên cho số lượng.");
            return;
        }


        String isbn = selectedBookFromAPI.getIsbn();
        boolean bookExistsInDB = checkBookExists(isbn); // Tạo hàm này để kiểm tra


        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể kết nối đến cơ sở dữ liệu.");
                return;
            }
            conn.setAutoCommit(false);


            if (bookExistsInDB) {
                String sqlUpdate = "UPDATE books SET availableNumber = availableNumber + ? WHERE isbn = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                    pstmt.setInt(1, quantityToAdd);
                    pstmt.setString(2, isbn);
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cộng thêm " + quantityToAdd + " cuốn cho sách '" + selectedBookFromAPI.getTitle() + "'.");
                }
            } else {
                String sqlInsert = "INSERT INTO books (isbn, title, author, description, thumbnail, availableNumber) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    pstmt.setString(1, selectedBookFromAPI.getIsbn());
                    pstmt.setString(2, selectedBookFromAPI.getTitle());
                    pstmt.setString(3, selectedBookFromAPI.getAuthor());
                    String desc = selectedBookFromAPI.getDescription();
                    if (desc != null && desc.length() > 1000) {
                        desc = desc.substring(0, 997) + "...";
                    }
                    pstmt.setString(4, desc);
                    pstmt.setString(5, selectedBookFromAPI.getThumbnailUrl());
                    pstmt.setInt(6, quantityToAdd);
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm sách mới '" + selectedBookFromAPI.getTitle() + "' với số lượng " + quantityToAdd + " vào thư viện.");
                }
            }
            conn.commit();
            quantityField.clear();


            if (dashboardController != null) {
                Object adminControllerInstance = dashboardController.getLoadedControllerInstance("/com/library/View/ADMIN.fxml");
                if (adminControllerInstance instanceof ADMINController) {
                    ((ADMINController) adminControllerInstance).refreshBookTableData();
                }
            }


        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Xảy ra lỗi khi thêm/cập nhật sách: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private boolean checkBookExists(String isbn) {
        String sql = "SELECT 1 FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}