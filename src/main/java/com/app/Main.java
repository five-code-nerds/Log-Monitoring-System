package com.app;

import com.app.repository.AlertRepository;
import com.app.repository.ClientRepository;
import com.app.repository.LogRepository;
import com.app.server.LogServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private LogServer logServer;

    private final LogRepository logRepository = new LogRepository();
    private final AlertRepository alertRepository = new AlertRepository();
    private final ClientRepository clientRepository = new ClientRepository();

    private TableView<String[]> logsTable;
    private TableView<String[]> alertsTable;
    private TableView<String[]> clientsTable;

    private Label totalLogsLabel;
    private Label alertsLabel;
    private Label clientsLabel;
    private Label statusLabel;

    private BarChart<String, Number> chart;

    private ScheduledExecutorService scheduler;


    @Override
    public void init() {

        logServer = new LogServer();

        Thread serverThread = new Thread(logServer::start);

        serverThread.setDaemon(true);

        serverThread.start();

        System.out.println("[App] Log server started.");
    }


    @Override
    public void start(Stage stage) {

        totalLogsLabel = new Label("Logs: 0");
        alertsLabel = new Label("Alerts: 0");
        clientsLabel = new Label("Clients: 0");

        HBox statsBar = new HBox(20,
                totalLogsLabel,
                alertsLabel,
                clientsLabel
        );
        logsTable = new TableView<>();

        TableColumn<String[], String> logClient =
                new TableColumn<>("Client");

        logClient.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1])
        );

        TableColumn<String[], String> logLevel =
                new TableColumn<>("Level");

        logLevel.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2])
        );

        TableColumn<String[], String> logMessage =
                new TableColumn<>("Message");

        logMessage.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3])
        );

        TableColumn<String[], String> logTime =
                new TableColumn<>("Time");

        logTime.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[5])
        );

        logsTable.getColumns().addAll(
                logClient,
                logLevel,
                logMessage,
                logTime
        );

        alertsTable = new TableView<>();

        TableColumn<String[], String> alertClient =
                new TableColumn<>("Client");

        alertClient.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1])
        );

        TableColumn<String[], String> alertLevel =
                new TableColumn<>("Alert");

        alertLevel.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2])
        );

        TableColumn<String[], String> alertMessage =
                new TableColumn<>("Message");

        alertMessage.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3])
        );

        alertsTable.getColumns().addAll(
                alertClient,
                alertLevel,
                alertMessage
        );
        clientsTable = new TableView<>();

        TableColumn<String[], String> clientId =
                new TableColumn<>("Client ID");

        clientId.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0])
        );

        TableColumn<String[], String> clientStatus =
                new TableColumn<>("Status");

        clientStatus.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3])
        );

        clientsTable.getColumns().addAll(
                clientId,
                clientStatus
        );

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Log Levels");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadAllData());
        Button exportBtn = new Button("Export CSV");

        exportBtn.setOnAction(e -> {
            logRepository.exportLogsToCSV("logs/export.csv");
            statusLabel.setText("Logs exported.");
        });

        HBox buttonBar = new HBox(10, refreshBtn, exportBtn);

        statusLabel = new Label("System running...");

        TabPane tabPane = new TabPane();

        Tab logsTab = new Tab("Logs", logsTable);
        logsTab.setClosable(false);

        Tab alertsTab = new Tab("Alerts", alertsTable);
        alertsTab.setClosable(false);

        Tab clientsTab = new Tab("Clients", clientsTable);
        clientsTab.setClosable(false);

        Tab chartTab = new Tab("Chart", chart);
        chartTab.setClosable(false);

        tabPane.getTabs().addAll(
                logsTab,
                alertsTab,
                clientsTab,
                chartTab
        );

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                statsBar,
                buttonBar,
                tabPane,
                statusLabel
        );
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Log Monitoring System");
        stage.setScene(scene);

        stage.show();
        loadAllData();
        startAutoRefresh();
        stage.setOnCloseRequest(e -> shutdown());

        System.out.println("[App] GUI started.");
    }


    private void loadAllData() {
        loadLogs();
        loadAlerts();
        loadClients();
        updateChart();

        statusLabel.setText(
                "Last refresh: " +
                        java.time.LocalTime.now().withNano(0)
        );
    }

    private void loadLogs() {

        List<String[]> logs =
                logRepository.getRecentLogs(100);

        logsTable.setItems(
                FXCollections.observableArrayList(logs)
        );

        totalLogsLabel.setText("Logs: " + logs.size());
    }

    private void loadAlerts() {

        List<String[]> alerts =
                alertRepository.getActiveAlerts();

        alertsTable.setItems(
                FXCollections.observableArrayList(alerts)
        );

        alertsLabel.setText("Alerts: " + alerts.size());
    }

    private void loadClients() {

        List<String[]> clients =
                clientRepository.getAllClients();

        clientsTable.setItems(
                FXCollections.observableArrayList(clients)
        );

        long online = clients.stream()
                .filter(c -> "ONLINE".equals(c[3]))
                .count();

        clientsLabel.setText("Clients: " + online);
    }


    private void updateChart() {

        chart.getData().clear();
        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        String[] levels = {
                "INFO",
                "WARN",
                "ERROR",
                "CRITICAL"
        };

        for (String level : levels) {

            int count =
                    logRepository.searchLogs(null, level, null).size();

            series.getData().add(
                    new XYChart.Data<>(level, count)
            );
        }

        chart.getData().add(series);
    }
    private void startAutoRefresh() {

        scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {

            Platform.runLater(this::loadAllData);

        }, 5, 5, TimeUnit.SECONDS);
    }

    private void shutdown() {

        System.out.println("[App] Shutting down...");

        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (logServer != null) {
            logServer.stop();
        }

        Platform.exit();
        System.exit(0);
    }
    public static void main(String[] args) {

        launch(args);
    }
}