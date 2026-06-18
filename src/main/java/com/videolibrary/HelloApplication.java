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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Client-side JavaFX application mapping interface components dynamically
 * based on connections back to the central RMI Server registry.
 */
public class HelloApplication extends Application {

    private VlsService vlsService;
    private String clientType = "Customer"; // Default role fallback
    private final String SERVER_IP = "localhost"; // Set to your server machine's real IP address during testing

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

        // 2. Role Selector Interface Window
        VBox roleSelectionRoot = new VBox(15);
        roleSelectionRoot.setPadding(new Insets(30));
        roleSelectionRoot.setAlignment(Pos.CENTER);
        roleSelectionRoot.setStyle("-fx-background-color: #f4f4f4;");

        Label label = new Label("Choose Your Application Interface Instance:");
        label.setFont(Font.font("serif", FontWeight.BOLD, 16));

        Button adminBtn = new Button("Launch Client 1: Admin Interface");
        Button customerBtn = new Button("Launch Client 2: Customer Interface");

        adminBtn.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size: 12pt;");
        customerBtn.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size: 12pt;");

        roleSelectionRoot.getChildren().addAll(label, adminBtn, customerBtn);
        Scene selectionScene = new Scene(roleSelectionRoot, 400, 200);
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

    // Replace the renderMainInterface method inside HelloApplication.java with this:
    private void renderMainInterface(Stage stage) {
        TabPane tabPane = new TabPane();

        if (clientType.equals("Admin")) {
            tabPane.getTabs().add(createGenreTab());
            tabPane.getTabs().add(createMovieTab());
            tabPane.getTabs().add(createCustomerTab());
            stage.setTitle("Video Library System - Admin Management Panel");
        } else {
            tabPane.getTabs().add(createRentalsTab());
            stage.setTitle("Video Library System - Customer Rental Window");
        }

        Scene mainScene = new Scene(tabPane, 650, 480);

        // LINK THE CSS STYLESHEET HERE Safely
        try {
            String cssPath = getClass().getResource("/com/videolibrary/styles.css").toExternalForm();
            mainScene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("Warning: styles.css stylesheet file not found in resource path.");
        }

        stage.setScene(mainScene);
    }
    // ==========================================
    // INTERFACE 1: GENRES VIEW (Page 2-3)
    // ==========================================
    private Tab createGenreTab() {
        Tab tab = new Tab("1. Genres");
        GridPane grid = createBaseGrid();

        Text text1 = new Text("Name:");
        TextField textField1 = new TextField();

        Button button1 = new Button("Save");

        Text text2 = new Text("Registered:");
        ComboBox<String> comboBox = new ComboBox<>();

        Button button2 = new Button("Remove");

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

    // ==========================================
    // INTERFACE 2: MOVIES VIEW (Page 5-6)
    // ==========================================
    private Tab createMovieTab() {
        Tab tab = new Tab("2. Movies");
        GridPane grid = createBaseGrid();

        Text lblGenre = new Text("Genre:");
        ComboBox<String> comboGenre = new ComboBox<>();

        Text lblName = new Text("Name:");
        TextField txtMovieName = new TextField();

        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size:13pt;");

        Text lblRegistered = new Text("Registered:");
        lblRegistered.setStyle("-fx-font: normal bold 20px 'serif' ");
        ComboBox<Movie> comboRegisteredMovies = new ComboBox<>();

        Button btnRemove = new Button("Remove");
        btnRemove.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size:13pt;");

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

    // ==========================================
    // INTERFACE 3: CUSTOMERS REGISTRATION (Page 7)
    // ==========================================
    private Tab createCustomerTab() {
        Tab tab = new Tab("3. Customers");
        GridPane grid = createBaseGrid();

        Text lblName = new Text("Name:");
        lblName.setStyle("-fx-font: normal bold 20px 'serif' ");
        TextField txtName = new TextField();

        Text lblPhone = new Text("Phone:");
        lblPhone.setStyle("-fx-font: normal bold 20px 'serif' ");
        TextField txtPhone = new TextField();

        Text lblEmail = new Text("Email:");
        lblEmail.setStyle("-fx-font: normal bold 20px 'serif' ");
        TextField txtEmail = new TextField();

        Button btnSave = new Button("Save");
        btnSave.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size:13pt;");

        Text lblRegistered = new Text("Registered:");
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

    // ==========================================
    // INTERFACE 4: RENTALS VIEW (Page 8)
    // ==========================================
    private Tab createRentalsTab() {
        Tab tab = new Tab("4. Rentals");
        GridPane grid = createBaseGrid();

        Text lblCust = new Text("Customer:");
        ComboBox<String> comboCust = new ComboBox<>();

        Text lblGen = new Text("Genre:");
        ComboBox<String> comboGen = new ComboBox<>();

        Text lblMovies = new Text("Movies:");
        ComboBox<Movie> comboMovies = new ComboBox<>();

        Button btnSaveRental = new Button("Save Rental");

        Text lblBorrowed = new Text("Borrowed:");
        ComboBox<Movie> comboBorrowed = new ComboBox<>();

        Button btnReturn = new Button("Return Movie");

        Text lblReturned = new Text("Returned:");
        ComboBox<Movie> comboReturned = new ComboBox<>();

        grid.add(lblCust, 0, 0); grid.add(comboCust, 1, 0);
        grid.add(lblGen, 0, 1); grid.add(comboGen, 1, 1);
        grid.add(lblMovies, 0, 2); grid.add(comboMovies, 1, 2);
        grid.add(btnSaveRental, 1, 3);
        grid.add(lblBorrowed, 0, 4); grid.add(comboBorrowed, 1, 4);
        grid.add(btnReturn, 1, 5);
        grid.add(lblReturned, 0, 6); grid.add(comboReturned, 1, 6);

        // Fetching dropdown values dynamically from the Server Registry
        comboCust.setOnShowing(e -> {
            try { comboCust.getItems().setAll(vlsService.getCustomers()); } catch (Exception ex) { ex.printStackTrace(); }
        });
        comboGen.setOnShowing(e -> {
            try { comboGen.getItems().setAll(vlsService.getGenres()); } catch (Exception ex) { ex.printStackTrace(); }
        });
        comboGen.setOnAction(e -> {
            try { if(comboGen.getValue() != null) comboMovies.getItems().setAll(vlsService.getMoviesByGenre(comboGen.getValue())); } catch (Exception ex) { ex.printStackTrace(); }
        });

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

        btnSaveRental.setOnAction(e -> {
            try {
                String c = comboCust.getValue();
                Movie m = comboMovies.getValue();
                if (c != null && m != null) {
                    vlsService.rentMovie(c, m.getId());
                    refreshRentalLists.run();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnReturn.setOnAction(e -> {
            try {
                String c = comboCust.getValue();
                Movie m = comboBorrowed.getValue();
                if (c != null && m != null) {
                    vlsService.returnMovie(c, m.getId());
                    refreshRentalLists.run();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        tab.setContent(grid);
        tab.setClosable(false);
        return tab;
    }

    // Helper method to keep UI base parameters centralized matching step-guidelines
    private GridPane createBaseGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(600, 400);
        gridPane.setPadding(new Insets(15, 15, 15, 15));
        gridPane.setVgap(12);
        gridPane.setHgap(12);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setStyle("-fx-background-color: BEIGE;");
        return gridPane;
    }

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