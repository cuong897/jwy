package com.library.Controller;

import com.library.Models.BookRankingEntry;
import com.library.Models.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CustomerRankingBook implements Initializable {

    @FXML
    private TableView<BookRankingEntry> rankingTableView;

    @FXML
    private TableColumn<BookRankingEntry, Integer> rankColumn;

    @FXML
    private TableColumn<BookRankingEntry, String> titleColumn;

    @FXML
    private TableColumn<BookRankingEntry, String> authorColumn;

    private ObservableList<BookRankingEntry> bookRankingList = FXCollections.observableArrayList();

    private CustomerDashboardController dashboardController;

    public void setDashboardController(CustomerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

        loadRankingData();
    }
    public void refreshRankingDataPublic() {
        loadRankingData();
    }
    private void loadRankingData() {
        ObservableList<BookRankingEntry> newBookRankingList = FXCollections.observableArrayList();

        String sql = "SELECT title, author, borrowCount FROM books WHERE borrowCount > 0 ORDER BY borrowCount DESC, title ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                int borrowCount = rs.getInt("borrowCount");
                newBookRankingList.add(new BookRankingEntry(rank++, title, author, borrowCount));
            }

            Platform.runLater(() -> {
                bookRankingList.setAll(newBookRankingList);
                rankingTableView.setItems(bookRankingList);

                if (bookRankingList.isEmpty()) {
                    rankingTableView.setPlaceholder(new Label("Chưa có sách nào trong bảng xếp hạng."));
                } else {
                    rankingTableView.setPlaceholder(null);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Không thể tải dữ liệu bảng xếp hạng: " + e.getMessage());
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