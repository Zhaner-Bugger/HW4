package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import databasePart1.DatabaseHelper;

/**
 * JUnit test suite for staff functionality including content viewing,
 * flagging, and reporting features.
 * 
 * Tests cover:
 * - User Story 1: Viewing all content (questions, answers, reviews, messages)
 * - User Story 2: Flagging inappropriate content
 * - User Story 3: Generating statistics and reports
 * 
 * @author Your Name
 * @version 1.0
 */
public class StaffFunctionalityTest {

    private DatabaseHelper db;
    private User staffUser;
    private User testStudent;
    private String testQuestionId;
    private String testAnswerId;
    private String testReviewId;

    /**
     * Sets up the test environment before each test.
     * Creates a clean database state with test users and content.
     * 
     * @throws SQLException if database operations fail
     */
    @BeforeEach
    void setUp() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();

        // Clean up any existing test data
        cleanupTestData();

        // Create test users
        staffUser = new User("testStaff", "Password123!", "staff@test.com", "Test Staff", "staff");
        testStudent = new User("testStudent", "Password123!", "student@test.com", "Test Student", "student");
        
        db.register(staffUser);
        db.register(testStudent);

        // Create test content
        setupTestContent();
    }

    /**
     * Cleans up test data after each test to ensure isolation.
     * 
     * @throws SQLException if cleanup operations fail
     */
    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestData();
        if (db != null) {
            db.closeConnection();
        }
    }

    /**
     * Removes all test data from the database.
     * This is called both before and after tests to ensure clean state.
     * 
     * @throws SQLException if deletion fails
     */
    private void cleanupTestData() {
        if (db == null) {
            return; // Nothing to clean if db is null
        }
        
        try {
            // Delete in reverse order of dependencies to respect foreign key constraints
            
            // 1. Delete content flags (all test users)
            db.executeUpdate("DELETE FROM content_flags WHERE " +
                "flaggedBy LIKE 'test%' OR " +
                "flaggedBy LIKE '%Active' OR " +
                "flaggedBy LIKE 'super%' OR " +
                "flaggedBy LIKE 'limitTest%' OR " +
                "flaggedBy = 'inactive' OR " +
                "flaggedBy = 'resolveTest'");
            
            // 2. Delete private messages
            db.executeUpdate("DELETE FROM PrivateMessages WHERE " +
                "fromUser LIKE 'test%' OR toUser LIKE 'test%' OR " +
                "fromUser LIKE '%Active' OR toUser LIKE '%Active' OR " +
                "fromUser LIKE 'super%' OR toUser LIKE 'super%' OR " +
                "fromUser LIKE 'limitTest%' OR toUser LIKE 'limitTest%' OR " +
                "fromUser = 'inactive' OR toUser = 'inactive' OR " +
                "fromUser = 'resolveTest' OR toUser = 'resolveTest'");
            
            // 3. Delete answer reviews
            db.executeUpdate("DELETE FROM answer_reviews WHERE " +
                "reviewerUserName LIKE 'test%' OR " +
                "reviewerUserName LIKE '%Active' OR " +
                "reviewerUserName LIKE 'super%' OR " +
                "reviewerUserName LIKE 'limitTest%' OR " +
                "reviewerUserName = 'inactive' OR " +
                "reviewerUserName = 'resolveTest'");
            
            // 4. Delete answers (all test patterns)
            db.executeUpdate("DELETE FROM answers WHERE " +
                "answerId LIKE 'TEST_%' OR " +
                "answerId LIKE 'ACTIVE_%' OR " +
                "answerId LIKE 'SUPER_%' OR " +
                "answerId LIKE 'DYNAMIC_%' OR " +
                "answerId LIKE 'LIMIT_%' OR " +
                "answerId LIKE 'RESOLVE_%' OR " +
                "author LIKE 'test%' OR " +
                "author LIKE '%Active' OR " +
                "author LIKE 'super%' OR " +
                "author LIKE 'limitTest%' OR " +
                "author = 'inactive' OR " +
                "author = 'resolveTest'");
            
            // 5. Delete question tags (all test question patterns)
            db.executeUpdate("DELETE FROM question_tags WHERE " +
                "questionId LIKE 'TEST_%' OR " +
                "questionId LIKE 'ACTIVE_%' OR " +
                "questionId LIKE 'SUPER_%' OR " +
                "questionId LIKE 'DYNAMIC_%' OR " +
                "questionId LIKE 'LIMIT_%' OR " +
                "questionId LIKE 'RESOLVE_%'");
            
            // 6. Delete questions (THIS IS THE CRITICAL ONE - must include ALL patterns)
            db.executeUpdate("DELETE FROM questions WHERE " +
                "questionId LIKE 'TEST_%' OR " +
                "questionId LIKE 'ACTIVE_%' OR " +
                "questionId LIKE 'SUPER_%' OR " +
                "questionId LIKE 'DYNAMIC_%' OR " +
                "questionId LIKE 'LIMIT_%' OR " +
                "questionId LIKE 'RESOLVE_%' OR " +
                "author LIKE 'test%' OR " +
                "author LIKE '%Active' OR " +
                "author LIKE 'super%' OR " +
                "author LIKE 'limitTest%' OR " +
                "author = 'inactive' OR " +
                "author = 'resolveTest'");
            
            // 7. Delete user roles (all test users)
            db.executeUpdate("DELETE FROM UserRoles WHERE " +
                "userName LIKE 'test%' OR " +
                "userName LIKE '%Active' OR " +
                "userName LIKE 'super%' OR " +
                "userName LIKE 'limitTest%' OR " +
                "userName = 'inactive' OR " +
                "userName = 'resolveTest'");
            
            // 8. Delete users (NOW safe to delete since all dependencies are gone)
            db.executeUpdate("DELETE FROM cse360users WHERE " +
                "userName LIKE 'test%' OR " +
                "userName LIKE '%Active' OR " +
                "userName LIKE 'super%' OR " +
                "userName LIKE 'limitTest%' OR " +
                "userName = 'inactive' OR " +
                "userName = 'resolveTest'");
            
        } catch (SQLException e) {
            // Log but don't fail - cleanup errors are often expected
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }

    /**
     * Creates test content (questions, answers, reviews, messages) for testing.
     * 
     * @throws SQLException if content creation fails
     */
    private void setupTestContent() throws SQLException {
        // Create a test question
        testQuestionId = "TEST_Q1";
        Question testQuestion = new Question(
            testQuestionId,
            "Test Question for Staff",
            "This is a test question content",
            testStudent.getUserName(),
            new Timestamp(System.currentTimeMillis())
        );
        db.insertQuestion(testQuestion);

        // Create a test answer
        testAnswerId = "TEST_A1";
        Answer testAnswer = new Answer(
            testAnswerId,
            testQuestionId,
            "This is a test answer",
            testStudent.getUserName(),
            new Timestamp(System.currentTimeMillis()),
            false
        );
        db.insertAnswer(testAnswer);

        // Create a test review
        testReviewId = "TEST_R1";
        Review testReview = new Review(
            testReviewId,
            testAnswerId,
            testStudent.getUserName(),
            "This is a test review",
            new Timestamp(System.currentTimeMillis()),
            null
        );
        db.insertReview(testReview);

        // Create a test private message
        db.insertPrivateMessage(testQuestionId, testStudent.getUserName(), 
                               testStudent.getUserName(), "Test private message");
    }

    // ==================== USER STORY 1 TESTS ====================
    // Testing: Staff can view all content

    /**
     * Tests that staff can retrieve all questions from the database.
     * Verifies User Story 1.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testStaffCanViewAllQuestions() throws SQLException {
        List<Question> questions = db.getAllQuestions();
        
        assertNotNull(questions, "Questions list should not be null");
        assertTrue(questions.size() >= 1, "Should retrieve at least the test question");
        
        // Verify our test question is in the list
        boolean foundTestQuestion = questions.stream()
            .anyMatch(q -> q.getQuestionId().equals(testQuestionId));
        assertTrue(foundTestQuestion, "Test question should be in the retrieved list");
    }

    /**
     * Tests that staff can retrieve all answers from the database.
     * Verifies User Story 1.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testStaffCanViewAllAnswers() throws SQLException {
        List<Answer> answers = db.getAllAnswers();
        
        assertNotNull(answers, "Answers list should not be null");
        assertTrue(answers.size() >= 1, "Should retrieve at least the test answer");
        
        // Verify our test answer is in the list
        boolean foundTestAnswer = answers.stream()
            .anyMatch(a -> a.getAnswerId().equals(testAnswerId));
        assertTrue(foundTestAnswer, "Test answer should be in the retrieved list");
    }

    /**
     * Tests that staff can retrieve all reviews from the database.
     * Verifies User Story 1.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testStaffCanViewAllReviews() throws SQLException {
        List<Review> reviews = db.getReviewsForAnswer(testAnswerId);
        
        assertNotNull(reviews, "Reviews list should not be null");
        assertEquals(1, reviews.size(), "Should retrieve exactly one review for test answer");
        assertEquals(testReviewId, reviews.get(0).getReviewId(), 
                    "Retrieved review should match test review");
    }

    /**
     * Tests that staff can retrieve all private messages from the database.
     * Verifies User Story 1.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testStaffCanViewAllPrivateMessages() throws SQLException {
        List<PrivateMessage> messages = db.getAllPrivateMessages();
        
        assertNotNull(messages, "Messages list should not be null");
        assertTrue(messages.size() >= 1, "Should retrieve at least the test message");
        
        // Verify our test message is in the list
        boolean foundTestMessage = messages.stream()
            .anyMatch(m -> m.getQuestionId().equals(testQuestionId));
        assertTrue(foundTestMessage, "Test message should be in the retrieved list");
    }

    /**
     * Tests that retrieved questions contain all expected fields.
     * Verifies data integrity for User Story 1.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testQuestionDataIntegrity() throws SQLException {
        List<Question> questions = db.getAllQuestions();
        Question testQ = questions.stream()
            .filter(q -> q.getQuestionId().equals(testQuestionId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(testQ, "Test question should be found");
        assertEquals("Test Question for Staff", testQ.getTitle(), "Title should match");
        assertEquals("This is a test question content", testQ.getContent(), "Content should match");
        assertEquals(testStudent.getUserName(), testQ.getAuthor(), "Author should match");
        assertNotNull(testQ.getCreatedAt(), "Created date should not be null");
    }

    // ==================== USER STORY 2 TESTS ====================
    // Testing: Staff can flag inappropriate content

    /**
     * Tests that staff can successfully flag a question.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flagging fails
     */
    @Test
    void testStaffCanFlagQuestion() throws SQLException {
        boolean result = db.flagContent("question", testQuestionId, 
                                       staffUser.getUserName(), 
                                       "Inappropriate content test");
        
        assertTrue(result, "Flagging question should succeed");
        
        // Verify the flag was created
        List<ContentFlag> flags = db.getAllFlags();
        boolean foundFlag = flags.stream()
            .anyMatch(f -> f.getContentType().equals("question") && 
                          f.getContentId().equals(testQuestionId));
        assertTrue(foundFlag, "Flag should be in the database");
    }

    /**
     * Tests that staff can successfully flag an answer.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flagging fails
     */
    @Test
    void testStaffCanFlagAnswer() throws SQLException {
        boolean result = db.flagContent("answer", testAnswerId, 
                                       staffUser.getUserName(), 
                                       "Suspicious answer");
        
        assertTrue(result, "Flagging answer should succeed");
        
        // Verify the flag exists
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag answerFlag = flags.stream()
            .filter(f -> f.getContentId().equals(testAnswerId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(answerFlag, "Answer flag should exist");
        assertEquals("answer", answerFlag.getContentType(), "Content type should be 'answer'");
        assertEquals(staffUser.getUserName(), answerFlag.getFlaggedBy(), "Flagged by should match staff user");
    }

    /**
     * Tests that staff can successfully flag a review.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flagging fails
     */
    @Test
    void testStaffCanFlagReview() throws SQLException {
        boolean result = db.flagContent("review", testReviewId, 
                                       staffUser.getUserName(), 
                                       "Unhelpful review");
        
        assertTrue(result, "Flagging review should succeed");
        
        List<ContentFlag> flags = db.getAllFlags();
        assertTrue(flags.stream().anyMatch(f -> f.getContentId().equals(testReviewId)), 
                  "Review flag should be created");
    }

    /**
     * Tests that staff can successfully flag a private message.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flagging fails
     */
    @Test
    void testStaffCanFlagMessage() throws SQLException {
        // Get a message ID first
        List<PrivateMessage> messages = db.getAllPrivateMessages();
        assertFalse(messages.isEmpty(), "Should have at least one message");
        
        String messageId = String.valueOf(messages.get(0).getId());
        boolean result = db.flagContent("message", messageId, 
                                       staffUser.getUserName(), 
                                       "Inappropriate message");
        
        assertTrue(result, "Flagging message should succeed");
    }

    /**
     * Tests that flags are created with correct initial status.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flag operations fail
     */
    @Test
    void testFlagInitialStatus() throws SQLException {
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), 
                      "Test flag status");
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(flag, "Flag should exist");
        assertEquals("Pending", flag.getStatus(), "Initial status should be 'Pending'");
    }

    /**
     * Tests that staff can update flag status to "Reviewed".
     * Verifies User Story 2.
     * 
     * @throws SQLException if update fails
     */
    @Test
    void testUpdateFlagStatusToReviewed() throws SQLException {
        db.flagContent("question", testQuestionId, 
                      staffUser.getUserName(), 
                      "Test flag update");
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.get(0);
        
        boolean updated = db.updateFlagStatus(flag.getFlagId(), "Reviewed");
        assertTrue(updated, "Flag status update should succeed");
        
        // Verify the status changed
        List<ContentFlag> updatedFlags = db.getAllFlags();
        ContentFlag updatedFlag = updatedFlags.stream()
            .filter(f -> f.getFlagId() == flag.getFlagId())
            .findFirst()
            .orElse(null);
        
        assertNotNull(updatedFlag, "Updated flag should exist");
        assertEquals("Reviewed", updatedFlag.getStatus(), "Status should be 'Reviewed'");
    }

    /**
     * Tests that staff can update flag status to "Resolved".
     * Verifies User Story 2.
     * 
     * @throws SQLException if update fails
     */
    @Test
    void testUpdateFlagStatusToResolved() throws SQLException {
        db.flagContent("answer", testAnswerId, 
                      staffUser.getUserName(), 
                      "Test resolve");
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.get(0);
        
        boolean updated = db.updateFlagStatus(flag.getFlagId(), "Resolved");
        assertTrue(updated, "Flag status update should succeed");
        
        List<ContentFlag> resolvedFlags = db.getAllFlags();
        ContentFlag resolvedFlag = resolvedFlags.stream()
            .filter(f -> f.getFlagId() == flag.getFlagId())
            .findFirst()
            .orElse(null);
        
        assertEquals("Resolved", resolvedFlag.getStatus(), "Status should be 'Resolved'");
    }

    /**
     * Tests that multiple flags can be created and retrieved.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flag operations fail
     */
    @Test
    void testMultipleFlagsCanBeCreated() throws SQLException {
        db.flagContent("question", testQuestionId, staffUser.getUserName(), "Flag 1");
        db.flagContent("answer", testAnswerId, staffUser.getUserName(), "Flag 2");
        db.flagContent("review", testReviewId, staffUser.getUserName(), "Flag 3");
        
        List<ContentFlag> flags = db.getAllFlags();
        assertTrue(flags.size() >= 3, "Should have at least 3 flags");
    }

    /**
     * Tests that flag contains all required information.
     * Verifies User Story 2.
     * 
     * @throws SQLException if flag operations fail
     */
    @Test
    void testFlagDataIntegrity() throws SQLException {
        String reason = "This is a detailed reason for flagging";
        db.flagContent("question", testQuestionId, staffUser.getUserName(), reason);
        
        List<ContentFlag> flags = db.getAllFlags();
        ContentFlag flag = flags.stream()
            .filter(f -> f.getContentId().equals(testQuestionId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(flag, "Flag should exist");
        assertEquals("question", flag.getContentType(), "Content type should match");
        assertEquals(testQuestionId, flag.getContentId(), "Content ID should match");
        assertEquals(staffUser.getUserName(), flag.getFlaggedBy(), "Flagged by should match");
        assertEquals(reason, flag.getReason(), "Reason should match");
        assertNotNull(flag.getCreatedAt(), "Created timestamp should exist");
    }

    // ==================== USER STORY 3 TESTS ====================
    // Testing: Staff can view statistics and trends

    /**
     * Tests that content statistics can be retrieved.
     * Verifies User Story 3.
     * 
     * @throws SQLException if statistics query fails
     */
    @Test
    void testGetContentStatistics() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        assertNotNull(stats, "Statistics map should not be null");
        assertTrue(stats.containsKey("totalQuestions"), "Should contain totalQuestions");
        assertTrue(stats.containsKey("totalAnswers"), "Should contain totalAnswers");
        assertTrue(stats.containsKey("totalReviews"), "Should contain totalReviews");
        assertTrue(stats.containsKey("totalMessages"), "Should contain totalMessages");
        
        // Verify counts are reasonable
        assertTrue(stats.get("totalQuestions") >= 1, "Should have at least 1 question");
        assertTrue(stats.get("totalAnswers") >= 1, "Should have at least 1 answer");
        assertTrue(stats.get("totalReviews") >= 1, "Should have at least 1 review");
        assertTrue(stats.get("totalMessages") >= 1, "Should have at least 1 message");
    }

    /**
     * Tests that unresolved questions are counted correctly.
     * Verifies User Story 3.
     * 
     * @throws SQLException if statistics query fails
     */
    @Test
    void testUnresolvedQuestionsStatistic() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        assertTrue(stats.containsKey("unresolvedQuestions"), "Should contain unresolvedQuestions");
        
        // Our test question is unresolved
        int unresolvedCount = stats.get("unresolvedQuestions");
        assertTrue(unresolvedCount >= 1, "Should have at least 1 unresolved question");
    }

    /**
     * Tests that pending flags are counted correctly.
     * Verifies User Story 3.
     * 
     * @throws SQLException if statistics query fails
     */
    @Test
    void testPendingFlagsStatistic() throws SQLException {
        // Create some flags
        db.flagContent("question", testQuestionId, staffUser.getUserName(), "Test flag 1");
        db.flagContent("answer", testAnswerId, staffUser.getUserName(), "Test flag 2");
        
        Map<String, Integer> stats = db.getContentStatistics();
        
        assertTrue(stats.containsKey("pendingFlags"), "Should contain pendingFlags");
        int pendingCount = stats.get("pendingFlags");
        assertTrue(pendingCount >= 2, "Should have at least 2 pending flags");
    }

    /**
     * Tests that user role statistics are counted correctly.
     * Verifies User Story 3.
     * 
     * @throws SQLException if statistics query fails
     */
    @Test
    void testUserRoleStatistics() throws SQLException {
        Map<String, Integer> stats = db.getContentStatistics();
        
        assertTrue(stats.containsKey("users_staff"), "Should contain staff user count");
        assertTrue(stats.containsKey("users_student"), "Should contain student user count");
        
        // We created one staff and one student
        assertTrue(stats.get("users_staff") >= 1, "Should have at least 1 staff user");
        assertTrue(stats.get("users_student") >= 1, "Should have at least 1 student user");
    }

    /**
     * Tests that most active users can be retrieved.
     * Verifies User Story 3.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testGetMostActiveUsers() throws SQLException {
        List<Map<String, Object>> activeUsers = db.getMostActiveUsers(10);
        
        assertNotNull(activeUsers, "Active users list should not be null");
        
        // Our test student should be in the list (created question, answer, review)
        boolean foundTestStudent = activeUsers.stream()
            .anyMatch(u -> testStudent.getUserName().equals(u.get("userName")));
        assertTrue(foundTestStudent, "Test student should be in active users list");
    }

    /**
     * Tests that activity count is calculated correctly.
     * Verifies User Story 3.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testActivityCountAccuracy() throws SQLException {
        List<Map<String, Object>> activeUsers = db.getMostActiveUsers(10);
        
        Map<String, Object> testStudentActivity = activeUsers.stream()
            .filter(u -> testStudent.getUserName().equals(u.get("userName")))
            .findFirst()
            .orElse(null);
        
        assertNotNull(testStudentActivity, "Test student should have activity");
        
        Integer activityCount = (Integer) testStudentActivity.get("activityCount");
        assertNotNull(activityCount, "Activity count should not be null");
        
        // Student created 1 question + 1 answer = 2 activities
        assertTrue(activityCount >= 2, "Test student should have at least 2 activities");
    }

    /**
     * Tests that most active users are ordered by activity count.
     * Verifies User Story 3.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testActiveUsersOrdering() throws SQLException {
        // Create another user with more activity
    	String uniqueStaffName = "veryActive_" + System.currentTimeMillis();
        User veryActiveUser = new User(uniqueStaffName, "Pass123!", "active@test.com", 
                                       "Very Active", "student");
        db.register(veryActiveUser);
        
        // Create multiple questions for this user
        for (int i = 0; i < 3; i++) {
            Question q = new Question("ACTIVE_Q" + i, "Active Question " + i, 
                                     "Content", veryActiveUser.getUserName(), 
                                     new Timestamp(System.currentTimeMillis()));
            db.insertQuestion(q);
        }
        
        List<Map<String, Object>> activeUsers = db.getMostActiveUsers(10);
        
        // Verify ordering (highest activity first)
        if (activeUsers.size() >= 2) {
            Integer firstCount = (Integer) activeUsers.get(0).get("activityCount");
            Integer secondCount = (Integer) activeUsers.get(1).get("activityCount");
            assertTrue(firstCount >= secondCount, 
                      "Users should be ordered by activity count (descending)");
        }
    }

    /**
     * Tests that statistics update in real-time.
     * Verifies User Story 3.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testStatisticsUpdateDynamically() throws SQLException {
        Map<String, Integer> initialStats = db.getContentStatistics();
        int initialQuestions = initialStats.get("totalQuestions");
        
        // Add a new question
        Question newQ = new Question("DYNAMIC_Q1", "New Question", "Content", 
                                    testStudent.getUserName(), 
                                    new Timestamp(System.currentTimeMillis()));
        db.insertQuestion(newQ);
        
        Map<String, Integer> updatedStats = db.getContentStatistics();
        int updatedQuestions = updatedStats.get("totalQuestions");
        
        assertEquals(initialQuestions + 1, updatedQuestions, 
                    "Question count should increase by 1");
    }

    /**
     * Tests that limit parameter works for getMostActiveUsers.
     * Verifies User Story 3.
     * 
     * @throws SQLException if query fails
     */
    @Test
    void testMostActiveUsersLimit() throws SQLException {
        // Create several users
        for (int i = 0; i < 5; i++) {
        	String uniqueStaffName = "limitTest_" + System.currentTimeMillis();
            User u = new User(uniqueStaffName + i, "Pass123!", "test" + i + "@test.com", 
                            "User " + i, "student");
            db.register(u);
            
            Question q = new Question("LIMIT_Q" + i, "Question " + i, "Content", 
                                     u.getUserName(), new Timestamp(System.currentTimeMillis()));
            db.insertQuestion(q);
        }
        
        List<Map<String, Object>> top3 = db.getMostActiveUsers(3);
        assertTrue(top3.size() <= 3, "Should return at most 3 users");
    }

    // ==================== INTEGRATION TESTS ====================

    /**
     * Tests the complete workflow: view content, flag it, update flag, view statistics.
     * Integration test covering all three user stories.
     * 
     * @throws SQLException if any operation fails
     */
    @Test
    void testCompleteStaffWorkflow() throws SQLException {
        // User Story 1: View content
        List<Question> questions = db.getAllQuestions();
        assertFalse(questions.isEmpty(), "Staff should see questions");
        
        // User Story 2: Flag content
        Question firstQuestion = questions.get(0);
        boolean flagged = db.flagContent("question", firstQuestion.getQuestionId(), 
                                        staffUser.getUserName(), 
                                        "Workflow test flag");
        assertTrue(flagged, "Staff should be able to flag content");
        
        // User Story 2: Update flag
        List<ContentFlag> flags = db.getAllFlags();
        assertFalse(flags.isEmpty(), "Should have flags");
        boolean updated = db.updateFlagStatus(flags.get(0).getFlagId(), "Reviewed");
        assertTrue(updated, "Staff should be able to update flag status");
        
        // User Story 3: View statistics
        Map<String, Integer> stats = db.getContentStatistics();
        assertNotNull(stats, "Staff should be able to view statistics");
        assertTrue(stats.get("totalQuestions") > 0, "Statistics should reflect content");
        assertTrue(stats.get("pendingFlags") >= 0, "Statistics should include flag counts");
    }

    /**
     * Tests that ContentFlag object creation works correctly.
     * Unit test for ContentFlag class.
     */
    @Test
    void testContentFlagObjectCreation() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ContentFlag flag = new ContentFlag(1, "question", "Q123", "staff1", 
                                          "Test reason", "Pending", now);
        
        assertEquals(1, flag.getFlagId(), "Flag ID should match");
        assertEquals("question", flag.getContentType(), "Content type should match");
        assertEquals("Q123", flag.getContentId(), "Content ID should match");
        assertEquals("staff1", flag.getFlaggedBy(), "Flagged by should match");
        assertEquals("Test reason", flag.getReason(), "Reason should match");
        assertEquals("Pending", flag.getStatus(), "Status should match");
        assertNotNull(flag.getCreatedAt(), "Created timestamp should not be null");
    }

    /**
     * Tests that ContentFlag status can be updated.
     * Unit test for ContentFlag class.
     */
    @Test
    void testContentFlagStatusUpdate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ContentFlag flag = new ContentFlag(1, "answer", "A123", "staff1", 
                                          "Inappropriate", "Pending", now);
        
        assertEquals("Pending", flag.getStatus(), "Initial status should be Pending");
        
        flag.setStatus("Reviewed");
        assertEquals("Reviewed", flag.getStatus(), "Status should be updated to Reviewed");
        
        flag.setStatus("Resolved");
        assertEquals("Resolved", flag.getStatus(), "Status should be updated to Resolved");
    }

    /**
     * Tests that JavaFX properties work correctly for ContentFlag.
     * Unit test for ContentFlag properties.
     */
    @Test
    void testContentFlagProperties() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ContentFlag flag = new ContentFlag(1, "review", "R123", "staff1", 
                                          "Test", "Pending", now);
        
        assertNotNull(flag.flagIdProperty(), "Flag ID property should not be null");
        assertNotNull(flag.contentTypeProperty(), "Content type property should not be null");
        assertNotNull(flag.contentIdProperty(), "Content ID property should not be null");
        assertNotNull(flag.flaggedByProperty(), "Flagged by property should not be null");
        assertNotNull(flag.reasonProperty(), "Reason property should not be null");
        assertNotNull(flag.statusProperty(), "Status property should not be null");
        assertNotNull(flag.createdAtProperty(), "Created at property should not be null");
    }
}