package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    

	/**
	 * Construct a WelcomeLoginPage.
	 * @param databaseHelper Database helper instance used for navigation
	 */
	public WelcomeLoginPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	/**
	 * Show the welcome page and navigate based on the provided user's roles.
	 * @param primaryStage the stage to show the page on
	 * @param user the authenticated user
	 */
	public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    continueButton.setOnAction(a -> {
	    	//create an array for the users roles
	    	List<String>roles = user.getRoles();
	    	//print roles
	    	for(int i=0; i< roles.size(); i++) {
	    		String role = roles.get(i);
	    		System.out.println(role);
	    	}
	    	//if user has one role, go to role's homepage
    		//if user has more than one role, go to dropdown menu
    	if(roles.size() == 1) {
    		NavigationHelper.goToHomePage(roles.get(0), primaryStage, databaseHelper, user);
    	}else if (roles.size() > 1) {
    		new RoleSelectionPage(roles, databaseHelper, user).show(primaryStage);

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
	    
	    // "Invite" button for admin to generate invitation codes
	    if (user.getRoles().contains("admin")) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user);
            });
            layout.getChildren().add(inviteButton);
        }

	    layout.getChildren().addAll(welcomeLabel,continueButton,logoutButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}