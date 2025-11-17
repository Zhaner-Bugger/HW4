package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.SQLException;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	private final DatabaseHelper databaseHelper;
	private final User currentUser;
    
	/**
	 * Construct a UserHomePage for the given user.
	 * @param databaseHelper database access helper
	 * @param currentUser the logged-in user
	 */
	public UserHomePage(DatabaseHelper databaseHelper, User currentUser) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
	}

	/**
	 * Show the user's home page in the provided JavaFX Stage.
	 * @param primaryStage the stage to display the UI on
	 */
	public void show(Stage primaryStage) {
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    Button backButton = new Button("Back");
	    backButton.setOnAction(e -> {
	    	new WelcomeLoginPage(databaseHelper).show(primaryStage,  currentUser);
	    });

		 // Logout Button
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(e -> {
	    	
	    	try {
		    	// To clear session if needed
		    	DatabaseHelper dbHelper = new DatabaseHelper();
	    		// Reconnect for login screen
	    		dbHelper.connectToDatabase();
	    		
	    		// Return to selection page
	    		SetupLoginSelectionPage setupPage = new SetupLoginSelectionPage(dbHelper);
	    		setupPage.show(primaryStage);
	    		
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    	
	    });
	    
	    //Request button
	    Button requestReviewerButton = new Button("Request Reviewer Role");
	    requestReviewerButton.setOnAction(e -> {
	        try {
	            databaseHelper.submitReviewerRequest(currentUser.getUserName());
	            Alert alert = new Alert(Alert.AlertType.INFORMATION);
	            alert.setTitle("Request Sent");
	            alert.setHeaderText(null);
	            alert.setContentText("Your request to become a reviewer has been submitted!");
	            alert.showAndWait();
	        } catch (SQLException ex) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Error");
	            alert.setHeaderText("Request Failed");
	            alert.setContentText(ex.getMessage());
	            alert.showAndWait();
	        }
	    });

	    // Only show if user is a student
	    if (currentUser.getRole().equalsIgnoreCase("Student")) {
	        layout.getChildren().add(requestReviewerButton);
	    }
	    
	    layout.getChildren().addAll(userLabel, backButton, requestReviewerButton, logoutButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
    	
    }
}