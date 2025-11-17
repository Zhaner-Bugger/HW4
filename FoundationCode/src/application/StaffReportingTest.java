package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import databasePart1.DatabaseHelper;

/**
 * Focused test suite for staff reporting and analytics functionality.
 * Tests User Story 3 in detail with various scenarios.
 * 
 * @author Your Name
 * @version 1.0
 */
public class StaffReportingTest {

    private static DatabaseHelper db;

    @BeforeAll
    static void setUpClass() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();
        
        // Clean ALL test data before starting
        try {
            db.executeUpdate("DELETE FROM content_flags");
        } catch (SQLException e) {
            System.out.println("Cleanup warning: " + e.getMessage());
        }
        
        // Create diverse test data for meaningful statistics
        setupComplexTestData();
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        cleanupComplexTestData();
        if (db != null) {
            db.closeConnection();
        }
    }

    /**
     * Creates a complex data set for testing statistics and trends.
     */
    private static void setupComplexTestData() throws SQLException {
        // Create multiple users of different roles
        for (int i = 1; i <= 5; i++) {
            User student = new User("reportStudent" + i, "Pass123!", 
                                   "student" + i + "@test.com", "Student " + i, "student");
            db.register(student);
            
            // Create questions
            Question q = new Question("REPORT_Q" + i, "Question " + i, 
                                     "Content for question " + i, 
                                     student.getUserName(), 
                                     new Timestamp(System.currentTimeMillis()));
            db.insertQuestion(q);
            
            // Create answers
            Answer a = new Answer("REPORT_A" + i, "REPORT_Q" + i, 
                                 "Answer content " + i, student.getUserName(), 
                                 new Timestamp(System.currentTimeMillis()), false);
            db.insertAnswer(a);
        }
        
        // Create some instructors
        User instructor = new User("reportInstructor", "Pass123!", 
                                  "inst@test.com", "Instructor", "instructor");
        db.register(instructor);
        
        // Create staff
        User staff = new User("reportStaff", "Pass123!", 
                             "staff@test.com", "Staff", "staff");
        db.register(staff);
    }

    /**
     * Cleans up complex test data.
     */
    private static void cleanupComplexTestData() {
        try {
            db.executeUpdate("DELETE FROM content_flags WHERE flaggedBy LIKE 'report%'");
            db.executeUpdate("DELETE FROM answer_reviews WHERE reviewerUserName LIKE 'report%'");
            db.executeUpdate("DELETE FROM answers WHERE author LIKE 'report%'");
            db.executeUpdate("DELETE FROM questions WHERE author LIKE 'report%'");
            db.executeUpdate("DELETE FROM UserRoles WHERE userName LIKE 'report%'");
            db.executeUpdate("DELETE FROM cse360users WHERE userName LIKE 'report%'");
        } catch (SQLException e) {
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }

    /**
     * Tests that statistics reflect accurate content counts.
     */
    @Test
    void testAccurateContentCounts() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        // We created 5 questions and 5 answers
        assertTrue(stats.get("totalQuestions") >= 5, 
                  "Should have at least 5 questions from test data");
        assertTrue(stats.get("totalAnswers") >= 5, 
                  "Should have at least 5 answers from test data");
    }

    /**
     * Tests that user role distribution is accurate.
     */
    @Test
    void testUserRoleDistribution() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        assertTrue(stats.get("users_student") >= 5, "Should have at least 5 students");
        assertTrue(stats.get("users_instructor") >= 1, "Should have at least 1 instructor");
        assertTrue(stats.get("users_staff") >= 1, "Should have at least 1 staff member");
    }

    /**
     * Tests that answer-to-question ratio can be calculated.
     */
    @Test
    void testAnswerToQuestionRatio() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        int questions = stats.get("totalQuestions");
        int answers = stats.get("totalAnswers");
        
        assertTrue(questions > 0, "Should have questions");
        assertTrue(answers > 0, "Should have answers");
        
        double ratio = (double) answers / questions;
        assertTrue(ratio >= 0.0, "Ratio should be non-negative");
    }

    /**
     * Tests that most active users are identified correctly with various activity levels.
     */
    @Test
    void testIdentifyMostActiveUsersWithVariedActivity() throws SQLException {
        // Create a very active user
        User superActive = new User("superActive", "Pass123!", 
                                   "super@test.com", "Super Active", "student");
        db.register(superActive);
        
        // Give them lots of activity
        for (int i = 0; i < 10; i++) {
            Question q = new Question("SUPER_Q" + i, "Question " + i, "Content", 
                                     superActive.getUserName(), 
                                     new Timestamp(System.currentTimeMillis()));
            db.insertQuestion(q);
        }
        
        List<Map<String, Object>> activeUsers = db.getMostActiveUsers(10);
        
        // Super active user should be first
        assertNotNull(activeUsers, "Active users list should not be null");
        assertTrue(activeUsers.size() > 0, "Should have active users");
        
        Map<String, Object> topUser = activeUsers.get(0);
        assertEquals("superActive", topUser.get("userName"), 
                    "Most active user should be superActive");
        
        Integer count = (Integer) topUser.get("activityCount");
        assertTrue(count >= 10, "Super active user should have at least 10 activities");
        
        // Cleanup
        db.executeUpdate("DELETE FROM questions WHERE author = 'superActive'");
        db.executeUpdate("DELETE FROM UserRoles WHERE userName = 'superActive'");
        db.executeUpdate("DELETE FROM cse360users WHERE userName = 'superActive'");
    }

    /**
     * Tests statistics when no flags exist.
     */
    @Test
    void testStatisticsWithNoFlags() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        assertTrue(stats.containsKey("pendingFlags"), "Should have pendingFlags key");
        // Value might be 0 if no flags exist, which is valid
        assertTrue(stats.get("pendingFlags") >= 0, "Pending flags should be non-negative");
    }

    /**
     * Tests that resolved questions reduce unresolved count.
     */
    @Test
    void testResolvedQuestionsImpactStatistics() throws SQLException {
        // Create a question and mark it resolved
        User testUser = new User("resolveTest", "Pass123!", 
                                "resolve@test.com", "Resolver", "student");
        db.register(testUser);
        
        Question q = new Question("RESOLVE_Q1", "To Be Resolved", "Content", 
                                 testUser.getUserName(), 
                                 new Timestamp(System.currentTimeMillis()));
        db.insertQuestion(q);
        
        Map<String, Integer> beforeStats = db.getContentStatistics();
        int unresolvedBefore = beforeStats.get("unresolvedQuestions");
        
        // Resolve the question
        db.updateQuestionResolved("RESOLVE_Q1", true);
        
        Map<String, Integer> afterStats = db.getContentStatistics();
        int unresolvedAfter = afterStats.get("unresolvedQuestions");
        
        assertTrue(unresolvedAfter < unresolvedBefore || unresolvedAfter == 0, 
                  "Unresolved count should decrease or stay at 0");
        
        // Cleanup
        db.executeUpdate("DELETE FROM questions WHERE questionId = 'RESOLVE_Q1'");
        db.executeUpdate("DELETE FROM UserRoles WHERE userName = 'resolveTest'");
        db.executeUpdate("DELETE FROM cse360users WHERE userName = 'resolveTest'");
    }

    /**
     * Tests that statistics handle edge case of no content.
     */
    @Test
    void testStatisticsWithMinimalContent() throws SQLException {
        // This tests that stats work even with minimal data
        Map<String, Integer> stats = db.getContentStatistics();
        
        // All values should be non-negative integers
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            assertTrue(entry.getValue() >= 0, 
                      entry.getKey() + " should be non-negative");
        }
    }

    /**
     * Tests that getMostActiveUsers handles case with no users.
     */
    @Test
    void testMostActiveUsersWithNoActivity() throws SQLException {
        // Create a user with no activity
        User inactive = new User("inactive", "Pass123!", 
                                "inactive@test.com", "Inactive", "student");
        db.register(inactive);
        
        List<Map<String, Object>> activeUsers = db.getMostActiveUsers(10);
        
        // Inactive user should not appear (or appear with 0 activity)
        Map<String, Object> inactiveUser = activeUsers.stream()
            .filter(u -> "inactive".equals(u.get("userName")))
            .findFirst()
            .orElse(null);
        
        // Either not in list or has 0 activity
        if (inactiveUser != null) {
            Integer count = (Integer) inactiveUser.get("activityCount");
            // If they appear, they should have 0 activity
            assertEquals(0, count, "Inactive user should have 0 activity");
        }
        
        // Cleanup
        db.executeUpdate("DELETE FROM UserRoles WHERE userName = 'inactive'");
        db.executeUpdate("DELETE FROM cse360users WHERE userName = 'inactive'");
    }

    /**
     * Tests that flag statistics update when flags are created and resolved.
     */
    @Test
    void testFlagStatisticsLifecycle() throws SQLException {
    	String uniqueStaffName = "flagStaff_" + System.currentTimeMillis();
        User flagStaff = new User(uniqueStaffName, "Pass123!", 
                                 "flagstaff@test.com", "Flag Staff", "staff");
        db.register(flagStaff);
        
        Map<String, Integer> initialStats = db.getContentStatistics();
        int initialPending = initialStats.get("pendingFlags");
        
        // Create a flag
        db.flagContent("question", "REPORT_Q1", 
                      flagStaff.getUserName(), "Test flag lifecycle");
        
        Map<String, Integer> afterFlagStats = db.getContentStatistics();
        int afterFlagPending = afterFlagStats.get("pendingFlags");
        
        assertEquals(initialPending + 1, afterFlagPending, 
                    "Pending flags should increase by 1");
        
        // Resolve the flag
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag lastFlag = flags.get(flags.size() - 1);
        db.updateFlagStatus(lastFlag.getFlagId(), "Resolved");
        
        Map<String, Integer> afterResolveStats = db.getContentStatistics();
        int afterResolvePending = afterResolveStats.get("pendingFlags");
        
        assertEquals(initialPending, afterResolvePending, 
                    "Pending flags should return to initial count after resolving");
        
        // Cleanup
        db.executeUpdate("DELETE FROM content_flags WHERE flaggedBy = '" + uniqueStaffName + "'");
        db.executeUpdate("DELETE FROM UserRoles WHERE userName = '" + uniqueStaffName +"'");
        db.executeUpdate("DELETE FROM cse360users WHERE userName = '" + uniqueStaffName + "'");
    }

    /**
     * Tests that activity counts are consistent across multiple queries.
     */
    @Test
    void testActivityCountConsistency() throws SQLException {
        List<Map<String, Object>> firstQuery = db.getMostActiveUsers(10);
        List<Map<String, Object>> secondQuery = db.getMostActiveUsers(10);
        
        assertEquals(firstQuery.size(), secondQuery.size(), 
                    "Consecutive queries should return same number of users");
        
        // Compare first user from both queries
        if (!firstQuery.isEmpty() && !secondQuery.isEmpty()) {
            assertEquals(firstQuery.get(0).get("userName"), 
                        secondQuery.get(0).get("userName"),
                        "Top user should be consistent");
            assertEquals(firstQuery.get(0).get("activityCount"), 
                        secondQuery.get(0).get("activityCount"),
                        "Activity count should be consistent");
        }
    }

    /**
     * Tests statistics performance with larger dataset.
     */
    @Test
    void testStatisticsPerformance() throws SQLException {
        long startTime = System.currentTimeMillis();
        
        Map<String, Integer> stats = db.getContentStatistics();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull(stats, "Statistics should be retrieved");
        assertTrue(duration < 5000, 
                  "Statistics query should complete in under 5 seconds");
    }

    /**
     * Tests that getMostActiveUsers performance is acceptable.
     */
    @Test
    void testMostActiveUsersPerformance() throws SQLException {
        long startTime = System.currentTimeMillis();
        
        List<Map<String, Object>> activeUsers = db.getMostActiveUsers(100);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull(activeUsers, "Active users should be retrieved");
        assertTrue(duration < 5000, 
                  "Active users query should complete in under 5 seconds");
    }
}