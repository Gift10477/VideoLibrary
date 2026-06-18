package com.videolibrary;

import com.videolibrary.models.Movie;
import com.videolibrary.rmi.VlsService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client-side JavaFX application for the Video Library System.
 * This class handles the graphical user interface and maps components dynamically
 * based on connections back to the central RMI Server registry.
 */
public class HelloApplication extends Application {

    /** The remote service interface used to communicate with the central database. */
    private VlsService vlsService;

    /** The operational role chosen by the user (defaults to Customer). */
    private String clientType = "Customer";

    /** The IP address of the machine hosting the RMI Registry and database. */
    private final String SERVER_IP = "10.255.51.142";

    /**
     * Default constructor for the HelloApplication class.
     */
    public HelloApplication() {
        // Explicit default constructor to satisfy Javadoc requirements.
    }

    /**
     * The main entry point for the JavaFX application.
     * Establishes the RMI connection and renders the initial role selection screen.
     *
     * @param stage The primary window for this application.
     */
    @Override
    public void start(Stage stage) {
        // 1. Establish connection to RMI Server
        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_IP, 1099);
            vlsService = (VlsService) registry.lookup("VlsService");
        } catch (Exception e) {
            showErrorWindow("Connection Failure", "Could not connect to VLS Server at " + SERVER_IP);
            return;
        }

        // 2. Role Selector Interface Window (Powered by CSS)
        VBox roleSelectionRoot = new VBox(20);
        roleSelectionRoot.getStyleClass().add("welcome-root");

        Label titleLabel = new Label("Video Library System");
        titleLabel.getStyleClass().add("title-text");

        Label subLabel = new Label("Welcome. Please choose your interface:");
        subLabel.getStyleClass().add("subtitle-text");

        Button adminBtn = new Button("Launch Admin Panel");
        adminBtn.getStyleClass().add("action-btn");

        Button customerBtn = new Button("Launch Customer Portal");
        customerBtn.getStyleClass().add("action-btn");

        roleSelectionRoot.getChildren().addAll(titleLabel, subLabel, adminBtn, customerBtn);

        Scene selectionScene = new Scene(roleSelectionRoot, 450, 300);

        // Safely link the stylesheet to the Welcome Screen
        try {
            String cssPath = getClass().getResource("/com/videolibrary/styles.css").toExternalForm();
            selectionScene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("Warning: styles.css stylesheet file not found in resource path.");
        }

        stage.setTitle("VLS Initialization");
        stage.setScene(selectionScene);
        stage.show();

        // 3. Render Views dynamically based on selection
        adminBtn.setOnAction(e -> {
            clientType = "Admin";
            renderMainInterface(stage);
        });

        customerBtn.setOnAction(e -> {
            clientType = "Customer";
            renderMainInterface(stage);
        });
    }

    /**
     * Transitions the application from the launch screen to the primary dashboard
     * based on the user's selected role (Admin or Customer).
     *
     * @param stage The primary window for this application.
     */
    private void renderMainInterface(Stage stage) {
        TabPane tabPane = new TabPane();

        if (clientType.equals("Admin")) {
            tabPane.getTabs().add(createGenreTab());
            tabPane.getTabs().add(createMovieTab());
            tabPane.getTabs().add(createCustomerTab());
            stage.setTitle("Video Library System - Admin Management Panel");
        } else {
            // Replaced the single Rentals tab with two separate tabs
            tabPane.getTabs().add(createRentMovieTab());
            tabPane.getTabs().add(createReturnMovieTab());
            stage.setTitle("Video Library System - Customer Portal");
        }

        Scene mainScene = new Scene(tabPane, 650, 480);

        try {
            String cssPath = getClass().getResource("/com/videolibrary/styles.css").toExternalForm();
            mainScene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("Warning: styles.css stylesheet file not found in resource path.");
        }

        stage.setScene(mainScene);
    }

    /**
     * Builds the Genre Management tab for the Admin panel.
     *
     * @return A fully configured Tab containing the genre registration and removal interface.
     */
    private Tab createGenreTab() {
        Tab tab = new Tab("1. Genres");
        GridPane grid = createBaseGrid();

        Label text1 = new Label("Name:");
        TextField textField1 = new TextField();

        Button button1 = new Button("Save");
        button1.getStyleClass().add("action-btn");

        Label text2 = new Label("Registered:");
        ComboBox<String> comboBox = new ComboBox<>();

        Button button2 = new Button("Remove");
        button2.getStyleClass().add("action-btn");

        grid.add(text1, 0, 0); grid.add(textField1, 1, 0);
        grid.add(button1, 1, 1);
        grid.add(text2, 0, 2); grid.add(comboBox, 1, 2);
        grid.add(button2, 1, 3);

        Runnable refreshGenres = () -> {
            try {
                comboBox.getItems().setAll(vlsService.getGenres());
            } catch (Exception ex) { ex.printStackTrace(); }
        };
        refreshGenres.run();

        button1.setOnAction(e -> {
            try {
                String genreInput = textField1.getText().trim();
                if (!genreInput.isEmpty()) {
                    boolean success = vlsService.registerGenre(genreInput); // Saves to MySQL
                    if (success) {
                        textField1.clear();
                        // Instantly re-fetch from DB to populate the "Registered" drop-down
                        comboBox.getItems().setAll(vlsService.getGenres());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        button2.setOnAction(e -> {
            try {
                String selectedGenre = comboBox.getValue();
                if (selectedGenre != null) {
                    boolean success = vlsService.removeGenre(selectedGenre);
                    if (success) {
                        // Instantly update the drop-down menu listing
                        comboBox.getItems().setAll(vlsService.getGenres());
                        System.out.println("Genre removed successfully.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tab.setContent(grid);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Builds the Movie Management tab for the Admin panel.
     *
     * @return A fully configured Tab containing the movie registration and removal interface.
     */
    private Tab createMovieTab() {
        Tab tab = new Tab("2. Movies");
        GridPane grid = createBaseGrid();

        Label lblGenre = new Label("Genre:");
        ComboBox<String> comboGenre = new ComboBox<>();

        Label lblName = new Label("Name:");
        TextField txtMovieName = new TextField();

        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("action-btn");

        Label lblRegistered = new Label("Registered:");
        lblRegistered.setStyle("-fx-font: normal bold 20px 'serif' ");
        ComboBox<Movie> comboRegisteredMovies = new ComboBox<>();

        Button btnRemove = new Button("Remove");
        btnRemove.getStyleClass().add("action-btn");

        grid.add(lblGenre, 0, 0); grid.add(comboGenre, 1, 0);
        grid.add(lblName, 0, 1); grid.add(txtMovieName, 1, 1);
        grid.add(btnSave, 1, 2);
        grid.add(lblRegistered, 0, 3); grid.add(comboRegisteredMovies, 1, 3);
        grid.add(btnRemove, 1, 4);

        comboGenre.setOnShowing(e -> {
            try { comboGenre.getItems().setAll(vlsService.getGenres()); } catch (Exception ex) { ex.printStackTrace(); }
        });

        comboGenre.setOnAction(e -> {
            try {
                String selected = comboGenre.getValue();
                if (selected != null) {
                    comboRegisteredMovies.getItems().setAll(vlsService.getMoviesByGenre(selected));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnSave.setOnAction(e -> {
            try {
                String gen = comboGenre.getValue();
                String title = txtMovieName.getText().trim();
                if (gen != null && !title.isEmpty()) {
                    vlsService.registerMovie(title, gen);
                    txtMovieName.clear();
                    comboRegisteredMovies.getItems().setAll(vlsService.getMoviesByGenre(gen));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnRemove.setOnAction(e -> {
            try {
                Movie selectedMovie = comboRegisteredMovies.getValue();
                String currentGenre = comboGenre.getValue();
                if (selectedMovie != null && currentGenre != null) {
                    boolean success = vlsService.removeMovie(selectedMovie.getId());
                    if (success) {
                        // Instantly update the movie drop-down for the current active genre selection
                        comboRegisteredMovies.getItems().setAll(vlsService.getMoviesByGenre(currentGenre));
                        System.out.println("Movie removed successfully.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tab.setContent(grid);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Builds the Customer Registration tab for the Admin panel.
     *
     * @return A fully configured Tab containing the customer sign-up interface.
     */
    private Tab createCustomerTab() {
        Tab tab = new Tab("3. Customers");
        GridPane grid = createBaseGrid();

        Label lblName = new Label("Name:");
        lblName.setStyle("-fx-font: normal bold 20px 'serif' ");
        TextField txtName = new TextField();

        Label lblPhone = new Label("Phone:");
        lblPhone.setStyle("-fx-font: normal bold 20px 'serif' ");
        TextField txtPhone = new TextField();

        Label lblEmail = new Label("Email:");
        lblEmail.setStyle("-fx-font: normal bold 20px 'serif' ");
        TextField txtEmail = new TextField();

        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("action-btn");

        Label lblRegistered = new Label("Registered:");
        lblRegistered.setStyle("-fx-font: normal bold 20px 'serif' ");
        ComboBox<String> comboRegistered = new ComboBox<>();

        grid.add(lblName, 0, 0); grid.add(txtName, 1, 0);
        grid.add(lblPhone, 0, 1); grid.add(txtPhone, 1, 1);
        grid.add(lblEmail, 0, 2); grid.add(txtEmail, 1, 2);
        grid.add(btnSave, 1, 3);
        grid.add(lblRegistered, 0, 4); grid.add(comboRegistered, 1, 4);

        Runnable refreshCustomers = () -> {
            try { comboRegistered.getItems().setAll(vlsService.getCustomers()); } catch (Exception ex) { ex.printStackTrace(); }
        };
        refreshCustomers.run();

        btnSave.setOnAction(e -> {
            try {
                String name = txtName.getText().trim();
                if (!name.isEmpty()) {
                    vlsService.registerCustomer(name, txtPhone.getText(), txtEmail.getText());
                    txtName.clear(); txtPhone.clear(); txtEmail.clear();
                    refreshCustomers.run();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        tab.setContent(grid);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Builds the Rental tab for the Customer portal.
     *
     * @return A fully configured Tab allowing customers to select and rent movies.
     */
    private Tab createRentMovieTab() {
        Tab tab = new Tab("Rent Movie");
        GridPane grid = createBaseGrid();

        Label lblCust = new Label("Select Customer:");
        ComboBox<String> comboCust = new ComboBox<>();

        Label lblGen = new Label("Select Genre:");
        ComboBox<String> comboGen = new ComboBox<>();

        Label lblMovies = new Label("Available Movies:");
        ComboBox<Movie> comboMovies = new ComboBox<>();

        Button btnSaveRental = new Button("Rent Movie");
        btnSaveRental.getStyleClass().add("action-btn");

        grid.add(lblCust, 0, 0); grid.add(comboCust, 1, 0);
        grid.add(lblGen, 0, 1); grid.add(comboGen, 1, 1);
        grid.add(lblMovies, 0, 2); grid.add(comboMovies, 1, 2);
        grid.add(btnSaveRental, 1, 3);

        // Fetch dropdown values dynamically
        comboCust.setOnShowing(e -> {
            try { comboCust.getItems().setAll(vlsService.getCustomers()); } catch (Exception ex) { ex.printStackTrace(); }
        });
        comboGen.setOnShowing(e -> {
            try { comboGen.getItems().setAll(vlsService.getGenres()); } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Load movies only after a genre is clicked
        comboGen.setOnAction(e -> {
            try {
                if(comboGen.getValue() != null) {
                    comboMovies.getItems().setAll(vlsService.getMoviesByGenre(comboGen.getValue()));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Rent Action
        btnSaveRental.setOnAction(e -> {
            try {
                String c = comboCust.getValue();
                Movie m = comboMovies.getValue();
                if (c != null && m != null) {
                    vlsService.rentMovie(c, m.getId());

                    // Clear the movie selection after a successful rental
                    comboMovies.getSelectionModel().clearSelection();

                    // Show a success popup to the user!
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Rental Successful");
                    alert.setHeaderText(null);
                    alert.setContentText(m.getTitle() + " has been successfully rented to " + c + ".");
                    alert.showAndWait();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        tab.setContent(grid);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Builds the Return tab for the Customer portal.
     *
     * @return A fully configured Tab allowing customers to return rented movies and view history.
     */
    private Tab createReturnMovieTab() {
        Tab tab = new Tab("Return Movie");
        GridPane grid = createBaseGrid();

        Label lblCust = new Label("Select Customer:");
        ComboBox<String> comboCust = new ComboBox<>();

        Label lblBorrowed = new Label("Currently Borrowed:");
        ComboBox<Movie> comboBorrowed = new ComboBox<>();

        Button btnReturn = new Button("Return Movie");
        btnReturn.getStyleClass().add("action-btn");

        Label lblReturned = new Label("Return History:");
        ComboBox<Movie> comboReturned = new ComboBox<>();

        grid.add(lblCust, 0, 0); grid.add(comboCust, 1, 0);
        grid.add(lblBorrowed, 0, 1); grid.add(comboBorrowed, 1, 1);
        grid.add(btnReturn, 1, 2);
        grid.add(lblReturned, 0, 3); grid.add(comboReturned, 1, 3);

        // Load Customers
        comboCust.setOnShowing(e -> {
            try { comboCust.getItems().setAll(vlsService.getCustomers()); } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Auto-refresh lists when a customer is selected
        Runnable refreshRentalLists = () -> {
            try {
                String c = comboCust.getValue();
                if (c != null) {
                    comboBorrowed.getItems().setAll(vlsService.getBorrowedMovies(c));
                    comboReturned.getItems().setAll(vlsService.getReturnedMovies(c));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        };

        comboCust.setOnAction(e -> refreshRentalLists.run());

        // Return Action
        btnReturn.setOnAction(e -> {
            try {
                String c = comboCust.getValue();
                Movie m = comboBorrowed.getValue();
                if (c != null && m != null) {
                    vlsService.returnMovie(c, m.getId());
                    refreshRentalLists.run(); // instantly refresh the borrowed/returned lists
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Force the lists to refresh instantly whenever this tab is clicked!
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected() && comboCust.getValue() != null) {
                refreshRentalLists.run();
            }
        });

        tab.setContent(grid);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Helper method to keep UI base parameters centralized matching step-guidelines.
     *
     * @return A configured GridPane to be used as the base layout for UI tabs.
     */
    private GridPane createBaseGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(600, 400);
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(15);
        gridPane.setHgap(15);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getStyleClass().add("base-grid");

        return gridPane;
    }

    /**
     * Displays a standalone error window to the user.
     *
     * @param title   The title of the error window.
     * @param message The specific error message to display.
     */
    private void showErrorWindow(String title, String message) {
        Stage errorStage = new Stage();
        VBox root = new VBox(10, new Label(message));
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        errorStage.setScene(new Scene(root, 350, 100));
        errorStage.setTitle(title);
        errorStage.show();
    }
}