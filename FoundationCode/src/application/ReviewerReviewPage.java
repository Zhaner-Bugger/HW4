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

/**
 * UI page where reviewers can view answers and submit reviews for selected answers.
 */
public class ReviewerReviewPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private TableView<Answer> answerTable;
    private TableView<Review> reviewTable;

    /**
     * Construct the reviewer review page controller.
     * @param databaseHelper database helper instance
     * @param currentUser currently logged in user
     */
    public ReviewerReviewPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Show the review UI where reviewers can select answers and write reviews.
     * @param primaryStage the Stage to display the UI on
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Label title = new Label("Review Answers");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Table of answers
        answerTable = createAnswerTable();
        populateAnswerTable();

        // Table of reviews for selected answer
        reviewTable = createReviewTable();

        // TextArea to write a review
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Write your review here...");
        reviewArea.setPrefRowCount(4);

        // Buttons
        Button submitReview = new Button("Submit Review");
        Button refreshReviews = new Button("Refresh Reviews");
        Button backButton = new Button("Back");

        HBox buttonBox = new HBox(10, submitReview, refreshReviews, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(title, new Label("Select an Answer:"), answerTable,
                new Label("Reviews for Selected Answer:"), reviewTable,
                new Label("Your Review:"), reviewArea, buttonBox);

        // Event: select an answer -> load its reviews
        answerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateReviewTable(newSelection.getAnswerId());
            }
        });

        // Submit review
        submitReview.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer == null) {
                showAlert("Error", "Please select an answer to review.", Alert.AlertType.ERROR);
                return;
            }
            String content = reviewArea.getText().trim();
            if (content.isEmpty()) {
                showAlert("Error", "Review cannot be empty.", Alert.AlertType.ERROR);
                return;
            }

            Review review = new Review(
                    "R" + UUID.randomUUID().toString().substring(0, 8),
                    selectedAnswer.getAnswerId(),
                    currentUser.getUserName(),
                    content,
                    java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()),
                    null 
            );

            try {
                if (databaseHelper.insertReview(review)) {
                    showAlert("Success", "Review submitted successfully!", Alert.AlertType.INFORMATION);
                    reviewArea.clear();
                    populateReviewTable(selectedAnswer.getAnswerId());
                } else {
                    showAlert("Error", "Failed to submit review.", Alert.AlertType.ERROR);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        refreshReviews.setOnAction(e -> {
            Answer selectedAnswer = answerTable.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                populateReviewTable(selectedAnswer.getAnswerId());
            }
        });

        
        backButton.setOnAction(e -> {
            NavigationHelper.goToHomePage(currentUser.getActiveRole(), primaryStage, databaseHelper, currentUser);
        }); 

        Scene scene = new Scene(layout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Review Page");
    }

    private TableView<Answer> createAnswerTable() {
        TableView<Answer> table = new TableView<>();
        TableColumn<Answer, String> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setMinWidth(80);

        TableColumn<Answer, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);

        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMinWidth(100);

        table.getColumns().addAll(idCol, contentCol, authorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<Review> createReviewTable() {
        TableView<Review> table = new TableView<>();
        TableColumn<Review, String> reviewerCol = new TableColumn<>("Reviewer");
        reviewerCol.setCellValueFactory(new PropertyValueFactory<>("reviewer"));

        TableColumn<Review, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);

        TableColumn<Review, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.getColumns().addAll(reviewerCol, contentCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void populateAnswerTable() {
        try {
            List<Answer> answers = databaseHelper.getAllAnswers(); // You should already have this
            ObservableList<Answer> data = FXCollections.observableArrayList(answers);
            answerTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load answers: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Load reviews for the provided answer id and populate the review table.
     * @param answerId id of the answer whose reviews should be loaded
     */
    private void populateReviewTable(String answerId) {
        try {
            List<Review> reviews = databaseHelper.getReviewsForAnswer(answerId);
            ObservableList<Review> data = FXCollections.observableArrayList(reviews);
            reviewTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load reviews: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Show an alert dialog.
     * @param title alert title
     * @param content alert message content
     * @param type type of the alert
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
