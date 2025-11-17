package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import databasePart1.DatabaseHelper;

/**
 * Tests covering the student-facing reviewer features implemented:
 * - Sending private feedback to a reviewer (stored as PrivateMessage)
 * - Adding a reviewer to the student's trusted reviewers list (in-memory User list)
 * - Searching reviewers (via DatabaseHelper.getAllReviewerProfiles + simple filtering)
 */
public class ReviewerFeatureTests {

    private static DatabaseHelper db;

    @BeforeAll
    static void setup() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();
        // keep database content but clear tables used by these tests to avoid collisions
        try {
            db.executeUpdate("DELETE FROM PrivateMessages");
            db.executeUpdate("DELETE FROM reviewer_profiles");
        } catch (SQLException e) {
            // ignore cleanup errors but print for debugging
            e.printStackTrace();
        }
    }

    @AfterAll
    static void teardown() {
        if (db != null) db.closeConnection();
    }

    @Test
    void testStudentCanSendPrivateFeedback() throws SQLException {
        String student = "stu_" + UUID.randomUUID().toString().substring(0, 8);
        String reviewer = "rev_" + UUID.randomUUID().toString().substring(0, 8);

        // create users
        application.User s = new application.User(student, "pass", student + "@example.com", "Student", "student");
        db.register(s);

        application.User r = new application.User(reviewer, "pass", reviewer + "@example.com", "Reviewer", "reviewer");
        db.register(r);

        int reviewerId = db.getUserIdByUsername(reviewer);
        // create reviewer profile row
        db.createReviewerProfile(reviewerId, reviewer);

        String qid = "REVFB:" + reviewerId;
        boolean inserted = db.insertPrivateMessage(qid, student, reviewer, "Thanks for your reviews!");
        assertTrue(inserted, "insertPrivateMessage should return true when inserting a new private feedback message");

        List<application.PrivateMessage> msgs = db.getMessagesForQuestion(qid, reviewer);
        assertFalse(msgs.isEmpty(), "getMessagesForQuestion should return at least one message");
        application.PrivateMessage pm = msgs.get(msgs.size() - 1);
        assertEquals("Thanks for your reviews!", pm.getContent());
        assertEquals(student, pm.getFromUser());
        assertEquals(reviewer, pm.getToUser());
    }

    @Test
    void testStudentCanAddTrustedReviewer() {
        application.User student = new application.User("student_local", "pw", "s_local@example.com", "Student Local", "student");
        int reviewerId = 99999;

        boolean firstAdd = student.addTrustedReviewer(reviewerId);
        assertTrue(firstAdd, "First addTrustedReviewer should return true");

        boolean secondAdd = student.addTrustedReviewer(reviewerId);
        assertFalse(secondAdd, "Adding the same reviewer twice should return false");

        assertTrue(student.getTrustedReviewerIds().contains(reviewerId), "Trusted reviewer list should contain the added id");
    }

    @Test
    void testStudentCanSearchReviewers() throws SQLException {
        // create a reviewer with a distinctive name
        String reviewerName = "SearchableReviewer_" + UUID.randomUUID().toString().substring(0, 6);
        String reviewerUser = "user_" + UUID.randomUUID().toString().substring(0, 6);

        application.User r = new application.User(reviewerUser, "pw", reviewerUser + "@ex.com", reviewerName, "reviewer");
        db.register(r);
        int rid = db.getUserIdByUsername(reviewerUser);
        db.createReviewerProfile(rid, reviewerName);

        List<application.ReviewerProfile> all = db.getAllReviewerProfiles();
        // perform same filtering used by the UI (case-insensitive substring)
        String q = reviewerName.substring(0, Math.min(6, reviewerName.length()));
        boolean found = all.stream().anyMatch(p -> p.getName() != null && p.getName().toLowerCase().contains(q.toLowerCase()));

        assertTrue(found, "Search should find the reviewer profile by substring");
    }

}
