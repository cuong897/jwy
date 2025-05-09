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
// import java.sql.Date; //  <--- KHÔNG CẦN NẾU CHỈ DÙNG CHO borrowDate BỊ XÓA
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import java.time.LocalDate; // <--- KHÔNG CẦN NẾU CHỈ DÙNG CHO borrowDate BỊ XÓA, nhưng có thể giữ nếu dùng ở chỗ khác
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CustomerBorrowBookController implements Initializable {
    // ... (các phần khác của class không đổi) ...
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
        booksListView.setCellFactory(new Callback<>() { // Sử dụng diamond operator cho gọn
            @Override
            public ListCell<Book> call(ListView<Book> listView) {
                return new ListCell<>() { // Sử dụng diamond operator
                    private final ImageView imageView = new ImageView(); // Khai báo final nếu không gán lại
                    private final Label label = new Label();
                    private final HBox hbox = new HBox(10);
                    {
                        imageView.setPreserveRatio(true);
                        imageView.setFitHeight(60); // Kích thước thumbnail trong danh sách
                        hbox.getChildren().addAll(imageView, label);
                        // Có thể thêm CSS styling cho HBox hoặc Label ở đây nếu muốn
                    }

                    @Override
                    protected void updateItem(Book book, boolean empty) {
                        super.updateItem(book, empty);
                        if (empty || book == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String bookDisplayText = book.getTitle() + (book.getAuthor() != null && !book.getAuthor().isEmpty() ? " - " + book.getAuthor() : "");
                            label.setText(bookDisplayText);
                            label.setWrapText(true); // Cho phép xuống dòng nếu tên sách/tác giả quá dài

                            String thumbnailUrl = book.getThumbnailUrl();
                            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                                try {
                                    // Sử dụng background loading cho Image để không block UI thread
                                    Image image = new Image(thumbnailUrl, true); // true cho background loading
                                    imageView.setImage(image);
                                } catch (IllegalArgumentException e) {
                                    System.err.println("URL ảnh không hợp lệ: " + thumbnailUrl + " Lỗi: " + e.getMessage());
                                    setDefaultBookImageInList();
                                } catch (Exception e) {
                                    System.err.println("Không thể tải thumbnail cho danh sách: " + thumbnailUrl + " Lỗi: " + e.getMessage());
                                    setDefaultBookImageInList();
                                }
                            } else {
                                setDefaultBookImageInList();
                            }
                            setGraphic(hbox);
                            setText(null); // Đảm bảo không có text nào khác hiển thị cùng HBox
                        }
                    }
                    private void setDefaultBookImageInList() {
                        try {
                            // Cân nhắc dùng ảnh mặc định nhỏ hơn cho danh sách
                            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/default_book_cover_small.png"))));
                        } catch (NullPointerException e) {
                            System.err.println("Không tìm thấy ảnh mặc định '/Image/default_book_cover_small.png'");
                            imageView.setImage(null);
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
            if (this.currentCustomer == null) {
                System.err.println("CustomerBorrowBookController: currentCustomer is null sau khi set parentDashboardController. Chức năng mượn sách có thể không hoạt động.");
                // Cân nhắc disable borrowButton nếu currentCustomer là null
                // borrowButton.setDisable(true);
            }
        } else {
            System.err.println("CustomerBorrowBookController: parentDashboardController is null. Chức năng thông báo cập nhật ranking sẽ không hoạt động.");
        }
    }

    public void updateSearchBooksResult(String query) {
        // System.out.println("updateSearchBooksResult called with query: " + query); // Gỡ bỏ hoặc dùng logger
        if (query == null || query.trim().isEmpty()) {
            bookObservableList.clear();
            if (detailViewPane != null) detailViewPane.setVisible(false);
            return;
        }
        if (detailViewPane != null) detailViewPane.setVisible(false); // Ẩn chi tiết khi tìm kiếm mới

        Task<List<Book>> searchTask = new Task<>() {
            @Override
            protected List<Book> call() throws Exception {
                List<Volume> volumes = GoogleBookAPI.searchBooks(query);
                if (volumes != null) {
                    return volumes.stream()
                            .map(CustomerBorrowBookController::convertVolumeToBook)
                            .filter(Objects::nonNull) // Lọc bỏ những sách không có ISBN hoặc lỗi chuyển đổi
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
                // System.out.println("Không tìm thấy sách nào cho query: " + query);
                // Cân nhắc hiển thị thông báo "Không tìm thấy kết quả" trên UI
            }
        });

        searchTask.setOnFailed(event -> {
            bookObservableList.clear();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tìm Kiếm API", "Đã có lỗi xảy ra khi tìm sách qua Google Books API.");
            Throwable e = searchTask.getException();
            if (e != null) {
                e.printStackTrace();
            }
        });
        new Thread(searchTask).start();
    }

    private static Book convertVolumeToBook(Volume volume) {
        if (volume == null || volume.getVolumeInfo() == null) return null;
        Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

        String isbn = GoogleBookAPI.getISBN(volumeInfo);
        if (isbn == null || isbn.trim().isEmpty()) {
            // System.out.println("Bỏ qua sách không có ISBN hợp lệ: " + volumeInfo.getTitle());
            return null; // ISBN là bắt buộc
        }

        Book book = new Book();
        book.setIsbn(isbn);
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
        book.setAvailableNumber(0); // Sẽ được cập nhật từ DB
        return book;
    }

    private void fetchFullBookDetailsAndDisplay(String isbn, Book apiBookInfo) {
        if (isbn == null || isbn.trim().isEmpty()) {
            if (detailViewPane != null) detailViewPane.setVisible(false);
            System.err.println("fetchFullBookDetailsAndDisplay: ISBN bị null hoặc rỗng.");
            return;
        }

        Task<Book> fetchDbTask = new Task<>() {
            @Override
            protected Book call() throws Exception {
                Book bookFromDb = getBookDetailsFromLocalDBIncludingAvailability(isbn);
                // Tạo một đối tượng Book mới để hiển thị, kết hợp thông tin từ API và DB
                Book displayBook = new Book();
                displayBook.setIsbn(isbn);

                // Ưu tiên thông tin từ API cho một số trường, hoặc từ DB nếu API không có
                displayBook.setTitle(apiBookInfo.getTitle() != null ? apiBookInfo.getTitle() : (bookFromDb != null ? bookFromDb.getTitle() : "N/A"));
                displayBook.setAuthor(apiBookInfo.getAuthor() != null ? apiBookInfo.getAuthor() : (bookFromDb != null ? bookFromDb.getAuthor() : "N/A"));
                displayBook.setDescription(apiBookInfo.getDescription() != null ? apiBookInfo.getDescription() : (bookFromDb != null ? bookFromDb.getDescription() : "Không có mô tả."));
                displayBook.setThumbnailUrl(apiBookInfo.getThumbnailUrl() != null ? apiBookInfo.getThumbnailUrl() : (bookFromDb != null ? bookFromDb.getThumbnailUrl() : null));

                if (bookFromDb != null) {
                    displayBook.setAvailableNumber(bookFromDb.getAvailableNumber());
                    displayBook.setBorrowCount(bookFromDb.getBorrowCount());
                    // Nếu muốn ghi đè hoàn toàn bằng thông tin DB nếu có:
                    // if (bookFromDb.getTitle() != null) displayBook.setTitle(bookFromDb.getTitle());
                    // ...
                } else {
                    // Sách từ API nhưng không (chưa) có trong thư viện local
                    displayBook.setAvailableNumber(0);
                    displayBook.setBorrowCount(0); // Mặc định borrowCount là 0 nếu không có trong DB
                }
                return displayBook;
            }
        };

        fetchDbTask.setOnSucceeded(event -> {
            selectedBookForDetail = fetchDbTask.getValue(); // Đây là sách sẽ được dùng để mượn
            if (selectedBookForDetail != null) {
                displayBookDetails(selectedBookForDetail);
            } else {
                // Xử lý trường hợp không lấy được chi tiết sách (hiếm khi xảy ra nếu logic call() đúng)
                if (detailViewPane != null) detailViewPane.setVisible(false);
                // Có thể hiển thị thông tin từ apiBookInfo với trạng thái "Không rõ"
                // displayBookDetails(apiBookInfo); // Và set available = 0
            }
        });

        fetchDbTask.setOnFailed(event -> {
            if (detailViewPane != null) detailViewPane.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy chi tiết sách từ thư viện.");
            Throwable e = fetchDbTask.getException();
            if (e != null) e.printStackTrace();
        });
        new Thread(fetchDbTask).start();
    }

    private Book getBookDetailsFromLocalDBIncludingAvailability(String isbn) {
        String sql = "SELECT title, author, description, availableNumber, thumbnail, borrowCount FROM books WHERE isbn = ?";
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
                book.setBorrowCount(rs.getInt("borrowCount"));
                return book;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn chi tiết sách từ DB cho ISBN " + isbn + ": " + e.getMessage());
            // e.printStackTrace(); // Gỡ bỏ hoặc dùng logger
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

        // Cập nhật ảnh bìa
        try {
            String imageUrl = book.getThumbnailUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image img = new Image(imageUrl, true); // true để tải nền
                coverImageView.setImage(img);
            } else {
                setDefaultCoverImageInDetailView();
            }
        } catch (Exception e) { // Bắt Exception rộng hơn để xử lý các lỗi URL không hợp lệ, etc.
            System.err.println("Lỗi khi tải ảnh bìa chi tiết: " + book.getThumbnailUrl() + " - " + e.getMessage());
            setDefaultCoverImageInDetailView();
        }

        // `book` ở đây là `selectedBookForDetail` đã được tổng hợp thông tin
        int availableCount = book.getAvailableNumber();
        // Kiểm tra xem sách có thực sự tồn tại trong DB local không (quan trọng cho nút Mượn/Không có ở thư viện)
        boolean inLocalLibrary = (getBookDetailsFromLocalDBIncludingAvailability(book.getIsbn()) != null);

        if (availableCount > 0) {
            statusLabel.setText("Có sẵn: " + availableCount);
            borrowButton.setText("Mượn sách");
            borrowButton.setDisable(false);
        } else {
            if (!inLocalLibrary) {
                // Sách từ API nhưng không có trong DB thư viện
                statusLabel.setText("Không có trong thư viện");
                borrowButton.setText("Không có ở thư viện");
            } else {
                // Sách có trong DB thư viện nhưng đã hết (availableNumber = 0)
                statusLabel.setText("Đã hết sách");
                borrowButton.setText("Đã hết sách");
            }
            borrowButton.setDisable(true);
        }
    }

    private void setDefaultCoverImageInDetailView() {
        try {
            coverImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/default_book_cover.png"))));
        } catch (NullPointerException e) {
            System.err.println("Không tìm thấy ảnh bìa mặc định: /Image/default_book_cover.png");
            coverImageView.setImage(null);
        }
    }


    private void processBorrowing(Book bookToBorrow) {
        if (bookToBorrow == null || currentCustomer == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Thông tin không đủ để mượn sách (thiếu sách hoặc người dùng).");
            return;
        }
        if (bookToBorrow.getIsbn() == null || bookToBorrow.getIsbn().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Sách", "Sách không có mã ISBN hợp lệ.");
            return;
        }
        if (parentDashboardController == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể kết nối với dashboard chính để cập nhật. Vui lòng thử lại sau.");
            System.err.println("processBorrowing: parentDashboardController is null. Cannot notify for ranking update.");
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
                        showAlert(Alert.AlertType.WARNING, "Hết sách", "Xin lỗi, cuốn sách '" + bookToBorrow.getTitle() + "' đã được mượn hết.");
                        if (selectedBookForDetail != null && selectedBookForDetail.getIsbn().equals(bookToBorrow.getIsbn())) {
                            selectedBookForDetail.setAvailableNumber(0);
                            displayBookDetails(selectedBookForDetail);
                        }
                        return;
                    }
                } else {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi Sách", "Sách '" + bookToBorrow.getTitle() + "' không tồn tại trong hệ thống thư viện.");
                    return;
                }
            }

            String updateBookSql = "UPDATE books SET availableNumber = availableNumber - 1 WHERE isbn = ?";
            try (PreparedStatement pstmtUpdateBook = conn.prepareStatement(updateBookSql)) {
                pstmtUpdateBook.setString(1, bookToBorrow.getIsbn());
                int affectedRows = pstmtUpdateBook.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật Sách", "Không thể cập nhật số lượng cho sách '" + bookToBorrow.getTitle() + "'.");
                    return;
                }
            }

            String updateBorrowCountSql = "UPDATE books SET borrowCount = borrowCount + 1 WHERE isbn = ?";
            try (PreparedStatement pstmtUpdateBorrowCount = conn.prepareStatement(updateBorrowCountSql)) {
                pstmtUpdateBorrowCount.setString(1, bookToBorrow.getIsbn());
                int affectedRowsBorrow = pstmtUpdateBorrowCount.executeUpdate();
                if (affectedRowsBorrow == 0) {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật Lượt Mượn", "Không thể cập nhật số lần mượn cho sách '" + bookToBorrow.getTitle() + "'.");
                    return;
                }
            }

            // ***** BỎ CỘT borrowDate KHỎI CÂU LỆNH INSERT *****
            String insertTransactionSql = "INSERT INTO transactions (userID, isbn) VALUES (?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertTransactionSql)) {
                pstmtInsert.setInt(1, currentCustomer.getUserID());
                pstmtInsert.setString(2, bookToBorrow.getIsbn());
                // KHÔNG CÓ pstmtInsert.setDate(...) nữa
                pstmtInsert.executeUpdate();
            }

            conn.commit();

            Book borrowedBookEntry = getBookDetailsFromLocalDBIncludingAvailability(bookToBorrow.getIsbn());
            if (borrowedBookEntry != null) {
                currentCustomer.getCurrentlyBorrowedBookList().add(borrowedBookEntry);
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Mượn sách '" + bookToBorrow.getTitle() + "' thành công!");

            int newAvailableCount = currentAvailable - 1;
            if (selectedBookForDetail != null && selectedBookForDetail.getIsbn().equals(bookToBorrow.getIsbn())) {
                selectedBookForDetail.setAvailableNumber(newAvailableCount);
                displayBookDetails(selectedBookForDetail);
            }

            parentDashboardController.notifyBookBorrowedAndUpdateRanking();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Xảy ra lỗi khi thực hiện mượn sách: " + e.getMessage());
            if (conn != null) {
                try {
                    if (!conn.getAutoCommit()) conn.rollback();
                } catch (SQLException exRollback) {
                    System.err.println("Lỗi nghiêm trọng khi rollback transaction: " + exRollback.getMessage());
                    exRollback.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng kết nối cơ sở dữ liệu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            });
        }
    }
}