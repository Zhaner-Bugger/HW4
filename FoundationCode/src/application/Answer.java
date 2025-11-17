// Updated Answer.java
package application;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class representing an answer to a question, to pass back and forth with the database and display.
 */
public class Answer {
    private final StringProperty answerId;
    private final StringProperty questionId;
    private final StringProperty content;
    private final StringProperty author;
    private final StringProperty createdAt;
    private final StringProperty isAccepted;
    private final StringProperty isRead;
    
    /**
     * Create an Answer for display (createdAt already formatted).
     * @param answerId unique answer id
     * @param questionId associated question id
     * @param content answer body
     * @param author author userName
     * @param createdAt formatted created timestamp
     * @param isAccepted whether answer is accepted
     */
    public Answer(String answerId, String questionId, String content, String author, String createdAt, boolean isAccepted) {
        this.answerId = new SimpleStringProperty(answerId);
        this.questionId = new SimpleStringProperty(questionId);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.isAccepted = new SimpleStringProperty(isAccepted ? "Yes" : "No");
        this.isRead = new SimpleStringProperty("No");
    }
    
    // New constructor for database usage
    /**
     * Create an Answer from database timestamp.
     * @param answerId unique id
     * @param questionId parent question id
     * @param content answer body
     * @param author author userName
     * @param createdAt DB timestamp
     * @param isAccepted whether accepted
     */
    public Answer(String answerId, String questionId, String content, String author, Timestamp createdAt, boolean isAccepted) {
        this.answerId = new SimpleStringProperty(answerId);
        this.questionId = new SimpleStringProperty(questionId);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        
        // Format timestamp for display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = createdAt.toLocalDateTime().format(formatter);
        this.createdAt = new SimpleStringProperty(formattedDate);
        
        this.isAccepted = new SimpleStringProperty(isAccepted ? "Yes" : "No");
        this.isRead = new SimpleStringProperty("No");
    }
    
    // Getters
    /** Returns the unique answer identifier. @return answer id */
    public String getAnswerId() { return answerId.get(); }
    /** Returns the question id this answer belongs to. @return question id */
    public String getQuestionId() { return questionId.get(); }
    /** Returns the content/body of the answer. @return content */
    public String getContent() { return content.get(); }
    /** Returns the author (userName) of the answer. @return author userName */
    public String getAuthor() { return author.get(); }
    /** Returns the formatted creation timestamp of the answer. @return createdAt formatted string */
    public String getCreatedAt() { return createdAt.get(); }
    /** Returns true if the answer has been accepted. @return true when accepted */
    public boolean getIsAccepted() { return "Yes".equals(isAccepted.get()); }
    /** Returns true if the answer has been marked read. @return true when read */
    public boolean getIsRead() { return "Yes".equals(isRead.get()); }
    
    // Property getters
    /** Property accessor for answerId (used by JavaFX tables). @return answerId property */
    public StringProperty answerIdProperty() { return answerId; }
    /** Property accessor for questionId (used by JavaFX tables). @return questionId property */
    public StringProperty questionIdProperty() { return questionId; }
    /** Property accessor for content (used by JavaFX tables). @return content property */
    public StringProperty contentProperty() { return content; }
    /** Property accessor for author (used by JavaFX tables). @return author property */
    public StringProperty authorProperty() { return author; }
    /** Property accessor for createdAt (used by JavaFX tables). @return createdAt property */
    public StringProperty createdAtProperty() { return createdAt; }
    /** Property accessor for isAccepted (used by JavaFX tables). @return isAccepted property */
    public StringProperty isAcceptedProperty() { return isAccepted; }
    /** Property accessor for isRead (used by JavaFX tables). @return isRead property */
    public StringProperty isReadProperty() { return isRead; }
    
    // Setters
    /** Sets new content for the answer.
     * @param content new content string
     */
    public void setContent(String content) { this.content.set(content); }
    /** Marks the answer as accepted or not.
     * @param isAccepted new accepted state
     */
    public void setIsAccepted(boolean isAccepted) { 
        this.isAccepted.set(isAccepted ? "Yes" : "No"); 
    }
    /** Marks the answer as read or unread.
     * @param isRead new read state
     */
    public void setIsRead(boolean isRead) {
    	this.isRead.set(isRead ? "Yes": "No");
    }
}