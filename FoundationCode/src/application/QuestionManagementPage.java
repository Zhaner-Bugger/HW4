package application;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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


public class QuestionManagementPage {
    /**
     * UI page for instructors to manage questions: create, edit, delete, search and view answers.
     */
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private TableView<Question> questionTable;
    
    public QuestionManagementPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Display the Question Management UI in the given primary stage.
     * @param primaryStage the JavaFX Stage where the UI will be shown
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Question Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Create question table
        questionTable = createQuestionTable();
        populateQuestionTable();
        
        //Drop down for filtering answered/unanswered (top-left corner)
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All","Answered","Unanswered");
        filterBox.setValue("All");
        filterBox.setPrefWidth(150);
        
        filterBox.setStyle("""
        		-fx-background-color: #f5f5f5;
        		-fx-border-color: #cccccc;
        		-fx-border-radius: 6;
        		-fx-background-radius: 6;
        		-fx-font-size: 14px;
        		-fx-padding: 4 8;
        		""");
        
        filterBox.setOnAction(e -> applyFilter(filterBox.getValue()));
        	
        
        
        
        
        // Buttons for CRUD operations
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Question");
        Button editButton = new Button("Edit Question");
        Button deleteButton = new Button("Delete Question");
        Button searchButton = new Button("Search Questions");
        Button messagesButton = new Button("Messages");
        Button refreshButton = new Button("Refresh");
        Button markResolvedButton = new Button("Mark Resolved");
        Button viewUnresolvedButton = new Button("View Unresolved");
        Button backButton = new Button("Back");
        Button followUpQuestion = new Button("Add Follow UP Question");
        Button viewAnswersButton = new Button("View Answers");
        
        addButton.setOnAction(e -> showAddQuestionDialog());
        editButton.setOnAction(e -> editSelectedQuestion());
        followUpQuestion.setOnAction(e -> createFollowUP());
        deleteButton.setOnAction(e -> deleteSelectedQuestion());
        searchButton.setOnAction(e -> showSearchDialog());
        messagesButton.setOnAction(e -> new UserMessagePage(databaseHelper, currentUser).show(primaryStage));
        markResolvedButton.setOnAction(e -> markQuestionAsResolved());
        refreshButton.setOnAction(e -> populateQuestionTable());
        viewUnresolvedButton.setOnAction(e -> showUnresolvedQuestions());
        viewAnswersButton.setOnAction(e -> showAnswersDialog());
        backButton.setOnAction(e -> {
            NavigationHelper.goToHomePage(currentUser.getActiveRole(), primaryStage, databaseHelper, currentUser);
        });
        
        buttonBox.getChildren().addAll(addButton, editButton, followUpQuestion, 
        		deleteButton, searchButton, refreshButton, messagesButton, filterBox, backButton, markResolvedButton, viewAnswersButton, viewUnresolvedButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        layout.getChildren().addAll(titleLabel, questionTable, buttonBox);
        Scene scene = new Scene(layout, 1200, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Management");
    }
    
    /**
     * Create and configure the TableView used to display questions.
     * @return configured Question TableView
     */
    private TableView<Question> createQuestionTable() {
        TableView<Question> table = new TableView<>();
        
        table.setRowFactory(tv -> {
            TableRow<Question> row = new TableRow<Question>() {
                @Override
                protected void updateItem(Question item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        System.out.println("DEBUG: Row contains - ID: " + item.getQuestionId() + ", Title: " + item.getTitle());
                    }
                }
            };
            return row;
        });
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        idCol.setMinWidth(80);
        
        TableColumn<Question, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setMinWidth(200);
        
        TableColumn<Question, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);
        
        TableColumn<Question, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMinWidth(100);
        
        TableColumn<Question, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        TableColumn<Question, String> resolvedCol = new TableColumn<>("Resolved");
        resolvedCol.setCellValueFactory(new PropertyValueFactory<>("isResolved"));
        resolvedCol.setMinWidth(100);
        
        TableColumn<Question, String> unreadCol = new TableColumn<>("Unread Answers");
        unreadCol.setCellValueFactory(new PropertyValueFactory<>("unreadAnswers"));
        unreadCol.setMinWidth(120);
        
