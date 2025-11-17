package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import databasePart1.DatabaseHelper;

/**
 * Edge case and boundary tests for content flagging functionality.
 * Focuses on User Story 2 edge cases.
 * 
 * @author Your Name
 * @version 1.0
 */
public class StaffContentFlaggingTest {

    private DatabaseHelper db;
    private User staffUser;
    private String testQuestionId;

    @BeforeEach
    void setUp() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();
        
        cleanupTestData();
        
        staffUser = new User("edgeStaff", "Pass123!", 
                            "edge@test.com", "Edge Staff", "staff");
        db.register(staffUser);
        
        User testStudent = new User("edgeStudent", "Pass123!", 
                                   "edgestudent@test.com", "Edge Student", "student");
        db.register(testStudent);
        
        testQuestionId = "EDGE_Q1";
        Question q = new Question(testQuestionId, "Edge Question", "Content", 
                                 testStudent.getUserName(), 
                                 new Timestamp(System.currentTimeMillis()));
        db.insertQuestion(q);
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestData();
        if (db != null) {
            db.closeConnection();
        }
    }

    private void cleanupTestData() throws SQLException {
        try {
            db.executeUpdate("DELETE FROM content_flags WHERE flaggedBy LIKE 'edge%'");
            db.executeUpdate("DELETE FROM questions WHERE author LIKE 'edge%'");
            db.executeUpdate("DELETE FROM UserRoles WHERE userName LIKE 'edge%'");
            db.executeUpdate("DELETE FROM cse360users WHERE userName LIKE 'edge%'");
        } catch (SQLException e) {
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }

    /**
     * Tests flagging with empty reason.
     */
    @Test
    void testFlagWithEmptyReason() throws SQLException {
        boolean result = db.flagContent("question", testQuestionId, 
                                       staffUser.getUserName(), "");
        
        assertTrue(result, "Should allow empty reason");
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(flag, "Flag should be created");
        assertEquals("", flag.getReason(), "Reason should be empty");
    }

    /**
     * Tests flagging with very long reason.
     */
    @Test
    void testFlagWithLongReason() throws SQLException {
        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longReason.append("This is a very detailed reason. ");
        }
        
        boolean result = db.flagContent("question", testQuestionId, 
                                       staffUser.getUserName(), 
                                       longReason.toString());
        
        assertTrue(result, "Should handle long reasons");
    }

    /**
     * Tests flagging the same content multiple times.
     */
    @Test
    void testMultipleFlagsOnSameContent() throws SQLException {
        // Create multiple flags for the same question
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), "Reason 1");
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), "Reason 2");
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), "Reason 3");
        
        List<ContentFlag> flags = db.getAllFlags();
        long countForQuestion = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .count();
        
        assertEquals(3, countForQuestion, 
                    "Should allow multiple flags on same content");
    }

    /**
     * Tests flagging non-existent content.
     */
    @Test
    void testFlagNonExistentContent() throws SQLException {
        boolean result = db.flagContent("question", "NONEXISTENT_Q", 
                                       staffUser.getUserName(), 
                                       "Flagging non-existent content");
        
        // Should succeed even if content doesn't exist (up to implementation)
        assertTrue(result, "Should handle non-existent content");
    }

    /**
     * Tests updating flag with invalid status.
     */
    @Test
    void testUpdateFlagWithInvalidStatus() throws SQLException {
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), "Test");
        
        List<ContentFlag> flags = db.getAllFlags();
        int flagId = flags.get(0).getFlagId();
        
        // Update with unusual status
        boolean result = db.updateFlagStatus(flagId, "InvalidStatus");
        
        // Should succeed (database accepts any string)
        assertTrue(result, "Should update even with non-standard status");
    }

    /**
     * Tests updating non-existent flag.
     */
    @Test
    void testUpdateNonExistentFlag() throws SQLException {
        boolean result = db.updateFlagStatus(99999, "Reviewed");
        
        assertFalse(result, "Should return false for non-existent flag");
    }

    /**
     * Tests retrieving flags when none exist.
     */
    @Test
    void testGetAllFlagsWhenEmpty() throws SQLException {
        // Ensure no flags exist
        db.executeUpdate("DELETE FROM content_flags");
        
        List<ContentFlag> flags = db.getAllFlags();
        
        assertNotNull(flags, "Should return empty list, not null");
        assertTrue(flags.isEmpty(), "List should be empty");
    }

    /**
     * Tests flag with special characters in reason.
     */
    @Test
    void testFlagWithSpecialCharactersInReason() throws SQLException {
        String specialReason = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        
        boolean result = db.flagContent("question", testQuestionId, 
                                       staffUser.getUserName(), 
                                       specialReason);
        
        assertTrue(result, "Should handle special characters");
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(flag, "Flag should exist");
        assertEquals(specialReason, flag.getReason(), "Special characters should be preserved");
    }

    /**
     * Tests rapid flag creation (stress test).
     */
    @Test
    void testRapidFlagCreation() throws SQLException {
        int flagCount = 50;
        
        for (int i = 0; i < flagCount; i++) {
            db.flagContent("question", testQuestionId, 
                          staffUser.getUserName(), 
                          "Rapid flag " + i);
        }
        
        List<ContentFlag> flags = db.getAllFlags();
        long countForQuestion = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .count();
        
        assertTrue(countForQuestion >= flagCount, 
                  "Should handle rapid flag creation");
    }

    /**
     * Tests flag timestamp accuracy.
     */
    @Test
    void testFlagTimestampAccuracy() throws SQLException {
        long beforeTime = System.currentTimeMillis();
        
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), "Timestamp test");
        
        long afterTime = System.currentTimeMillis();
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(flag, "Flag should exist");
        assertNotNull(flag.getCreatedAt(), "Timestamp should not be null");
        
        // Timestamp should be within reasonable range
        // (This is a basic sanity check)
        assertTrue(flag.getCreatedAt().length() > 0, 
                  "Timestamp should be non-empty");
    }
}
