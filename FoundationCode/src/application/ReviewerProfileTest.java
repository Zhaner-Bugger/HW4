package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import databasePart1.DatabaseHelper;
import application.ReviewerProfile;
import application.Review;
import application.Feedback;
/**
 * Unit tests for verifying the functionality of the Reviewer Profile system. 
 * 
 * <p> This class contains test operations related to reviewer profiles such as profile creation, 
 *  experience updates, review insertion and retrieval, feedback retrieval, and
 *  fetching all reviewer profiles. Each test runs with a clean temporary test user
 *  and database state.</p>
 *  
 *  <p> Author: Tanya Ceniceros
 */

class ReviewerProfileTest {
	/**
	 * Database helper used for all test operations
	 */
	private DatabaseHelper db;
	/** ID of the temporary user created before each test */
	private int testUserId;
	/** username used for all test data operations. */
	private String testUserName = "testuser";

	/**
	 * Sets up the test environment before each test case
	 * 
	 * <p> Connects to the database, removes any existing test data with the same username,
	 * creates a clean test user, retrieves the generated user ID and creates 
	 * an associated reviewer profile. </p>
	 * 
	 * @throws SQLException if any database operation fails during setup
	 */
	
	@BeforeEach
	void setup() throws SQLException {
		db = new DatabaseHelper();
		db.connectToDatabase();
		
		// Clean up test data safely
	    db.executeUpdate("DELETE FROM answer_reviews WHERE reviewerUserName = '" + testUserName + "'");
		db.executeUpdate("DELETE FROM answers WHERE author =  'testuser'");
		db.executeUpdate("DELETE FROM questions WHERE author = 'testuser'");
		db.executeUpdate("DELETE FROM reviewer_profiles WHERE reviewer_id = " + testUserId);
	    db.executeUpdate("DELETE FROM PrivateMessages WHERE fromUser = '" + testUserName + "' OR toUser = '" + testUserName + "'");
	    db.executeUpdate("DELETE FROM cse360users WHERE userName = '" + testUserName + "'");
		    
		
		//create a test User
		db.executeUpdate("INSERT INTO cse360users (userName, password, name, email, role) " + 
						"VALUES ('" + testUserName + "', 'pass', 'Test User', 'test@example.com', 'user')");
		testUserId = db.getUserIdByUsername(testUserName);
		
		
		//create reviewer profile
		db.createReviewerProfile(testUserId, testUserName);
	}
	/**
	 * Cleans up test data after each test case.
	 * 
	 * <p> removes all data associated with the temporary test user to ensure 
	 * database state isolation between tests. </p>
	 * @throws SQLException if any cleanup query fails
	 */
	
	@AfterEach 
	void cleanup() throws SQLException {
		//remove test data
	    db.executeUpdate("DELETE FROM answer_reviews WHERE reviewerUserName = '" + testUserName + "'");
		db.executeUpdate("DELETE FROM answers WHERE author =  'testuser'");
		db.executeUpdate("DELETE FROM questions WHERE author = 'testuser'");
		db.executeUpdate("DELETE FROM reviewer_profiles WHERE reviewer_id = " + testUserId);
        db.executeUpdate("DELETE FROM PrivateMessages WHERE fromUser = '" + testUserName + "' OR toUser = '" + testUserName + "'");
        db.executeUpdate("DELETE FROM cse360users WHERE userName = '" + testUserName + "'");
	    

    
	}
	/**
	 * Tests that a reviewer profile is successfully created for the test user. 
	 * 
	 * <p> Verifies that the created profile exists, has the expected username,
	 * contains no experience text, and starts with an empty review list. </p>
	 * @throws SQLException if a database query fails
	 */
	@Test
	void testReviewerProfileCreation() throws SQLException{
		ReviewerProfile profile = db.getReviewerProfileById(testUserId);
		assertNotNull(profile, "Reviewer profile should exist after creation");
		assertEquals(testUserName, profile.getName());
		assertEquals("",profile.getExperience());
		assertTrue(profile.getReviews().isEmpty(),"Reviews list should be empty initially");
	}
	/**
	 * Tests updating the reviewer's experience field.
	 * 
	 * <p> Updates the reviewer's experience, retrieves the profile again,
	 * and checks that the stored experience matches the new value. </p>
	 * 
	 * @throws SQLException if the update or retrieval fails
	 */
	@Test
	 void testUpdateReviewerExperience() throws SQLException {
        String newExp = "5 years reviewing experience";
        db.updateReviewerExperience(testUserId, newExp);

        ReviewerProfile profile = db.getReviewerProfileById(testUserId);
        assertEquals(newExp, profile.getExperience());
    }
	/**
	 * Tests adding a review to the database and retrieving it by reviewer username.
	 * 
	 * <p> Creates a sample question  and answer authored by the test user, inserts a
	 * new review, and then ensures the review can be correctly fetched. </p>
	 * @throws SQLException if any insert or retrieval fails
	 */
	 @Test
	    void testAddAndGetReview() throws SQLException {
		 	db.executeUpdate("INSERT INTO questions (questionId, title, content, author) VALUES ('q1', 'Sample Question', 'This is the question content','" + testUserName + "')");
		    db.executeUpdate("INSERT INTO answers (answerId, questionId, author, content) VALUES ('ans1', 'q1', '"+ testUserName + "', 'Answer content')");

	        Review review = new Review("rev1", "ans1", testUserName, "Great answer!", new Timestamp(System.currentTimeMillis()), "par1");
	        db.insertReview(review);

	        List<Review> reviews = db.getReviewsByReviewer(testUserName);
	        assertEquals(1, reviews.size());
	        assertEquals("Great answer!", reviews.get(0).getContent());
	    }
	 /**
	  * Tests adding an retrieving feedback messages sent to the reviewer. 
	  * 
	  * <p> Inserts a mock feedback message into the {@code PrivateMessages} table and verifies 
	  * that it can be retrieved using the reviewer's username.</p>
	  * 
	  * @throws SQLException if a database insert or retrieval fails
	  */
	 @Test
	    void testAddAndGetFeedback() throws SQLException {
		 
	        db.executeUpdate("INSERT INTO PrivateMessages (questionId, fromUser, toUser, content) " +
	                         "VALUES ('REVFB:" + testUserId + "', 'student1', '" + testUserName + "', 'Helpful feedback!')");
	        
	        List<Feedback> feedbackList = db.getFeedbackByReviewer(testUserName);
	        assertEquals(1, feedbackList.size());
	        assertEquals("Helpful feedback!", feedbackList.get(0).getFeedbackText());
	        assertEquals("student1", feedbackList.get(0).getStudentUserName());
	    }
	 /**
	  * Tests retrieving all reviewer profiles from the database.
	  * 
	  * <p> Ensures that the test reviewer profile appears in the returned list
	  * of all profiles. </p>
	  * @throws SQLException
	  */
	 @Test
	    void testGetAllReviewerProfiles() throws SQLException {
	        List<ReviewerProfile> profiles = db.getAllReviewerProfiles();
	        assertTrue(profiles.stream().anyMatch(p -> p.getName().equals(testUserName)));
	    }

}
