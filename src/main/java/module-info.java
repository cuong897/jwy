module library { // Tên module "library" có vẻ không khớp với package gốc "com.library".
    // Bạn nên xem xét đổi tên module thành "com.library" để nhất quán,
    // nhưng "library" vẫn có thể hoạt động.
    requires java.sql;
    requires mysql.connector.j; // Đảm bảo bạn đã thêm thư viện này vào project
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop; // Thường không cần thiết cho ứng dụng JavaFX thuần túy trừ khi bạn dùng AWT/Swing
    requires com.google.api.services.books;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires javafx.media;

    // Mở package chứa các Controller cho javafx.fxml
    // Đây là điều CỰC KỲ QUAN TRỌNG để FXMLLoader hoạt động
    opens com.library.Controller to javafx.fxml;

    // Mở package chứa các lớp Model nếu chúng có JavaFX Properties
    // hoặc nếu chúng được sử dụng/tham chiếu trong FXML hoặc bởi các controller
    // thông qua reflection.
    opens com.library.Models to javafx.fxml, javafx.base;

    // Mở package chính (nơi chứa LibraryApplication) cho javafx.fxml và javafx.graphics
    // javafx.graphics để khởi chạy, javafx.fxml nếu LibraryApplication
    // có liên quan đến FXML (ít phổ biến).
    opens com.library to javafx.fxml, javafx.graphics;

    // Exports package chính nếu LibraryApplication là điểm vào chính.
    exports com.library;
    // Bạn có thể cần exports các package khác nếu các module khác cần truy cập trực tiếp chúng.
    // exports com.library.Controller; // Nếu cần
    // exports com.library.Models; // Nếu cần
}