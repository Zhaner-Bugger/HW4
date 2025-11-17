package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;



import java.util.List;
import javafx.collections.FXCollections;
import databasePart1.*;



public class RoleSelectionPage  {
	private final List<String> roles;
	private DatabaseHelper databaseHelper;
	private User user;


	public RoleSelectionPage(List<String> roles,DatabaseHelper databaseHelper,User user ) {
		this.roles = roles;
		this.databaseHelper = databaseHelper;
		this.user = user;

	}


	public void show(Stage primaryStage) {
		Label label = new Label("Select your role:");

		ChoiceBox<String> roleChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(roles));


		Button continueButton = new Button("Continue");
		continueButton.setOnAction(e -> {
			String selectedRole = roleChoiceBox.getValue();
			user.setActiveRole(selectedRole);
			if(selectedRole != null) {
				NavigationHelper.goToHomePage(selectedRole,primaryStage, databaseHelper, user);

			}else {
				System.out.println("No role selected");
			}
		});

		VBox layout = new VBox(10);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(label, roleChoiceBox, continueButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Role Selection");
        primaryStage.show();



	}




}