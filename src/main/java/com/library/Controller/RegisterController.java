package com.library.Controller;

import com.library.Models.DatabaseConnection;
import com.library.Models.User;
import com.library.Models.ADMIN;
import com.library.Models.Customer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterController implements Initializable {
    @FXML
    private Label registerNotificationLabel;
    @FXML
    AnchorPane registerRoot;
    @FXML
    private TextField NameField;
    @FXML
    private TextField userNameField;
    @FXML
    private TextField EmailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Hyperlink loginLink;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        NameField.setOnAction(this::handleRegisterButtonAction);
        EmailField.setOnAction(this::handleRegisterButtonAction);
        phoneNumberField.setOnAction(this::handleRegisterButtonAction);
        passwordField.setOnAction(this::handleRegisterButtonAction);
        confirmPasswordField.setOnAction(this::handleRegisterButtonAction);
        registerButton.setOnAction(this::handleRegisterButtonAction);
        loginLink.setOnAction(event -> handleLoginLinkAction());
        userNameField.setOnAction(this::handleRegisterButtonAction);
    }
    @FXML
    private void handleLoginLinkAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/library/View/Login.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) registerRoot.getScene().getWindow();
            stage.setScene(new Scene(root, 749, 555));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean checkUsernameAvailable(String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT username FROM users WHERE BINARY username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return !rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        String name = NameField.getText();
        String email = EmailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            registerNotificationLabel.setText("Hãy điền đầy đủ thông tin");
            return;
        }
        if (!checkUsernameAvailable(userNameField.getText())) {
            registerNotificationLabel.setText("Tên đăng nhập đã tồn tại");
            return;
        }

        if (!password.equals(confirmPassword)) {
            registerNotificationLabel.setText("Mật khẩu không khớp");
            return;
        }

        if (password.length() < 6) {
            registerNotificationLabel.setText("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO users (fullName, username, password, phoneNumber, email) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, NameField.getText());
            stmt.setString(2, userNameField.getText());
            stmt.setString(3, passwordField.getText());
            stmt.setString(4, phoneNumberField.getText());
            stmt.setString(5, EmailField.getText());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                registerNotificationLabel.setText("Tài khoản đã được tạo thành công");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Đăng ký thành công");
                alert.setHeaderText(null);
                alert.setContentText("Tài khoản của bạn đã được tạo thành công. Bạn có thể đăng nhập ngay bây giờ.");
                alert.showAndWait();
            } else {
                registerNotificationLabel.setText("Đã xảy ra lỗi. Vui lòng thử lại sau.");
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            registerNotificationLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }


    }
}