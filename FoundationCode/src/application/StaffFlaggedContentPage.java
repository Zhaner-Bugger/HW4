package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * The StaffFlaggedContentPage class provides an interface for staff members to view
 * and manage content that has been flagged as inappropriate or concerning.
 * </p>
 * 
 * <p>
 * This page is part of the implementation for User Story 2: "As a staff member, 
 * I should be able to flag any content that seems inappropriate or concerning, 
 * so that I can help prevent issues early."
 * </p>
 * 
 * <p>
 * Staff members can:
 * <ul>
 *   <li>View all flagged content with details about who flagged it and why</li>
 *   <li>View the actual content that was flagged</li>
 *   <li>Update flag status to "Reviewed" or "Resolved"</li>
 *   <li>Track the history of flags through status changes</li>
 * </ul>
 * </p>
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-16
 */
public class StaffFlaggedContentPage {
    /** Database helper for accessing flag data */
    private final DatabaseHelper databaseHelper;
    
    /** Currently logged-in staff user */
    private final User currentUser;
    
    /** Table view displaying all flagged content */
    private TableView<ContentFlag> flagTable;
    
    /**
     * Constructs a new StaffFlaggedContentPage.
     * 
     * @param databaseHelper the database helper for flag data access
     * @param currentUser the currently logged-in staff user
     */
    public StaffFlaggedContentPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the flagged content management page in the provided stage.
     * Shows a table of all flagged content with controls to view details and
     * update flag status.
     * 
     * @param primaryStage the stage to display the page
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");
        
