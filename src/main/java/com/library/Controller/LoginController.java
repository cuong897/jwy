package com.library.Controller;
import com.library.Models.Customer;
import com.library.Models.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    public TextField email;
    public PasswordField password;

    public void login(ActionEvent event) {
        String Email = email.getText();
        String Password = password.getText();
        if (Email.isEmpty() || Password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tên đăng nhập hoặc mật khẩu không được để trống.");
            return;

        }

        Thread loginThread = new Thread(() -> {
            boolean loginSuccessful = checkLogin(Email, Password);
            javafx.application.Platform.runLater(() -> {
                if (loginSuccessful) {
                    showAlert(Alert.AlertType.INFORMATION, "Đăng nhập thành công.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Sai email hoặc sai mật khẩu.");
                }
            });
        });

        loginThread.start();


    }

    public void register(ActionEvent event) {
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();

    }

    private boolean checkLogin(String email, String password) {
        Connection connection1 = DatabaseConnection.getConnection();
        if (connection1 == null) {
            showAlert(Alert.AlertType.ERROR, "Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "SELECT * FROM customers WHERE email = ? AND password = ?";
        try {
            PreparedStatement statement = connection1.prepareStatement(sql);
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Có lỗi xảy ra khi kiểm tra đăng nhâp.");


        }
        return false;
    }
}
