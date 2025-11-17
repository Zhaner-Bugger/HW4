package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * The StaffContentViewPage class provides a comprehensive interface for staff members
 * to view all content in the system. This includes questions, answers, reviews, and
 * private messages.
 * </p>
 * 
 * <p>
 * This page implements User Story 1: "As a staff member, I should be able to see all 
 * questions, answers, and private feedback, so that I can monitor user interactions."
 * </p>
 * 
 * <p>
 * This page also implements User Story 2: "As a staff member, I should be able to flag 
 * any content that seems inappropriate or concerning, so that I can help prevent issues early."
 * Staff can flag content by selecting it and clicking the "Flag Content" button.
 * </p>
 * 
 * <p>
 * The interface uses a tabbed layout where each tab displays a different type of content.
 * Staff members can view detailed information about each item and flag concerning content
 * for further review.
 * </p>
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-16
 */
public class StaffContentViewPage {
    /** Database helper for accessing content data */
    private final DatabaseHelper databaseHelper;
    
    /** Currently logged-in staff user */
    private final User currentUser;
    
    /** Main tab pane containing all content tabs */
    private TabPane tabPane;
    
    /**
     * Constructs a new StaffContentViewPage.
     * 
     * @param databaseHelper the database helper for content access
     * @param currentUser the currently logged-in staff user
     */
    public StaffContentViewPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the staff content view page in the provided stage.
     * Creates a tabbed interface with separate views for questions, answers,
     * reviews, and private messages.
     * 
     * @param primaryStage the stage to display the content view page
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");
        
        Label titleLabel = new Label("Content Monitoring - All System Content");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        tabPane = new TabPane();
        
        // Create tabs for different content types
        Tab questionsTab = createQuestionsTab();
        Tab answersTab = createAnswersTab();
        Tab reviewsTab = createReviewsTab();
        Tab messagesTab = createMessagesTab();
        
