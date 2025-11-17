package application;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StudentHomePage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    public StudentHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Student Home Page");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Button manageQuestionsButton = new Button("Manage Questions");
        Button manageAnswersButton = new Button("Manage Answers");
        Button viewReviewersButton = new Button("View Reviewer Profiles");
        Button backButton = new Button("Back");
        
        backButton.setOnAction(e -> {
            if (currentUser.getRoles().size() > 1) {
                new RoleSelectionPage(currentUser.getRoles(), databaseHelper, currentUser).show(primaryStage);
            } else {
                new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser);
            }
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

        manageQuestionsButton.setOnAction(e -> {
            new QuestionManagementPage(databaseHelper, currentUser).show(primaryStage);
        });

        manageAnswersButton.setOnAction(e -> {
            new AnswerManagementPage(databaseHelper, currentUser).show(primaryStage);
        });
        viewReviewersButton.setOnAction(e ->{
        	new ReviewerProfilesPage(databaseHelper, currentUser).show(primaryStage);
        });

        // Add to layout
        layout.getChildren().addAll(titleLabel, manageQuestionsButton, manageAnswersButton,viewReviewersButton, backButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Home Page");
    }
}