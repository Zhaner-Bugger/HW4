package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * <p>
 * The StaffHomePage class serves as the main navigation hub for staff members.
 * It provides access to all staff-specific functionality including content monitoring,
 * flag management, and system reporting.
 * </p>
 * 
 * <p>
 * This page is the central point for implementing all three staff user stories:
 * <ul>
 *   <li>User Story 1: Viewing all system content</li>
 *   <li>User Story 2: Flagging inappropriate content</li>
 *   <li>User Story 3: Generating reports and analyzing trends</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Staff members can navigate to:
 * <ul>
 *   <li>Content View - See all questions, answers, reviews, and messages</li>
 *   <li>Flagged Content - Review and manage flagged items</li>
 *   <li>Reports & Trends - View analytics and system health metrics</li>
 * </ul>
 * </p>
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-16
 */
public class StaffHomePage {
	/** Database helper for system operations */
    private final DatabaseHelper databaseHelper;
    
    /** Currently logged-in staff user */
    private final User currentUser;
    
    /**
     * Constructs a new StaffHomePage.
     * 
     * @param databaseHelper the database helper for system operations
     * @param currentUser the currently logged-in staff user
     */
    public StaffHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the staff home page with navigation buttons for all staff functions.
     * Provides access to content viewing, flag management, reporting, and logout.
     * 
     * @param primaryStage the stage to display the home page
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Staff Home Page");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Button to view all content
        Button viewContentButton = new Button("View All Content");
        viewContentButton.setOnAction(e -> {
            new StaffContentViewPage(databaseHelper, currentUser).show(primaryStage);
        });
        
        // Button to view flagged content
        Button viewFlagsButton = new Button("View Flagged Content");
        viewFlagsButton.setOnAction(e -> {
            new StaffFlaggedContentPage(databaseHelper, currentUser).show(primaryStage);
        });
        
        // Button to view reports/statistics
        Button viewReportsButton = new Button("View Reports & Trends");
        viewReportsButton.setOnAction(e -> {
            new StaffReportPage(databaseHelper, currentUser).show(primaryStage);
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
        });
        
        // Logout Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper();
                dbHelper.connectToDatabase();
                SetupLoginSelectionPage setupPage = new SetupLoginSelectionPage(dbHelper);
                setupPage.show(primaryStage);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(titleLabel, viewContentButton, viewFlagsButton, 
                                    viewReportsButton, backButton, logoutButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Home Page");
    }
}