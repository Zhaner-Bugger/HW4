package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField userInfoName = new TextField();
        userInfoName.setPromptText("Enter User's Name");
        userInfoName.setMaxWidth(250);
        
        TextField userInfoEmail = new TextField();
        userInfoEmail.setPromptText("Enter User's Email");
        userInfoEmail.setMaxWidth(250);
        
        // User Namer Error Message
        Label userNameErrorLabel = new Label();
        userNameErrorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        
        // Password Error Message
        Label passwordErrorLabel = new Label ();
        passwordErrorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String email = userInfoEmail.getText();
            String name = userInfoName.getText();
            
            // Retrieve outcome of checks
            String userNameCheck = UserNameRecognizer.checkForValidUserName(userName);
            String passwordCheck = PasswordEvaluator.evaluatePassword(password);

            try {
            	// Checking if UserName and Password are valid
            	if (userNameCheck.isEmpty() && passwordCheck.isEmpty()) {
            		// Create a new User object with admin role and register in the database
            		User user = new User(userName, password, email, name, "admin");
            		databaseHelper.register(user);
            		System.out.println("Administrator setup completed.");
                
            		// Navigate to the Welcome Login Page
            		new UserLoginPage(databaseHelper).show(primaryStage);
            	// If not valid display error message
            	} else {
            		if (!userNameCheck.isEmpty()) {
                		userNameErrorLabel.setText(userNameCheck);
                	} else {
                		userNameErrorLabel.setText(userNameCheck);
                	}
                	if (!passwordCheck.isEmpty()) {
                		passwordErrorLabel.setText(passwordCheck);
                	} else {
                		passwordErrorLabel.setText(passwordCheck);
                	}
            	}
            }
            		catch (SQLException e) {
            		System.err.println("Database error: " + e.getMessage());
            		e.printStackTrace();
            	}
            });

        VBox layout = new VBox(10, userNameField, passwordField, userInfoName, userInfoEmail, setupButton, 
        		userNameErrorLabel ,passwordErrorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
