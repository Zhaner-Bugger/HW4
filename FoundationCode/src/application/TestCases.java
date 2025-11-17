package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.*;

import org.junit.jupiter.api.*;

import databasePart1.DatabaseHelper;


/**
 * {@code TestCases} contains unit and integration tests for database operations
 * within the CSE360 application. These tests verify the main user work flows:
 * <ul>
 *   <li>Student reviewer request submission</li>
 *   <li>Instructor reviewing and approving requests</li>
 *   <li>Reviewer creating and retrieving answer reviews</li>
 * </ul>
 * 
 * <p>All tests use a shared {@link DatabaseHelper} instance \
 * The tests are designed for use
 * with an in-memory H2 database or 
 * a local test database.</p>
 *  
 *  <p>At this time clearing memory those not work. 
 *  A conflict on User creation makes deleting dependents
 *  problematic.</p>
 * 
 * <p><strong>Tables tested:</strong>
 * <ul>
 *   <li>cse360users</li>
 *   <li>reviewer_requests</li>
 *   <li>questions</li>
 *   <li>answers</li>
 *   <li>answer_reviews</li>
 * </ul>
 * </p>
 * 
 * @author Omar Diaz
 */

public class TestCases {

    /** Shared database helper instance used for all tests. */
    private static DatabaseHelper db;


    /**
     * Initializes the test database connection and clears key tables before tests run.
     * Unsure if clearing helps with testing.
     * 
     * @throws SQLException if a database connection or statement fails
     */
    @BeforeAll
    static void setupDatabase() throws SQLException {
    	try {
        db = new DatabaseHelper();
        db.connectToDatabase(); // Creates tables if needed
        
        // Cleanup start-state
        db.executeUpdate("DELETE FROM reviewer_requests");
        db.executeUpdate("DELETE FROM answer_reviews");
        db.executeUpdate("DELETE FROM answers");
        db.executeUpdate("DELETE FROM questions");
        
        System.out.println("Database setup complete.");
    } catch (Exception e) {
    	e.printStackTrace();
    }
 }
    
/*
    @AfterEach
    void cleanupAfterEach() throws SQLException {
        db.executeUpdate("DELETE FROM reviewer_requests");
        db.executeUpdate("DELETE FROM answer_reviews");
        db.executeUpdate("DELETE FROM answers");
        db.executeUpdate("DELETE FROM UserRoles");
        db.executeUpdate("DELETE FROM cse360users");
    }
*/  


    /**
     * Closes the database connection after all tests have completed.
     */
    @AfterAll
    static void tearDownAll() {
        db.closeConnection();
    }

 // ---------- Test 1: Student submits a reviewer request ----------
    /**
     * Verifies that a student can successfully submit a reviewer request.
     * <ul>
     *   <li>Ensures user accounts exist</li>
     *   <li>If user dose not exist, create them.</li>
     *   <li>Submits a reviewer request via {@link DatabaseHelper#submitReviewerRequest}</li>
     *   <li>Checks that the request is stored with status "Pending"</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    void testStudentCanSubmitReviewerRequest() throws SQLException {
        System.out.println("\n=== Running testStudentCanSubmitReviewerRequest ===");
        
        String userName = "Tester_1";
        // Boolean values for user 
        boolean userExists = false;
        // Check if user already exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
                "SELECT userName FROM cse360users WHERE userName = ?")) {
            checkStmt.setString(1, userName);
            ResultSet rs = checkStmt.executeQuery();
            userExists = rs.next();
        }

        // Insert only if user doesn't exist
        if (!userExists) {
            boolean inserted = db.insertUserDirect(3, userName, "Password123!", userName + "@example.com", "Test Student", "student");
            assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
            System.out.println("Inserted new test student user into database.");
        } else {
            System.out.println("User already exists, skipping insertion.");
        }
        /**
         * Currently submitReviewerRequest may be creating an error when called on
         * by a user with an existing reviewer role.
         */
        // Use existing method to submit a request
        db.submitReviewerRequest(userName);

