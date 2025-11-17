package application;

import java.sql.SQLException;

import application.ReviewerProfilePage.AccessContext;
import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * UI providing reviewer-specific actions such as viewing feedback and profiles.
 */
public class ReviewerHomePage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    
	/**
	 * Construct a ReviewerHomePage instance.
	 * @param databaseHelper database helper instance
	 * @param currentUser currently logged in user
	 */
	public ReviewerHomePage(DatabaseHelper databaseHelper, User currentUser) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
	}

	/**
	 * Show the reviewer home UI on the provided stage.
	 * @param primaryStage the Stage to display the UI on
	 */
	public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Reviewer Home Page");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Button to go to review answers page
        Button reviewAnswersButton = new Button("Review Answers");
        reviewAnswersButton.setOnAction(e -> {
            new ReviewerReviewPage(databaseHelper, currentUser).show(primaryStage);
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new WelcomeLoginPage(databaseHelper).show(primaryStage,  currentUser);
        });
        
        Button manageReviewsButton = new Button("Manage My Reviews"); //added by JA
        manageReviewsButton.setOnAction(e -> {
            new ReviewManagementPage(databaseHelper, currentUser).show(primaryStage);
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
	    
	    Button viewProfileButton = new Button("View My Profile");
		// Button to view private feedback sent to this reviewer (messages stored with qid REVFB:<reviewerId>)
		Button viewFeedbackButton = new Button("View Private Feedback");
		viewFeedbackButton.setOnAction(e -> {
			try {
				String qid = "REVFB:" + currentUser.getUserId();
				java.util.List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(qid, currentUser.getUserName());
				if (msgs == null || msgs.isEmpty()) {
					Alert a = new Alert(Alert.AlertType.INFORMATION, "No private feedback found.");
					a.showAndWait();
					return;
				}
				StringBuilder sb = new StringBuilder();
				for (PrivateMessage m : msgs) {
					sb.append(String.format("%s -> %s: %s\n", m.getFromUser(), m.getToUser(), m.getContent()));
				}
				Alert a = new Alert(Alert.AlertType.INFORMATION);
				a.setHeaderText("Private feedback to you");
				a.setContentText(sb.toString());
				a.getDialogPane().setPrefWidth(600);
				a.showAndWait();
			} catch (SQLException ex) {
				ex.printStackTrace();
				Alert a = new Alert(Alert.AlertType.ERROR, "Failed to load feedback: " + ex.getMessage());
				a.showAndWait();
			}
		});
		
	    viewProfileButton.setOnAction(e -> {
	    	try {
	    		int reviewerId = currentUser.getUserId();
	    		System.out.println("Current User ID: " + reviewerId);
	    		ReviewerProfile profile = databaseHelper.getReviewerProfileById(reviewerId);
	    	
	    	
	    		if(profile != null) {
	    			ReviewerProfilePage profilePage = new ReviewerProfilePage(databaseHelper, currentUser,profile ,AccessContext.REVIEWER_HOME);
	    			profilePage.show(primaryStage);
	    		}else {
	    			Alert alert = new Alert(Alert.AlertType.WARNING,"Reviewer profile not found.");
	    			alert.showAndWait();
	    		}
	    	
	    	}catch (SQLException ex){
	    		ex.printStackTrace();
	    		Alert alert = new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage());
	    		alert.showAndWait();
	    	}
	   });

        layout.getChildren().addAll(titleLabel, reviewAnswersButton, viewProfileButton, backButton, manageReviewsButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Home Page");
    }
}