        tabPane.getTabs().addAll(questionsTab, answersTab, reviewsTab, messagesTab);
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new StaffHomePage(databaseHelper, currentUser).show(primaryStage);
        });
        
        layout.getChildren().addAll(titleLabel, tabPane, backButton);
        
        Scene scene = new Scene(layout, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Content View");
    }
    
    /**
     * Creates the tab for viewing all questions in the system.
     * The tab includes a table view and buttons for refreshing, viewing details,
     * and flagging content.
     * 
     * @return the questions tab
     */
    private Tab createQuestionsTab() {
        Tab tab = new Tab("Questions");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TableView<Question> table = new TableView<>();
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        idCol.setMinWidth(100);
        
        TableColumn<Question, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setMinWidth(200);
        
        TableColumn<Question, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMinWidth(100);
        
        TableColumn<Question, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        TableColumn<Question, String> resolvedCol = new TableColumn<>("Resolved");
        resolvedCol.setCellValueFactory(new PropertyValueFactory<>("isResolved"));
        resolvedCol.setMinWidth(80);
        
        table.getColumns().addAll(idCol, titleCol, authorCol, dateCol, resolvedCol);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button refreshButton = new Button("Refresh");
        Button viewButton = new Button("View Details");
        Button flagButton = new Button("Flag Content");
        
        refreshButton.setOnAction(e -> loadQuestions(table));
        viewButton.setOnAction(e -> showQuestionDetails(table.getSelectionModel().getSelectedItem()));
        flagButton.setOnAction(e -> flagQuestion(table.getSelectionModel().getSelectedItem()));
        
        buttonBox.getChildren().addAll(refreshButton, viewButton, flagButton);
        
        content.getChildren().addAll(table, buttonBox);
        tab.setContent(content);
        
        // Initial load
        loadQuestions(table);
        
        return tab;
    }
    
    /**
     * Creates the tab for viewing all answers in the system.
     * The tab includes a table view and buttons for refreshing and flagging content.
     * 
     * @return the answers tab
     */
    private Tab createAnswersTab() {
        Tab tab = new Tab("Answers");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TableView<Answer> table = new TableView<>();
        
        TableColumn<Answer, String> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setMinWidth(100);
        
        TableColumn<Answer, String> questionIdCol = new TableColumn<>("Question ID");
        questionIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        questionIdCol.setMinWidth(100);
        
        TableColumn<Answer, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);
        
        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMinWidth(100);
        
        TableColumn<Answer, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        table.getColumns().addAll(idCol, questionIdCol, contentCol, authorCol, dateCol);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button refreshButton = new Button("Refresh");
        Button flagButton = new Button("Flag Content");
        
        refreshButton.setOnAction(e -> loadAnswers(table));
        flagButton.setOnAction(e -> flagAnswer(table.getSelectionModel().getSelectedItem()));
        
        buttonBox.getChildren().addAll(refreshButton, flagButton);
        
        content.getChildren().addAll(table, buttonBox);
        tab.setContent(content);
        
        loadAnswers(table);
        
        return tab;
    }
    
    /**
     * Creates the tab for viewing all reviews in the system.
     * The tab includes a table view and buttons for refreshing and flagging content.
     * 
     * @return the reviews tab
     */
    private Tab createReviewsTab() {
        Tab tab = new Tab("Reviews");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TableView<Review> table = new TableView<>();
        
        TableColumn<Review, String> idCol = new TableColumn<>("Review ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        idCol.setMinWidth(100);
        
        TableColumn<Review, String> answerIdCol = new TableColumn<>("Answer ID");
        answerIdCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        answerIdCol.setMinWidth(100);
        
        TableColumn<Review, String> reviewerCol = new TableColumn<>("Reviewer");
        reviewerCol.setCellValueFactory(new PropertyValueFactory<>("reviewer"));
        reviewerCol.setMinWidth(100);
        
        TableColumn<Review, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);
        
        TableColumn<Review, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        table.getColumns().addAll(idCol, answerIdCol, reviewerCol, contentCol, dateCol);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button refreshButton = new Button("Refresh");
        Button flagButton = new Button("Flag Content");
        
        refreshButton.setOnAction(e -> loadReviews(table));
        flagButton.setOnAction(e -> flagReview(table.getSelectionModel().getSelectedItem()));
        
        buttonBox.getChildren().addAll(refreshButton, flagButton);
        
        content.getChildren().addAll(table, buttonBox);
        tab.setContent(content);
        
        loadReviews(table);
        
        return tab;
    }
    
    /**
     * Creates the tab for viewing all private messages in the system.
     * The tab includes a table view and buttons for refreshing and flagging content.
     * 
     * @return the private messages tab
     */
    private Tab createMessagesTab() {
        Tab tab = new Tab("Private Messages");
        tab.setClosable(false);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TableView<PrivateMessage> table = new TableView<>();
        
        TableColumn<PrivateMessage, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idCol.setMinWidth(60);
        
        TableColumn<PrivateMessage, String> questionIdCol = new TableColumn<>("Question ID");
        questionIdCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getQuestionId()));
        questionIdCol.setMinWidth(100);
        
        TableColumn<PrivateMessage, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFromUser()));
        fromCol.setMinWidth(100);
        
        TableColumn<PrivateMessage, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getToUser()));
        toCol.setMinWidth(100);
        
        TableColumn<PrivateMessage, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        contentCol.setMinWidth(300);
        
        table.getColumns().addAll(idCol, questionIdCol, fromCol, toCol, contentCol);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button refreshButton = new Button("Refresh");
        Button flagButton = new Button("Flag Content");
        
        refreshButton.setOnAction(e -> loadMessages(table));
        flagButton.setOnAction(e -> flagMessage(table.getSelectionModel().getSelectedItem()));
        
        buttonBox.getChildren().addAll(refreshButton, flagButton);
        
        content.getChildren().addAll(table, buttonBox);
        tab.setContent(content);
        
        loadMessages(table);
        
        return tab;
    }
    
    /**
     * Loads all questions from the database and populates the table view.
     * 
     * @param table the table view to populate with questions
     */
    private void loadQuestions(TableView<Question> table) {
        try {
            List<Question> questions = databaseHelper.getAllQuestions();
            table.setItems(FXCollections.observableArrayList(questions));
        } catch (SQLException e) {
            showAlert("Error", "Failed to load questions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Loads all answers from the database and populates the table view.
     * 
     * @param table the table view to populate with answers
     */
    private void loadAnswers(TableView<Answer> table) {
        try {
            List<Answer> answers = databaseHelper.getAllAnswers();
            table.setItems(FXCollections.observableArrayList(answers));
        } catch (SQLException e) {
            showAlert("Error", "Failed to load answers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Loads all reviews from the database and populates the table view.
     * Reviews are collected from all answers in the system.
     * 
     * @param table the table view to populate with reviews
     */
    private void loadReviews(TableView<Review> table) {
        try {
            List<Answer> answers = databaseHelper.getAllAnswers();
            List<Review> allReviews = new java.util.ArrayList<>();
            for (Answer answer : answers) {
                allReviews.addAll(databaseHelper.getReviewsForAnswer(answer.getAnswerId()));
            }
            table.setItems(FXCollections.observableArrayList(allReviews));
        } catch (SQLException e) {
            showAlert("Error", "Failed to load reviews: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Loads all private messages from the database and populates the table view.
     * 
     * @param table the table view to populate with private messages
     */
    private void loadMessages(TableView<PrivateMessage> table) {
        try {
            List<PrivateMessage> messages = databaseHelper.getAllPrivateMessages();
            table.setItems(FXCollections.observableArrayList(messages));
        } catch (SQLException e) {
            showAlert("Error", "Failed to load messages: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Displays detailed information about a selected question in a dialog.
     * 
     * @param question the question to display details for, or null if none selected
     */
    private void showQuestionDetails(Question question) {
        if (question == null) {
            showAlert("Selection Error", "Please select a question to view.", Alert.AlertType.WARNING);
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Question Details");
        alert.setHeaderText("Question: " + question.getTitle());
        alert.setContentText(
            "ID: " + question.getQuestionId() + "\n" +
            "Author: " + question.getAuthor() + "\n" +
            "Created: " + question.getCreatedAt() + "\n" +
            "Resolved: " + (question.getIsResolved() ? "Yes" : "No") + "\n\n" +
            "Content:\n" + question.getContent()
        );
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }
    
    /**
     * Flags a question as inappropriate or concerning.
     * Prompts the staff member for a reason and creates a flag record in the database.
     * Implements User Story 2.
     * 
     * @param question the question to flag, or null if none selected
     */
    private void flagQuestion(Question question) {
        if (question == null) {
            showAlert("Selection Error", "Please select a question to flag.", Alert.AlertType.WARNING);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Content");
        dialog.setHeaderText("Flag Question: " + question.getQuestionId());
        dialog.setContentText("Reason for flagging:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert("Error", "Please provide a reason for flagging.", Alert.AlertType.WARNING);
                return;
            }
            try {
                boolean success = databaseHelper.flagContent("question", question.getQuestionId(), 
                                                             currentUser.getUserName(), reason);
                if (success) {
                    showAlert("Success", "Content flagged successfully.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to flag content.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }
    
    /**
     * Flags an answer as inappropriate or concerning.
     * Prompts the staff member for a reason and creates a flag record in the database.
     * Implements User Story 2.
     * 
     * @param answer the answer to flag, or null if none selected
     */
    private void flagAnswer(Answer answer) {
        if (answer == null) {
            showAlert("Selection Error", "Please select an answer to flag.", Alert.AlertType.WARNING);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Content");
        dialog.setHeaderText("Flag Answer: " + answer.getAnswerId());
        dialog.setContentText("Reason for flagging:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert("Error", "Please provide a reason for flagging.", Alert.AlertType.WARNING);
                return;
            }
            try {
                boolean success = databaseHelper.flagContent("answer", answer.getAnswerId(), 
                                                             currentUser.getUserName(), reason);
                if (success) {
                    showAlert("Success", "Content flagged successfully.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to flag content.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }
    
    /**
     * Flags a review as inappropriate or concerning.
     * Prompts the staff member for a reason and creates a flag record in the database.
     * Implements User Story 2.
     * 
     * @param review the review to flag, or null if none selected
     */
    private void flagReview(Review review) {
        if (review == null) {
            showAlert("Selection Error", "Please select a review to flag.", Alert.AlertType.WARNING);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Content");
        dialog.setHeaderText("Flag Review: " + review.getReviewId());
        dialog.setContentText("Reason for flagging:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert("Error", "Please provide a reason for flagging.", Alert.AlertType.WARNING);
                return;
            }
            try {
                boolean success = databaseHelper.flagContent("review", review.getReviewId(), 
                                                             currentUser.getUserName(), reason);
                if (success) {
                    showAlert("Success", "Content flagged successfully.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to flag content.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }
    
    /**
     * Flags a private message as inappropriate or concerning.
     * Prompts the staff member for a reason and creates a flag record in the database.
     * Implements User Story 2.
     * 
     * @param message the private message to flag, or null if none selected
     */
    private void flagMessage(PrivateMessage message) {
        if (message == null) {
            showAlert("Selection Error", "Please select a message to flag.", Alert.AlertType.WARNING);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Content");
        dialog.setHeaderText("Flag Private Message ID: " + message.getId());
        dialog.setContentText("Reason for flagging:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert("Error", "Please provide a reason for flagging.", Alert.AlertType.WARNING);
                return;
            }
            try {
                boolean success = databaseHelper.flagContent("message", String.valueOf(message.getId()), 
                                                             currentUser.getUserName(), reason);
                if (success) {
                    showAlert("Success", "Content flagged successfully.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to flag content.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
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