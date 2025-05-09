package com.library.Controller;

import com.library.Models.Book;
import com.library.Models.Customer;
import com.library.Models.DatabaseConnection;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class CustomerMyLibraryController implements Initializable {

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnCol;
    @FXML private TableColumn<Book, String> titleCol;
    @FXML private TableColumn<Book, String> authorCol;
    @FXML private TableColumn<Book, Boolean> returnCol;

    @FXML private Button switchToReturnModeButton;
    @FXML private Button backButton;
    @FXML private Button okButton;

    private CustomerDashboardController dashboardController;
    private Customer currentCustomer;

    private ObservableList<Book> currentlyBorrowedBooksList = FXCollections.observableArrayList();
    private boolean inReturnMode = false;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        returnCol.setCellValueFactory(new PropertyValueFactory<>("selectedForReturn"));
        returnCol.setCellFactory(CheckBoxTableCell.forTableColumn(returnCol));
        returnCol.setEditable(false);
        returnCol.setText("Return");

        bookTable.setItems(currentlyBorrowedBooksList);
        bookTable.setEditable(true);

        if (switchToReturnModeButton != null) {
            switchToReturnModeButton.setOnAction(event -> handleSwitchToReturnMode());
        }
        if (backButton != null) {
            backButton.setOnAction(event -> handleBackButton());
        }
        if (okButton != null) {
            okButton.setOnAction(event -> handleOkReturnSelectedButton());
        }
    }

    public void setDashboardController(CustomerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
        if (this.dashboardController != null) {
            this.currentCustomer = this.dashboardController.getCurrentCustomer();
            if (this.currentCustomer != null) {
                loadBorrowedBooks();
            } else {
                System.err.println("CustomerMyLibraryController: Customer data is null via DashboardController.");
                currentlyBorrowedBooksList.clear();
                bookTable.setPlaceholder(new Label("No customer data. Please log in again."));
            }
        } else {
            System.err.println("CustomerMyLibraryController: DashboardController is null.");
            currentlyBorrowedBooksList.clear();
            bookTable.setPlaceholder(new Label("System error: Dashboard context not available."));
        }
    }

    private void loadBorrowedBooks() {
        if (this.currentCustomer == null) {
            currentlyBorrowedBooksList.clear();
            bookTable.setPlaceholder(new Label("No customer data available."));
            return;
        }

        exitReturnMode();
        currentlyBorrowedBooksList.clear();

        String sql = "SELECT b.isbn, b.title, b.author, b.thumbnail " +
                "FROM books b JOIN transactions t ON b.isbn = t.isbn " +
                "WHERE t.userID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentCustomer.getUserID());
            ResultSet rs = pstmt.executeQuery();

            List<Book> booksFromDb = new ArrayList<>();
            while (rs.next()) {
                Book book = new Book();
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setThumbnailUrl(rs.getString("thumbnail"));
                book.setSelectedForReturn(false);
                booksFromDb.add(book);
            }
            currentlyBorrowedBooksList.addAll(booksFromDb); // Thêm tất cả vào ObservableList

            if (this.currentCustomer != null) {
                this.currentCustomer.setCurrentlyBorrowedBookList(new ArrayList<>(booksFromDb));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load borrowed books: " + e.getMessage());
        }
        bookTable.refresh();
        if (currentlyBorrowedBooksList.isEmpty()) {
            bookTable.setPlaceholder(new Label("You have no books currently borrowed."));
        }
    }

    @FXML
    private void handleSwitchToReturnMode() {
        if (currentlyBorrowedBooksList.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Books", "There are no books to return.");
            return;
        }

        inReturnMode = true;
        switchToReturnModeButton.setVisible(false);
        backButton.setVisible(true);
        okButton.setVisible(true);

        returnCol.setText("Select");
        returnCol.setEditable(true);

        for (Book book : currentlyBorrowedBooksList) {
            book.setSelectedForReturn(false);
        }
        bookTable.refresh();
    }

    @FXML
    private void handleBackButton() {
        exitReturnMode();
    }

    private void exitReturnMode() {
        inReturnMode = false;
        switchToReturnModeButton.setVisible(true);
        backButton.setVisible(false);
        okButton.setVisible(false);
        returnCol.setText("Return");
        returnCol.setEditable(false);

        for (Book book : currentlyBorrowedBooksList) {
            book.setSelectedForReturn(false);
        }
        bookTable.refresh();
    }

    @FXML
    private void handleOkReturnSelectedButton() {
        if (!inReturnMode || this.currentCustomer == null) return;
        List<Book> selectedBooks = currentlyBorrowedBooksList.stream()
                .filter(Book::isSelectedForReturn)
                .collect(Collectors.toList());
        if (selectedBooks.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select at least one book to return.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Return");
        confirmAlert.setHeaderText("Are you sure you want to return " + selectedBooks.size() + " book(s)?");
        StringBuilder bookTitles = new StringBuilder();
        for (Book book : selectedBooks) {
            bookTitles.append("- ").append(book.getTitle()).append("\n");
        }
        confirmAlert.setContentText(bookTitles.toString());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processReturnBooks(selectedBooks);
        }
    }


    private void processReturnBooks(List<Book> booksToReturn) {
        if (this.currentCustomer == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User session error. Please try again.");
            return;
        }

        String deleteOneTransactionSql = "DELETE FROM transactions WHERE userID = ? AND isbn = ? LIMIT 1";
        String updateBookSql = "UPDATE books SET availableNumber = availableNumber + 1 WHERE isbn = ?";

        Connection conn = null;
        boolean allSuccessful = true;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteOneTransactionSql);
                 PreparedStatement pstmtUpdateBook = conn.prepareStatement(updateBookSql)) {

                for (Book copyToReturn : booksToReturn) {
                    this.currentCustomer.removeBorrowedBookByIsbn(copyToReturn.getIsbn());
                    pstmtDelete.setInt(1, currentCustomer.getUserID());
                    pstmtDelete.setString(2, copyToReturn.getIsbn());
                    int deletedRows = pstmtDelete.executeUpdate();

                    if (deletedRows > 0) {
                        pstmtUpdateBook.setString(1, copyToReturn.getIsbn());
                        pstmtUpdateBook.addBatch();
                    } else {
                        System.err.println("Warning: Could not find a transaction record to delete for UserID: "
                                + currentCustomer.getUserID() + ", ISBN: " + copyToReturn.getIsbn());
                        conn.rollback();
                        allSuccessful = false;
                        showAlert(Alert.AlertType.ERROR, "Return Error", "Could not process return for: " + copyToReturn.getTitle() + ". Transaction record not found.");
                        break;
                    }
                }

                if (allSuccessful) {
                    pstmtUpdateBook.executeBatch();
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            allSuccessful = false;
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to process book returns: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        if (allSuccessful && !booksToReturn.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully processed return for " + booksToReturn.size() + " book(s).");
            loadBorrowedBooks();
        } else if (!allSuccessful && !booksToReturn.isEmpty()){
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