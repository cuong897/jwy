package com.library.Controller;

import com.library.Models.Customer;
import com.library.Models.User;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class CustomerDashboardController implements Initializable {
    @FXML
    private ImageView ADMINButtonView;
    @FXML
    private Button adminPanelButton;
    @FXML
    private Button logOutButton;
    @FXML
    private AnchorPane dashboardRoot;
    @FXML
    private AnchorPane mainContainer;
    @FXML
    private Label currentLocationLabel;
    @FXML
    private Button HomeButton;
    @FXML
    private Button settingButton;
    @FXML
    private Button borrowBookButton;
    @FXML
    private Button myLibraryButton;

    @FXML
    private Label userNameLabel;
    @FXML
    private Pane navigationBorderPane;

    @FXML
    private Node currentNode;
    @FXML
    private TextField searchToolBar;
    @FXML
    private Button searchButton;
    private Customer currentCustomer;

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        userNameLabel.setText(currentCustomer.getName() == null ?
                "Name of customer" : currentCustomer.getName());
        if (adminPanelButton != null) {
            adminPanelButton.setVisible(false);
            adminPanelButton.setManaged(false);
        } else {
            System.err.println("Cảnh báo: adminPanelButton là null trong setCustomer. Đảm bảo nó đã được inject từ FXML.");
        }
        if (customer != null) {
            if (customer.getRole() == User.Role.ADMIN) {
                if (adminPanelButton != null) {
                    adminPanelButton.setVisible(true);
                    adminPanelButton.setManaged(true);
                }
            } else {
                if (adminPanelButton != null) {
                    adminPanelButton.setVisible(false);
                    adminPanelButton.setManaged(false);
                }
            }
            if (userNameLabel != null) {
                userNameLabel.setText(currentCustomer.getName());
            }
        }
        preLoad();
        if (HomeButton != null && navigationBorderPane != null) {
            javafx.application.Platform.runLater(() -> {
                gotoHome();
            });
        } else {
            System.err.println("Lỗi: HomeButton hoặc navigationBorderPane chưa được khởi tạo khi gọi setCustomer.");
        }
    }

    private Map<String, FXMLLoader> cache = new HashMap<>();

    private ActiveButton activeButton;


    public enum ActiveButton {
        ADMIN,
        HOME,
        SETTING,
        BORROW_BOOK,
        MY_LIBRARY;
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (adminPanelButton != null) { // Kiểm tra null để tránh lỗi nếu FXML chưa cập nhật
            adminPanelButton.setOnAction(e -> gotoAdminPanel());
        }
        settingButton.setOnAction(e -> {
            try {
                gotoSetting();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        HomeButton.setOnAction(e -> {
            gotoHome();
        });
        borrowBookButton.setOnAction(e -> {
            gotoBorrowBook();
        });
        myLibraryButton.setOnAction(e -> {
            gotoMyLibrary();
        });
        searchButton.setOnAction(e -> {
            String query = searchToolBar.getText();
            searchBooks(query);
        });
        searchToolBar.setOnAction(e -> {
            String query = searchToolBar.getText();
            searchBooks(query);
        });
        logOutButton.setOnAction(e -> {
            try {
                logOut();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

//    private void playClickSound() {
//        String soundPath = Objects.requireNonNull(getClass().getResource("/Sounds/clickSound.wav")).toString();
//        javafx.scene.media.AudioClip clickSound = new javafx.scene.media.AudioClip(soundPath);
//        clickSound.play();
//    }

    private void navigationBorderAnimation(Button activeButton) {
        if (activeButton.getScene() == null || activeButton.getParent() == null) {
            System.out.println("Cảnh báo: Nút " + activeButton.getId() + " chưa sẵn sàng để lấy layoutY.");
        }

        TranslateTransition navigationButtonBorderTransition = new TranslateTransition(Duration.seconds(0.5),
                navigationBorderPane);
        double buttonCenterYInVBox = activeButton.getLayoutY() + activeButton.getHeight() / 2;
        double vBoxLayoutY = activeButton.getParent().getLayoutY();
        double absoluteButtonCenterY = vBoxLayoutY + buttonCenterYInVBox;
        double targetNavigationPaneY = absoluteButtonCenterY - (navigationBorderPane.getHeight() / 2);
        navigationButtonBorderTransition.setToY(targetNavigationPaneY);
        navigationButtonBorderTransition.setToY(targetNavigationPaneY - navigationBorderPane.getLayoutY());
        navigationButtonBorderTransition.play();
//        playClickSound();
    }
    public void gotoAdminPanel() {
        if (activeButton != ActiveButton.ADMIN) {
            loadPane("/com/library/View/ADMIN.fxml");
            activeButton = ActiveButton.ADMIN;
            currentLocationLabel.setText("ADMIN");
            navigationBorderAnimation(adminPanelButton);
        }
    }
    public void gotoHome() {
        if (activeButton != ActiveButton.HOME) {
            loadPane("/com/library/View/CustomerHome.fxml");
            activeButton = ActiveButton.HOME;
            currentLocationLabel.setText("Home");
            navigationBorderAnimation(HomeButton);
        }
    }

    public void gotoSetting() throws IOException {
        if (activeButton != ActiveButton.SETTING) {
            loadPane("/com/library/View/CustomerSetting.fxml");
            activeButton = ActiveButton.SETTING;
            currentLocationLabel.setText("Setting");
            navigationBorderAnimation(settingButton);
        }
    }

    public void gotoBorrowBook() {
        if (activeButton != ActiveButton.BORROW_BOOK) {
            loadPane("/com/library/View/CustomerBorrowBook.fxml");
            activeButton = ActiveButton.BORROW_BOOK;
            currentLocationLabel.setText("Borrow Book");
            navigationBorderAnimation(borrowBookButton);
        }
    }

    public void gotoMyLibrary() {
        if (activeButton != ActiveButton.MY_LIBRARY) {
            loadPane("/com/library/View/CustomerMyLibrary.fxml");
            activeButton = ActiveButton.MY_LIBRARY;
            currentLocationLabel.setText("My Library");
            navigationBorderAnimation(myLibraryButton);
        }
    }
    public void switchToAdminAddBookView(String searchQuery) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/library/View/AdminAddBook.fxml")));
            Parent adminAddBookRoot = loader.load();

            AdminAddBookController controller = loader.getController();
            if (controller != null) {
                controller.setDashboardController(this);
                controller.initializeSearchWithQuery(searchQuery);
            } else {
                System.err.println("AdminAddBookController is null after loading AdminAddBook.fxml");
                return;
            }
            switchPaneAnimation(adminAddBookRoot);
            if(currentLocationLabel!=null) currentLocationLabel.setText("Add Book to Library");
            activeButton = ActiveButton.BORROW_BOOK;
            navigationBorderAnimation(borrowBookButton); // Cập nhật nút active nếu có
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object getLoadedControllerInstance(String fxmlPath) {
        FXMLLoader loader = cache.get(fxmlPath);
        if (loader != null) {
            return loader.getController();
        }
        System.err.println("No cached loader found for: " + fxmlPath + ". You might need to load it first if it's not part of the main navigation buttons.");
        return null;
    }
    private void preLoad() {
        String[] fxmlPaths = {
                "/com/library/View/CustomerHome.fxml",
                "/com/library/View/CustomerBorrowBook.fxml",
                "/com/library/View/CustomerSetting.fxml",
                "/com/library/View/CustomerMyLibrary.fxml",
                "/com/library/View/ADMIN.fxml",
        };

        for (String fxmlPath : fxmlPaths) {
            if (!cache.containsKey(fxmlPath)) {

                Task<FXMLLoader> loadTask = new Task<>() {
                    @Override
                    protected FXMLLoader call() throws Exception {
                        FXMLLoader loaderInstance = new FXMLLoader();
                        URL resourceUrl = getClass().getResource(fxmlPath);
                        if (resourceUrl == null) {
                            throw new IOException("Cannot find FXML resource: " + fxmlPath);
                        }
                        loaderInstance.setLocation(resourceUrl);
                        loaderInstance.load();
                        return loaderInstance;
                    }
                };

                loadTask.setOnSucceeded(event -> {
                    FXMLLoader loader = loadTask.getValue();
                    if (loader != null && loader.getController() != null) {
                        cache.put(fxmlPath, loader);
                    } else {
                        System.err.println("Lỗi preload: FXMLLoader hoặc Controller là null sau khi load FXML: " + fxmlPath);
                    }
                });

                loadTask.setOnFailed(event -> {
                    if (loadTask.getException() != null) {
                        loadTask.getException().printStackTrace();
                    }
                });

                Thread loadPaneThread = new Thread(loadTask);
                loadPaneThread.setDaemon(true);
                loadPaneThread.start();
            }
        }
    }

    public void loadPane(String fxmlPath) {
        FXMLLoader loader = cache.get(fxmlPath);

        if (loader == null) {
            try {
                loader = new FXMLLoader();
                URL resourceUrl = getClass().getResource(fxmlPath);
                if (resourceUrl == null) {
                    System.err.println("loadPane: Không tìm thấy FXML resource: " + fxmlPath);
                    return;
                }
                loader.setLocation(resourceUrl);
                Parent paneRoot = loader.load();
                if (loader.getController() == null) {
                    System.err.println("loadPane: Controller là null sau khi load FXML: " + fxmlPath);
                    return;
                }
                cache.put(fxmlPath, loader);
                configureChildController(loader, fxmlPath);
                switchPaneAnimation(paneRoot);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) { // Bắt các lỗi khác
                e.printStackTrace();
            }
        } else {
            configureChildController(loader, fxmlPath); // Gọi cấu hình
            switchPaneAnimation(loader.getRoot());
        }
    }
    private void configureChildController(FXMLLoader loader, String fxmlPath) {
        if (loader == null) {
            System.err.println("configureChildController: FXMLLoader là null cho " + fxmlPath + ". Không thể cấu hình controller.");
            return;
        }
        Object controller = loader.getController();
        if (controller == null) {
            System.err.println("configureChildController: Controller là null (từ loader.getController()) cho " + fxmlPath + ".");
            return;
        }
        if (controller instanceof CustomerSettingController) {
            ((CustomerSettingController) controller).setCustomerDashboardController(this);
        } else if (controller instanceof CustomerMyLibraryController) {
            ((CustomerMyLibraryController) controller).setDashboardController(this);
        } else if (controller instanceof CustomerHomeController) {
            ((CustomerHomeController) controller).setParentController(this);
        } else if (controller instanceof CustomerBorrowBookController) {
            ((CustomerBorrowBookController) controller).setParentDashboardController(this);
        } else if (controller instanceof ADMINController) {
            ((ADMINController) controller).setDashboardController(this);
        }
    }
    public void switchPaneAnimation(Node node) {
        if (currentNode != null) {
            FadeTransition fadeout = new FadeTransition(Duration.seconds(0.4), currentNode);
            fadeout.setFromValue(1.0);
            fadeout.setToValue(0.0);
            fadeout.play();
            fadeout.setOnFinished(event -> {
                mainContainer.getChildren().remove(currentNode);
                mainContainer.getChildren().add(node);
                FadeTransition fadein = new FadeTransition(Duration.seconds(0.4), node);
                fadein.setFromValue(0.0);
                fadein.setToValue(1.0);
                fadein.play();
            });
        } else {
            mainContainer.getChildren().add(node);
            FadeTransition fadein = new FadeTransition(Duration.seconds(0.4), node);
            fadein.setFromValue(0.0);
            fadein.setToValue(1.0);
            fadein.play();
        }
        currentNode = node;

    }

    public void searchBooks(String query) {
        if (cache.containsKey("/com/library/View/CustomerBorrowBook.fxml")) {
            FXMLLoader loader = cache.get("/com/library/View/CustomerBorrowBook.fxml");
            CustomerBorrowBookController customerBorrowBookController = loader.getController();
            customerBorrowBookController.updateSearchBooksResult(query);
            navigationBorderAnimation(borrowBookButton);
        } else {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/library/View/customerBorrowBook.fxml")));
            try {
                Parent searchPane = loader.load();
                CustomerBorrowBookController customerBorrowBookController = loader.getController();
                customerBorrowBookController.updateSearchBooksResult(query);
                navigationBorderAnimation(borrowBookButton);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gotoBorrowBook();
    }
    public void refreshCustomerDisplayInfo() {
        if (this.currentCustomer != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(this.currentCustomer.getName() == null ?
                        "Name of customer" : this.currentCustomer.getName());
            }
        }
    }
    public void logOut() throws IOException {
        Parent login = FXMLLoader.load(Objects.requireNonNull(getClass()
                .getResource("/com/library/View/Login.fxml")));
        Stage stage = (Stage) dashboardRoot.getScene().getWindow();
        stage.setScene(new Scene(login, 749, 555));
        stage.centerOnScreen();
        stage.show();
    }
}
