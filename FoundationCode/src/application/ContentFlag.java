package application;

import java.sql.Timestamp;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

/**
 * <p>
 * The ContentFlag class represents a flag placed on content by staff members for review.
 * This class is used to track inappropriate, concerning, or problematic content within the system
 * including questions, answers, reviews, and private messages.
 * </p>
 * 
 * <p>
 * Each flag contains information about what content was flagged, who flagged it, why it was flagged,
 * and the current status of the flag (Pending, Reviewed, or Resolved).
 * </p>
 * 
 * <p>
 * This class uses JavaFX properties to enable easy binding with UI components such as TableView.
 * </p>
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-16
 */
public class ContentFlag {
    /** Unique identifier for this flag */
    private final IntegerProperty flagId;
    
    /** Type of content being flagged (question, answer, message, review) */
    private final StringProperty contentType;
    
    /** ID of the specific content item being flagged */
    private final StringProperty contentId;
    
    /** Username of the staff member who created this flag */
    private final StringProperty flaggedBy;
    
    /** Explanation or reason for flagging the content */
    private final StringProperty reason;
    
    /** Current status of the flag (Pending, Reviewed, Resolved) */
    private final StringProperty status;
    
    /** Timestamp when the flag was created */
    private final StringProperty createdAt;
    
    /**
     * Constructs a new ContentFlag instance with all required information.
     * The timestamp is formatted to a human-readable string format (yyyy-MM-dd HH:mm).
     * 
     * @param flagId unique identifier for this flag
     * @param contentType type of content (question, answer, message, review)
     * @param contentId ID of the specific content being flagged
     * @param flaggedBy username of the staff member who flagged the content
     * @param reason explanation for why the content was flagged
     * @param status current status of the flag (Pending, Reviewed, Resolved)
     * @param createdAt timestamp when the flag was created
     */
    public ContentFlag(int flagId, String contentType, String contentId, String flaggedBy, 
                      String reason, String status, Timestamp createdAt) {
        this.flagId = new SimpleIntegerProperty(flagId);
        this.contentType = new SimpleStringProperty(contentType);
        this.contentId = new SimpleStringProperty(contentId);
        this.flaggedBy = new SimpleStringProperty(flaggedBy);
        this.reason = new SimpleStringProperty(reason);
        this.status = new SimpleStringProperty(status);
        
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = new SimpleStringProperty(
            createdAt.toLocalDateTime().format(formatter));
    }
    
    /**
     * Gets the unique flag identifier.
     * 
     * @return the flag ID
     */
    public int getFlagId() { return flagId.get(); }
    
    /**
     * Gets the type of content that was flagged.
     * 
     * @return the content type (question, answer, message, review)
     */
    public String getContentType() { return contentType.get(); }
    
    /**
     * Gets the ID of the specific content item that was flagged.
     * 
     * @return the content ID
     */
    public String getContentId() { return contentId.get(); }
    
    /**
     * Gets the username of the staff member who flagged this content.
     * 
     * @return the username who flagged the content
     */
    public String getFlaggedBy() { return flaggedBy.get(); }
    
    /**
     * Gets the reason or explanation for why the content was flagged.
     * 
     * @return the reason for flagging
     */
    public String getReason() { return reason.get(); }
    
    /**
     * Gets the current status of this flag.
     * 
     * @return the status (Pending, Reviewed, Resolved)
     */
    public String getStatus() { return status.get(); }
    
    /**
     * Gets the formatted creation timestamp.
     * 
     * @return the creation timestamp as a formatted string
     */
    public String getCreatedAt() { return createdAt.get(); }
    
    /**
     * Gets the flag ID property for JavaFX binding.
     * 
     * @return the flagId property
     */
    public IntegerProperty flagIdProperty() { return flagId; }
    
    /**
     * Gets the content type property for JavaFX binding.
     * 
     * @return the contentType property
     */
    public StringProperty contentTypeProperty() { return contentType; }
    
    /**
     * Gets the content ID property for JavaFX binding.
     * 
     * @return the contentId property
     */
    public StringProperty contentIdProperty() { return contentId; }
    
    /**
     * Gets the flaggedBy property for JavaFX binding.
     * 
     * @return the flaggedBy property
     */
    public StringProperty flaggedByProperty() { return flaggedBy; }
    
    /**
     * Gets the reason property for JavaFX binding.
     * 
     * @return the reason property
     */
    public StringProperty reasonProperty() { return reason; }
    
    /**
     * Gets the status property for JavaFX binding.
     * 
     * @return the status property
     */
    public StringProperty statusProperty() { return status; }
    
    /**
     * Gets the createdAt property for JavaFX binding.
     * 
     * @return the createdAt property
     */
    public StringProperty createdAtProperty() { return createdAt; }
    
    /**
     * Updates the status of this flag.
     * 
     * @param status the new status (Pending, Reviewed, Resolved)
     */
    public void setStatus(String status) { this.status.set(status); }
}