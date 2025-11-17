package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
    	TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
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
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: bold;");
        
        // User Namer Error Message
        Label userNameErrorLabel = new Label();
        userNameErrorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        
        // Password Error Message
        Label passwordErrorLabel = new Label ();
        passwordErrorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        
        Button setupButton = new Button("Setup");
        Button backButton = new Button("Back"); //Added by JA
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
        	String userName = userNameField.getText();
            String password = passwordField.getText();
            String email = userInfoEmail.getText();
            String name = userInfoName.getText();
            String code = inviteCodeField.getText();
            
            // Retrieve outcome of checks
            String userNameCheck = UserNameRecognizer.checkForValidUserName(userName);
            String passwordCheck = PasswordEvaluator.evaluatePassword(password);
            
            	try {
            		// Checks if UserName and Password are valid
            		if (userNameCheck.isEmpty() && passwordCheck.isEmpty()) {
            			// Resets error messages
        				userNameErrorLabel.setText(userNameCheck);
                		passwordErrorLabel.setText(passwordCheck);

            			// Check if the user already exists
                    	if(!databaseHelper.doesUserExist(userName)) {
                    		
                    		// Validate the invitation code
                    		if(databaseHelper.validateInvitationCode(code)) {
            			
                    			// Create a new user and register them in the database
        		            	User user = new User(userName, password, email, name, "user");
        		                databaseHelper.register(user);
        		                
        		                
        		             // Navigate to the Welcome Login Page
        		                new UserLoginPage(databaseHelper).show(primaryStage);
                    		}
                    		else {
                    			errorLabel.setText("Please enter a valid invitation code");
                    		}
                    	}
                    	else {
                    		errorLabel.setText("This useruserName is taken!!.. Please use another to setup an account");
                    	}
                    		
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
            		
            	}catch (SQLException e) {
            		System.err.println("Database error: " + e.getMessage());
            		e.printStackTrace();
            	}
        });
        
        backButton.setOnAction (a -> {
        	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, userInfoName, userInfoEmail, 
        		inviteCodeField, setupButton, backButton, errorLabel, 
        		userNameErrorLabel, passwordErrorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
