package application;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class representing a  question for display and passing to and from the database.
 */
public class Question {
    private final StringProperty questionId;
    private final StringProperty title;
    private final StringProperty content;
    private final StringProperty author;
    private final StringProperty createdAt;
    private final StringProperty followUpOf;
    private final StringProperty isResolved;
    private final StringProperty unreadAnswers = new SimpleStringProperty("0");
    private List<String> tags = new ArrayList<>();
    private boolean isAnswered;
    
    /**
     * Create a Question (display-oriented constructor).
     * @param questionId unique id
     * @param title question title
     * @param content question body
     * @param author author userName
     * @param createdAt formatted timestamp
     * @param followUpOf id of parent question (optional)
     * @param isResolved resolved flag value
     */
    public Question(String questionId, String title, String content, String author, String createdAt, String followUpOf, String isResolved) {
        this.questionId = new SimpleStringProperty(questionId);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.followUpOf = new SimpleStringProperty(followUpOf);
        this.isResolved = new SimpleStringProperty("No");
    }
       
    // New Questions with out follow up
    /**
     * Create a new Question (no follow-up).
     * @param questionId id
     * @param title title
     * @param content body
     * @param author author userName
     * @param createdAt creation timestamp
     */
    public Question(String questionId, String title, String content, String author, Timestamp createdAt) {
        this(questionId, title, content, author, createdAt, "");
    }
    
    // New constructor for database usage
    /**
     * Constructor used when loading from database.
     * @param questionId id
     * @param title title
     * @param content body
     * @param author author userName
     * @param createdAt timestamp from DB
     * @param followUpOf parent id or empty
     */
    public Question(String questionId, String title, String content, String author, Timestamp createdAt, String followUpOf) {
        this.questionId = new SimpleStringProperty(questionId);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        
        this.followUpOf = new SimpleStringProperty(followUpOf != null ? followUpOf : "");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = createdAt.toLocalDateTime().format(formatter);
        this.createdAt = new SimpleStringProperty(formattedDate);
		this.isResolved = new SimpleStringProperty("No");
    }
    //check if a question has been answered
    /** Returns true when the question has been marked as answered. */
   /** @return boolean whether answered */
   public boolean isAnswered() {
       return isAnswered;
   }
   //set a question to answered
   /**
    * Mark this question as answered or unanswered.
    * @param answered new answered state
    */
   public void setAnswered(boolean answered) {
       this.isAnswered = answered;
   }
    
    // Getters
    /** Returns the unique question identifier. */
    /** @return question id */
    public String getQuestionId() { return questionId.get(); }
    /** Returns the title of the question. */
    /** @return title */
    public String getTitle() { return title.get(); }
    /** Returns the content/body of the question. */
    /** @return content */
    public String getContent() { return content.get(); }
    /** Returns the author (userName) who posted the question. */
    /** @return author userName */
    public String getAuthor() { return author.get(); }
    /** Returns the formatted creation timestamp of the question. */
    /** @return formatted createdAt */
    public String getCreatedAt() { return createdAt.get(); }
    /** Returns true if the question has been marked resolved. */
    /** @return true when resolved */
    public boolean getIsResolved() { return "Yes".equals(isResolved.get()); }
    /** Returns the number of unread answers as a string. */
    /** @return unread answers count as string */
    public String getUnreadAnswers() { return unreadAnswers.get(); }
    /** Returns the list of tags associated with this question. */
    /** @return list of tags */
    public List<String> getTags() { return tags; }
    /** Returns the id of the parent question this is a follow-up of, or null. */
    /** @return parent question id or null */
    public String getFollowUpOf() {  
    	String val = followUpOf.get();
    	return (val == null || val.isBlank()) ? null : val;
    }
    
    // Property getters for TableView
    /** Property accessor for questionId (used by JavaFX tables). */
    /** @return questionId property */
    public StringProperty questionIdProperty() { return questionId; }
    /** Property accessor for title (used by JavaFX tables). */
    /** @return title property */
    public StringProperty titleProperty() { return title; }
    /** Property accessor for content (used by JavaFX tables). */
    /** @return content property */
    public StringProperty contentProperty() { return content; }
    /** Property accessor for author (used by JavaFX tables). */
    /** @return author property */
    public StringProperty authorProperty() { return author; }
    /** Property accessor for createdAt (used by JavaFX tables). */
    /** @return createdAt property */
    public StringProperty createdAtProperty() { return createdAt; }
    /** Property accessor for isResolved (used by JavaFX tables). */
    /** @return isResolved property */
    public StringProperty isResolvedProperty() { return isResolved; }
    /** Property accessor for unread answer count (used by JavaFX tables). */
    /** @return unreadAnswers property */
    public StringProperty unreadAnswersProperty() { return unreadAnswers; }
    
    // Setters
    /** Sets a new title for the question.
     * @param title new title
     */
    public void setTitle(String title) { this.title.set(title); }
    /** Sets new content/body for the question.
     * @param content new content
     */
    public void setContent(String content) { this.content.set(content); }
    /** Sets the follow-up question id (parent) for this question.
     * @param followUpOf parent id or empty
     */
    public void setFollowUpOf(String followUpOf) { 
    	this.followUpOf.set(followUpOf != null ? followUpOf : "");
    	}
    /** Add a tag to the question if not already present.
     * @param tag tag to add
     */
    public void addTag(String tag) { 
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag)) {
            tags.add(tag);
        }
    }
    /** Mark the question as resolved or unresolved.
     * @param resolved new resolved state
     */
    public void setIsResolved(boolean resolved) { this.isResolved.set(resolved ? "Yes" : "No"); }
    /** Sets the unread answers count display string.
     * @param count unread count string
     */
    public void setUnreadAnswers(String count) { this.unreadAnswers.set(count); }
    /** Removes a tag from the question if present.
     * @param tag tag to remove
     */
    public void removeTag(String tag) { tags.remove(tag); }
    /** Clears all tags from this question. */
    public void clearTags() { tags.clear(); }
}