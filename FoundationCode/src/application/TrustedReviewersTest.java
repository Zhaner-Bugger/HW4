package application;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for trusted reviewers and basic answer/review flows used by AnswerManagementPage.
 *
 * <p>These tests focus on database used functions that AnswerManagementPage relies on:
 * creating users/questions/answers, adding reviews, and managing trusted reviewers.
 * Each test runs an isolated set of test rows and cleans up after complete.</p>
 *
 * <p>Author: Scott Bunning</p>
 */

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import org.junit.jupiter.api.*;

import databasePart1.DatabaseHelper;

class TrustedReviewersTest {

    private DatabaseHelper db;
    private User student;
    private User reviewer1;
    private User reviewer2;
    private String qid = "Q_curate";
    private String a1 = "A_cur_1";
    private String a2 = "A_cur_2";

    /**
     * Sets up the test environment before each test case.
     *
     * <p>Connects to the database then deletes old information that was in the database,
     * then sets up new information for the tests.</p>
     *
     * @throws SQLException if any database operation fails during setup
     */
    @BeforeEach
    void setup() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();

        db.executeUpdate("DELETE FROM answer_reviews WHERE answerId IN ('" + a1 + "','" + a2 + "')");
        db.executeUpdate("DELETE FROM answers WHERE answerId IN ('" + a1 + "','" + a2 + "')");
        db.executeUpdate("DELETE FROM questions WHERE questionId = '" + qid + "'");
        db.executeUpdate("DELETE FROM trusted_reviewers WHERE studentUserName IN ('studentX')");
        db.executeUpdate("DELETE FROM cse360users WHERE userName IN ('studentX','rev1','rev2')");

        // Create users
        db.executeUpdate("INSERT INTO cse360users (userName, password, name, email, role) " +
                "VALUES ('studentX','p','Student X','s@example.com','user')");
        db.executeUpdate("INSERT INTO cse360users (userName, password, name, email, role) " +
                "VALUES ('rev1','p','Reviewer One','r1@example.com','reviewer')");
        db.executeUpdate("INSERT INTO cse360users (userName, password, name, email, role) " +
                "VALUES ('rev2','p','Reviewer Two','r2@example.com','reviewer')");

        // Create a question and two answers
        db.executeUpdate("INSERT INTO questions (questionId, title, content, author, createdAt, isResolved) " +
                "VALUES ('" + qid + "','T','C','studentX', CURRENT_TIMESTAMP(), FALSE)");
        db.executeUpdate("INSERT INTO answers (answerId, questionId, content, author, createdAt, isAccepted, isRead) " +
                "VALUES ('" + a1 + "','" + qid + "','Answer 1','rev1', CURRENT_TIMESTAMP(), FALSE, FALSE)");
        db.executeUpdate("INSERT INTO answers (answerId, questionId, content, author, createdAt, isAccepted, isRead) " +
                "VALUES ('" + a2 + "','" + qid + "','Answer 2','rev2', CURRENT_TIMESTAMP(), FALSE, FALSE)");

        // Add reviews: rev1 reviewed a1; rev2 reviewed a2
        Review r1 = new Review("R1", a1, "rev1", "good", new Timestamp(System.currentTimeMillis()), "");
        Review r2 = new Review("R2", a2, "rev2", "better", new Timestamp(System.currentTimeMillis()), "");
        db.insertReview(r1);
        db.insertReview(r2);

        // Trust both reviewers with different weights
        db.updateTrustedReviewer("studentX", "rev1", 1.0);
        db.updateTrustedReviewer("studentX", "rev2", 2.0);

