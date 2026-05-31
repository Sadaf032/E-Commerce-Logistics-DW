// ModernEcoLogisDashboard.java - Complete Fixed Version with Proper Header Spacing
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ModernDashboard extends Application {

    private VBox sidebar;
    private BorderPane root;
    private HBox kpiRow;
    private VBox center;
    private Stage stage;
    private Stage loginStage;
    private String currentPage = "Dashboard";

    // KPI Labels
    private Label totalOrdersValue, totalRevenueValue, totalProfitValue, avgDeliveryValue;
    private Label onTimeRateValue, delayedRateValue;

    // Tables
    private TableView<Map<String, Object>> ordersTable;
    private TableView<Map<String, Object>> yearlySummaryTable;
    private TableView<Map<String, Object>> topProductsTable;

    // Charts
    private LineChart<String, Number> yearlyRevenueChart;
    private BarChart<String, Number> categoryRevenueChart;
    private PieChart statusChart;
    private BarChart<String, Number> deliveryChart;

    private ScheduledExecutorService scheduler;
    private Label loadingLabel;
    private ComboBox<String> yearFilter;
    private Label dbStatusLabel;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        loginStage = new Stage();
        loginStage.initStyle(StageStyle.UNDECORATED);

        BorderPane loginRoot = new BorderPane();
        loginRoot.setStyle("-fx-background-color: white;");

        // Title bar with window controls
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5));
        titleBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        Button minimizeBtn = new Button("─");
        minimizeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand; -fx-text-fill: #6c757d;");
        minimizeBtn.setOnAction(e -> loginStage.setIconified(true));

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand; -fx-text-fill: #6c757d;");
        closeBtn.setOnMouseEntered(_ -> closeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 14px;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-text-fill: #6c757d;"));
        closeBtn.setOnAction(e -> System.exit(0));

        titleBar.getChildren().addAll(minimizeBtn, closeBtn);
        loginRoot.setTop(titleBar);

        // Login card
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40, 50, 40, 50));
        loginCard.setMaxWidth(400);
        loginCard.setStyle("-fx-background-color: white;");

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER);
        Label logoIcon = new Label("🚚");
        logoIcon.setFont(Font.font("Arial", 45));
        Label logoText = new Label("Eco Logistics");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        logoText.setStyle("-fx-text-fill: #2c3e50;");
        logoBox.getChildren().addAll(logoIcon, logoText);

        Label welcomeLabel = new Label("Welcome Back!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        welcomeLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-text-fill: #495057; -fx-font-weight: bold; -fx-font-size: 13px;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 10; -fx-background-radius: 5;");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: #495057; -fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 10; -fx-background-radius: 5;");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Database status
        dbStatusLabel = new Label("Checking database connection...");
        dbStatusLabel.setFont(Font.font("Arial", 11));
        dbStatusLabel.setStyle("-fx-text-fill: #6c757d;");

        // Check database connection
        new Thread(() -> {
            try {
                Connection conn = DBConnector.connect();
                if (conn != null && !conn.isClosed()) {
                    Platform.runLater(() -> {
                        dbStatusLabel.setText("✅ Database Connected Successfully");
                        dbStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 11px;");
                    });
                    conn.close();
                } else {
                    Platform.runLater(() -> {
                        dbStatusLabel.setText("⚠️ Database not connected, using sample data");
                        dbStatusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 11px;");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    dbStatusLabel.setText("⚠️ " + e.getMessage());
                    dbStatusLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 11px;");
                });
            }
        }).start();

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"));
        loginBtn.setOnAction(e -> {
            if (usernameField.getText().equals("admin") && passwordField.getText().equals("admin123")) {
                loginStage.close();
                showDashboardMain();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Invalid credentials! Use admin/admin123");
                alert.showAndWait();
            }
        });

        loginCard.getChildren().addAll(logoBox, welcomeLabel, usernameBox, passwordBox, dbStatusLabel, loginBtn);
        loginRoot.setCenter(loginCard);

        Scene scene = new Scene(loginRoot, 450, 550);
        scene.setFill(Color.WHITE);
        loginStage.setScene(scene);
        loginStage.show();
    }

    private void showDashboardMain() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");

        createSidebar();
        createHeader();

        center = new VBox(15);
        center.setPadding(new Insets(15, 20, 20, 20));
        center.setStyle("-fx-background-color: #f5f6fa;");

        createKPIRow();
        createCharts();
        createTables();

        showDashboard();

        root.setLeft(sidebar);
        root.setCenter(center);

        Scene scene = new Scene(root, 1300, 800);
        scene.setFill(Color.WHITE);
        stage.setScene(scene);
        stage.setTitle("EcoLogis - Logistics Dashboard");
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.show();

        animateOnStart();
        loadAllData();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::loadAllData), 60, 60, TimeUnit.SECONDS);
    }

    private void createSidebar() {
        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(230);

        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 15, 20, 15));
        Label logoIcon = new Label("🚚");
        logoIcon.setFont(Font.font("Arial", 28));
        Label logoText = new Label("EcoLogis");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        logoText.setStyle("-fx-text-fill: white;");
        logoBox.getChildren().addAll(logoIcon, logoText);

        VBox navBox = new VBox(5);
        navBox.setPadding(new Insets(10));

        String[][] items = {
                {"📊  Dashboard", "Dashboard"},
                {"📦  All Orders", "Orders"},
                {"📈  Yearly Summary", "YearlySummary"},
                {"🏆  Top Products", "TopProducts"},
                {"📉  Analytics", "Analytics"}
        };

        for (String[] item : items) {
            Button btn = new Button(item[0]);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-font-size: 13px; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-font-size: 13px; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-font-size: 13px;"));
            btn.setOnAction(e -> navigateTo(item[1]));
            navBox.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 5;");
        logoutBtn.setOnAction(e -> logout());

        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.getChildren().addAll(new Separator(), logoutBtn);

        sidebar.getChildren().addAll(logoBox, new Separator(), navBox, spacer, bottomBox);
    }

    private void createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(12, 25, 12, 25));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Left: Dashboard title
        HBox leftBox = new HBox(8);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📊");
        iconLabel.setFont(Font.font("Arial", 18));

        Label title = new Label("Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #1e293b;");

        leftBox.getChildren().addAll(iconLabel, title);

        // Center: Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right: Controls
        HBox rightBox = new HBox(12);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        // Year filter group
        HBox yearGroup = new HBox(8);
        yearGroup.setAlignment(Pos.CENTER_LEFT);
        yearGroup.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 5 10;"
        );

        Label yearIcon = new Label("📅");
        yearIcon.setFont(Font.font("Arial", 11));

        Label yearLabel = new Label("Year:");
        yearLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");

        yearFilter = new ComboBox<>();
        yearFilter.getItems().addAll("All", "2023", "2024", "2025", "2026");
        yearFilter.setValue("All");
        yearFilter.setPrefWidth(75);
        yearFilter.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 0;"
        );
        yearFilter.setOnAction(e -> loadAllData());

        yearGroup.getChildren().addAll(yearIcon, yearLabel, yearFilter);

        // Vertical separator
        Separator vertSep1 = new Separator();
        vertSep1.setOrientation(Orientation.VERTICAL);
        vertSep1.setStyle("-fx-background-color: #e2e8f0;");
        vertSep1.setPrefHeight(20);

        // Loading status badge
        loadingLabel = new Label("✅ Live");
        loadingLabel.setStyle(
                "-fx-text-fill: #059669; " +
                        "-fx-font-size: 10px; " +
                        "-fx-background-color: #d1fae5; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 4 10; " +
                        "-fx-font-weight: bold;"
        );

        // Refresh button
        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle(
                "-fx-background-color: #f1f5f9; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 6 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 1;"
        );
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 6 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px;"
        ));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle(
                "-fx-background-color: #f1f5f9; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 6 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 1;"
        ));
        refreshBtn.setOnAction(e -> loadAllData());

        // Vertical separator 2
        Separator vertSep2 = new Separator();
        vertSep2.setOrientation(Orientation.VERTICAL);
        vertSep2.setStyle("-fx-background-color: #e2e8f0;");
        vertSep2.setPrefHeight(20);

        // Date badge
        HBox dateBox = new HBox(6);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 5 10;"
        );

        Label calIcon = new Label("📅");
        calIcon.setFont(Font.font("Arial", 11));

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        dateBox.getChildren().addAll(calIcon, dateLabel);

        // Add all right side elements
        rightBox.getChildren().addAll(yearGroup, vertSep1, loadingLabel, refreshBtn, vertSep2, dateBox);

        // Assemble header
        header.getChildren().addAll(leftBox, spacer, rightBox);

        root.setTop(header);
    }

    private void createKPIRow() {
        kpiRow = new HBox(12);
        kpiRow.setAlignment(Pos.CENTER);
        kpiRow.setPadding(new Insets(0, 0, 15, 0));

        VBox card1 = createKPICard("📦 Total Orders", "0", "#007bff");
        VBox card2 = createKPICard("💰 Total Revenue", "$0", "#28a745");
        VBox card3 = createKPICard("📈 Total Profit", "$0", "#17a2b8");
        VBox card4 = createKPICard("⏱ Avg Delivery", "0 hrs", "#ffc107");
        VBox card5 = createKPICard("✅ On-Time Rate", "0%", "#20c997");
        VBox card6 = createKPICard("⚠ Delayed Rate", "0%", "#dc3545");

        totalOrdersValue = (Label) card1.getChildren().get(1);
        totalRevenueValue = (Label) card2.getChildren().get(1);
        totalProfitValue = (Label) card3.getChildren().get(1);
        avgDeliveryValue = (Label) card4.getChildren().get(1);
        onTimeRateValue = (Label) card5.getChildren().get(1);
        delayedRateValue = (Label) card6.getChildren().get(1);

        for (VBox card : new VBox[]{card1, card2, card3, card4, card5, card6}) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        kpiRow.getChildren().addAll(card1, card2, card3, card4, card5, card6);
    }

    private VBox createKPICard(String title, String value, String color) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(130);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        titleLabel.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        valueLabel.setAlignment(Pos.CENTER);

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void createCharts() {
        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis yAxis1 = new NumberAxis();
        yearlyRevenueChart = new LineChart<>(xAxis1, yAxis1);
        yearlyRevenueChart.setTitle("Yearly Revenue");
        yearlyRevenueChart.setAnimated(false);
        yearlyRevenueChart.setStyle("-fx-background-color: white;");

        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        categoryRevenueChart = new BarChart<>(xAxis2, yAxis2);
        categoryRevenueChart.setTitle("Revenue by Category");
        categoryRevenueChart.setAnimated(false);
        categoryRevenueChart.setStyle("-fx-background-color: white;");

        statusChart = new PieChart();
        statusChart.setTitle("Order Status");
        statusChart.setLabelsVisible(true);
        statusChart.setAnimated(false);
        statusChart.setStyle("-fx-background-color: white;");

        CategoryAxis xAxis3 = new CategoryAxis();
        NumberAxis yAxis3 = new NumberAxis();
        deliveryChart = new BarChart<>(xAxis3, yAxis3);
        deliveryChart.setTitle("Delivery by Warehouse");
        deliveryChart.setAnimated(false);
        deliveryChart.setStyle("-fx-background-color: white;");
    }

    private void createTables() {
        ordersTable = new TableView<>();
        ordersTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        yearlySummaryTable = new TableView<>();
        yearlySummaryTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        yearlySummaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        topProductsTable = new TableView<>();
        topProductsTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        topProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void showDashboard() {
        center.getChildren().clear();
        center.getChildren().add(kpiRow);

        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(12);
        chartsGrid.setVgap(12);

        ColumnConstraints colConst = new ColumnConstraints();
        colConst.setPercentWidth(50);
        chartsGrid.getColumnConstraints().addAll(colConst, colConst);

        RowConstraints rowConst = new RowConstraints();
        rowConst.setPercentHeight(50);
        chartsGrid.getRowConstraints().addAll(rowConst, rowConst);

        chartsGrid.add(yearlyRevenueChart, 0, 0);
        chartsGrid.add(categoryRevenueChart, 1, 0);
        chartsGrid.add(statusChart, 0, 1);
        chartsGrid.add(deliveryChart, 1, 1);

        VBox.setVgrow(chartsGrid, Priority.ALWAYS);
        center.getChildren().add(chartsGrid);

        Label summaryLabel = new Label("Yearly Performance Summary");
        summaryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        summaryLabel.setStyle("-fx-text-fill: #2c3e50; -fx-padding: 10 0 5 0;");
        center.getChildren().addAll(summaryLabel, yearlySummaryTable);
        VBox.setVgrow(yearlySummaryTable, Priority.ALWAYS);

        currentPage = "Dashboard";
    }

    private void showOrders() {
        center.getChildren().clear();
        Label title = new Label("All Orders");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");
        center.getChildren().addAll(title, ordersTable);
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        currentPage = "Orders";
        loadOrdersData();
    }

    private void showYearlySummary() {
        center.getChildren().clear();
        Label title = new Label("Yearly Summary");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");
        center.getChildren().addAll(title, yearlySummaryTable);
        VBox.setVgrow(yearlySummaryTable, Priority.ALWAYS);
        currentPage = "YearlySummary";
    }

    private void showTopProducts() {
        center.getChildren().clear();
        Label title = new Label("Top Products");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");
        center.getChildren().addAll(title, topProductsTable);
        VBox.setVgrow(topProductsTable, Priority.ALWAYS);
        currentPage = "TopProducts";
    }

    private void showAnalytics() {
        center.getChildren().clear();
        Label title = new Label("Analytics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #2c3e50;");

        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(12);
        chartsGrid.setVgap(12);

        ColumnConstraints colConst = new ColumnConstraints();
        colConst.setPercentWidth(50);
        chartsGrid.getColumnConstraints().addAll(colConst, colConst);

        RowConstraints rowConst = new RowConstraints();
        rowConst.setPercentHeight(50);
        chartsGrid.getRowConstraints().addAll(rowConst, rowConst);

        chartsGrid.add(yearlyRevenueChart, 0, 0);
        chartsGrid.add(categoryRevenueChart, 1, 0);
        chartsGrid.add(statusChart, 0, 1);
        chartsGrid.add(deliveryChart, 1, 1);

        VBox.setVgrow(chartsGrid, Priority.ALWAYS);
        center.getChildren().addAll(title, chartsGrid);
        currentPage = "Analytics";
    }

    private void navigateTo(String page) {
        switch(page) {
            case "Dashboard": showDashboard(); break;
            case "Orders": showOrders(); break;
            case "YearlySummary": showYearlySummary(); break;
            case "TopProducts": showTopProducts(); break;
            case "Analytics": showAnalytics(); break;
        }
    }

    private void loadAllData() {
        Platform.runLater(() -> {
            loadingLabel.setText("⏳ Loading");
            loadingLabel.setStyle(
                    "-fx-text-fill: #d97706; " +
                            "-fx-font-size: 10px; " +
                            "-fx-background-color: #fef3c7; " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 4 10; " +
                            "-fx-font-weight: bold;"
            );
        });

        new Thread(() -> {
            try {
                boolean dbSuccess = loadRealData();

                if (!dbSuccess) {
                    loadSampleData();
                }

                Platform.runLater(() -> {
                    loadingLabel.setText("✅ Live");
                    loadingLabel.setStyle(
                            "-fx-text-fill: #059669; " +
                                    "-fx-font-size: 10px; " +
                                    "-fx-background-color: #d1fae5; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-padding: 4 10; " +
                                    "-fx-font-weight: bold;"
                    );
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("⚠️ Sample");
                    loadingLabel.setStyle(
                            "-fx-text-fill: #d97706; " +
                                    "-fx-font-size: 10px; " +
                                    "-fx-background-color: #fef3c7; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-padding: 4 10; " +
                                    "-fx-font-weight: bold;"
                    );
                });
                loadSampleData();
                e.printStackTrace();
            }
        }).start();
    }

    private boolean loadRealData() {
        boolean success = loadKPIData();
        if (!success) return false;

        loadYearlyRevenueChart();
        loadCategoryChart();
        loadStatusChart();
        loadDeliveryChart();
        loadYearlySummary();
        loadTopProducts();

        if (currentPage.equals("Orders")) {
            loadOrdersData();
        }
        return true;
    }

    private boolean loadKPIData() {
        String sql = "SELECT COUNT(*) AS total_orders, " +
                "COALESCE(SUM(revenue), 0) AS total_revenue, " +
                "COALESCE(SUM(profit), 0) AS total_profit, " +
                "COALESCE(AVG(delivery_time_hours), 0) AS avg_delivery " +
                "FROM fact_logistics";

        if (!yearFilter.getValue().equals("All")) {
            sql = "SELECT COUNT(*) AS total_orders, " +
                    "COALESCE(SUM(f.revenue), 0) AS total_revenue, " +
                    "COALESCE(SUM(f.profit), 0) AS total_profit, " +
                    "COALESCE(AVG(f.delivery_time_hours), 0) AS avg_delivery " +
                    "FROM fact_logistics f " +
                    "JOIN dim_date d ON f.date_id = d.date_id " +
                    "WHERE d.year = " + yearFilter.getValue();
        }

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int totalOrders = rs.getInt("total_orders");
                double totalRevenue = rs.getDouble("total_revenue");
                double totalProfit = rs.getDouble("total_profit");
                double avgDelivery = rs.getDouble("avg_delivery");

                String statusSql = "SELECT " +
                        "SUM(CASE WHEN order_status = 'Delivered' THEN 1 ELSE 0 END) AS delivered_count, " +
                        "SUM(CASE WHEN order_status = 'Delayed' THEN 1 ELSE 0 END) AS delayed_count " +
                        "FROM fact_logistics";

                if (!yearFilter.getValue().equals("All")) {
                    statusSql = "SELECT " +
                            "SUM(CASE WHEN f.order_status = 'Delivered' THEN 1 ELSE 0 END) AS delivered_count, " +
                            "SUM(CASE WHEN f.order_status = 'Delayed' THEN 1 ELSE 0 END) AS delayed_count " +
                            "FROM fact_logistics f " +
                            "JOIN dim_date d ON f.date_id = d.date_id " +
                            "WHERE d.year = " + yearFilter.getValue();
                }

                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs2 = stmt2.executeQuery(statusSql)) {

                    if (rs2.next()) {
                        int delivered = rs2.getInt("delivered_count");
                        int delayed = rs2.getInt("delayed_count");
                        double onTimeRate = totalOrders > 0 ? (delivered * 100.0 / totalOrders) : 0;
                        double delayedRate = totalOrders > 0 ? (delayed * 100.0 / totalOrders) : 0;

                        Platform.runLater(() -> {
                            totalOrdersValue.setText(String.format("%,d", totalOrders));
                            totalRevenueValue.setText(String.format("$%,.0f", totalRevenue));
                            totalProfitValue.setText(String.format("$%,.0f", totalProfit));
                            avgDeliveryValue.setText(String.format("%.0f hrs", avgDelivery));
                            onTimeRateValue.setText(String.format("%.0f%%", onTimeRate));
                            delayedRateValue.setText(String.format("%.0f%%", delayedRate));
                        });
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("KPI Error: " + e.getMessage());
        }
        return false;
    }

    private void loadYearlyRevenueChart() {
        String sql = "SELECT d.year, COALESCE(SUM(f.revenue), 0) AS revenue " +
                "FROM fact_logistics f " +
                "JOIN dim_date d ON f.date_id = d.date_id " +
                "GROUP BY d.year ORDER BY d.year";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        String.valueOf(rs.getInt("year")),
                        rs.getDouble("revenue")
                ));
            }

            Platform.runLater(() -> {
                yearlyRevenueChart.getData().clear();
                if (!series.getData().isEmpty()) {
                    yearlyRevenueChart.getData().add(series);
                }
            });
        } catch (Exception e) {
            System.err.println("Yearly Revenue Chart Error: " + e.getMessage());
        }
    }

    private void loadCategoryChart() {
        String sql = "SELECT p.category, COALESCE(SUM(f.revenue), 0) AS revenue " +
                "FROM fact_logistics f " +
                "JOIN dim_product p ON f.product_id = p.product_id " +
                "GROUP BY p.category ORDER BY revenue DESC";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue by Category");
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("category"),
                        rs.getDouble("revenue")
                ));
            }

            Platform.runLater(() -> {
                categoryRevenueChart.getData().clear();
                if (!series.getData().isEmpty()) {
                    categoryRevenueChart.getData().add(series);
                }
            });
        } catch (Exception e) {
            System.err.println("Category Chart Error: " + e.getMessage());
        }
    }

    private void loadStatusChart() {
        String sql = "SELECT order_status, COUNT(*) AS cnt FROM fact_logistics GROUP BY order_status";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            while (rs.next()) {
                pieData.add(new PieChart.Data(
                        rs.getString("order_status"),
                        rs.getInt("cnt")
                ));
            }

            Platform.runLater(() -> {
                if (!pieData.isEmpty()) {
                    statusChart.setData(pieData);
                }
            });
        } catch (Exception e) {
            System.err.println("Status Chart Error: " + e.getMessage());
        }
    }

    private void loadDeliveryChart() {
        String sql = "SELECT w.warehouse_city, COALESCE(AVG(f.delivery_time_hours), 0) AS avg_delivery " +
                "FROM fact_logistics f " +
                "JOIN dim_warehouse w ON f.warehouse_id = w.warehouse_id " +
                "GROUP BY w.warehouse_city ORDER BY avg_delivery";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Avg Delivery Hours");
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        rs.getString("warehouse_city"),
                        rs.getDouble("avg_delivery")
                ));
            }

            Platform.runLater(() -> {
                deliveryChart.getData().clear();
                if (!series.getData().isEmpty()) {
                    deliveryChart.getData().add(series);
                }
            });
        } catch (Exception e) {
            System.err.println("Delivery Chart Error: " + e.getMessage());
        }
    }

    private void loadYearlySummary() {
        Platform.runLater(() -> {
            yearlySummaryTable.getColumns().clear();
            String[] cols = {"Year", "Total Orders", "Total Revenue", "Total Profit", "Total Loss", "Avg Delivery (hrs)"};
            for (String col : cols) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(col);
                final String c = col;
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get(c))));
                yearlySummaryTable.getColumns().add(column);
            }
        });

        String sql = "SELECT * FROM yearly_summary ORDER BY year";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("Year", rs.getInt("year"));
                row.put("Total Orders", rs.getInt("total_orders"));
                row.put("Total Revenue", String.format("$%,.0f", rs.getDouble("total_revenue")));
                row.put("Total Profit", String.format("$%,.0f", rs.getDouble("total_profit")));
                row.put("Total Loss", String.format("$%,.0f", rs.getDouble("total_loss")));
                row.put("Avg Delivery (hrs)", String.format("%.0f", rs.getDouble("avg_delivery_time")));
                data.add(row);
            }

            Platform.runLater(() -> yearlySummaryTable.setItems(data));
        } catch (Exception e) {
            System.err.println("Yearly Summary Error: " + e.getMessage());
        }
    }

    private void loadTopProducts() {
        Platform.runLater(() -> {
            topProductsTable.getColumns().clear();
            String[] cols = {"Product Name", "Category", "Units Sold", "Revenue", "Profit"};
            for (String col : cols) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(col);
                final String c = col;
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get(c))));
                topProductsTable.getColumns().add(column);
            }
        });

        String sql = "SELECT p.product_name, p.category, " +
                "COALESCE(SUM(f.order_quantity), 0) AS units_sold, " +
                "COALESCE(SUM(f.revenue), 0) AS revenue, " +
                "COALESCE(SUM(f.profit), 0) AS profit " +
                "FROM fact_logistics f " +
                "JOIN dim_product p ON f.product_id = p.product_id " +
                "GROUP BY p.product_id, p.product_name, p.category " +
                "ORDER BY revenue DESC LIMIT 10";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("Product Name", rs.getString("product_name"));
                row.put("Category", rs.getString("category"));
                row.put("Units Sold", rs.getInt("units_sold"));
                row.put("Revenue", String.format("$%,.0f", rs.getDouble("revenue")));
                row.put("Profit", String.format("$%,.0f", rs.getDouble("profit")));
                data.add(row);
            }

            Platform.runLater(() -> topProductsTable.setItems(data));
        } catch (Exception e) {
            System.err.println("Top Products Error: " + e.getMessage());
        }
    }

    private void loadOrdersData() {
        Platform.runLater(() -> {
            ordersTable.getColumns().clear();
            String[] cols = {"ID", "Customer", "Product", "Qty", "Revenue", "Status", "Date"};
            for (String col : cols) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(col);
                final String c = col;
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get(c))));
                ordersTable.getColumns().add(column);
            }
        });

        String sql = "SELECT f.fact_id, c.customer_name, p.product_name, " +
                "f.order_quantity, f.revenue, f.order_status, d.date " +
                "FROM fact_logistics f " +
                "JOIN dim_customer c ON f.customer_id = c.customer_id " +
                "JOIN dim_product p ON f.product_id = p.product_id " +
                "JOIN dim_date d ON f.date_id = d.date_id " +
                "ORDER BY f.fact_id DESC LIMIT 100";

        try (Connection conn = DBConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ID", rs.getInt("fact_id"));
                row.put("Customer", rs.getString("customer_name"));
                row.put("Product", rs.getString("product_name"));
                row.put("Qty", rs.getInt("order_quantity"));
                row.put("Revenue", String.format("$%,.0f", rs.getDouble("revenue")));
                row.put("Status", rs.getString("order_status"));
                row.put("Date", String.valueOf(rs.getDate("date")));
                data.add(row);
            }

            Platform.runLater(() -> ordersTable.setItems(data));
        } catch (Exception e) {
            System.err.println("Orders Error: " + e.getMessage());
        }
    }

    private void loadSampleData() {
        // Sample KPI Data
        Platform.runLater(() -> {
            totalOrdersValue.setText("1,250");
            totalRevenueValue.setText("$458,920");
            totalProfitValue.setText("$127,450");
            avgDeliveryValue.setText("24 hrs");
            onTimeRateValue.setText("85%");
            delayedRateValue.setText("15%");
        });

        // Sample Yearly Revenue Chart
        Platform.runLater(() -> {
            yearlyRevenueChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            series.getData().add(new XYChart.Data<>("2023", 350000));
            series.getData().add(new XYChart.Data<>("2024", 420000));
            series.getData().add(new XYChart.Data<>("2025", 458920));
            yearlyRevenueChart.getData().add(series);
        });

        // Sample Category Chart
        Platform.runLater(() -> {
            categoryRevenueChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue");
            series.getData().add(new XYChart.Data<>("Electronics", 180000));
            series.getData().add(new XYChart.Data<>("Clothing", 120000));
            series.getData().add(new XYChart.Data<>("Food", 90000));
            series.getData().add(new XYChart.Data<>("Furniture", 68920));
            categoryRevenueChart.getData().add(series);
        });

        // Sample Status Chart
        Platform.runLater(() -> {
            statusChart.getData().clear();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pieData.add(new PieChart.Data("Delivered", 1062));
            pieData.add(new PieChart.Data("Pending", 125));
            pieData.add(new PieChart.Data("Delayed", 63));
            statusChart.setData(pieData);
        });

        // Sample Delivery Chart
        Platform.runLater(() -> {
            deliveryChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Avg Hours");
            series.getData().add(new XYChart.Data<>("New York", 18));
            series.getData().add(new XYChart.Data<>("Los Angeles", 22));
            series.getData().add(new XYChart.Data<>("Chicago", 26));
            series.getData().add(new XYChart.Data<>("Houston", 30));
            deliveryChart.getData().add(series);
        });

        // Sample Yearly Summary Table
        Platform.runLater(() -> {
            yearlySummaryTable.getColumns().clear();
            String[] cols = {"Year", "Total Orders", "Total Revenue", "Total Profit", "Total Loss", "Avg Delivery (hrs)"};
            for (String col : cols) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(col);
                final String c = col;
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get(c))));
                yearlySummaryTable.getColumns().add(column);
            }

            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 3; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("Year", 2023 + i);
                row.put("Total Orders", 380 + (i * 70));
                row.put("Total Revenue", String.format("$%,d", 350000 + (i * 70000)));
                row.put("Total Profit", String.format("$%,d", 95000 + (i * 20000)));
                row.put("Total Loss", String.format("$%,d", 12000 - (i * 2000)));
                row.put("Avg Delivery (hrs)", 22 + i);
                data.add(row);
            }
            yearlySummaryTable.setItems(data);
        });

        // Sample Top Products Table
        Platform.runLater(() -> {
            topProductsTable.getColumns().clear();
            String[] cols = {"Product Name", "Category", "Units Sold", "Revenue", "Profit"};
            for (String col : cols) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(col);
                final String c = col;
                column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get(c))));
                topProductsTable.getColumns().add(column);
            }

            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
            String[][] products = {
                    {"iPhone 15", "Electronics", "250", "$85,000", "$25,500"},
                    {"Nike Shoes", "Clothing", "400", "$48,000", "$14,400"},
                    {"Samsung TV", "Electronics", "180", "$72,000", "$18,000"},
                    {"Office Chair", "Furniture", "300", "$36,000", "$10,800"},
                    {"Protein Powder", "Food", "500", "$25,000", "$7,500"}
            };
            for (String[] p : products) {
                Map<String, Object> row = new HashMap<>();
                row.put("Product Name", p[0]);
                row.put("Category", p[1]);
                row.put("Units Sold", p[2]);
                row.put("Revenue", p[3]);
                row.put("Profit", p[4]);
                data.add(row);
            }
            topProductsTable.setItems(data);
        });

        // Sample Orders
        if (currentPage.equals("Orders")) {
            Platform.runLater(() -> {
                ordersTable.getColumns().clear();
                String[] cols = {"ID", "Customer", "Product", "Qty", "Revenue", "Status", "Date"};
                for (String col : cols) {
                    TableColumn<Map<String, Object>, String> column = new TableColumn<>(col);
                    final String c = col;
                    column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(data.getValue().get(c))));
                    ordersTable.getColumns().add(column);
                }

                ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
                String[][] orders = {
                        {"1001", "John Doe", "iPhone 15", "2", "$1,700", "Delivered", "2025-01-15"},
                        {"1002", "Jane Smith", "Nike Shoes", "1", "$120", "Delivered", "2025-01-16"},
                        {"1003", "Bob Johnson", "Samsung TV", "1", "$400", "Pending", "2025-01-17"},
                        {"1004", "Alice Brown", "Office Chair", "3", "$360", "Delivered", "2025-01-18"},
                        {"1005", "Charlie Wilson", "Protein Powder", "5", "$250", "Delayed", "2025-01-19"}
                };
                for (String[] o : orders) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", o[0]); row.put("Customer", o[1]); row.put("Product", o[2]);
                    row.put("Qty", o[3]); row.put("Revenue", o[4]); row.put("Status", o[5]); row.put("Date", o[6]);
                    data.add(row);
                }
                ordersTable.setItems(data);
            });
        }
    }

    private void logout() {
        if (scheduler != null) scheduler.shutdown();
        stage.close();
        Platform.runLater(this::showLoginScreen);
    }

    private void animateOnStart() {
        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @Override
    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}