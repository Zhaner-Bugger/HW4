package application;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
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
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleStringProperty;

/**
 * AnswerManagementPage class provides interface for viewing, creating, 
 * editing, deleting, and managing answers within the system.
 * Also includes features for reviewer trust weighting and curated answer ranking
 * based on trusted reviewers evaluations.
 */
public class AnswerManagementPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private TableView<Answer> answerTable;
    
    private Map<String, Double> trusted = new HashMap<>();
    private String lastCuratedQuestionId = null;
    
    /**
     * Constructs the AnswerManagementPage for the given logged in user.
     * 
     * @param databaseHelper The active database connection
     * @param currentUser The current user logged into the system
     */
    public AnswerManagementPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the Answer Management UI page inside the primary stage.
     * Builds the table view of answers and attaches user action controls
     * for creating, searching, editing, deleting, sorting and trusted reviewer management.
     * 
     * @param primaryStage The JavaFX stage where the interface is displayed.
     */
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Answer Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Create answer table
        answerTable = createAnswerTable();
        populateAnswerTable();
        
        // Buttons for CRUD operations
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Answer");
        Button editButton = new Button("Edit Answer");
        Button deleteButton = new Button("Delete Answer");
        Button searchButton = new Button("Search Answers");
        Button toggleAcceptedButton = new Button("Toggle Accepted");
        Button sortByAcceptedButton = new Button("Sort by Accepted");
        Button refreshButton = new Button("Refresh");
        Button backButton = new Button("Back");
        Button manageTrustedButton = new Button("Manange Trusted");
        Button curateNowButton = new Button("Curate Now");
        Button checkUpdatesButton = new Button("Check Updates");
        
        addButton.setOnAction(e -> showAddAnswerDialog());
        editButton.setOnAction(e -> editSelectedAnswer());
        deleteButton.setOnAction(e -> deleteSelectedAnswer());
        searchButton.setOnAction(e -> showSearchDialog());
        toggleAcceptedButton.setOnAction(e -> toggleAcceptedStatus());
        sortByAcceptedButton.setOnAction(e -> sortByAccepted());
        refreshButton.setOnAction(e -> populateAnswerTable());
        manageTrustedButton.setOnAction(e -> openManageTrustedDialog());
        curateNowButton.setOnAction(e -> curateNow());
        checkUpdatesButton.setOnAction(e -> checkForTrustedUpdates());
        backButton.setOnAction(e -> {
            NavigationHelper.goToHomePage(currentUser.getActiveRole(), primaryStage, databaseHelper, currentUser);
        });
        
        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, searchButton, 
        		toggleAcceptedButton, sortByAcceptedButton, refreshButton, manageTrustedButton, curateNowButton, checkUpdatesButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        layout.getChildren().addAll(titleLabel, answerTable, buttonBox);
        Scene scene = new Scene(layout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answer Management");
    }
    
    /**
     * Creates and returns the TableView used to list answers.
     * 
     * @return a configured TableView for displaying answers
     */
    private TableView<Answer> createAnswerTable() {
        TableView<Answer> table = new TableView<>();
        
        TableColumn<Answer, String> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setMinWidth(80);
        
        TableColumn<Answer, String> questionIdCol = new TableColumn<>("Question ID");
        questionIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        questionIdCol.setMinWidth(80);
        
        TableColumn<Answer, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);
        
        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMinWidth(100);
        
        TableColumn<Answer, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        TableColumn<Answer, String> acceptedCol = new TableColumn<>("Accepted");
        acceptedCol.setCellValueFactory(new PropertyValueFactory<>("isAccepted"));
        acceptedCol.setMinWidth(80);
        
        table.getColumns().addAll(idCol, questionIdCol, contentCol, authorCol, dateCol, acceptedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }
    
    /**
     * Loads all answers from the database and displays them in the answer table
     * Displays and error alert if data retrieval fails.
     */
    private void populateAnswerTable() {
        try {
            List<Answer> answersList = databaseHelper.getAllAnswers();
            ObservableList<Answer> answers = FXCollections.observableArrayList(answersList);
            answerTable.setItems(answers);
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to load answers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens a dialog allowing the user to add a new answer.
     * Validates input and inserts the answer into the database.
     * Refreshes the table when it is successful.
     */
    private void showAddAnswerDialog() {
        Dialog<Answer> dialog = new Dialog<>();
        dialog.setTitle("Add New Answer");
        dialog.setHeaderText("Enter answer details");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField questionIdField = new TextField();
        questionIdField.setPromptText("Enter question ID");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter answer content");
        contentArea.setPrefRowCount(4);
      //CheckBox acceptedCheckbox = new CheckBox("Mark as accepted answer");
        
        grid.add(new Label("Question ID:"), 0, 0);
        grid.add(questionIdField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);
      //grid.add(new Label("Accepted:"), 0, 2);
      //grid.add(acceptedCheckbox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/Disable add button depending on whether content was entered
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        contentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String validation = AnswerValidator.validateAnswer(contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }
                
                String answerId = "A" + UUID.randomUUID().toString().substring(0, 8);
                Answer answer = new Answer(answerId, questionIdField.getText(), 
                    contentArea.getText(), currentUser.getUserName(), 
                    java.time.LocalDateTime.now().toString(), 
                    false );
                
                try {
                    if (databaseHelper.insertAnswer(answer)) {
                        return answer;
                    } else {
                        showErrorAlert("Database Error", "Failed to insert answer into database");
                        return null;
                    }
                } catch (SQLException ex) {
                    showErrorAlert("Database Error", "Failed to insert answer: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<Answer> result = dialog.showAndWait();
        result.ifPresent(answer -> {
            showInfoAlert("Success", "Answer added successfully!");
            populateAnswerTable();
        });
    }
    
    /**
     * Edits the content of the the currently selected answer.
     * Updates the database and refreshes the view after saving.
     */
    private void editSelectedAnswer() {
        Answer selected = answerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select an answer to edit.");
            return;
        }
      //Error if user does not own answer
        if(!currentUser.getUserName().equals(selected.getAuthor())) {
        	showErrorAlert("Permission Denied", "You can only edit your own answers");
        	return;
        }
        
        Dialog<Answer> dialog = new Dialog<>();
        dialog.setTitle("Edit Answer");
        dialog.setHeaderText("Edit answer details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextArea contentArea = new TextArea(selected.getContent());
        contentArea.setPrefRowCount(4);
      //Check box for Chosen Answer
      //CheckBox acceptedCheckbox = new CheckBox("Mark as accepted answer");
      //acceptedCheckbox.setSelected(selected.getIsAccepted());
        
        grid.add(new Label("Content:"), 0, 0);
        grid.add(contentArea, 1, 0);
      //grid.add(new Label("Accepted:"), 0, 1);
      //grid.add(acceptedCheckbox, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String validation = AnswerValidator.validateAnswer(contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }
                
                selected.setContent(contentArea.getText());
              //selected.setIsAccepted(acceptedCheckbox.isSelected());
                return selected;
            }
            return null;
        });
        
        Optional<Answer> result = dialog.showAndWait();
        result.ifPresent(answer -> {
            try {
                if (databaseHelper.updateAnswer(answer)) {
                    showInfoAlert("Success", "Answer updated successfully!");
                    populateAnswerTable();
                } else {
                    showErrorAlert("Error", "Failed to update answer in database");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update answer: " + e.getMessage());
            }
        });
    }
    
    /**
     * Deletes the selected answer from the database.
     * Only the author of the answer can delete it.
     */
    private void deleteSelectedAnswer() {
        Answer selected = answerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select an answer to delete.");
            return;
        }
        //Error if user does not own answer
        if(!currentUser.getUserName().equals(selected.getAuthor())) {
        	showErrorAlert("Permission Denied", "You can only delete your own answers");
        	return;
        }
        
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Answer");
        confirmation.setContentText("Are you sure you want to delete this answer?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (databaseHelper.deleteAnswer(selected.getAnswerId())) {
                    showInfoAlert("Success", "Answer deleted successfully!");
                    populateAnswerTable();
                } else {
                    showErrorAlert("Error", "Failed to delete answer from database");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete answer: " + e.getMessage());
            }
        }
    }
    
    /**
     * Opens a search dialog allowing the user to search for answers by content.
     * question ID, or author. Updates table to display search results.
     */
    private void showSearchDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Search Answers");
        dialog.setHeaderText("Enter search criteria");
        
        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search by content");
        ComboBox<String> searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("Content", "Question ID", "Author");
        searchTypeCombo.setValue("Content");
        
        grid.add(new Label("Search:"), 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(new Label("Search in:"), 0, 1);
        grid.add(searchTypeCombo, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {
                String searchTerm = searchField.getText();
                String searchType = searchTypeCombo.getValue();
                
                try {
                    List<Answer> searchResults;
                    switch (searchType) {
                        case "Content":
                            searchResults = databaseHelper.searchAnswersByContent(searchTerm);
                            break;
                        case "Question ID":
                            searchResults = databaseHelper.getAnswersForQuestion(searchTerm);
                            break;
                        case "Author":
                            // You would need to implement this method
                            searchResults = databaseHelper.getAllAnswers(); // Temporary
                            break;
                        default:
                            searchResults = databaseHelper.getAllAnswers();
                            break;
                    }
                    
                    ObservableList<Answer> results = FXCollections.observableArrayList(searchResults);
                    answerTable.setItems(results);
                    
                } catch (SQLException e) {
                    showErrorAlert("Search Error", "Failed to search answers: " + e.getMessage());
                }
                return searchTerm + "|" + searchType;
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
    }
    
    /**
     * Toggles the accepted status of the selected answer.
     */
    private void toggleAcceptedStatus() {
        Answer selected = answerTable.getSelectionModel().getSelectedItem();
        // Checking for selected answer
        if (selected == null) { 
            showErrorAlert("Selection Error", "Please select an answer to toggle its accepted status.");
            return;
        }

        boolean currentStatus = selected.getIsAccepted();
        boolean newStatus = !currentStatus;
        // Confirmation on chosen answer
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Status Change");
        confirmation.setHeaderText("Change Accepted Status");
        confirmation.setContentText(
            "Do you want to mark this answer as " + 
            (newStatus ? "ACCEPTED?" : "NOT ACCEPTED?")
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setIsAccepted(newStatus); //Changes UI for Accepter to show yes/no
            try {
                if (databaseHelper.updateAnswer(selected)) {
                    showInfoAlert("Success", "Answer marked as " + 
                        (newStatus ? "ACCEPTED" : "NOT ACCEPTED") + " successfully!");
                    populateAnswerTable();
                } else {
                    showErrorAlert("Error", "Failed to update accepted status in database.");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update accepted status: " + e.getMessage());
            }
        } else {
            populateAnswerTable();
        }
    }
    
    /**
     * Sorts the answers so that accepted answers appear at the top of the table.
     */
    private void sortByAccepted() {
        ObservableList<Answer> currentAnswers = answerTable.getItems();

        if (currentAnswers == null || currentAnswers.isEmpty()) {
            showErrorAlert("No Data", "There are no answers to sort.");
            return;
        }

        // Sort by accepted answers first, then un-accepted
        FXCollections.sort(currentAnswers, (a1, a2) -> {
            boolean a1Accepted = a1.getIsAccepted();
            boolean a2Accepted = a2.getIsAccepted();
            // true comes before false
            return Boolean.compare(a2Accepted, a1Accepted);
        });

        answerTable.setItems(currentAnswers);

        showInfoAlert("Sorted", "Answers sorted with accepted ones first.");
    }
    
    /**
     * Reloads the trusted reviewer list and their weights from the database into local cache.
     */
    private void loadTrustedFromDb() {
    	try {
    		trusted.clear();
    		trusted.putAll(databaseHelper.getTrustedReviewers(currentUser.getUserName()));
    	} catch(SQLException ex) {
    		showErrorAlert("Trusted reviewers", "Failed to load trusted reviewers: " + ex.getMessage());
    	}
    }
    
    /**
     * Produces a curated list of answers for the given question, ranked based on:
     * reviewer trust weight, accepted status, and creation time.
     * 
     * @param questionId the ID of the question to curate answers for
     * @return a sorted list of curated answers
     * @throws SQLException if retrieval of answers or reviews fails
     */
    private List<Answer> curateForQuestion(String questionId) throws SQLException {
    	List<Answer> all = databaseHelper.getAnswersForQuestion(questionId);
    	Map<String, Double> scoreByAnswer = new HashMap<>();
    	for(Answer a : all) {
    		double score = trustedScoreForAnswer(a.getAnswerId());
    		if(score > 0.0) {
    			scoreByAnswer.put(a.getAnswerId(), score);
    		}
    	}
    	
    	List<Answer> filtered = all.stream().filter(a -> scoreByAnswer.containsKey(a.getAnswerId())).toList();
    	
    	List<Answer> sorted = new ArrayList<>(filtered);
    	sorted.sort((a, b) -> {
    		int acc = Boolean.compare(b.getIsAccepted(), a.getIsAccepted());
    		if(acc != 0) return acc;
    		
    		double sb = scoreByAnswer.getOrDefault(b.getAnswerId(), 0.0);
    		double sa = scoreByAnswer.getOrDefault(a.getAnswerId(), 0.0);
    		int sc = Double.compare(sb, sa);
    		if(sc != 0) return sc;
    		
    		return b.getCreatedAt().compareTo(a.getCreatedAt());
    	});
    	return sorted;
    }
    
    /**
     * Calculates the total trust score for an answer, based on the weight of
     * reviewers who have reviewed the answer.
     * 
     * @param answerId the ID of the answer being evaluated
     * @return the computed trust score
     * @throws SQLException if reviews cannot be retrieved
     */
    private double trustedScoreForAnswer(String answerId) throws SQLException {
    	List<Review> reviews = databaseHelper.getReviewsForAnswer(answerId);
    	
    	double total = 0.0;
    	for(Review r : reviews) {
    		String reviewerId = r.getReviewer();
    		Double w = trusted.get(reviewerId);
    		if(w != null) {
    			total += w;
    		}
    	}
    	return total;
    }
    
    /**
     * Checks for curated answers list when trusted reviewer weights have changed.
     * If a question was previously curated, the curated results are updated.
     * Otherwise, the user is told to run manually.
     */
    private void checkForTrustedUpdates() {
    	String qid = lastCuratedQuestionId;
    	if(qid == null) {
    		Answer sel = answerTable.getSelectionModel().getSelectedItem();
    		if(sel != null) qid = sel.getQuestionId();
    	}
    	
    	if(qid == null) {
    		showInfoAlert("Updates", "Trusted updated found. Select an answer or run Curate Now.");
    		return;
    	}
    	
    	try {
    		var curated = curateForQuestion(qid);
    		answerTable.setItems(FXCollections.observableArrayList(curated));
    		showInfoAlert("Updated", "Curated list refreshed for Question " + qid);
    	} catch(Exception ex) {
    		showErrorAlert("Refresh error", ex.getMessage());
    	}
    }
    /**
     * Runs the curation for the currently selected question or for a question ID
     * entered by the user. This filters and ranks answers based on:
     * <ul>
     * 	<li>Trusted reviewer presence</li>
     * 	<li>The assigned trust weight for each reviewer</li>
     * 	<li>Whether the answer is marked as accepted</li>
     * 	<li>How long ago it was created</li>
     * </ul>
     * The curated list is then displayed in the answer table.
     */
    private void curateNow() {
    	String qid = null;
    	Answer sel = answerTable.getSelectionModel().getSelectedItem();
    	if(sel != null) qid = sel.getQuestionId();
    	if(qid == null) {
    		TextInputDialog tid = new TextInputDialog();
    		tid.setTitle("Curate Answers");
    		tid.setHeaderText("Enter a Question ID to curate");
    		tid.setContentText("Question ID:");
    		var res = tid.showAndWait();
    		if(res.isEmpty() || res.get().trim().isEmpty()) {
    			showErrorAlert("Input", "Question ID is required");
    			return;
    		}
    		qid = res.get().trim();
    	}
    	
    	lastCuratedQuestionId = qid;
    	try {
    		var curated = curateForQuestion(qid);
    		answerTable.setItems(FXCollections.observableArrayList(curated));
    		showInfoAlert("Curated", "Showing curated answers for Question " + qid + " (answers with trusted reviews only, ranked).");
    	} catch(Exception ex) {
    		showErrorAlert("Curation Error", ex.getMessage());
    	}
    }
    
    /**
     * Opens the Trusted Reviewer Management dialog. This dialog allows the user to:
     * <ul>
     * 	<li>Add or update the trust weight assigned to a reviewer</li>
     * 	<li>Remove an existing trusted reviewer</li>
     * 	<li>Refresh the list of trusted reviewers from the database</li>
     * </ul>
     * Trusted weighting changes the answer curation and ranking.
     */
    private void openManageTrustedDialog() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Manage Trusted Reviewers");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Entry<String, Double>> tv = new TableView<>();
        var cId = new TableColumn<Entry<String, Double>, String>("Reviewer ID");
        cId.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getKey()));
        var cW = new TableColumn<Entry<String, Double>, String>("Weight");
        cW.setCellValueFactory(v -> new SimpleStringProperty(Double.toString(v.getValue().getValue())));
        tv.getColumns().addAll(cId, cW);

        TextField tfId = new TextField(); tfId.setPromptText("reviewerId");
        TextField tfW  = new TextField(); tfW.setPromptText("weight (e.g., 1.0)");

        Button btnAddUpdate = new Button("Trust/Update");
        btnAddUpdate.setOnAction(e -> {
            try {
                String id = tfId.getText() == null ? "" : tfId.getText().trim();
                if (id.isEmpty()) { showErrorAlert("Input", "Reviewer ID is required"); return; }
                double w = Double.parseDouble(tfW.getText().trim());
                if(databaseHelper.updateTrustedReviewer(currentUser.getUserName(), id, w)) {
                	trusted.put(id, w);
                	tv.setItems(FXCollections.observableArrayList(trusted.entrySet()));
                } else {
                	showErrorAlert("Trusted reviewers", "Unable to save trusted reviewer.");
                }
            } catch (Exception ex) {
                showErrorAlert("Invalid input", "Enter a numeric weight (e.g., 1.0)");
            }
        });

        Button btnRemove = new Button("Untrust");
        btnRemove.setOnAction(e -> {
            var sel = tv.getSelectionModel().getSelectedItem();
            if (sel != null) {
            	try {
            		if(databaseHelper.removeTrustedReviewer(currentUser.getUserName(), sel.getKey())) {
                        trusted.remove(sel.getKey());
                        tv.setItems(FXCollections.observableArrayList(trusted.entrySet()));
            		} else {
            			showErrorAlert("Trusted reviewers", "Unable to remove trusted reviewer.");
            		}
            	} catch(SQLException ex) {
            		showErrorAlert("Trusted reviewers", "Unable to remove trusted reviewer.");
            	}
            }
        });

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> {
        	loadTrustedFromDb();
            tv.setItems(FXCollections.observableArrayList(trusted.entrySet()));
        });

        tv.setItems(FXCollections.observableArrayList(trusted.entrySet()));
        VBox box = new VBox(10,
            new Label("Trusted Reviewers (with weights):"),
            tv,
            new HBox(8, tfId, tfW, btnAddUpdate, btnRemove, btnRefresh)
        );
        box.setPadding(new Insets(12));
        dlg.getDialogPane().setContent(box);
        dlg.showAndWait();
    }

    /**
     * Displays an error alert dialog with specified message.
     * 
     * @param title the title of the alert window
     * @param message the error message to display
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Displays informational alert with the specified message.
     * 
     * @param title the title of the alert window
     * @param message the message to display
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}