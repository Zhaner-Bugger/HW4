package application;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


/**
 * ReviewManagementPage handles the user interface for reviewers to manage their own reviews.
 * <p>
 * It enables reviewers to:
 * <ul>
 * <li>View a list of all reviews they have submitted.</li>
 * <li>See the number of private feedback messages received for each review.</li>
 * <li>Update the content of their reviews (creating a new version linked to the old one).</li>
 * <li>Delete their reviews.</li>
 * </ul>
 * 
 * <p> Author: Joel Arizmendi
 */
public class ReviewManagementPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private TableView<Review> reviewTable;

    /**
     * Constructs a new ReviewManagementPage.
     *
     * @param databaseHelper The database helper instance for performing data operations.
     * @param currentUser    The currently logged-in user (must have the 'reviewer' role).
     */
    public ReviewManagementPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Initializes and displays the Review Management page in the primary stage.
     *
     * @param primaryStage The main application window.
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label header = new Label("Manage My Reviews");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        reviewTable = new TableView<>();
        setupTable();
        loadReviews();

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new ReviewerHomePage(databaseHelper, currentUser).show(primaryStage));

        layout.getChildren().addAll(header, reviewTable, backButton);
        primaryStage.setScene(new Scene(layout, 900, 600));
        primaryStage.setTitle("Manage Reviews");
    }

    /**
     * Configures the TableView columns for displaying review data, feedback counts, and action buttons.
     */
    private void setupTable() {
        TableColumn<Review, String> dateCol = new TableColumn<>("Date Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);

        TableColumn<Review, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);

        // User Story 1: See number of private feedback messages
        TableColumn<Review, Integer> feedbackCol = new TableColumn<>("Feedback");
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedbackCount"));
        feedbackCol.setMinWidth(80);

        // User Stories 2 & 3: Update and Delete actions
        TableColumn<Review, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(150);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(10, updateBtn, deleteBtn);

            {
                updateBtn.setOnAction(event -> handleUpdate(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        reviewTable.getColumns().addAll(dateCol, contentCol, feedbackCol, actionCol);
    }

    /**
     * Loads the current reviewer's reviews from the database into the TableView.
     */
    private void loadReviews() {
        try {
            reviewTable.setItems(FXCollections.observableArrayList(
                databaseHelper.getReviewsByReviewer(currentUser.getUserName())
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the update action for a specific review.
     * Prompts the user for new content and creates a new review version linked to the old one.
     *
     * @param oldReview The review object being updated.
     */
    private void handleUpdate(Review oldReview) {
        TextInputDialog dialog = new TextInputDialog(oldReview.getContent());
        dialog.setTitle("Update Review");
        dialog.setHeaderText("Update Review (A new version will be created)");
        dialog.setContentText("New Content:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            Review newReview = new Review(
                "R" + UUID.randomUUID().toString().substring(0, 8),
                oldReview.getAnswerId(),
                currentUser.getUserName(),
                newContent,
                Timestamp.valueOf(LocalDateTime.now()),
                oldReview.getReviewId() // <--- LINKS TO OLD REVIEW
            );
            try {
                databaseHelper.insertReview(newReview);
                loadReviews();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Update failed: " + e.getMessage()).show();
            }
        });
    }

    /**
     * Handles the delete action for a specific review.
     * Asks for confirmation before removing the review from the database.
     *
     * @param review The review object to be deleted.
     */
    private void handleDelete(Review review) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this review?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    databaseHelper.deleteReview(review.getReviewId());
                    loadReviews();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Delete failed: " + e.getMessage()).show();
                }
            }
        });
    }
}