        Label titleLabel = new Label("Flagged Content Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        flagTable = createFlagTable();
        loadFlags();
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button refreshButton = new Button("Refresh");
        Button viewDetailsButton = new Button("View Content Details");
        Button markReviewedButton = new Button("Mark as Reviewed");
        Button markResolvedButton = new Button("Mark as Resolved");
        Button backButton = new Button("Back");
        
        refreshButton.setOnAction(e -> loadFlags());
        viewDetailsButton.setOnAction(e -> viewFlaggedContent());
        markReviewedButton.setOnAction(e -> updateFlagStatus("Reviewed"));
        markResolvedButton.setOnAction(e -> updateFlagStatus("Resolved"));
        backButton.setOnAction(e -> {
            new StaffHomePage(databaseHelper, currentUser).show(primaryStage);
        });
        
        buttonBox.getChildren().addAll(refreshButton, viewDetailsButton, markReviewedButton, 
                                       markResolvedButton, backButton);
        
        layout.getChildren().addAll(titleLabel, flagTable, buttonBox);
        
        Scene scene = new Scene(layout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flagged Content");
    }
    
    /**
     * Creates and configures the table view for displaying flagged content.
     * The table includes columns for flag ID, content type, content ID,
     * who flagged it, reason, status, and creation date.
     * 
     * @return a configured TableView for ContentFlag objects
     */
    private TableView<ContentFlag> createFlagTable() {
        TableView<ContentFlag> table = new TableView<>();
        
        TableColumn<ContentFlag, Integer> idCol = new TableColumn<>("Flag ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("flagId"));
        idCol.setMinWidth(80);
        
        TableColumn<ContentFlag, String> typeCol = new TableColumn<>("Content Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("contentType"));
        typeCol.setMinWidth(100);
        
        TableColumn<ContentFlag, String> contentIdCol = new TableColumn<>("Content ID");
        contentIdCol.setCellValueFactory(new PropertyValueFactory<>("contentId"));
        contentIdCol.setMinWidth(100);
        
        TableColumn<ContentFlag, String> flaggedByCol = new TableColumn<>("Flagged By");
        flaggedByCol.setCellValueFactory(new PropertyValueFactory<>("flaggedBy"));
        flaggedByCol.setMinWidth(100);
        
        TableColumn<ContentFlag, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonCol.setMinWidth(250);
        
        TableColumn<ContentFlag, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMinWidth(100);
        
        TableColumn<ContentFlag, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        table.getColumns().addAll(idCol, typeCol, contentIdCol, flaggedByCol, reasonCol, statusCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Loads all flags from the database and populates the table view.
     * Displays an error alert if the load operation fails.
     */
    private void loadFlags() {
        try {
            List<ContentFlag> flags = databaseHelper.getAllFlags();
            flagTable.setItems(FXCollections.observableArrayList(flags));
        } catch (SQLException e) {
            showAlert("Error", "Failed to load flags: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Retrieves and displays the actual content that was flagged.
     * Shows different information based on the content type (question, answer, review, message).
     * Provides full context for the staff member to evaluate the flag.
     */
    private void viewFlaggedContent() {
        ContentFlag selected = flagTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a flag to view.", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            String content = "";
            String contentType = selected.getContentType();
            String contentId = selected.getContentId();
            
            switch (contentType) {
                case "question":
                    List<Question> questions = databaseHelper.getAllQuestions();
                    for (Question q : questions) {
                        if (q.getQuestionId().equals(contentId)) {
                            content = "Title: " + q.getTitle() + "\n\n" +
                                     "Content: " + q.getContent() + "\n\n" +
                                     "Author: " + q.getAuthor();
                            break;
                        }
                    }
                    break;
                case "answer":
                    List<Answer> answers = databaseHelper.getAllAnswers();
                    for (Answer a : answers) {
                        if (a.getAnswerId().equals(contentId)) {
                            content = "Content: " + a.getContent() + "\n\n" +
                                     "Author: " + a.getAuthor() + "\n" +
                                     "Question ID: " + a.getQuestionId();
                            break;
                        }
                    }
                    break;
                case "review":
                    List<Answer> allAnswers = databaseHelper.getAllAnswers();
                    for (Answer ans : allAnswers) {
                        List<Review> reviews = databaseHelper.getReviewsForAnswer(ans.getAnswerId());
                        for (Review r : reviews) {
                            if (r.getReviewId().equals(contentId)) {
                                content = "Content: " + r.getContent() + "\n\n" +
                                         "Reviewer: " + r.getReviewer() + "\n" +
                                         "Answer ID: " + r.getAnswerId();
                                break;
                            }
                        }
                    }
                    break;
                case "message":
                    List<PrivateMessage> messages = databaseHelper.getAllPrivateMessages();
                    for (PrivateMessage m : messages) {
                        if (String.valueOf(m.getId()).equals(contentId)) {
                            content = "From: " + m.getFromUser() + "\n" +
                                     "To: " + m.getToUser() + "\n\n" +
                                     "Content: " + m.getContent();
                            break;
                        }
                    }
                    break;
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Flagged Content Details");
            alert.setHeaderText("Flag ID: " + selected.getFlagId() + " - " + contentType.toUpperCase());
            alert.setContentText(
                "Flagged by: " + selected.getFlaggedBy() + "\n" +
                "Reason: " + selected.getReason() + "\n" +
                "Status: " + selected.getStatus() + "\n\n" +
                "=== Content ===\n\n" + content
            );
            alert.getDialogPane().setPrefWidth(700);
            alert.showAndWait();
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to retrieve content: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Updates the status of a selected flag to a new status.
     * Confirms the action with the user before updating the database.
     * 
     * @param newStatus the new status to set (Reviewed or Resolved)
     */
    private void updateFlagStatus(String newStatus) {
        ContentFlag selected = flagTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a flag to update.", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Status Update");
        confirmation.setHeaderText("Update Flag Status");
        confirmation.setContentText("Mark flag " + selected.getFlagId() + " as " + newStatus + "?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = databaseHelper.updateFlagStatus(selected.getFlagId(), newStatus);
                if (success) {
                    showAlert("Success", "Flag status updated successfully.", Alert.AlertType.INFORMATION);
                    loadFlags(); // Refresh the table
                } else {
                    showAlert("Error", "Failed to update flag status.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Displays an alert dialog with the specified title, content, and type.
     * 
     * @param title the title of the alert
     * @param content the content message to display
     * @param type the type of alert (ERROR, WARNING, INFORMATION, etc.)
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}