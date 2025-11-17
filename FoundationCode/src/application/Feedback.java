package application;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Represents feedback left by a student for a review.
 */
public class Feedback {
    private final String feedbackId;
    private final String reviewId;
    private final String studentUserName;
    private final int rating;
    private final String feedbackText;
    private final Timestamp createdAt;

    /**
     * Create a Feedback instance.
     * @param feedbackId unique id for the feedback
     * @param reviewId id of the review this feedback is about
     * @param studentUserName student who provided the feedback
     * @param rating numeric rating (0 if not used)
     * @param feedbackText optional free-text feedback
     * @param createdAt timestamp when feedback was created
     */
    public Feedback(String feedbackId, String reviewId, String studentUserName, int rating, String feedbackText, Timestamp createdAt) {
        this.feedbackId = feedbackId;
        this.reviewId = reviewId;
        this.studentUserName = studentUserName;
        this.rating = rating;
        this.feedbackText = feedbackText;
        this.createdAt = createdAt;
    }

    /** @return feedback id */
    public String getFeedbackId() { return feedbackId; }
    /** @return associated review id */
    public String getReviewId() { return reviewId; }
    /** @return student username who left feedback */
    public String getStudentUserName() { return studentUserName; }
    /** @return numeric rating */
    public int getRating() { return rating; }
    /** @return feedback text (may be null) */
    public String getFeedbackText() { return feedbackText; }
    /** @return creation timestamp */
    public Timestamp getCreatedAt() { return createdAt; }
    /** 
     * @return formatted timestamp 
     */
    public String getFormattedCreatedAt() {
    	 if (createdAt == null) return "";
    	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	    return formatter.format(createdAt);
    }

    @Override
    public String toString() {
        return String.format("%s (by %s) - %s", feedbackText == null ? "" : feedbackText, studentUserName, rating);
    }
}
