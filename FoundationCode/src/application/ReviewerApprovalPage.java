package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

/**
 * Page for instructors to approve or deny pending reviewer role requests.
 */
public class ReviewerApprovalPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    /**
     * Construct a ReviewerApprovalPage.
     * @param databaseHelper DB helper instance
     * @param currentUser current user performing approvals
     */
    public ReviewerApprovalPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Display the pending reviewer requests UI on the provided stage.
     * @param primaryStage the JavaFX Stage to show the UI on
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Label title = new Label("Pending Reviewer Requests");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Retrieve pending requests from the database
        List<User> pendingRequests = new ArrayList();
        try {
            pendingRequests = databaseHelper.getPendingReviewerRequests();
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally show an alert to the instructor
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not retrieve reviewer requests.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        
        if (pendingRequests.isEmpty()) {
            layout.getChildren().add(new Label("No pending reviewer requests."));
        } else {
            for (User student : pendingRequests) {
                HBox row = new HBox(10);
                row.setStyle("-fx-alignment: center;");
                Label nameLabel = new Label(student.getUserName());
                Button approveButton = new Button("Approve");
                Button denyButton = new Button("Deny");

                approveButton.setOnAction(e -> {
                    try {
                        databaseHelper.processReviewerRequest(student.getUserName(), true, currentUser.getUserName());
                        show(primaryStage); // refresh
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                denyButton.setOnAction(e -> {
                    try {
                        databaseHelper.processReviewerRequest(student.getUserName(), false, currentUser.getUserName());
                        show(primaryStage); // refresh
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                row.getChildren().addAll(nameLabel, approveButton, denyButton);
                layout.getChildren().add(row);
            }
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new InstructorHomePage(databaseHelper, currentUser).show(primaryStage));

        layout.getChildren().addAll(title, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Approval Page");
    }
}