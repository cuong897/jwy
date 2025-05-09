package com.library.Controller;

import com.library.Models.Book;
import com.library.Models.DatabaseConnection;
import com.library.Models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ADMINController implements Initializable {

    @FXML private AnchorPane adminRoot;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Void> removeColumn;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> availableColumn;

    @FXML private TextField searchBookToAddField;

    private CustomerDashboardController dashboardController;
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    public void setDashboardController(CustomerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // Sửa thành "name" nếu User.getName() là đúng
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("userName")); // Đảm bảo User có getUserName()
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber")); // Đảm bảo User có getPhoneNumber()
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));     // Đảm bảo User có getEmail()
        configureRemoveUserColumn();
        userTable.setItems(userList);
        loadUsers();

        // Cấu hình bảng sách
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableNumber"));
        bookTable.setItems(bookList);
        loadBooks();

        if (searchBookToAddField != null) {
            searchBookToAddField.setOnAction(event -> handleSearchBookToAdd());
        }
    }

    private void loadUsers() {
        userList.clear();
        String sql = "SELECT userID, fullName, username, phoneNumber, email, role FROM users WHERE role = 'CUSTOMER'"; // Ví dụ: Lấy tất cả user
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("userID"));
                user.setName(rs.getString("fullName")); // Gán cho trường name của User
                user.setUserName(rs.getString("username"));
                user.setPhoneNumber(rs.getString("phoneNumber"));
                user.setEmail(rs.getString("email"));
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load users: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Error", "Invalid role found in database: " + e.getMessage());
        }
    }

    private void configureRemoveUserColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<>() {
                    private final Button btn = new Button("Remove");
                    {
                        btn.setOnAction((ActionEvent event) -> {
                            User userToRemove = getTableView().getItems().get(getIndex());
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirm Removal");
                            alert.setHeaderText("Remove User: " + userToRemove.getName());
                            alert.setContentText("Are you sure you want to remove this user? This action will also return all their borrowed books.");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                removeUserFromDatabase(userToRemove);
                            }
                        });
                        btn.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white;");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            User user = getTableView().getItems().get(getIndex());
                            btn.setDisable(false);
                            btn.setText("Remove");
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        removeColumn.setCellFactory(cellFactory);
    }

    private void removeUserFromDatabase(User user) {
        String selectBorrowedBooksSql = "SELECT isbn FROM transactions WHERE userID = ?";
        String updateBookSql = "UPDATE books SET availableNumber = availableNumber + 1 WHERE isbn = ?";
        String deleteTransactionsSql = "DELETE FROM transactions WHERE userID = ?";
        String deleteUserSql = "DELETE FROM users WHERE userID = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not connect to database.");
                return;
            }
            conn.setAutoCommit(false);
            List<String> borrowedBookIsbns = new ArrayList<>();
            try (PreparedStatement pstmtSelectBorrowed = conn.prepareStatement(selectBorrowedBooksSql)) {
                pstmtSelectBorrowed.setInt(1, user.getUserID());
                try (ResultSet rs = pstmtSelectBorrowed.executeQuery()) {
                    while (rs.next()) {
                        borrowedBookIsbns.add(rs.getString("isbn"));
                    }
                }
            }

            // Bước 2: Cập nhật số lượng cho từng sách đã mượn
            if (!borrowedBookIsbns.isEmpty()) {
                try (PreparedStatement pstmtUpdateBook = conn.prepareStatement(updateBookSql)) {
                    for (String isbn : borrowedBookIsbns) {
                        pstmtUpdateBook.setString(1, isbn);
                        pstmtUpdateBook.addBatch();
                    }
                    pstmtUpdateBook.executeBatch();
                }
            }

            try (PreparedStatement pstmtTransactions = conn.prepareStatement(deleteTransactionsSql)) {
                pstmtTransactions.setInt(1, user.getUserID());
                pstmtTransactions.executeUpdate();
            }

            try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSql)) {
                pstmtUser.setInt(1, user.getUserID());
                int affectedRows = pstmtUser.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit();
                    userList.remove(user);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User " + user.getName() + " removed and their books returned.");
                    loadBooks();
                } else {
                    conn.rollback();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not remove user from database (user not found or no changes made).");
                }
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error removing user: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void loadBooks() {
        bookList.clear();
        String sql = "SELECT isbn, title, author, availableNumber, thumbnail FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book();
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setAvailableNumber(rs.getInt("availableNumber"));
                bookList.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load books: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchBookToAdd() {
        String searchQuery = searchBookToAddField.getText();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search", "Please enter a search query to find books to add.");
            return;
        }
        // System.out.println("Admin searching for book to add: " + searchQuery);

        if (dashboardController != null) {
            dashboardController.switchToAdminAddBookView(searchQuery);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/library/View/AdminAddBook.fxml"));
                Parent adminAddBookRoot = loader.load();
                AdminAddBookController adminAddBookController = loader.getController();
                adminAddBookController.initializeSearchWithQuery(searchQuery);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Add Book to Library");
                stage.setScene(new Scene(adminAddBookRoot));
                stage.showAndWait();
                loadBooks();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load the Add Book interface.");
            }
            System.err.println("DashboardController is null in ADMINController. Cannot switch to AdminAddBookView via dashboard.");
        }
    }
    public void refreshBookTableData() {
        Platform.runLater(this::loadBooks);
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public CustomerDashboardController getDashboardController() {
        return dashboardController;
    }
}