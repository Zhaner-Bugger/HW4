
package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitePage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */

public class InvitationPage {
	private User currentAdmin;	

	/**
     * Displays the Invite Page in the provided primary stage.
     * 
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     */
	public void show(DatabaseHelper databaseHelper, Stage primaryStage, User currentAdmin) {
		this.currentAdmin = currentAdmin;
		VBox layout = new VBox(10);
		layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

		Label titleLabel = new Label("Invite User via One-Time Code");
		titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		TextField emailField = new TextField();
		emailField.setPromptText("Enter user email");
		emailField.setMaxWidth(250);

		TextField expirationField = new TextField();
		expirationField.setPromptText("Expiration (yyyy-MM-dd HH:mm)");
		expirationField.setMaxWidth(250);

		Button generateButton = new Button("Generate Invitation Code");
		Label codeLabel = new Label("");
		codeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
		Label errorLabel = new Label("");
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
		
		Button backButton = new Button("Back"); // Added by JA
		backButton.setOnAction(e -> {
			new WelcomeLoginPage(databaseHelper).show(primaryStage, currentAdmin);
		});


		generateButton.setOnAction(e -> {
			String email = emailField.getText();
			String expirationStr = expirationField.getText();
			errorLabel.setText("");
			codeLabel.setText("");
			if (!DatabaseHelper.isValidEmail(email)) {
				errorLabel.setText("Invalid email format.");
				return;
			}
			if (!DatabaseHelper.isValidExpiration(expirationStr)) {
				errorLabel.setText("Invalid expiration format. Use yyyy-MM-dd HH:mm");
				return;
			}
			try {
				java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(expirationStr, formatter);
				java.sql.Timestamp expiration = java.sql.Timestamp.valueOf(dateTime);
				String code = databaseHelper.generateInvitationCode(email, expiration);
				codeLabel.setText("Invitation Code: " + code);
			} catch (Exception ex) {
				errorLabel.setText("Error: " + ex.getMessage());
			}
		});

		layout.getChildren().addAll(titleLabel, emailField, expirationField, 
				generateButton, backButton, errorLabel, codeLabel);
		Scene inviteScene = new Scene(layout, 800, 400);
		primaryStage.setScene(inviteScene);
		primaryStage.setTitle("Invite Page");
	}
}