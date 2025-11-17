package application;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;
import databasePart1.DatabaseHelper;

/**
 * JUnit tests for verifying the Reviewer Management features implemented in Phase 3.
 * This class tests database operations related to listing reviews, counting feedback,
 * versioning updates, and deletion.
 * 
 * <p> Author: Joel Arizmendi
 */
class ReviewManagementTest {

    private DatabaseHelper db;
    private final String TEST_USER = "junit_reviewer";
    private final String TEST_ANSWER_ID = "ans_junit_1";

    /**
     * Sets up the test environment before each test execution.
     * Establishes a database connection, cleans any potential stale data,
     * and inserts the necessary prerequisite data (user, question, answer).
     *
     * @throws Exception if database connection or insertion fails.
     */
    @BeforeEach
    void setUp() throws Exception {
        db = new DatabaseHelper();
        db.connectToDatabase();

        db.register(new User(TEST_USER, "password", "reviewer", "", ""));
        db.executeUpdate("INSERT INTO questions (questionId, title, content, author) VALUES ('q_junit', 'Title', 'Content', '" + TEST_USER + "')");
        db.executeUpdate("INSERT INTO answers (answerId, questionId, content, author) VALUES ('" + TEST_ANSWER_ID + "', 'q_junit', 'Ans Content', '" + TEST_USER + "')");
    }

    /**
     * Cleans up the test environment after each test execution.
     * Removes all data inserted during the test to ensure isolation.
     *
     * @throws Exception if database deletion operations fail.
     */
    @AfterEach
    void tearDown() throws Exception {
        db.executeUpdate("DELETE FROM PrivateMessages WHERE questionId LIKE 'REV:%'");
        db.executeUpdate("DELETE FROM answer_reviews WHERE reviewerUserName = '" + TEST_USER + "'");
        db.executeUpdate("DELETE FROM answers WHERE answerId = '" + TEST_ANSWER_ID + "'");
        db.executeUpdate("DELETE FROM questions WHERE questionId = 'q_junit'");
        db.executeUpdate("DELETE FROM cse360users WHERE userName = '" + TEST_USER + "'");
        db.closeConnection();
    }

    /**
     * Verifies that reviews authored by a specific reviewer can be correctly retrieved.
     *
     * @throws SQLException if a database access error occurs.
     */
    @Test
    void testGetReviewsByReviewer() throws SQLException {
        db.insertReview(new Review("rev1", TEST_ANSWER_ID, TEST_USER, "Review 1", new Timestamp(System.currentTimeMillis()), null));
        db.insertReview(new Review("rev2", TEST_ANSWER_ID, TEST_USER, "Review 2", new Timestamp(System.currentTimeMillis()), null));

        List<Review> reviews = db.getReviewsByReviewer(TEST_USER);

        assertEquals(2, reviews.size(), "Should retrieve exactly 2 reviews for the user.");
    }

    /**
     * Verifies that the number of private feedback messages linked to a specific review
     * is counted correctly.
     *
     * @throws SQLException if a database access error occurs.
     */
    @Test
    void testGetFeedbackCountForReview() throws SQLException {
        String reviewId = "rev_feedback_test";
        db.insertReview(new Review(reviewId, TEST_ANSWER_ID, TEST_USER, "Content", new Timestamp(System.currentTimeMillis()), null));

        String feedbackSql = "INSERT INTO PrivateMessages (questionId, fromUser, toUser, content) VALUES ('REV:" + reviewId + "', 'student', '" + TEST_USER + "', 'msg')";
        db.executeUpdate(feedbackSql);
        db.executeUpdate(feedbackSql);
        db.executeUpdate(feedbackSql);

        int count = db.getFeedbackCountForReview(reviewId);

        assertEquals(3, count, "Feedback count should be 3.");
    }

    /**
     * Verifies that updating a review creates a new review entry that is correctly
     * linked to the original review via the parentReviewID.
     *
     * @throws SQLException if a database access error occurs.
     */
    @Test
    void testUpdateReviewVersioning() throws SQLException {
        String originalId = "rev_v1";
        String newId = "rev_v2";

        Review original = new Review(originalId, TEST_ANSWER_ID, TEST_USER, "Version 1", new Timestamp(System.currentTimeMillis()), null);
        db.insertReview(original);

        Review updated = new Review(newId, TEST_ANSWER_ID, TEST_USER, "Version 2", new Timestamp(System.currentTimeMillis()), originalId);
        db.insertReview(updated);

        List<Review> reviews = db.getReviewsByReviewer(TEST_USER);
        assertEquals(2, reviews.size(), "Both versions should exist in the database.");
        
        Review fetchedNewReview = reviews.stream().filter(r -> r.getReviewId().equals(newId)).findFirst().orElse(null);
        assertNotNull(fetchedNewReview);
        assertEquals(originalId, fetchedNewReview.getParentReviewID(), "New review should be linked to the original.");
    }

    /**
     * Verifies that a review can be successfully deleted from the database.
     *
     * @throws SQLException if a database access error occurs.
     */
    @Test
    void testDeleteReview() throws SQLException {
        String reviewId = "rev_to_delete";
        db.insertReview(new Review(reviewId, TEST_ANSWER_ID, TEST_USER, "Delete me", new Timestamp(System.currentTimeMillis()), null));

        boolean deleted = db.deleteReview(reviewId);

        assertTrue(deleted, "Delete operation should return true.");
        List<Review> reviews = db.getReviewsByReviewer(TEST_USER);
        assertTrue(reviews.isEmpty(), "Review list should be empty after deletion.");
    }
}