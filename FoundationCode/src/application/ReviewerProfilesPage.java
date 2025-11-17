package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import application.ReviewerProfilePage.AccessContext;
/**
 * Represents a user interface page displaying all reviewer profiles
 */


public class ReviewerProfilesPage {
	private final DatabaseHelper databaseHelper;
	private final User currentUser;
	private TableView<ReviewerProfile> reviewerTable;
	
	/**
	 * Creates a new instance of the reviewers profiles page 
	 * @param databaseHelper access the database
	 * @param currentUser the user accessing the page 
	 */
	public ReviewerProfilesPage(DatabaseHelper databaseHelper, User currentUser ) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
		
	}
	/**
	 * Displays reviewer profiles page within the given Stage.
	 * 
	 * <p>This method sets up the layout, loads all reviewers
	 * populates reviewer table and configures navigation controls
	 * </p>
	 * @param primaryStage main application window to display the scene on
	 */
	
	public void show(Stage primaryStage) {
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-alignment: center;");
		
		Label title = new Label("Reviewer Profiles");
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		// Search controls
		TextField searchField = new TextField();
		searchField.setPromptText("Search reviewers by name...");
		Button searchButton = new Button("Search");
		Button showTrustedButton = new Button("Show My Trusted Reviewers");
		
		
		reviewerTable = createReviewerTable();
		populateReviewerTable();

		// search actions
		searchButton.setOnAction(e -> {
			String q = searchField.getText();
			if (q == null || q.trim().isEmpty()) {
				populateReviewerTable();
			} else {
				filterReviewerTable(q.trim());
			}
		});
		showTrustedButton.setOnAction(e -> {
			// show the trusted reviewers for currentUser
			try {
				java.util.List<Integer> ids = currentUser.getTrustedReviewerIds();
				if (ids.isEmpty()) {
					showAlert("You have no trusted reviewers yet.");
					return;
				}
				java.util.List<ReviewerProfile> list = new java.util.ArrayList<>();
				for (int id : ids) {
					ReviewerProfile p = databaseHelper.getReviewerProfileById(id);
					if (p != null) list.add(p);
				}
				reviewerTable.setItems(FXCollections.observableArrayList(list));
			} catch (Exception ex) {
				ex.printStackTrace();
				showAlert("Failed to load trusted reviewers.");
			}
		});
		
		
		Button viewProfileBttn = new Button("View Profile");
		Button backButton = new Button("Back");
		
		HBox buttonBox = new HBox(10, viewProfileBttn, backButton);
		buttonBox.setAlignment(Pos.CENTER);
		
		HBox searchBox = new HBox(8, searchField, searchButton, showTrustedButton);
		searchBox.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(title, searchBox, reviewerTable, buttonBox);
		
		
		Scene scene = new Scene(layout, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle( "Reviewer Profiles");
		primaryStage.show();
		
		
		viewProfileBttn.setOnAction(e -> {
			ReviewerProfile selected = reviewerTable.getSelectionModel().getSelectedItem();
			if(selected != null) {
				ReviewerProfilePage profilePage = new ReviewerProfilePage(databaseHelper, currentUser, selected,AccessContext.STUDENT_HOME);
				profilePage.show(primaryStage);
				
			}else {
				showAlert("Please select a reviewer to view their profile. ");
				
			}
		});
		
		backButton.setOnAction(e -> {
			StudentHomePage studentHome = new StudentHomePage(databaseHelper, currentUser);
			studentHome.show(primaryStage);
			
		});

	}
	/**
	 * Reviewer table displays reviewer name and experience
	 * @return reviewer table 
	 */
	private TableView<ReviewerProfile> createReviewerTable(){
		TableView<ReviewerProfile> table = new TableView<>();
		
		TableColumn<ReviewerProfile, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		TableColumn<ReviewerProfile, String> experienceCol = new TableColumn<>("Experience");
		experienceCol.setCellValueFactory(new PropertyValueFactory<>("experience"));
		
		table.getColumns().addAll(nameCol, experienceCol);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		return table;
	}
	private void populateReviewerTable() {
		try {
			List<ReviewerProfile> reviewers = databaseHelper.getAllReviewerProfiles();
			ObservableList<ReviewerProfile> reviewerList = FXCollections.observableArrayList(reviewers);
			reviewerTable.setItems(reviewerList);
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Error loading reviewer profiles from the database");
		}
	}
	/**
	 * Show a simple information alert with the provided message.
	 * @param message the message to display
	 */
	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * Filter reviewer table by name (case-insensitive substring match).
	 * @param q query text
	 */
	private void filterReviewerTable(String q) {
		try {
			List<ReviewerProfile> reviewers = databaseHelper.getAllReviewerProfiles();
			java.util.List<ReviewerProfile> out = new java.util.ArrayList<>();
			for (ReviewerProfile p : reviewers) {
				if (p.getName() != null && p.getName().toLowerCase().contains(q.toLowerCase())) out.add(p);
			}
			reviewerTable.setItems(FXCollections.observableArrayList(out));
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Search failed: " + e.getMessage());
		}
	}
	

}
