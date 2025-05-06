package com.library.Controller;

import com.library.Models.Customer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class CustomerDashboardController implements Initializable {
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
    private Label dateLabel;
    @FXML
    private Pane navigationBorderPane;
    private Customer customer;
    private Node currentNode;
    public CustomerDashboardController() {
    }

    public Customer getCustomer() {
        return customer;
    }

    private TextField searchToolBar;
    private Button searchButton;

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
            gotoSetting();
        });
        HomeButton.setOnAction(e -> {
            gotoHome();
        });
        settingButton.setOnAction(e -> {
            gotoSetting();
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
        welcomeLabel.setText("Welcome," + customer.getName() + " !");
        dateLabel.setText("Date: " + java.time.LocalDate.now());

    }

    private void playClickSound() {
        String soundPath = Objects.requireNonNull(getClass().getResource("src/main/resources/Sounds/clickSound.wav")).toString();
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
            loadPane("src/main/resources/View/customerDasboard.fxml");
        }
    }
    public void gotoSetting() {
        if (activeButton != ActiveButton.SETTING) {
            activeButton = ActiveButton.SETTING;
            currentLocationLabel.setText("Setting");
            loadPane("src/main/resources/View/customerSetting.fxml");
        }
    }
    public void gotoBorrowBook() {
        if (activeButton != ActiveButton.BORROW_BOOK) {
            activeButton = ActiveButton.BORROW_BOOK;
            currentLocationLabel.setText("Borrow Book");
            navigationBorderAnimation(borrowBookButton);
            loadPane("src/main/resources/View/customerBorrowBook.fxml");
        }
    }
    public void gotoMyLibrary() {
        if (activeButton != ActiveButton.MY_LIBRARY) {
            activeButton = ActiveButton.MY_LIBRARY;
            currentLocationLabel.setText("My Library");
            loadPane("src/main/resources/View/customerMyLibrary.fxml");
        }
    }
    private void preLoad() {
        String[] fxmlPaths = {
                "/com/lightlibrary/Views/CustomerDashboard.fxml",
                "/com/lightlibrary/Views/CustomerBorrowBook.fxml",
                "/com/lightlibrary/Views/CustomerSetting.fxml",
                "/com/lightlibrary/Views/CustomerMyLibrary.fxml",
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
                    e.printStackTrace(); // Consider logging the exception more robustly
                }
            });

            loadTask.setOnFailed(event -> loadTask.getException().printStackTrace());

            Thread loadPaneThread = new Thread(loadTask);
            loadPaneThread.setDaemon(true);
            loadPaneThread.start();
        }
    }
    public void loadPane(String fxmlPath) {
        if(cache.containsKey(fxmlPath)) {
            FXMLLoader loader = cache.get(fxmlPath);
            switchPaneAnimation(loader.getRoot());
        } else {
            Task<FXMLLoader> loadTask = new Task <>() {
                @Override
                protected FXMLLoader call() throws IOException {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    loader.load();
                    return loader;
                }
            };
            loadTask.setOnSucceeded(event -> {
                FXMLLoader loader = loadTask.getValue();
                loader.load();
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
        if(currentNode != null) {
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

}
