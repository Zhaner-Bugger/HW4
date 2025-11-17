package application;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.*; //cleaning up import JA

/**
 * Represents a review made by a reviewer on an answer.
 */

public class Review {
    private final StringProperty reviewId;
    private final StringProperty answerId;
    private final StringProperty reviewer;
    private final StringProperty content;
    private final StringProperty createdAt;
    private final StringProperty parentReviewID; //added by JA
    private final IntegerProperty feedbackCount;	//added by JA

    /**
     * Creates a Review instance.
     * @param reviewId unique review id
     * @param answerId associated answer id
     * @param reviewer reviewer's username
     * @param content review body
     * @param createdAt timestamp of creation
     * @param parentReviewID id of parent review (if any)
     */

    public Review(String reviewId, String answerId, String reviewer, String content, Timestamp createdAt, String parentReviewID) {
        this.reviewId = new SimpleStringProperty(reviewId);
        this.answerId = new SimpleStringProperty(answerId);
        this.reviewer = new SimpleStringProperty(reviewer);
        this.content = new SimpleStringProperty(content);
        this.parentReviewID = new SimpleStringProperty(parentReviewID);
        this.feedbackCount = new SimpleIntegerProperty(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = new SimpleStringProperty(createdAt.toLocalDateTime().format(formatter));
    }

    /** get reviewId
     * @return reviewId
     */
    public String getReviewId() { return reviewId.get(); }

    /** get answerId
     * @return answerId
     */
    public String getAnswerId() { return answerId.get(); }

    /** get reviewer
     * @return reviewer
     */
    public String getReviewer() { return reviewer.get(); }

    /** get content
     * @return content
     */
    public String getContent() { return content.get(); }

    /** get createdAt
     * @return createdAt
     */
    public String getCreatedAt() { return createdAt.get(); }

    /** get parentReviewID
     * @return parentReviewID
     */
    public String getParentReviewID() { return parentReviewID.get(); }

    /** get feedbackCount
     * @return feedbackCount
     */
    public int getFeedbackCount() { return feedbackCount.get(); }

    /**get review id property
     * @return reviewId property
     */
    public StringProperty reviewIdProperty() { return reviewId; }
    public StringProperty answerIdProperty() { return answerId; }
    public StringProperty reviewerProperty() { return reviewer; }
    public StringProperty contentProperty() { return content; }
    public StringProperty createdAtProperty() { return createdAt; }
    public IntegerProperty feedbackCountProperty() { return feedbackCount; } 

    public void setContent(String content) { this.content.set(content); }
    public void setFeedbackCount(int count) { this.feedbackCount.set(count); }
}