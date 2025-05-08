package com.library.Controller;

import com.library.Models.Customer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private Button logOutButton;
    @FXML
    private AnchorPane dashboardRoot;
    @FXML
    private AnchorPane mainContainer;
    @FXML
    private Label currentLocationLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button HomeButton;
    @FXML
    private ImageView HomeImage;
    @FXML
    private Button settingButton;
    @FXML
    private ImageView settingImage;
    @FXML
    private Button borrowBookButton;
    @FXML
    private ImageView borrowBookImage;
    @FXML
    private Button myLibraryButton;
    @FXML
    private ImageView myLibraryImage;

    @FXML
    private Pane avatarContainer;
    @FXML
    private ImageView avatarImage;
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
        welcomeLabel.setText("Welcome," + currentCustomer.getName() + " !");
        preLoad();
        gotoHome();
    }

    private Map<String, FXMLLoader> cache = new HashMap<>();

    private ActiveButton activeButton;


    public enum ActiveButton {
        HOME,
        SETTING,
        BORROW_BOOK,
        MY_LIBRARY;
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    private void playClickSound() {
        String soundPath = Objects.requireNonNull(getClass().getResource("/Sounds/clickSound.wav")).toString();
        javafx.scene.media.AudioClip clickSound = new javafx.scene.media.AudioClip(soundPath);
        clickSound.play();
    }

    private void navigationBorderAnimation(Button activeButton) {
        TranslateTransition navigationButtonBorderTransition = new TranslateTransition(Duration.seconds(0.15),
                navigationBorderPane);
        navigationButtonBorderTransition.setToY(activeButton.getLayoutY() - 10 - navigationBorderPane.getLayoutY());
        navigationButtonBorderTransition.play();

        playClickSound();
    }

    public void gotoHome() {
        if (activeButton != ActiveButton.HOME) {
            activeButton = ActiveButton.HOME;
            currentLocationLabel.setText("Dashboard");
            navigationBorderAnimation(HomeButton);
            loadPane("/View/CustomerHome.fxml");
        }
    }

    public void gotoSetting() throws IOException {
        if (activeButton != ActiveButton.SETTING) {
            FXMLLoader loader = cache.get("/View/CustomerSetting.fxml");
            CustomerSettingController controller = loader.getController();
            controller.setCustomerDashboardController(CustomerDashboardController.this);
            activeButton = ActiveButton.SETTING;
            currentLocationLabel.setText("Setting");
            navigationBorderAnimation(settingButton);
            loadPane("/View/CustomerSetting.fxml");
        }
    }

    public void gotoBorrowBook() {
        if (activeButton != ActiveButton.BORROW_BOOK) {
            activeButton = ActiveButton.BORROW_BOOK;
            currentLocationLabel.setText("Borrow Book");
            navigationBorderAnimation(borrowBookButton);
            loadPane("/View/CustomerBorrowBook.fxml");
        }
    }

    public void gotoMyLibrary() {
        if (activeButton != ActiveButton.MY_LIBRARY) {
            activeButton = ActiveButton.MY_LIBRARY;
            currentLocationLabel.setText("My Library");
            navigationBorderAnimation(myLibraryButton);
            loadPane("/View/CustomerMyLibrary.fxml");

        }
    }

    private void preLoad() {
        String[] fxmlPaths = {
                "/Views/CustomerHome.fxml",
                "/Views/CustomerBorrowBook.fxml",
                "/Views/CustomerSetting.fxml",
                "/Views/CustomerMyLibrary.fxml",
        };

        for (String fxmlPath : fxmlPaths) {
            Task<FXMLLoader> loadTask = new Task<>() {
                @Override
                protected FXMLLoader call() throws IOException {
                    return new FXMLLoader(getClass().getResource(fxmlPath));
                }
            };

            loadTask.setOnSucceeded(event -> {
                try {
                    FXMLLoader loader = loadTask.getValue();
                    loader.load();
                    cache.put(fxmlPath, loader);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            loadTask.setOnFailed(event -> loadTask.getException().printStackTrace());

            Thread loadPaneThread = new Thread(loadTask);
            loadPaneThread.setDaemon(true);
            loadPaneThread.start();
        }
    }

    public void loadPane(String fxmlPath) {
        if (cache.containsKey(fxmlPath)) {
            FXMLLoader loader = cache.get(fxmlPath);
            switchPaneAnimation(loader.getRoot());
        } else {
            Task<FXMLLoader> loadTask = new Task<>() {
                @Override
                protected FXMLLoader call() throws IOException {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    loader.load();
                    return loader;
                }
            };
            loadTask.setOnSucceeded(event -> {
                FXMLLoader loader = loadTask.getValue();
                cache.put(fxmlPath, loader);
                switchPaneAnimation(loader.getRoot());
            });
            loadTask.setOnFailed(event -> {
                Throwable exception = loadTask.getException();
                System.out.println("Failed to load FXML: " + exception.getMessage());
            });
            Thread loadPaneThread = new Thread(loadTask);
            loadPaneThread.setDaemon(true);
            loadPaneThread.start();
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
        if (cache.containsKey("/View/customerBorrowBook.fxml")) {
            FXMLLoader loader = cache.get("/View/customerBorrowBook.fxml");
            CustomerBorrowBookController customerBorrowBookController = loader.getController();
            customerBorrowBookController.updateSearchBooksResult(query);
            navigationBorderAnimation(borrowBookButton);
        } else {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/View/customerBorrowBook.fxml")));
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

    public void logOut() throws IOException {
        Parent login = FXMLLoader.load(Objects.requireNonNull(getClass()
                .getResource("/View/LoginAndRegister.fxml")));
        Stage stage = (Stage) dashboardRoot.getScene().getWindow();
        stage.setScene(new Scene(login, 960, 640));
        stage.show();
    }
}
