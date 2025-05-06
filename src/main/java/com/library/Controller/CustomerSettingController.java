package com.library.Controller;

import com.library.Models.Customer;
import com.library.Models.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class CustomerSettingController implements Initializable {

    @FXML
    private AnchorPane settingRoot;
    @FXML
    private Label userIDLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label gmailLabel;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private Button changePasswordButton;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField changeUserNameField;
    @FXML
    private TextField changePhoneNumberField;
    @FXML
    private TextField changeGmailField;
    @FXML
    private TextField changeNameField;
    @FXML
    private Button changeAvatarButton;
    @FXML
    private ImageView avatarImage;

    private Customer currentCustomer;

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public static void setCurrentCustomer(Customer currentCustomer) {
        this.currentCustomer = currentCustomer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userIDLabel.setText(currentCustomer.getUserID());
        userNameLabel.setText(currentCustomer.getUserName());
        gmailLabel.setText("Email: " + currentCustomer.getEmail());
        phoneNumberLabel.setText("Phone number: " + currentCustomer.getPhoneNumber());
        changeAvatarButton.setOnAction(event -> {
            handleChangeAvatar();
        });
        changePasswordButton.setOnAction(event -> {
            handleChangePassword();
        });
        changePhoneNumberField.setOnAction(event -> {
            handleChangePhoneNumber();
        });
        changeGmailField.setOnAction(event -> {
            handleChangeGmail();
        });
        changeUserNameField.setOnAction(event -> {
            handleChangeUserName();
        });

    }
    public void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Lấy Stage hiện tại từ nút nhấn
        Stage stage = (Stage) changeAvatarButton.getScene().getWindow();

        File selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                avatarImage.setImage(image);

            } catch (Exception e) {
                System.err.println("Lỗi khi tải ảnh: " + e.getMessage());

            }
        }
        String imagePath = selectedImageFile.getAbsolutePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String sql = "UPDATE customer SET avatar = ? WHERE userID = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, imagePath);
                preparedStatement.setString(2, userIDLabel.getText());
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật ảnh đại diện: " + e.getMessage());
            }
        }
    }
}