        TableColumn<Question, String> follCol = new TableColumn<>("Follow UP");
        follCol.setCellValueFactory(new PropertyValueFactory<>("followUpOf"));
        follCol.setMinWidth(150);
        
        table.getColumns().addAll(idCol, titleCol, contentCol, authorCol, dateCol,resolvedCol, unreadCol, follCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }
    
    /**
     * Load questions from the database, refresh unread counts and answers state,
     * and populate the managed table view.
     */
    private void populateQuestionTable() {
        try {
            List<Question> questionsList = databaseHelper.getAllQuestions();
         // Update unread count for each question 
            for(Question q : questionsList) {
            	int unread = databaseHelper.countUnreadAnswers(q.getQuestionId(), currentUser.getUserName());
            	q.setUnreadAnswers(String.valueOf(unread));
            }
            
            
          
            //For each question check if there are answers
            for(Question q : questionsList) {
            	try {
            		List<Answer> answers = databaseHelper.getAnswersForQuestion(q.getQuestionId());
            		q.setAnswered(!answers.isEmpty());
            	
            	}catch (SQLException e) {
            		System.err.println("DEBUG: Failed to check answers for question " + q.getQuestionId());
            		q.setAnswered(false);
            	}
            }
            
            
            
            
            
            ObservableList<Question> questions = FXCollections.observableArrayList(questionsList);
            
            // Clear and reset the table items
            questionTable.getItems().clear();
            questionTable.setItems(questions);
            
            // Force table refresh
            questionTable.refresh();
            
            System.out.println("DEBUG: Table populated with " + questions.size() + " items");
            
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to load questions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show a dialog to add a new question. Validates input and inserts into DB.
     */
    private void showAddQuestionDialog() {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Add New Question");
        dialog.setHeaderText("Enter question details");
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Enter question title");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter question content");
        contentArea.setPrefRowCount(4);
        TextField tagsField = new TextField();
        tagsField.setPromptText("Enter tags (comma separated)");
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);
        grid.add(new Label("Tags:"), 0, 2);
        grid.add(tagsField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String validation = QuestionValidator.validateQuestion(titleField.getText(), contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }
                
                String questionId = "Q" + UUID.randomUUID().toString().substring(0, 8);
                Question question = new Question(
                	    questionId, 
                	    titleField.getText(), 
                	    contentArea.getText(), 
                	    currentUser.getUserName(), 
                	    new java.sql.Timestamp(System.currentTimeMillis()) // Use Timestamp instead of String
                	);
                
                // Add tags
                String[] tags = tagsField.getText().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        String tagValidation = QuestionValidator.validateTag(trimmedTag);
                        if (tagValidation.isEmpty()) {
                            question.addTag(trimmedTag);
                        }
                    }
                }
                
