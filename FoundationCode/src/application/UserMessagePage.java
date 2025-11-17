package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

/**
 * UserMessagePage
 * - Lists recent questions
 * - Students can send a private message to the question author about a question
 * - Question authors see unread counts and can open/read/reply to private message threads
 */
public class UserMessagePage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    /**
     * Construct a UserMessagePage for the given user.
     * @param databaseHelper database helper for messaging operations
     * @param currentUser current logged-in user
     */
    public UserMessagePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Show the Messages & Questions UI.
     * @param primaryStage stage to display the UI on
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(16));
        layout.setStyle("-fx-alignment: top-center;");

        Label title = new Label("Questions & Private Messages");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<Question> questionList = new ListView<>();
        questionList.setPrefHeight(360);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        Button refreshBtn = new Button("Refresh");
        Button backBtn = new Button("Back");
        controls.getChildren().addAll(refreshBtn, backBtn);

        layout.getChildren().addAll(title, questionList, controls);

        refreshBtn.setOnAction(e -> loadQuestions(questionList));
        backBtn.setOnAction(e -> NavigationHelper.goToHomePage(currentUser.getRole(), primaryStage, databaseHelper, currentUser));

        // Double click to open message thread or send feedback
        questionList.setCellFactory(lv -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String display = String.format("%s - %s (by %s)", item.getQuestionId(), item.getTitle(), item.getAuthor());
                    // If current user is the author show unread count
                    if (currentUser.getUserName().equals(item.getAuthor())) {
                        try {
                            int unread = databaseHelper.getUnreadCountForQuestion(item.getQuestionId(), currentUser.getUserName());
                            if (unread > 0) display += String.format("   [unread: %d]", unread);
                        } catch (SQLException ex) {
                            // ignore display unread on error
                        }
                    }
                    setText(display);
                }
            }
        });

        questionList.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                Question q = questionList.getSelectionModel().getSelectedItem();
                if (q == null) return;

                // If current user is the author, open the thread to read/reply
                if (currentUser.getUserName().equals(q.getAuthor())) {
                    openThreadForAuthor(primaryStage, q);
                } else {
                    // Non-author: open a participant thread view showing messages they sent and messages sent to them
                    openParticipantThread(primaryStage, q);
                }
            }
        });

        loadQuestions(questionList);

        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Questions & Private Messages");
        primaryStage.show();
    }

    private void loadQuestions(ListView<Question> questionList) {
        /**
         * Load questions for the message page and populate the provided ListView.
         * @param questionList the ListView to populate
         */
        try {
            List<Question> questions = databaseHelper.getAllQuestions();
            ObservableList<Question> obs = FXCollections.observableArrayList(questions);
            questionList.setItems(obs);
        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to load questions: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }

    /**
     * Open a dialog to compose a private message about a question.
     * @param primaryStage the parent stage
     * @param q the Question the message refers to
     */
    private void openComposeDialog(Stage primaryStage, Question q) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Send Private Feedback");
        dialog.setHeaderText("Send feedback to " + q.getAuthor() + " about: " + q.getTitle());
        ButtonType sendBtn = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendBtn, ButtonType.CANCEL);

        TextArea content = new TextArea();
        content.setPromptText("Write helpful, constructive feedback...");
        content.setPrefRowCount(6);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == sendBtn) return content.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(msg -> {
            if (msg.trim().isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Message cannot be empty", ButtonType.OK);
                a.showAndWait();
                return;
            }
            try {
                boolean ok = databaseHelper.insertPrivateMessage(q.getQuestionId(), currentUser.getUserName(), q.getAuthor(), msg.trim());
                if (ok) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Message sent.", ButtonType.OK);
                    a.showAndWait();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to send message.", ButtonType.OK);
                    a.showAndWait();
                }
            } catch (SQLException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });
    }

    /**
     * Open a dialog for the author of a question to view and reply to private messages.
     * @param primaryStage parent stage
     * @param q the question whose message thread is opened
     */
    private void openThreadForAuthor(Stage primaryStage, Question q) {
        Stage dialogStage = new Stage();
        VBox v = new VBox(8);
        v.setPadding(new Insets(12));

        Label header = new Label("Messages about: " + q.getTitle());
        ListView<PrivateMessage> threadList = new ListView<>();
        threadList.setCellFactory(lv -> new ListCell<PrivateMessage>() {
            @Override
            protected void updateItem(PrivateMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%s -> %s: %s", item.getFromUser(), item.getToUser(), item.getContent()));
            }
        });

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Reply to the sender...");
        replyArea.setPrefRowCount(4);

        Button markReadBtn = new Button("Mark all read");
        Button sendReplyBtn = new Button("Send Reply");

        HBox btns = new HBox(8, markReadBtn, sendReplyBtn);

        v.getChildren().addAll(header, threadList, replyArea, btns);

        // load thread
        try {
            List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(q.getQuestionId(), currentUser.getUserName());
            threadList.setItems(FXCollections.observableArrayList(msgs));
        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to load messages: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }

        markReadBtn.setOnAction(evt -> {
            try {
                databaseHelper.markMessagesRead(q.getQuestionId(), currentUser.getUserName());
                // refresh
                List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(q.getQuestionId(), currentUser.getUserName());
                threadList.setItems(FXCollections.observableArrayList(msgs));
            } catch (SQLException e) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Failed to mark read: " + e.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        sendReplyBtn.setOnAction(evt -> {
            String reply = replyArea.getText();
            if (reply.trim().isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Reply cannot be empty", ButtonType.OK);
                a.showAndWait();
                return;
            }
            // Send reply to the original askers (this is simple: send to the last fromUser if available)
            PrivateMessage target = threadList.getSelectionModel().getSelectedItem();
            String toUser = (target != null) ? target.getFromUser() : null;
            if (toUser == null) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Select a message to reply to, or reply will not be sent.", ButtonType.OK);
                a.showAndWait();
                return;
            }
            try {
                boolean ok = databaseHelper.insertPrivateMessage(q.getQuestionId(), currentUser.getUserName(), toUser, reply.trim());
                if (ok) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Reply sent.", ButtonType.OK);
                    a.showAndWait();
                    // refresh thread and clear reply
                    List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(q.getQuestionId(), currentUser.getUserName());
                    threadList.setItems(FXCollections.observableArrayList(msgs));
                    replyArea.clear();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to send reply.", ButtonType.OK);
                    a.showAndWait();
                }
            } catch (SQLException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        Scene s = new Scene(v, 700, 500);
        dialogStage.setScene(s);
        dialogStage.setTitle("Message Thread - " + q.getQuestionId());
        dialogStage.show();
    }

    // For non-authors: show messages where current user is either sender or recipient for this question
    /**
     * Open a dialog for a participant (non-author) to view/send messages for a question.
     * @param primaryStage parent stage
     * @param q the question to view messages for
     */
    private void openParticipantThread(Stage primaryStage, Question q) {
        Stage dialogStage = new Stage();
        VBox v = new VBox(8);
        v.setPadding(new Insets(12));

        Label header = new Label("Your messages about: " + q.getTitle());
        ListView<PrivateMessage> threadList = new ListView<>();
        threadList.setCellFactory(lv -> new ListCell<PrivateMessage>() {
            @Override
            protected void updateItem(PrivateMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%s -> %s: %s", item.getFromUser(), item.getToUser(), item.getContent()));
            }
        });

        TextArea composeArea = new TextArea();
        composeArea.setPromptText("Write a private message to the question author or reply to a selected message...");
        composeArea.setPrefRowCount(4);

        Button markReadBtn = new Button("Mark my messages read");
        Button sendBtn = new Button("Send to Author / Reply");
        HBox btns = new HBox(8, markReadBtn, sendBtn);

        v.getChildren().addAll(header, threadList, composeArea, btns);

        // load messages for this participant
        try {
            List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(q.getQuestionId(), currentUser.getUserName());
            threadList.setItems(FXCollections.observableArrayList(msgs));
        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to load messages: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }

        markReadBtn.setOnAction(evt -> {
            try {
                databaseHelper.markMessagesRead(q.getQuestionId(), currentUser.getUserName());
                List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(q.getQuestionId(), currentUser.getUserName());
                threadList.setItems(FXCollections.observableArrayList(msgs));
            } catch (SQLException e) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Failed to mark read: " + e.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        sendBtn.setOnAction(evt -> {
            String text = composeArea.getText();
            if (text == null || text.trim().isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Message cannot be empty", ButtonType.OK);
                a.showAndWait();
                return;
            }
            // If a message is selected, reply to its sender; otherwise send to question author
            PrivateMessage sel = threadList.getSelectionModel().getSelectedItem();
            String toUser = (sel != null) ? sel.getFromUser() : q.getAuthor();
            try {
                boolean ok = databaseHelper.insertPrivateMessage(q.getQuestionId(), currentUser.getUserName(), toUser, text.trim());
                if (ok) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Message sent.", ButtonType.OK);
                    a.showAndWait();
                    List<PrivateMessage> msgs = databaseHelper.getMessagesForQuestion(q.getQuestionId(), currentUser.getUserName());
                    threadList.setItems(FXCollections.observableArrayList(msgs));
                    composeArea.clear();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to send message.", ButtonType.OK);
                    a.showAndWait();
                }
            } catch (SQLException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        Scene s = new Scene(v, 700, 500);
        dialogStage.setScene(s);
        dialogStage.setTitle("My Messages - " + q.getQuestionId());
        dialogStage.show();
    }
}
