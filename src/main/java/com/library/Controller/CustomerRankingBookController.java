package com.library.Controller;

import com.library.Models.Book;
import com.library.Models.DatabaseConnection; // Giả sử bạn có lớp này
// Bỏ Customer vì không dùng trực tiếp ở đây cho ranking chung
// import com.library.Models.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement; // Sử dụng PreparedStatement an toàn hơn Statement
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CustomerRankingBookController implements Initializable {

    @FXML
    private TableView<Book> rankingTableView; // Liên kết với TableView trong FXML

    @FXML
    private TableColumn<Book, Integer> rankColumn; // Cột STT/Hạng

    @FXML
    private TableColumn<Book, String> titleColumn; // Cột Tên Sách

    @FXML
    private TableColumn<Book, String> authorColumn; // Cột Tác Giả

    // Không cần cột borrowCount nếu bạn không muốn hiển thị số lần mượn cụ thể
    // @FXML
    // private TableColumn<Book, Integer> borrowCountColumn;


    // Không cần currentCustomer và BookinRanking cho bảng xếp hạng chung
    // private Customer currentCustomer;
    // private Book BookinRanking;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Thiết lập CellValueFactory cho các cột
        // Giá trị trong PropertyValueFactory phải khớp với tên thuộc tính trong lớp Book
        // (ví dụ: "title" sẽ gọi book.getTitle())

        rankColumn.setCellValueFactory(cellData ->
                // Hiển thị thứ hạng dựa trên vị trí trong danh sách (đã được sắp xếp từ SQL)
                new javafx.beans.property.SimpleIntegerProperty(
                        rankingTableView.getItems().indexOf(cellData.getValue()) + 1
                ).asObject()
        );
        rankColumn.setSortable(false); // Không cho phép sắp xếp cột hạng

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

        // Nếu bạn có cột borrowCountColumn và muốn ẩn nó thay vì xóa khỏi FXML:
        // if (borrowCountColumn != null) {
        //     borrowCountColumn.setCellValueFactory(new PropertyValueFactory<>("borrowCount"));
        //     borrowCountColumn.setVisible(false); // Ẩn cột này
        // }

        loadRankedBooks(); // Tải dữ liệu khi controller được khởi tạo
    }

    // Trong file: CustomerRankingBookController.java

    public void loadRankedBooks() {
        System.out.println("CustomerRankingBookController: Loading ranked books...");
        ObservableList<Book> booksList = FXCollections.observableArrayList();
        String query = "SELECT isbn, title, author, borrowCount FROM books " +
                "ORDER BY borrowCount DESC, title ASC " +
                "LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book();
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setBorrowCount(rs.getInt("borrowCount"));
                booksList.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Không thể tải bảng xếp hạng sách: " + e.getMessage());
        }

        // THÊM DÒNG NÀY ĐỂ XÓA SẮP XẾP CŨ (NẾU CÓ) TRƯỚC KHI SET ITEMS MỚI
        if (rankingTableView.getSortOrder() != null) {
            rankingTableView.getSortOrder().clear();
        }
        // Bạn cũng có thể muốn xóa comparator nếu nó được đặt bằng cách nào đó:
        // rankingTableView.setComparator(null); // Thường thì clear SortOrder là đủ

        rankingTableView.setItems(booksList); // Gán danh sách đã được sắp xếp từ SQL
        System.out.println("CustomerRankingBookController: Loaded " + booksList.size() + " books into ranking.");
    }

    // Phương thức tiện ích để hiển thị Alert
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Phương thức getBooksRanked() cũ có thể không cần thiết nữa
    // nếu bạn tải trực tiếp vào TableView như loadRankedBooks()
    /*
    public ObservableList<Book> getBooksRanked() {
        // ... logic cũ ...
        // Đoạn code này bạn cung cấp có lỗi:
        // 1. int bookId = rs.getInt("book_id "); // Có khoảng trắng thừa
        // 2. books.add(new Book()); // Thêm một Book rỗng, không có dữ liệu
        // 3. Query lồng nhau không hiệu quả
        // => Nên dùng JOIN hoặc lấy trực tiếp từ bảng books như trong loadRankedBooks()
        return FXCollections.observableArrayList(); // Trả về rỗng hoặc danh sách đã được load
    }
    */
}