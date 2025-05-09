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

public class LoginController implements Initializable {
    @FXML
    private Label loginNotificationLabel;
    @FXML
    private AnchorPane loginRoot;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink registerLink;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameField.setOnAction(this::handleLoginButtonAction);
        passwordField.setOnAction(this::handleLoginButtonAction);
        loginButton.setOnAction(this::handleLoginButtonAction);
        registerLink.setOnAction(event -> handleRegisterLinkAction());

    }
    private User checkLoginValidity(String username, String password) {
        Connection connectDB = DatabaseConnection.getConnection();

        if (connectDB == null) {
            System.out.println("Something were wrong connectDB is null!");
            return null;
        }

        String connectQuery = "SELECT * FROM users WHERE BINARY username = ? AND BINARY password = ?";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(connectQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String role = resultSet.getString("role");
                if (role.equalsIgnoreCase("CUSTOMER")) {
                    Customer customer = new Customer();
                    customer.setRole(User.Role.CUSTOMER);
                    customer.setUserID(resultSet.getInt("userID"));
                    customer.setName(resultSet.getString("fullName"));
                    customer.setUserName(resultSet.getString("username"));
                    customer.setPassword(resultSet.getString("password"));
                    customer.setEmail(resultSet.getString("email"));
                    customer.setPhoneNumber(resultSet.getString("phoneNumber"));
                    customer.setAvatarUrl(resultSet.getString("avatarImage"));
                    return customer;
                } else if (role.equalsIgnoreCase("ADMIN")) {
                    ADMIN admin = new ADMIN();
                    admin.setRole(User.Role.ADMIN);
                    admin.setUserID(resultSet.getInt("userID"));
                    admin.setName(resultSet.getString("fullName"));
                    admin.setUserName(resultSet.getString("username"));
                    admin.setPassword(resultSet.getString("password"));
                    admin.setEmail(resultSet.getString("email"));
                    admin.setPhoneNumber(resultSet.getString("phoneNumber"));
                    return admin;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
            return null;
        }

        return null;
    }
    public void handleRegisterLinkAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/library/View/Register.fxml"));
            Parent register = loader.load();
            Stage stage = (Stage) ((Node) registerLink).getScene().getWindow();
            Platform.runLater(stage::centerOnScreen);
            stage.setScene(new Scene(register, 960, 640));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            loginNotificationLabel.setText("Bạn chưa nhập tài khoản hoặc mặt khẩu");
            return;
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return checkLoginValidity(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            if (user != null) {
                if (user instanceof Customer) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/com/library/View/CustomerDashboard.fxml"));
                        Parent dashboard = loader.load();
                        CustomerDashboardController controller = loader.getController();
                        controller.setCustomer((Customer) user);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Platform.runLater(stage::centerOnScreen);
                        stage.setScene(new Scene(dashboard, 1024, 768));
                        stage.show();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (user instanceof ADMIN) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/com/library/View/CustomerDashboard.fxml"));
                        Parent dashboard = loader.load();
                        CustomerDashboardController controller = loader.getController();
                        ADMIN admin = (ADMIN) user;
                        controller.setCustomer( admin.convertADMINtoCustomer((ADMIN) user));
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Platform.runLater(stage::centerOnScreen);
                        stage.setScene(new Scene(dashboard, 1024, 768));
                        stage.show();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                executorService.shutdown();
            } else {
                passwordField.clear();
                loginNotificationLabel.setText("Đăng nhập thất bại, hãy kiểm tra lại thông tin");
                executorService.shutdown();
            }
        });
        executorService.submit(loginTask);
    }

}
