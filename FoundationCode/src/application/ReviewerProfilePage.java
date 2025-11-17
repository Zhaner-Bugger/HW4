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
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
/**
 * Represents a user interface page displaying a reviewer's profile 
 */
public class ReviewerProfilePage {
	
	public enum AccessContext{
		STUDENT_HOME, REVIEWER_HOME, ADMIN_HOME
		
	}
	private final DatabaseHelper databaseHelper;
	private final User currentUser;
	private final ReviewerProfile reviewer;
	private TableView<Review> reviewTable;
	private TableView<Feedback> feedbackTable;
	private final AccessContext accessContext;
	/**
	 * Creates an instance of the reviewer profile page
	 * @param databaseHelper access to the database
	 * @param currentUser  the user accessing the page 
	 * @param reviewer    the reviewer the profile belongs to 
	 * @param accessContext  where page was accessed from 
	 */
	public ReviewerProfilePage(DatabaseHelper databaseHelper, User currentUser, ReviewerProfile reviewer, AccessContext accessContext) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
		this.reviewer = reviewer;
		this.accessContext = accessContext;
	}
	/**
	 * Displays the reviewer profile within the given stage 
	 * 
	 * <p> This method sets up the layout, loads reviewer information, 
	 * populates the reviews and feedback tables and configures
	 * navigation controls
	 * </p>
	 * @param primaryStage
	 */
	public void show(Stage primaryStage) {
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);
		
		Label title = new Label(reviewer.getName() + "'s Profile");
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
		
		Label experienceLabel = new Label("Experience: " + reviewer.getExperience());
		experienceLabel.setStyle("-fx-font-size: 14px;");
		/**
		 * textArea for editing ( hidden by default)
		 */
		
		TextArea experienceEdit = new TextArea(reviewer.getExperience());
		experienceEdit.setVisible(false);
		experienceEdit.setPrefRowCount(3);
		
		/**
		 * Buttons
		 */
		Button editButton = new Button("Edit Experience");
		Button saveButton = new Button("Save Changes");
		Button cancelButton = new Button("Cancel");
		Button addTrustedButton = new Button("Add to Trusted Reviewers");
		Button sendFeedbackButton = new Button("Send Private Feedback");
		Button backButton = new Button("Back");
		
		saveButton.setVisible(false);
		cancelButton.setVisible(false);
		/**
		 * Checks if the user is the reviewer (profile owner)
		 * If viewing as reviewer allow editing experience 
		 */
		boolean isOwner = currentUser.getUserId() == reviewer.getReviewerId();
		if (isOwner) {
			layout.getChildren().addAll(editButton, saveButton, cancelButton);
		}
		/**
		 * If viewing as a student, allow adding to trusted list and sending private feedback
		 */
		if (accessContext == AccessContext.STUDENT_HOME) {
			layout.getChildren().addAll(addTrustedButton, sendFeedbackButton);
		}
		
		reviewTable = createReviewTable();
		populateReviewTable();
		feedbackTable = createFeedbackTable();
		populateFeedbackTable();
		
		
		
		layout.getChildren().addAll(title, experienceLabel,experienceEdit, new Label("Reviews Provided: "), reviewTable,new Label("Feedback Received: "), feedbackTable, backButton);
		
		Scene scene = new Scene(layout, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Reviewer Profile");
		primaryStage.show();
		
		editButton.setOnAction( e-> {
			experienceEdit.setVisible(true);
			experienceLabel.setVisible(false);
			saveButton.setVisible(true);
			cancelButton.setVisible(true);
			editButton.setVisible(false);
		});
		cancelButton.setOnAction(e -> {
			experienceEdit.setVisible(false);
			experienceLabel.setVisible(true);
			saveButton.setVisible(false);
			cancelButton.setVisible(false);
			editButton.setVisible(true);
		});
		saveButton.setOnAction(e -> {
			String newExperience = experienceEdit.getText().trim();
			try {
				databaseHelper.updateReviewerExperience(currentUser.getUserId(),newExperience);
				reviewer.setExperience(newExperience);
				experienceLabel.setText("Experience: " + newExperience);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
			experienceEdit.setVisible(false);
			experienceLabel.setVisible(true);
			saveButton.setVisible(false);
			cancelButton.setVisible(false);
			editButton.setVisible(true);
		});
		/**
		 * Back button changes depending on where page was accessed
		 */
		backButton.setOnAction(e -> {
			switch(accessContext) {
			case REVIEWER_HOME:
				ReviewerHomePage reviewerHome = new ReviewerHomePage(databaseHelper, currentUser);
				reviewerHome.show(primaryStage);
				break;
			case STUDENT_HOME:
				ReviewerProfilesPage profilesPage = new ReviewerProfilesPage(databaseHelper, currentUser);
				profilesPage.show(primaryStage);
				break;
				
			}
			
		});

		/** Add to trusted reviewers (client-side list in User)
		 * 
		 */
		addTrustedButton.setOnAction(e -> {
			boolean added = currentUser.addTrustedReviewer(reviewer.getReviewerId());
			Alert a = new Alert(Alert.AlertType.INFORMATION);
			if (added) {
				a.setContentText("Reviewer added to your trusted list.");
			} else {
				a.setContentText("This reviewer is already in your trusted list.");
			}
			a.showAndWait();
		});

		/** Send private feedback to reviewer
		 * 
		 */
		sendFeedbackButton.setOnAction(e -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Send Private Feedback");
			dialog.setHeaderText("Send private feedback to " + reviewer.getName());
			dialog.setContentText("Message:");
			Optional<String> res = dialog.showAndWait();
			res.ifPresent(msg -> {
				String content = msg.trim();
				if (content.isEmpty()) {
					Alert a = new Alert(Alert.AlertType.WARNING, "Message cannot be empty");
					a.showAndWait();
					return;
				}
				try {
					/**
					 *  Store feedback as a private message with a reviewer-specific questionId marker
					 */
					String qid = "REVFB:" + reviewer.getReviewerId();
					boolean ok = databaseHelper.insertPrivateMessage(qid, currentUser.getUserName(), reviewer.getName(), content);
					if (ok) {
						Alert a = new Alert(Alert.AlertType.INFORMATION, "Feedback sent.");
						a.showAndWait();
					} else {
						Alert a = new Alert(Alert.AlertType.ERROR, "Failed to send feedback.");
						a.showAndWait();
					}
				} catch (SQLException ex) {
					Alert a = new Alert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
					a.showAndWait();
				}
			});
		});
		
	}
	
	private TableView<Review> createReviewTable(){
		TableView<Review> table = new TableView<>();
		
		TableColumn<Review, String> answerCol = new TableColumn<>("Answer ID");
		answerCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
		
		
		TableColumn<Review, String> contentCol = new TableColumn<>("Review Text");
		contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
		
		TableColumn<Review, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
		table.getColumns().addAll(answerCol, contentCol, dateCol);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		return table;
		
	}
	private TableView<Feedback> createFeedbackTable(){
		TableView<Feedback> table = new TableView<>();
		
		TableColumn<Feedback,String> studentCol = new TableColumn<>("From Student:");
		studentCol.setCellValueFactory(new PropertyValueFactory<>("studentUserName"));
		
		TableColumn<Feedback,String> contentCol = new TableColumn<>("Feedback Text");
		contentCol.setCellValueFactory(new PropertyValueFactory<>("feedbackText"));
		
		TableColumn<Feedback, String> dateCol = new TableColumn<>("Date: ");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedAt"));
			
		table.getColumns().addAll(studentCol, contentCol, dateCol);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		return table;
	}
	private void populateReviewTable() {
		try {
			List<Review> reviews = databaseHelper.getReviewsByReviewer(reviewer.getName());
			ObservableList<Review> reviewList = FXCollections.observableArrayList(reviews);
			reviewTable.setItems(reviewList);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void populateFeedbackTable() {
		try {
			List<Feedback> feedback = databaseHelper.getFeedbackByReviewer(reviewer.getName());
			ObservableList<Feedback> feedbackList = FXCollections.observableArrayList(feedback);
			feedbackTable.setItems(feedbackList);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