                try {
                    if (databaseHelper.insertQuestion(question)) {
                        return question;
                    } else {
                        showErrorAlert("Database Error", "Failed to insert question into database");
                        return null;
                    }
                } catch (SQLException ex) {
                    showErrorAlert("Database Error", "Failed to insert question: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            showInfoAlert("Success", "Question added successfully!");
            populateQuestionTable();
        });
    }
    
    /**
     * Open an edit dialog for the selected question and save updates to DB.
     */
    private void editSelectedQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select a question to edit.");
            return;
        }
        
        //Error if user does not own question
        if(!currentUser.getUserName().equals(selected.getAuthor())) {
        	showErrorAlert("Permission Denied", "You can only edit your own questions");
        	return;
        }
        
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Edit question details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField titleField = new TextField(selected.getTitle());
        TextArea contentArea = new TextArea(selected.getContent());
        contentArea.setPrefRowCount(4);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String validation = QuestionValidator.validateQuestion(titleField.getText(), contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }
                
                selected.setTitle(titleField.getText());
                selected.setContent(contentArea.getText());
                return selected;
            }
            return null;
        });
        
        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            try {
                if (databaseHelper.updateQuestion(question)) {
                    showInfoAlert("Success", "Question updated successfully!");
                    populateQuestionTable();
                } else {
                    showErrorAlert("Error", "Failed to update question in database");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update question: " + e.getMessage());
            }
        });
    }
    // Creates a follow up question 
    /**
     * Create a follow-up question for the currently selected question.
     */
    private void createFollowUP() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select a question to create a follow-up for.");
            return;
        }

        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Add Follow-Up Question");
        dialog.setHeaderText("Follow-up for: " + selected.getTitle());

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField titleField = new TextField();
        titleField.setPromptText("Enter follow-up title");

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter follow-up question content");
        contentArea.setPrefRowCount(4);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String validation = QuestionValidator.validateQuestion(titleField.getText(), contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }

                String questionId = "Q" + UUID.randomUUID().toString().substring(0, 8);

                Question followUp = new Question(
                    questionId,
                    titleField.getText(),
                    contentArea.getText(),
                    currentUser.getUserName(),
                    new java.sql.Timestamp(System.currentTimeMillis())
                );

                // Attach follow-up relationship
                followUp.setFollowUpOf(selected.getQuestionId()); // assuming followUpOf is a String or Integer

                try {
                    if (databaseHelper.insertQuestion(followUp)) {
                        return followUp;
                    } else {
                        showErrorAlert("Database Error", "Failed to insert follow-up question.");
                        return null;
                    }
                } catch (SQLException ex) {
                    showErrorAlert("Database Error", "Failed to insert follow-up: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(followUp -> {
            showInfoAlert("Success", "Follow-up question added successfully!");
            populateQuestionTable();
        });
    }
    
    /**
     * Delete the selected question (and its follow-ups) after confirmation.
     */
    private void deleteSelectedQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select a question to delete.");
            return;
        }
        //Error if user does not own question
        if(!currentUser.getUserName().equals(selected.getAuthor())) {
        	showErrorAlert("Permission Denied", "You can only delete your own questions");
        	return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Question");
        confirmation.setContentText("Are you sure you want to delete the question: " + selected.getTitle() + "?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (databaseHelper.deleteQuestion(selected.getQuestionId())) {
                    showInfoAlert("Success", "Question deleted successfully!");
                    populateQuestionTable();
                } else {
                    showErrorAlert("Error", "Failed to delete question from database");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete question: " + e.getMessage());
            }
        }
    }
    
    /**
     * Show a search dialog to filter questions by title/content/author.
     */
    private void showSearchDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Search Questions");
        dialog.setHeaderText("Enter search criteria");
        
        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search by title or content");
        ComboBox<String> searchTypeCombo = new ComboBox<>();  // Fixed variable name
        searchTypeCombo.getItems().addAll("Title", "Content", "Author", "All");
        searchTypeCombo.setValue("All");
        
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
                    List<Question> searchResults;
                    switch (searchType) {
                        case "Title":
                            searchResults = databaseHelper.searchQuestionsByTitle(searchTerm);
                            break;
                        case "Author":
                            searchResults = databaseHelper.searchQuestionsByAuthor(searchTerm);
                            break;
                        case "Content":
                            searchResults = databaseHelper.searchQuestionsByContent(searchTerm);
                            break;
                        default: // "All"
                            searchResults = databaseHelper.searchQuestionsByTitle(searchTerm);
                            break;
                    }
                    
                    ObservableList<Question> results = FXCollections.observableArrayList(searchResults);
                    questionTable.setItems(results);
                    
                } catch (SQLException e) {
                    showErrorAlert("Search Error", "Failed to search questions: " + e.getMessage());
                }
                return searchTerm + "|" + searchType;
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        // Search is already handled in the result converter
    }
 // Will mark a question as resolved
    /**
     * Mark the selected question as resolved (updates DB and UI).
     */
    private void markQuestionAsResolved() {
    	Question selected = questionTable.getSelectionModel().getSelectedItem();
    	if(selected == null) {
    		showErrorAlert("Selection Error", "Please select a question to mark as resolved.");
    		return;
    	}
    	
    	try {
    		selected.setIsResolved(true);
    		
    		if(databaseHelper.updateQuestionResolved(selected.getQuestionId(), true)) {
    			showInfoAlert("Success", "Question marked as resolved.");
    			populateQuestionTable();
    		} else {
    			showErrorAlert("Error", "Failed to update question status in database.");
    		}
    	} catch(Exception ex) {
    		showErrorAlert("Database error", "Error updating question: " +ex.getMessage());
    	}
    }
 // Show unresolved questions
    /**
     * Display only unresolved questions in the table view.
     */
    private void showUnresolvedQuestions() {
    	try {
    		List<Question> unresolved = databaseHelper.getUnresolvedQuestions();
    		questionTable.setItems(FXCollections.observableArrayList(unresolved));
    		
    		if(unresolved.isEmpty()) {
    			showInfoAlert("No Unresolved Questions", "All questions are resolved.");
    		}
    	} catch(Exception e) {
    		showErrorAlert("Error", "Failed to load unresolved question: " +e.getMessage());
    		e.printStackTrace();
    	}
    }
    
    // Show answers for question
    /**
     * Show a dialog listing answers for the selected question; marks them read.
     */
    private void showAnswersDialog() {
    	Question selected = questionTable.getSelectionModel().getSelectedItem();
    	if(selected == null) {
    		showErrorAlert("Selection Error", "Please select a question to view its answer.");
    		return;
    	}
    	
    	try {
    		List<Answer> answers = databaseHelper.getAnswersForQuestion(selected.getQuestionId());
    		
    		databaseHelper.markAnswersAsRead(selected.getQuestionId(), currentUser.getUserName());
    		
    		Dialog<Void> dialog = new Dialog<>();
    		dialog.setTitle("Answers for Questions");
    		dialog.setHeaderText("Question: " + selected.getTitle());
    		
    		VBox contentBox = new VBox(10);
    		contentBox.setPadding(new Insets(10));
    		
    		if(answers.isEmpty()) {
    			contentBox.getChildren().add(new Label("No answers have been submitted for this question."));
    		} else {
    			for(Answer a : answers) {
    				Label answerLabel = new Label(
    						"Author: "+ a.getAuthor() +
    						"\nAnswered: "+ a.getCreatedAt() +
    						"\nAccepted: " + (a.getIsAccepted() ? "Yes" : "No") +
    						"\nContent: \n" + a.getContent()
    					);
    					answerLabel.setStyle("-fx-padding: 8; -fx-border-color: grey; -fx-border-width: 0 0 1 0;");
    					contentBox.getChildren().add(answerLabel);
    			}
    		}
    		
    		dialog.getDialogPane().setContent(contentBox);
    		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    		
    		dialog.showAndWait();
    	} catch(Exception ex) {
    		showErrorAlert("Error", "Failed to load answers: "+ ex.getMessage());
    		ex.printStackTrace();
    	}
    }
    
    
    //filtering out questions
    /**
     * Apply a filter to the questions list (All/Answered/Unanswered).
     * @param filterType the name of the filter to apply
     */
    private void applyFilter(String filterType) {
    	try {
    
    	List<Question> allQuestions = databaseHelper.getAllQuestions();
    	for(Question q: allQuestions) {
    		List<Answer> answers = databaseHelper.getAnswersForQuestion(q.getQuestionId());
    		q.setAnswered(!answers.isEmpty());
    	}
    	
    	List<Question> filteredQuestions;
    	switch (filterType) {
    	case "Answered":
    		filteredQuestions = allQuestions.stream()
    			.filter(Question::isAnswered)
    			.collect(Collectors.toList());
    		break;
    	case "Unanswered" :
    		filteredQuestions = allQuestions.stream()
    				.filter(q -> !q.isAnswered())
    				.collect(Collectors.toList());
    		break;
    		default:
    			filteredQuestions = allQuestions;
    	}
    	questionTable.setItems(FXCollections.observableArrayList(filteredQuestions));
    	}catch (SQLException e) {
    		showErrorAlert("Database Error", "Failed to apply filter: " + e.getMessage());
    	}
    	
    }
    
    /**
     * Show an error alert dialog.
     * @param title the alert title
     * @param message the error message to display
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show an informational alert dialog.
     * @param title the alert title
     * @param message the message to display
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}