        // Prepare the "current user"
        student = new User("studentX", "p", "Student X", "s@example.com", "user");
        reviewer1 = new User("rev1", "p", "Reviewer One", "r1@example.com", "reviewer");
        reviewer2 = new User("rev2", "p", "Reviewer Two", "r2@example.com", "reviewer");
    }

    /**
     * Cleans up test data after each test case.
     *
     * <p>Removes all of the data from the temporary test user to make sure
     * there is isolation between each test.</p>
     *
     * @throws SQLException if any cleanup queries fail
     */
    @AfterEach
    void cleanup() throws SQLException {
        db.executeUpdate("DELETE FROM answer_reviews WHERE answerId IN ('" + a1 + "','" + a2 + "')");
        db.executeUpdate("DELETE FROM answers WHERE answerId IN ('" + a1 + "','" + a2 + "')");
        db.executeUpdate("DELETE FROM questions WHERE questionId = '" + qid + "'");
        db.executeUpdate("DELETE FROM trusted_reviewers WHERE studentUserName IN ('studentX')");
        db.executeUpdate("DELETE FROM cse360users WHERE userName IN ('studentX','rev1','rev2')");
    }

    /**
     * Uses reflection to use AnswerManagementPage.loadTrustedFromDb().
     *
     * <p>This helper allows tests to populate the trusted reviewer cache
     * inside AnswerManagementPage without changing the class's visibility.</p>
     *
     * @param page the page instance under test
     * @throws Exception if the method cannot be found or invoked, or if the target method throws
     */
    private void callLoadTrusted(AnswerManagementPage page) throws Exception {
        Method m = AnswerManagementPage.class.getDeclaredMethod("loadTrustedFromDb");
        m.setAccessible(true);
        m.invoke(page);
    }

    /**
     * Uses reflection to invoke the private method curateForQuestion(String) and return its results.
     *
     * <p>Convenience wrapper so tests can examine the curated list without exposing the
     * method publicly in production code.</p>
     *
     * @param page        the page instance under test
     * @param questionId  the question identifier whose answers should be curated
     * @return the curated list of answer objects for the given question
     * @throws Exception if the method cannot be found
     */
    @SuppressWarnings("unchecked")
    private List<Answer> callCurate(AnswerManagementPage page, String questionId) throws Exception {
        Method m = AnswerManagementPage.class.getDeclaredMethod("curateForQuestion", String.class);
        m.setAccessible(true);
        return (List<Answer>) m.invoke(page, questionId);
    }

    /**
     * Verifies trusted reviewer weights are created and updated correctly.
     *
     * <p>Given two trusted reviewers (rev1 weight 1.0, rev2 weight 2.0).</p>
     *
     * @throws Exception if database access fails during the test
     */
    @Test
    void testTrustedReviewerWeight() throws Exception {
        Map<String, Double> trusted = db.getTrustedReviewers("studentX");
        assertEquals(2, trusted.size());
        assertEquals(1.0, trusted.get("rev1"));
        assertEquals(2.0, trusted.get("rev2"));
    }

    /**
     * Ensures curation ranks answers according to the student's trusted reviewer weights.
     *
     * <p>Given rev2 has higher weight than rev1, when we curate for the question,
     * then the answer authored/reviewed by rev2 appears before rev1's answer.</p>
     *
     * @throws Exception if database access fail
     */
    @Test
    void testCurateRanksByTrustedWeight() throws Exception {
        AnswerManagementPage page = new AnswerManagementPage(db, student);
        callLoadTrusted(page);
        List<Answer> curated = callCurate(page, qid);

        assertEquals(2, curated.size(), "Both answers should be included");
        assertEquals(a2, curated.get(0).getAnswerId());
        assertEquals(a1, curated.get(1).getAnswerId());
    }

    /**
     * Confirms answers associated with no weight or untrusted reviewers are filtered out.
     *
     * <p>Given rev1 is removed from the student's trusted list, when we curate,
     * then only rev2's answer remains in the curated results.</p>
     *
     * @throws Exception if database access fail
     */
    @Test
    void testCurateFiltersOutNoWeightAnswers() throws Exception {
        db.removeTrustedReviewer("studentX", "rev1");

        AnswerManagementPage page = new AnswerManagementPage(db, student);
        callLoadTrusted(page);
        List<Answer> curated = callCurate(page, qid);

        assertEquals(1, curated.size(), "Only answers with >0 trusted score remain");
        assertEquals(a2, curated.get(0).getAnswerId());
    }

    /**
     * Verifies accepted answers are prioritized to the top of the curated list.
     *
     * <p>Given answer is marked accepted, when we curate for the question,
     * then appears first regardless of trusted weights.</p>
     *
     * @throws Exception if database access fail
     */
    @Test
    void testCuratePutsAcceptedFirst() throws Exception {
        db.executeUpdate("UPDATE answers SET isAccepted = TRUE WHERE answerId = '" + a1 + "'");

        AnswerManagementPage page = new AnswerManagementPage(db, student);
        callLoadTrusted(page);
        List<Answer> curated = callCurate(page, qid);

        assertEquals(2, curated.size());
        assertTrue(curated.get(0).getIsAccepted(), "First curated answer should be accepted");
        assertEquals(a1, curated.get(0).getAnswerId(), "Accepted answer should be first");
    }
}
