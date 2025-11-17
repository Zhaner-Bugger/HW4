// InstructorHomePage.java
package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * UI providing instructor-specific actions such as managing questions and answers.
 */
public class InstructorHomePage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    public InstructorHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Instructor Home Page");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Add similar buttons to InstructorHomePage
        Button manageQuestionsButton = new Button("Manage Questions");
        Button manageAnswersButton = new Button("Manage Answers");
        Button backButton = new Button("Back");

        manageQuestionsButton.setOnAction(e -> {
            new QuestionManagementPage(databaseHelper, currentUser).show(primaryStage);
        });

        manageAnswersButton.setOnAction(e -> {
            new AnswerManagementPage(databaseHelper, currentUser).show(primaryStage);
        });
        
        backButton.setOnAction(e -> {
            NavigationHelper.goToHomePage(currentUser.getActiveRole(), primaryStage, databaseHelper, currentUser);
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
	    
        Button approveRequestsButton = new Button("Approve Reviewer Requests");

        // Adds a button to show pending requests 
        approveRequestsButton.setOnAction(e -> {
            new ReviewerApprovalPage(databaseHelper, currentUser).show(primaryStage);
        });

        // Add to layout
        layout.getChildren().addAll(titleLabel, manageQuestionsButton, manageAnswersButton, approveRequestsButton, backButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Instructor Home Page");
    }
}