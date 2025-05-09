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
import java.util.Objects;
import java.util.ResourceBundle;

public class CustomerSettingController implements Initializable {
    @FXML
    private Button changeNameButton;
    @FXML
    private Button changePhoneNumberButton;
    @FXML
    private Button changeGmailButton;

    @FXML
    private AnchorPane settingRoot;
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
    private TextField changeNameField;
    @FXML
    private TextField changePhoneNumberField;
    @FXML
    private TextField changeGmailField;
    @FXML
    private Button changeAvatarButton;
    @FXML
    private ImageView avatarImage;
    private CustomerDashboardController parentController;

    private Customer currentCustomer;

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCustomerDashboardController(CustomerDashboardController parentController) {
        this.parentController = parentController;
        if (this.parentController != null) {
            this.currentCustomer = this.parentController.getCurrentCustomer();
            populateUIWithCustomerData();
        } else {
            clearCustomerDataView();
        }
    }
    private void populateUIWithCustomerData() {
        if (this.currentCustomer != null) {
            if (userNameLabel != null) userNameLabel.setText(this.currentCustomer.getName());
            if (gmailLabel != null) gmailLabel.setText("Email: " + this.currentCustomer.getEmail());
            if (phoneNumberLabel != null) phoneNumberLabel.setText("Phone number: " + this.currentCustomer.getPhoneNumber());

            if (avatarImage != null) {
                String avatarPath = this.currentCustomer.getAvatarUrl();
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    try {
                        Image image;
                        if (avatarPath.matches("^(https?|file):.*$")) {
                            image = new Image(avatarPath, true);
                        } else {
                            File imageFile = new File(avatarPath);
                            if (imageFile.exists() && imageFile.isFile()) {
                                image = new Image(imageFile.toURI().toString(), true);
                            } else {
                                System.err.println("Lỗi: File ảnh đại diện không tồn tại hoặc không phải file: " + avatarPath);
                                Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/library/Image/ApplicationLogo.jpg")));
                                avatarImage.setImage(defaultImage);                                return;
                            }
                        }

                        if (image.isError()) {
                            System.err.println("Lỗi khi tải ảnh đại diện từ (Image.isError()): " + avatarPath);
                            if(image.getException() != null) {
                                image.getException().printStackTrace();
                            }
                            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/library/Image/ApplicationLogo.jpg")));
                            avatarImage.setImage(defaultImage);                        } else {
                            avatarImage.setImage(image);
                        }
                    } catch (Exception e) {
                        System.err.println("Ngoại lệ khi tạo Image từ đường dẫn: " + avatarPath);
                        e.printStackTrace();
                        Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/library/Image/ApplicationLogo.jpg")));
                        avatarImage.setImage(defaultImage);
                    }
                } else {
                    Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/library/Image/ApplicationLogo.jpg")));
                    avatarImage.setImage(defaultImage);
                }
            }

        } else {
            System.err.println("CustomerSettingController: currentCustomer là null trong populateUIWithCustomerData.");
            clearCustomerDataView();
        }
    }
    public void clearCustomerDataView() {
            if (userNameLabel != null) userNameLabel.setText("N/A");
            if (gmailLabel != null) gmailLabel.setText("Email: N/A");
            if (phoneNumberLabel != null) phoneNumberLabel.setText("Phone number: N/A");
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/library/Image/ApplicationLogo.jpg")));
            avatarImage.setImage(defaultImage);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        oldPasswordField.setOnAction(event -> {
            handleChangePassword();
        });
        newPasswordField.setOnAction(event -> {
            handleChangePassword();
        });
        confirmPasswordField.setOnAction(event -> {
            handleChangePassword();
        });
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
        changeNameField.setOnAction(event -> {
            handleChangeName();
        });
        changeNameButton.setOnAction(event -> {
            handleChangeName();
        });
        changePhoneNumberButton.setOnAction(event -> {
            handleChangePhoneNumber();
        });
        changeGmailButton.setOnAction(event -> {
            handleChangeGmail();
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
            currentCustomer.setAvatarUrl(selectedImageFile.getAbsolutePath());
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
                String sql = "UPDATE users SET avatarImage = ? WHERE userID = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, imagePath);
                preparedStatement.setInt(2, currentCustomer.getUserID());
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật ảnh đại diện: " + e.getMessage());
            }
        }
    }

    public void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();


        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng điền đầy đủ thông tin");
            return;
        }
        if (!oldPassword.equals(currentCustomer.getPassword())) {
            JOptionPane.showMessageDialog(null, "Mật khẩu cũ không đúng");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "Mật khẩu mới không khớp");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET password = ? WHERE userID = ? AND password = ?";
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, currentCustomer.getUserID());
            preparedStatement.setString(3, oldPassword);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công");
                currentCustomer.setPassword(newPassword);
            } else {
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu không thành công");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đổi mật khẩu: " + e.getMessage());
        }

    }

    public void handleChangePhoneNumber() {
        String newPhoneNumber = changePhoneNumberField.getText();
        if (newPhoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng điền số điện thoại mới");
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET phoneNumber = ? WHERE userID = ?";
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newPhoneNumber);
            preparedStatement.setInt(2, currentCustomer.getUserID());
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Cập nhật số điện thoại thành công");
                phoneNumberLabel.setText("Số điện thoại: " + newPhoneNumber);
            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật số điện thoại không thành công");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật số điện thoại: " + e.getMessage());
        }
    }

    public void handleChangeGmail() {
        String newGmail = changeGmailField.getText();
        if (newGmail.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng điền email mới");
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET email = ? WHERE userID = ?";
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newGmail);
            preparedStatement.setInt(2, currentCustomer.getUserID());
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Cập nhật email thành công");
                gmailLabel.setText("Email: " + newGmail);
            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật email không thành công");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật email: " + e.getMessage());
        }
    }
    public void handleChangeName() {
        String newUserName = changeNameField.getText();
        if (newUserName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng điền tên người dùng mới");
            return;
        }
        parentController.getCurrentCustomer().setName(newUserName);
        changeNameField.clear();

        if (parentController != null) {
            parentController.refreshCustomerDisplayInfo();
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET fullname = ? WHERE userID = ?";
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newUserName);
            preparedStatement.setInt(2, currentCustomer.getUserID());
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Cập nhật tên người dùng thành công");
                userNameLabel.setText(newUserName);

            } else {
                JOptionPane.showMessageDialog(null, "Cập nhật tên người dùng không thành công");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật tên người dùng: " + e.getMessage());
        }
    }

}