        /**
         * User submits a request to become a reviewer.
         */
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "SELECT studentUserName, status FROM reviewer_requests WHERE studentUserName = ?")) {
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Reviewer request should be present");
            assertEquals("Pending", rs.getString("status"));
        }
        System.out.println("/n=== Test testStudentCanSubmitReviewerRequest PASSED! ===");
    }

    // ---------- Test 2: Instructor retrieves pending reviewer requests ----------
    /**
     * Verifies that an instructor can retrieve all pending reviewer requests.
     * <ul>
     *   <li>Ensures both instructor and user accounts exist</li>
     *   <li>If they don't exist, create them.</li>
     *   <li>Submits a reviewer request for the student</li>
     *   <li>Uses {@link DatabaseHelper#getPendingReviewerRequests} to fetch pending requests</li>
     *   <li>Asserts that the student appears in the result list</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    void testInstructorCanFetchPendingRequests() throws SQLException {
    	System.out.println("\n=== Running testInstructorCanFetchPendingRequests ===");
    	
        String instructor = "test_instructor1";
        String userName = "Tester_2";
        
        // Boolean values for user and instructor verification
        boolean userExists = false;
        boolean instructorExists = false;
        // Checking if User exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
                "SELECT userName FROM cse360users WHERE userName = ?")) {
            checkStmt.setString(1, userName);
            ResultSet rs = checkStmt.executeQuery();
            userExists = rs.next();
        }
        // Checking if Instructor exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
                "SELECT userName FROM cse360users WHERE userName = ?")) {
            checkStmt.setString(1, instructor);
            ResultSet rs = checkStmt.executeQuery();
            instructorExists = rs.next();
        }

        // Insert only if user doesn't exist
        if (!userExists) {
            boolean inserted = db.insertUserDirect(1, userName, "Password123!", userName + "@example.com", "Test Student", "student");
            assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
            System.out.println("Inserted new test student user into database.");
        } else {
            System.out.println("User already exists, skipping insertion.");
        }
        
     // Insert only if instructor doesn't exist
        if (!instructorExists) {
            boolean inserted = db.insertUserDirect(12, instructor, "Password123!", instructor + "@example.com", "Instructor", "instructor");
            assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
            System.out.println("Inserted new test student user into database.");
        } else {
            System.out.println("User already exists, skipping insertion.");
        }
        
        db.submitReviewerRequest(userName);

        // Retrieve pending requests
        java.util.List<application.User> pending = db.getPendingReviewerRequests();
        assertFalse(pending.isEmpty(), "Pending list should not be empty");
        boolean found = pending.stream().anyMatch(u -> u.getUserName().equals(userName));
        assertTrue(found, "Pending list must contain the test student");
        
        System.out.println("\n=== Test testInstructorCanFetchPendingRequests PASSED! ===");
    }

    // ---------- Test 3: Instructor approves a reviewer request ----------
    /**
     * Verifies that an instructor can approve a pending reviewer request.
     * <ul>
     *   <li>Ensures both instructor and user accounts exist</li>
     *   <li>If they don't exist, create them.</li>
     *   <li>Student submits a reviewer request</li>
     *   <li>Instructor approves it using {@link DatabaseHelper#processReviewerRequest}</li>
     *   <li>Confirms that the request is marked "Approved" and the user gains the "reviewer" role</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    void testInstructorCanApproveRequest() throws SQLException {
    	System.out.println("\n=== Test testInstructorCanApproveRequest ===");
        String instructor = "test_instructor2";
    	String userName = "Tester_3";
    	
        // Boolean values for user and instructor verification
        boolean userExists = false;
        boolean instructorExists = false;
        // Checking if User exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
                "SELECT userName FROM cse360users WHERE userName = ?")) {
            checkStmt.setString(1, userName);
            ResultSet rs = checkStmt.executeQuery();
            userExists = rs.next();
        }
        // Checking if Instructor exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
                "SELECT userName FROM cse360users WHERE userName = ?")) {
            checkStmt.setString(1, instructor);
            ResultSet rs = checkStmt.executeQuery();
            instructorExists = rs.next();
        }

        // Insert only if user doesn't exist
        if (!userExists) {
            boolean inserted = db.insertUserDirect(4, userName, "Password123!", userName + "@example.com", "Test Student", "student");
            assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
            System.out.println("Inserted new test student user into database.");
        } else {
            System.out.println("User already exists, skipping insertion.");
        }
        
     // Insert only if instructor doesn't exist
        if (!instructorExists) {
            boolean inserted = db.insertUserDirect(24, instructor, "Password123!", instructor + "@example.com", "Instructor", "instructor");
            assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
            System.out.println("Inserted new instructor student user into database.");
        } else {
            System.out.println("User already exists, skipping insertion.");
        }
        
        // User submits request
        db.submitReviewerRequest(userName);

        boolean processed = db.processReviewerRequest(userName, true, instructor);
        assertTrue(processed, "processReviewerRequest should return true for the update");

        // verify status updated
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "SELECT status FROM reviewer_requests WHERE studentUserName = ?")) {
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("Approved", rs.getString("status"));
        }

        // verify userRoles contains 'reviewer'
        java.util.List<String> roles = db.getUserRoles(userName);
        assertTrue(roles.contains("reviewer"), "User roles should contain 'reviewer' after approval");
        
        System.out.println("\n=== Test testInstructorCanApproveRequest PASSED! ===");
    }

    // ---------- Test 4: Reviewer can create a review ----------

    /**
     * Tests that a reviewer can successfully insert a new review for an answer.
     * <ul>
     *   <li>Ensures reviewer accounts exist</li>
     *   <li>If they don't exist, create them.</li>
     *   <li>Creates a test question, and an answer</li>
     *   <li>Inserts a {@link application.Review}</li>
     *   <li>Validates that the review appears in the {@code answer_reviews} table</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    void testReviewerCanCreateReview() throws SQLException {
    	System.out.println("\n=== Test testReviewerCanCreateReview ===");
        String reviewer = "test_reviewer2";
        
        // Checking if reviewer exists
        boolean reviewerExists = false;
        // Checking if Reviewer exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
        		"SELECT userName FROM cse360users WHERE userName = ?")) {
        	checkStmt.setString(1, reviewer);
        	ResultSet rs = checkStmt.executeQuery();
        	reviewerExists = rs.next();
    	}

    	// Insert only if reviewer doesn't exist
    	if (!reviewerExists) {
    		boolean inserted = db.insertUserDirect(18, reviewer, "Password123!", reviewer + "@example.com", "Test Student", "student");
    		assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
        	System.out.println("Inserted new test reviewer user into database.");
    	} else {
    		System.out.println("Reviewer already exists, skipping insertion.");
    	}
    	db.insertUserDirect(10, reviewer, "pass", reviewer + "@example.com", "Reviewer One", "reviewer");

    	// Creates question
    	String questionId = "QTEST1";
    	boolean questionExists = false;
    	try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
    	        "SELECT questionId FROM questions WHERE questionId = ?")) {
    	    checkStmt.setString(1, questionId);
    	    ResultSet rs = checkStmt.executeQuery();
    	    questionExists = rs.next();
    	}
    	// Checks for existing question
    	if (!questionExists) {
    		// If it those not exist, create one
    	    try (PreparedStatement stmt = db.getConnection().prepareStatement(
    	            "INSERT INTO questions (questionId, title, content, author, createdAt, followUpOf, isResolved) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
    	        stmt.setString(1, questionId);
    	        stmt.setString(2, "Test Case");
    	        stmt.setString(3, "Sample text."); // author must exist in users
    	        stmt.setString(4, reviewer);
    	        stmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
    	        stmt.setObject(6, null);
    	        stmt.setBoolean(7, false);
    	        stmt.executeUpdate();
    	        System.out.println("Inserted new test question with ID " + questionId);
    	    }
    	// Existing question in database
    	} else {
    	    System.out.println("Test question already exists, skipping insertion.");
    	}
    	
    	
    	// create a test answer for the reviewer
        String answerId = "TANS1";
        boolean answerExists = false;
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
                "SELECT answerId FROM answers WHERE answerId = ?")) {
            checkStmt.setString(1, answerId);
            ResultSet rs = checkStmt.executeQuery();
            answerExists = rs.next();
        }
        //Checks for existing answer
        if (!answerExists) {
        	// If it those not exist, create one
            try (PreparedStatement stmt = db.getConnection().prepareStatement(
                    "INSERT INTO answers (answerId, questionId, content, author) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, answerId);
                stmt.setString(2, questionId);
                stmt.setString(3, "Sample answer content");
                stmt.setString(4, reviewer);
                stmt.executeUpdate();
                System.out.println("Inserted new test answer for reviewer.");
            }
        // Existing question in database
        } else {
            System.out.println("Test answer already exists, skipping insertion.");
        }

        // create Review object 
        application.Review review = new application.Review(
            "R" + UUID.randomUUID().toString().substring(0,8),
            answerId, reviewer, "This is a test review",
            new Timestamp(System.currentTimeMillis()), "");

        boolean inserted = db.insertReview(review);
        assertTrue(inserted, "insertReview should return true");

        // verify row present
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "SELECT reviewerUserName, reviewContent FROM answer_reviews WHERE reviewId = ?")) {
            stmt.setString(1, review.getReviewId());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals(reviewer, rs.getString("reviewerUserName"));
            assertEquals("This is a test review", rs.getString("reviewContent"));
        }
        System.out.println("\n=== Test testReviewerCanCreateReview PASSED! ===");
    }

    // ---------- Test 5: Retrieve all reviews for an answer ----------
    /**
     * Tests that all reviews for a specific answer can be retrieved in order.
     * <ul>
	 *   <li>Ensures reviewer accounts exist</li>
     *   <li>If they don't exist, create them.</li>
     *   <li>Creates a question, answer, and two reviews</li>
     *   <li>Calls {@link DatabaseHelper#getReviewsForAnswer}</li>
     *   <li>Verifies both reviews are returned in ascending creation order</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    void testRetrieveAllReviewsForAnswer() throws SQLException {
    	System.out.println("\n=== Test testRetrieveAllReviewsForAnswer ===");
        String reviewer = "test_reviewer3";
           
        // 
        boolean reviewerExists = false;
        // Checking if Reviewer exists
        try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
        		"SELECT userName FROM cse360users WHERE userName = ?")) {
        	checkStmt.setString(1, reviewer);
        	ResultSet rs = checkStmt.executeQuery();
        	reviewerExists = rs.next();
    	}
    	// Insert only if reviewer doesn't exist
    	if (!reviewerExists) {
    		boolean inserted = db.insertUserDirect(30, reviewer, "Password123!", reviewer + "@example.com", "Test Student", "student");
    		assertTrue(inserted, "insertUserDirect should return true when inserting a new user");
        	System.out.println("Inserted new test reviewer user into database.");
    	} else {
    		System.out.println("Reviewer already exists, skipping insertion.");
    	}
    	
    	
    	// Creating question QTEST2
    	String questionId = "QTEST2";
    	boolean questionExists = false;
    	try (PreparedStatement checkStmt = db.getConnection().prepareStatement(
    	        "SELECT questionId FROM questions WHERE questionId = ?")) {
    	    checkStmt.setString(1, questionId);
    	    ResultSet rs = checkStmt.executeQuery();
    	    questionExists = rs.next();
    	}
    	// Checking for Existing question
    	if (!questionExists) {
    		// If it those not exist, create one
    	    try (PreparedStatement stmt = db.getConnection().prepareStatement(
    	            "INSERT INTO questions (questionId, title, content, author, createdAt, followUpOf, isResolved) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
    	        stmt.setString(1, questionId);
    	        stmt.setString(2, "Review Question 2");
    	        stmt.setString(3, "Question text for review test");
    	        stmt.setString(4, reviewer);
    	        stmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
    	        stmt.setObject(6, null);
    	        stmt.setBoolean(7, false);
    	        stmt.executeUpdate();
    	        System.out.println("Inserted new question QTEST2 for review test.");
    	    }
        // Existing question in database
    	} else {
    		System.out.println("Test question already exists, skipping insertion.");
    	}

    	// Creating answers for questions
        String answerId = "TANS2";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
                "INSERT INTO answers (answerId, questionId, content, author) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, answerId);
            stmt.setString(2, questionId);
            stmt.setString(3, "Answer two");
            stmt.setString(4, reviewer);
            stmt.executeUpdate();
        }

        String r1 = "R" + UUID.randomUUID().toString().substring(0,8);
        String r2 = "R" + UUID.randomUUID().toString().substring(0,8);
        try (PreparedStatement stmt = db.getConnection().prepareStatement(
        		"INSERT INTO answer_reviews (reviewId, answerId, reviewerUserName, reviewContent) VALUES (?, ?, ?, ?)")) {
        	// Answer 1
            stmt.setString(1, r1);
            stmt.setString(2, answerId);
            stmt.setString(3, reviewer);
            stmt.setString(4, "First review");
            stmt.executeUpdate();

            //Answer 2
            stmt.setString(1, r2);
            stmt.setString(2, answerId);
            stmt.setString(3, reviewer);
            stmt.setString(4, "Second review");
            stmt.executeUpdate();
        }

        java.util.List<application.Review> reviews = db.getReviewsForAnswer(answerId);
        assertEquals(2, reviews.size(), "Should return two reviews");
        assertEquals("First review", reviews.get(0).getContent());
        assertEquals("Second review", reviews.get(1).getContent());
        System.out.println("\n=== Test testRetrieveAllReviewsForAnswer PASSED! ===");
    }
}