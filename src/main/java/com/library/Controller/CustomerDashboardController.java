package com.library.Controller;

import com.library.Models.Customer;
import com.library.Models.User;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform; // Thêm import này
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
// import org.checkerframework.checker.units.qual.A; // Bỏ import này nếu không dùng

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class CustomerDashboardController implements Initializable {
    @FXML
    private ImageView ADMINButtonView; // Có thể không dùng đến nếu chỉ có adminPanelButton
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
    private Button rankingButton;

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

    private Map<String, FXMLLoader> cache = new HashMap<>();
    private ActiveButton activeButton;

    public enum ActiveButton {
        ADMIN,
        HOME,
        SETTING,
        BORROW_BOOK,
        MY_LIBRARY,
        RANKING
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (userNameLabel != null) {
            userNameLabel.setText(customer != null && customer.getName() != null ?
                    customer.getName() : "Tên khách hàng"); // Mặc định nếu null
        }

        // Xử lý hiển thị nút Admin Panel
        boolean isAdmin = (customer != null && customer.getRole() == User.Role.ADMIN);
        if (adminPanelButton != null) {
            adminPanelButton.setVisible(isAdmin);
            adminPanelButton.setManaged(isAdmin); // Quan trọng để không chiếm không gian khi ẩn
        } else {
            System.err.println("Cảnh báo: adminPanelButton là null trong setCustomer. Đảm bảo nó đã được inject từ FXML.");
        }

        preLoad(); // Preload các FXML

        // Tự động chuyển đến trang Home sau khi setCustomer và preload
        // Đảm bảo HomeButton và navigationBorderPane đã được khởi tạo
        if (HomeButton != null && navigationBorderPane != null) {
            Platform.runLater(this::gotoHome); // Sử dụng method reference cho gọn
        } else {
            System.err.println("Lỗi: HomeButton hoặc navigationBorderPane chưa được khởi tạo khi gọi setCustomer.");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Sử dụng Platform.runLater cho các setOnAction nếu có nguy cơ FXML chưa load hoàn toàn
        // Tuy nhiên, thường thì trong initialize, các component đã được inject

        if (adminPanelButton != null) {
            adminPanelButton.setOnAction(e -> gotoAdminPanel());
        }
        settingButton.setOnAction(e -> {
            try {
                gotoSetting();
            } catch (IOException ex) {
                // Nên log lỗi hoặc hiển thị thông báo cho người dùng thay vì chỉ throw RuntimeException
                System.err.println("Lỗi khi chuyển đến Setting: " + ex.getMessage());
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Chuyển Trang", "Không thể mở trang cài đặt.");
            }
        });
        HomeButton.setOnAction(e -> gotoHome());
        borrowBookButton.setOnAction(e -> gotoBorrowBook());
        myLibraryButton.setOnAction(e -> gotoMyLibrary());
        rankingButton.setOnAction(e -> gotoRankingBook()); // Đảm bảo phương thức này tồn tại

        searchButton.setOnAction(e -> {
            String query = searchToolBar.getText();
            searchBooks(query);
        });
        searchToolBar.setOnAction(e -> { // Cho phép tìm kiếm khi nhấn Enter trên TextField
            String query = searchToolBar.getText();
            searchBooks(query);
        });
        logOutButton.setOnAction(e -> {
            try {
                logOut();
            } catch (IOException ex) {
                System.err.println("Lỗi khi đăng xuất: " + ex.getMessage());
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Đăng Xuất", "Không thể thực hiện đăng xuất.");
            }
        });
    }

    private void navigationBorderAnimation(Button targetButton) {
        // Kiểm tra null cho các thành phần trước khi sử dụng
        if (targetButton == null || targetButton.getScene() == null || targetButton.getParent() == null || navigationBorderPane == null) {
            String buttonId = targetButton != null ? targetButton.getId() : "UNKNOWN_ID";
            System.err.println("Cảnh báo: Nút " + buttonId + " hoặc navigationBorderPane chưa sẵn sàng cho animation.");
            return;
        }

        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.4), navigationBorderPane);
        // Tính toán vị trí Y của targetButton so với parent của navigationBorderPane
        // Giả sử các nút và navigationBorderPane có cùng parent (hoặc parent của parent là giống nhau)
        // và parent đó là một VBox hoặc AnchorPane nơi layoutY có ý nghĩa.
        double buttonCenterY = targetButton.getLayoutY() + targetButton.getHeight() / 2;
        double targetY = buttonCenterY - (navigationBorderPane.getHeight() / 2);

        // Nếu navigationBorderPane và các nút không nằm trong cùng một container trực tiếp,
        // bạn có thể cần chuyển đổi tọa độ phức tạp hơn.
        // Cách đơn giản nhất là đặt chúng vào cùng một VBox/Pane ở bên trái.
        // Giả sử navigationBorderPane.getLayoutY() là 0 so với parent của nó.
        transition.setToY(targetY - navigationBorderPane.getLayoutY());
        transition.play();
    }

    public void gotoAdminPanel() {
        if (activeButton != ActiveButton.ADMIN) {
            loadPane("/com/library/View/ADMIN.fxml");
            activeButton = ActiveButton.ADMIN;
            currentLocationLabel.setText("Bảng Điều Khiển ADMIN");
            if (adminPanelButton != null) navigationBorderAnimation(adminPanelButton);
        }
    }

    public void gotoHome() {
        if (activeButton != ActiveButton.HOME) {
            loadPane("/com/library/View/CustomerHome.fxml");
            activeButton = ActiveButton.HOME;
            currentLocationLabel.setText("Trang Chủ");
            if (HomeButton != null) navigationBorderAnimation(HomeButton);
        }
    }

    public void gotoRankingBook() { // Sửa tên cho nhất quán
        if (activeButton != ActiveButton.RANKING) {
            loadPane("/com/library/View/CustomerRankingBook.fxml");
            activeButton = ActiveButton.RANKING;
            currentLocationLabel.setText("Bảng Xếp Hạng Sách");
            if (rankingButton != null) navigationBorderAnimation(rankingButton);
        }
    }

    public void gotoSetting() throws IOException { // Giữ throws IOException nếu loadPane có thể ném
        if (activeButton != ActiveButton.SETTING) {
            loadPane("/com/library/View/CustomerSetting.fxml");
            activeButton = ActiveButton.SETTING;
            currentLocationLabel.setText("Cài Đặt");
            if (settingButton != null) navigationBorderAnimation(settingButton);
        }
    }

    public void gotoBorrowBook() {
        if (activeButton != ActiveButton.BORROW_BOOK) {
            loadPane("/com/library/View/CustomerBorrowBook.fxml");
            activeButton = ActiveButton.BORROW_BOOK;
            currentLocationLabel.setText("Mượn Sách");
            if (borrowBookButton != null) navigationBorderAnimation(borrowBookButton);
        }
    }

    public void gotoMyLibrary() {
        if (activeButton != ActiveButton.MY_LIBRARY) {
            loadPane("/com/library/View/CustomerMyLibrary.fxml");
            activeButton = ActiveButton.MY_LIBRARY;
            currentLocationLabel.setText("Thư Viện Của Tôi");
            if (myLibraryButton != null) navigationBorderAnimation(myLibraryButton);
        }
    }

    public void switchToAdminAddBookView(String searchQuery) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/library/View/AdminAddBook.fxml")));
            Parent adminAddBookRoot = loader.load();

            AdminAddBookController controller = loader.getController();
            if (controller != null) {
                controller.setDashboardController(this); // Truyền tham chiếu dashboard
                controller.initializeSearchWithQuery(searchQuery);
            } else {
                System.err.println("AdminAddBookController is null sau khi load AdminAddBook.fxml");
                showAlert(Alert.AlertType.ERROR, "Lỗi Controller", "Không thể khởi tạo controller cho trang thêm sách.");
                return;
            }
            switchPaneAnimation(adminAddBookRoot);
            if(currentLocationLabel!=null) currentLocationLabel.setText("Thêm Sách Vào Thư Viện");
            // Cần quyết định nút nào sẽ active khi ở trang này.
            // Ví dụ, nếu nó được gọi từ khu vực admin, thì adminPanelButton nên active.
            // activeButton = ActiveButton.ADMIN;
            // if(adminPanelButton != null) navigationBorderAnimation(adminPanelButton);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Trang", "Không thể mở trang thêm sách: " + e.getMessage());
        }
    }

    public Object getLoadedControllerInstance(String fxmlPath) {
        FXMLLoader loader = cache.get(fxmlPath);
        if (loader != null) {
            return loader.getController();
        }
        // Thông báo này có thể không cần thiết nếu việc controller không có trong cache được xử lý ở nơi gọi
        // System.err.println("No cached loader found for: " + fxmlPath);
        return null;
    }

    private void preLoad() {
        String[] fxmlPaths = {
                "/com/library/View/CustomerHome.fxml",
                "/com/library/View/CustomerBorrowBook.fxml",
                "/com/library/View/CustomerSetting.fxml",
                "/com/library/View/CustomerMyLibrary.fxml",
                "/com/library/View/CustomerRankingBook.fxml", // Đã có
                "/com/library/View/ADMIN.fxml" // Nếu admin panel cũng là một pane trong mainContainer
        };

        for (String fxmlPath : fxmlPaths) {
            if (!cache.containsKey(fxmlPath)) {
                Task<FXMLLoader> loadTask = new Task<>() {
                    @Override
                    protected FXMLLoader call() throws Exception {
                        // System.out.println("Preloading: " + fxmlPath); // Để debug
                        FXMLLoader loaderInstance = new FXMLLoader();
                        URL resourceUrl = getClass().getResource(fxmlPath);
                        if (resourceUrl == null) {
                            System.err.println("Preload Error: Cannot find FXML resource: " + fxmlPath);
                            throw new IOException("Cannot find FXML resource: " + fxmlPath);
                        }
                        loaderInstance.setLocation(resourceUrl);
                        // Quan trọng: Phải gọi load() để controller được tạo và FXML được phân tích cú pháp
                        loaderInstance.load();
                        return loaderInstance;
                    }
                };

                loadTask.setOnSucceeded(event -> {
                    FXMLLoader loader = loadTask.getValue();
                    if (loader != null && loader.getRoot() != null && loader.getController() != null) {
                        cache.put(fxmlPath, loader);
                        // System.out.println("Preloaded and cached: " + fxmlPath);
                        // Không cần gọi configureChildController ở đây, nó sẽ được gọi khi loadPane thực sự
                    } else {
                        System.err.println("Lỗi preload: FXMLLoader, Root hoặc Controller là null sau khi load FXML: " + fxmlPath);
                    }
                });

                loadTask.setOnFailed(event -> {
                    Throwable exception = loadTask.getException();
                    System.err.println("Preload FXML " + fxmlPath + " failed.");
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                });

                Thread loadPaneThread = new Thread(loadTask);
                loadPaneThread.setDaemon(true); // Để thread tự kết thúc khi ứng dụng đóng
                loadPaneThread.start();
            }
        }
    }

    public void loadPane(String fxmlPath) {
        FXMLLoader loader = cache.get(fxmlPath);
        Parent paneRoot = null;

        if (loader != null && loader.getRoot() != null) { // Kiểm tra loader và root đã được load từ cache
            // System.out.println("Loading from cache: " + fxmlPath);
            paneRoot = loader.getRoot();
            // Đảm bảo controller được cấu hình ngay cả khi load từ cache
            configureChildController(loader, fxmlPath);
        } else {
            // System.out.println("Loading from FXML (not in cache or cache invalid): " + fxmlPath);
            try {
                loader = new FXMLLoader(); // Tạo loader mới
                URL resourceUrl = getClass().getResource(fxmlPath);
                if (resourceUrl == null) {
                    System.err.println("loadPane Error: Không tìm thấy FXML resource: " + fxmlPath);
                    showAlert(Alert.AlertType.ERROR, "Lỗi Tải Trang", "Không thể tìm thấy tệp giao diện: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1));
                    return;
                }
                loader.setLocation(resourceUrl);
                paneRoot = loader.load(); // Load FXML, controller sẽ được tạo

                if (loader.getController() == null) {
                    System.err.println("loadPane Error: Controller là null sau khi load FXML: " + fxmlPath);
                    showAlert(Alert.AlertType.ERROR, "Lỗi Controller", "Không thể khởi tạo controller cho trang.");
                    return;
                }
                cache.put(fxmlPath, loader); // Lưu vào cache sau khi load thành công
                configureChildController(loader, fxmlPath);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi I/O", "Lỗi khi tải giao diện: " + e.getMessage());
                return; // Không tiếp tục nếu có lỗi
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Không Xác Định", "Lỗi không mong muốn khi tải trang: " + e.getMessage());
                return; // Không tiếp tục nếu có lỗi
            }
        }

        if (paneRoot != null) {
            switchPaneAnimation(paneRoot);
        } else {
            System.err.println("loadPane Error: paneRoot is null for " + fxmlPath + ". Cannot switch pane.");
        }
    }

    private void configureChildController(FXMLLoader loader, String fxmlPath) {
        if (loader == null || loader.getController() == null) {
            // System.err.println("configureChildController: FXMLLoader hoặc Controller là null cho " + fxmlPath);
            return; // Đã có log ở nơi gọi hoặc preload, không cần log lại trừ khi muốn debug sâu
        }
        Object controller = loader.getController();
        // System.out.println("Configuring controller for: " + fxmlPath + " - Type: " + controller.getClass().getName());

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
        } else if (controller instanceof CustomerRankingBookController) {
            // CustomerRankingBookController hiện không cần tham chiếu ngược lại Dashboard
            // Nhưng nếu sau này cần, bạn có thể thêm:
            // ((CustomerRankingBookController) controller).setDashboardController(this);
            // System.out.println("CustomerRankingBookController instance configured (if it had a setDashboardController method).");
        }
        // Thêm các else if khác cho các child controller khác nếu cần
    }

    public void switchPaneAnimation(Node newPaneNode) {
        if (newPaneNode == null) {
            System.err.println("switchPaneAnimation: newPaneNode is null. Cannot switch.");
            return;
        }
        if (mainContainer == null) {
            System.err.println("switchPaneAnimation: mainContainer is null. Cannot switch.");
            return;
        }

        Node oldPaneNode = currentNode; // Lưu lại node hiện tại để làm mờ

        if (oldPaneNode != null && oldPaneNode != newPaneNode) { // Chỉ chạy animation nếu node thay đổi
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), oldPaneNode);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                mainContainer.getChildren().remove(oldPaneNode); // Xóa node cũ sau khi mờ hẳn
                mainContainer.getChildren().add(newPaneNode);    // Thêm node mới
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), newPaneNode);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                currentNode = newPaneNode; // Cập nhật currentNode
            });
            fadeOut.play();
        } else if (oldPaneNode == null) { // Trường hợp mainContainer rỗng ban đầu
            mainContainer.getChildren().add(newPaneNode);
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), newPaneNode);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            currentNode = newPaneNode;
        } else {
            // oldPaneNode == newPaneNode (không có gì để làm, hoặc reload cùng pane)
            // Nếu muốn reload cùng pane thì phải clear children rồi add lại
            // mainContainer.getChildren().clear();
            // mainContainer.getChildren().add(newPaneNode);
            // currentNode = newPaneNode;
            // Hoặc có thể không làm gì cả nếu không muốn hiệu ứng khi reload cùng pane
        }
    }

    public void searchBooks(String query) {
        // Luôn chuyển đến trang BorrowBook để hiển thị kết quả tìm kiếm
        gotoBorrowBook();

        // Đảm bảo rằng CustomerBorrowBook.fxml đã được load và controller của nó đã sẵn sàng.
        // Sử dụng Platform.runLater để đảm bảo việc chuyển trang (UI update) đã hoàn tất
        // trước khi cố gắng lấy controller và gọi phương thức của nó.
        Platform.runLater(() -> {
            String borrowBookFxmlPath = "/com/library/View/CustomerBorrowBook.fxml";
            FXMLLoader loader = cache.get(borrowBookFxmlPath);

            if (loader != null && loader.getController() instanceof CustomerBorrowBookController) {
                CustomerBorrowBookController controller = (CustomerBorrowBookController) loader.getController();
                controller.updateSearchBooksResult(query);
                // navigationBorderAnimation đã được gọi trong gotoBorrowBook()
            } else {
                System.err.println("searchBooks: Không thể tìm thấy controller cho CustomerBorrowBook.fxml hoặc controller không đúng loại.");
                // Cân nhắc: nếu không tìm thấy, có thể là do gotoBorrowBook() gặp lỗi.
                // showAlert(Alert.AlertType.WARNING, "Lỗi Tìm Kiếm", "Không thể hiển thị kết quả tìm kiếm vào lúc này.");
            }
        });
    }

    public void refreshCustomerDisplayInfo() {
        if (this.currentCustomer != null && userNameLabel != null) {
            userNameLabel.setText(this.currentCustomer.getName() != null ?
                    this.currentCustomer.getName() : "Tên khách hàng");
        }
    }

    public void logOut() throws IOException {
        // Nên xóa cache hoặc các tài nguyên khác nếu cần trước khi logout
        cache.clear(); // Ví dụ: xóa cache FXML
        currentNode = null; // Reset node hiện tại

        Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(getClass()
                .getResource("/com/library/View/Login.fxml")));
        Stage stage = (Stage) dashboardRoot.getScene().getWindow();
        stage.setScene(new Scene(loginRoot, 749, 555)); // Kích thước màn hình login
        stage.centerOnScreen();
        stage.setTitle("Đăng Nhập Thư Viện"); // Cập nhật tiêu đề cửa sổ
        stage.show();
    }

    // ---------- PHƯƠNG THỨC QUAN TRỌNG ĐỂ CẬP NHẬT RANKING ----------
    public void notifyBookBorrowedAndUpdateRanking() {
        String rankingFxmlPath = "/com/library/View/CustomerRankingBook.fxml";
        FXMLLoader loader = cache.get(rankingFxmlPath);

        if (loader != null && loader.getController() instanceof CustomerRankingBookController) {
            CustomerRankingBookController rankingController = (CustomerRankingBookController) loader.getController();
            // System.out.println("Dashboard: Yêu cầu cập nhật bảng xếp hạng.");
            // Chạy trên UI thread để đảm bảo an toàn khi thao tác với giao diện
            Platform.runLater(rankingController::loadRankedBooks); // Sử dụng method reference
        } else {
            if (loader == null) {
                System.err.println("Dashboard - UpdateRanking: FXMLLoader cho " + rankingFxmlPath + " không có trong cache. Ranking sẽ được làm mới khi người dùng điều hướng đến đó.");
                // Nếu muốn chủ động load Ranking view nếu nó chưa có trong cache:
                // Platform.runLater(() -> {
                //     gotoRankingBook(); // Điều này sẽ load và hiển thị nó, tự động lấy dữ liệu mới
                //     // Sau đó nếu bạn muốn nó vẫn ở trang hiện tại, bạn cần logic phức tạp hơn
                // });
            } else { // loader.getController() không phải là CustomerRankingBookController
                System.err.println("Dashboard - UpdateRanking: Controller trong cache cho " + rankingFxmlPath + " không phải là CustomerRankingBookController. Actual: " + (loader.getController() != null ? loader.getController().getClass().getName() : "null controller"));
            }
        }
    }

    // Phương thức tiện ích để hiển thị Alert
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        // Đảm bảo alert chạy trên UI thread
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(alertType);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            });
        }
    }
}