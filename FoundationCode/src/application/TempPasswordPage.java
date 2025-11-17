package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TempPasswordPage allows admin to set a one-time password for a user who forgot theirs.
 */
public class TempPasswordPage {

	private User currentAdmin; // Stores current admin - JA
	
    public void show(DatabaseHelper databaseHelper, Stage primaryStage, User currentAdmin) {
    	this.currentAdmin = currentAdmin;

        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Set Temporary Password for User");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField tempPasswordField = new PasswordField();
        tempPasswordField.setPromptText("Enter Temporary Password");
        tempPasswordField.setMaxWidth(250);

        TextField expirationField = new TextField();
        expirationField.setPromptText("Expiration (yyyy-MM-dd HH:mm)");
        expirationField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        Button setButton = new Button("Set Temporary Password");

        
		Button backButton = new Button("Back"); // Added by JA
		backButton.setOnAction(e -> {
			new AdminHomePage(databaseHelper, currentAdmin).show(primaryStage);
		});
		

        setButton.setOnAction(e -> {
            String userName = userNameField.getText();
            String tempPassword = tempPasswordField.getText();
            String expirationStr = expirationField.getText();
            errorLabel.setText("");
            successLabel.setText("");

            if (userName.isEmpty() || tempPassword.isEmpty() || expirationStr.isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }
            if (!DatabaseHelper.isValidExpiration(expirationStr)) {
                errorLabel.setText("Invalid expiration format. Use yyyy-MM-dd HH:mm");
                return;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(expirationStr, formatter);
                Timestamp expiration = Timestamp.valueOf(dateTime);
                boolean result = databaseHelper.setOneTimePassword(userName, tempPassword, expiration);
                if (result) {
                    successLabel.setText("Temporary password set successfully.");
                } else {
                    errorLabel.setText("Failed to set temporary password. User may not exist.");
                }
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(titleLabel, userNameField, tempPasswordField, expirationField, setButton, backButton, errorLabel, successLabel);
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Set Temporary Password");
        primaryStage.show();
    }
}
