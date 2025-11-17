package databasePart1;

import application.Answer;
import application.Feedback;
import application.PrivateMessage;
import application.Question;
import application.User;
import application.Review;
import application.ReviewerProfile;
import application.ContentFlag;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The DatabaseHelper class is responsible for managing the connection to the
 * database, performing operations such as user registration, login validation,
 * and handling invitation codes. CHANGES MADE: added new table userRoles to
 * store multiple roles per user updated 'register(User user)' to insert roles
 * into both 'cse360users' and 'userRoles' updated 'login(User user) to load all
 * roles from 'userRoles' instead of just one (if no entry exists in userRoles,
 * fallback to single role in cse360users) added helper method
 * 'getUserRoles(String userName)' to retrieve multiple roles
 */
public class DatabaseHelper {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase_Phase3;AUTO_SERVER=TRUE";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	private Connection connection = null;
	private Statement statement = null;
	// PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement();
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables(); // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
			throw new SQLException("JDBC Driver not found", e);
		}
	}

	// Admin sets a one-time password for a user who forgot theirs
	public boolean setOneTimePassword(String userName, String otp, Timestamp expiration) {
		String query = "INSERT INTO OneTimePasswords (userName, otp, expiration) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			pstmt.setTimestamp(3, expiration);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Validate a one-time password for a user (not expired, not used)
	public boolean validateOneTimePassword(String userName, String otp) {
		String query = "SELECT expiration FROM OneTimePasswords WHERE userName = ? AND otp = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				if (expiration != null && expiration.after(new Timestamp(System.currentTimeMillis()))) {
					markOneTimePasswordAsUsed(userName, otp);
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Mark a one-time password as used
	private void markOneTimePasswordAsUsed(String userName, String otp) {
		String query = "UPDATE OneTimePasswords SET isUsed = TRUE WHERE userName = ? AND otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, " + "password VARCHAR(255), " + "name VARCHAR(255), "
				+ "email VARCHAR(255), " + "role VARCHAR(20))";
		statement.execute(userTable);
		// new table for multiple roles
		String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255), " + "role VARCHAR(20),"
				+ "FOREIGN KEY (userName) REFERENCES cse360users(userName))";
		statement.execute(userRolesTable);

		// Create the invitation codes table
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes (" + "code VARCHAR(10) PRIMARY KEY, "
				+ "email VARCHAR(255), " + "expiration TIMESTAMP, " + "isUsed BOOLEAN DEFAULT FALSE)";
		statement.execute(invitationCodesTable);

		// Create private messages table
		String messagesTable = "CREATE TABLE IF NOT EXISTS PrivateMessages (" + "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "questionId VARCHAR(64), " + "fromUser VARCHAR(255), " + "toUser VARCHAR(255), "
				+ "content VARCHAR(2000), " + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "isRead BOOLEAN DEFAULT FALSE)";
		statement.execute(messagesTable);

		// Create the one-time passwords table
		String otpTable = "CREATE TABLE IF NOT EXISTS OneTimePasswords (" + "userName VARCHAR(255), "
				+ "otp VARCHAR(255), " + "expiration TIMESTAMP, " + "isUsed BOOLEAN DEFAULT FALSE, "
				+ "PRIMARY KEY(userName, otp))";
		statement.execute(otpTable);

		String questionsTable = "CREATE TABLE IF NOT EXISTS questions (" + "questionId VARCHAR(50) PRIMARY KEY, "
				+ "title VARCHAR(500) NOT NULL, " + "content TEXT, " + "author VARCHAR(255), "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "followUpOf VARCHAR(50),"
				+ "isResolved BOOLEAN DEFAULT FALSE, " + "FOREIGN KEY (author) REFERENCES cse360users(userName),"
				+ "FOREIGN KEY (followUpOf) REFERENCES questions(questionId))";
		statement.execute(questionsTable);

		String questionTagsTable = "CREATE TABLE IF NOT EXISTS question_tags (" + "questionId VARCHAR(50), "
				+ "tag VARCHAR(100), " + "PRIMARY KEY (questionId, tag), "
				+ "FOREIGN KEY (questionId) REFERENCES questions(questionId))";
		statement.execute(questionTagsTable);

		String answersTable = "CREATE TABLE IF NOT EXISTS answers (" + "answerId VARCHAR(50) PRIMARY KEY, "
				+ "questionId VARCHAR(50), " + "content VARCHAR(50), " + "author VARCHAR(255), "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "isAccepted BOOLEAN DEFAULT FALSE, "
				+ "isRead BOOLEAN DEFAULT FALSE, " + "FOREIGN KEY (questionId) REFERENCES questions(questionId), "
				+ "FOREIGN KEY (author) REFERENCES cse360users(userName))";
		statement.execute(answersTable);

		// Shows a table for the pending requests
		String reviewerTables = "CREATE TABLE IF NOT EXISTS reviewer_requests (" + "requestId TEXT PRIMARY KEY,"
				+ "studentUserName VARCHAR(255)," + "status TEXT DEFAULT 'Pending',"
				+ "requestDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "FOREIGN KEY(studentUserName) REFERENCES cse360users(userName))";
		statement.execute(reviewerTables);

		// Shows a table of answer for the reviewer
		String answerReviews = "CREATE TABLE IF NOT EXISTS answer_reviews (" + "reviewId VARCHAR(50) PRIMARY KEY,"
				+ "answerId VARCHAR(50)," + "reviewerUserName VARCHAR(255)," + "reviewContent TEXT NOT NULL,"
				+ "parentReviewID VARCHAR(50)," + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "FOREIGN KEY (answerId) REFERENCES answers(answerId),"
				+ "FOREIGN KEY (reviewerUserName) REFERENCES cse360users(userName))";
		statement.execute(answerReviews);

		// table for reviewer profiles
		String reviewerProfileTables = "CREATE TABLE IF NOT EXISTS reviewer_profiles (" + "reviewer_id INT PRIMARY KEY,"
				+ "name VARCHAR(255)," + "experience VARCHAR(255)," + "FOREIGN KEY (reviewer_id )REFERENCES cse360users(id)) ";
		statement.execute(reviewerProfileTables);
		
		//Shows a table of trusted reviewers for the student
		String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trusted_reviewers ("
				+ "studentUserName VARCHAR(255), "
				+ "reviewerUserName VARCHAR(255), "
				+ "weight DOUBLE NOT NULL DEFAULT 1.0, "
				+ "PRIMARY KEY (studentUserName, reviewerUserName), "
				+ "FOREIGN KEY (studentUserName) REFERENCES cse360users(userName), "
				+ "FOREIGN KEY (reviewerUserName) REFERENCES cse360users(userName))";
		statement.execute(trustedReviewersTable);

		//Used by Staff 
		String flagsTable = "CREATE TABLE IF NOT EXISTS content_flags (" +
		        "flagId INT AUTO_INCREMENT PRIMARY KEY, " +
		        "contentType VARCHAR(20), " +  // 'question', 'answer', 'message', 'review'
		        "contentId VARCHAR(255), " +
		        "flaggedBy VARCHAR(255), " +
		        "reason TEXT, " +
		        "status VARCHAR(20) DEFAULT 'Pending', " +  // 'Pending', 'Reviewed', 'Resolved'
		        "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
		        "FOREIGN KEY (flaggedBy) REFERENCES cse360users(userName))";
		    statement.execute(flagsTable);
		
	}

	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, name, email, role) VALUES (?,?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getUserInfoName());
			pstmt.setString(4, user.getEmail());
			pstmt.setString(5, user.getRole());
			pstmt.executeUpdate();

			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				user.setUserId(rs.getInt(1));
			}
		}

		// insert into userRoles for full role list
		String insertRole = "INSERT INTO UserRoles (userName, role) VALUES (?,?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertRole)) {
			for (String role : user.getRoles()) {
				pstmt.setString(1, user.getUserName());
				pstmt.setString(2, role);
				pstmt.executeUpdate();
			}
		}
	}

	// Loads all existing roles unto getRoles()
	public List<String> getRolesForUser(String userName) throws SQLException {
		List<String> roles = new ArrayList<>();
		String sql = "SELECT role FROM user_roles WHERE userName = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					roles.add(rs.getString("role"));
				}
			}
		}
		return roles;
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					// login successful
					int id = rs.getInt("id");
					user.setUserId(id);

					// step 2: clear and load roles from UserRoles
					user.getRoles().clear();
					List<String> roles = getUserRoles(user.getUserName());
					if (!roles.isEmpty()) {
						for (String role : roles) {
							user.addRole(role);
						}
					} else {
						// fallback: is UserRoles is empty, use cse360users.role
						String singleRole = rs.getString("role");
						if (singleRole != null) {
							user.addRole(singleRole);
						}

					}
					return true;
				}
			}
		}
		return false;
	}

	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
		String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {

			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				// If the count is greater than 0, the user exists
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; // If an error occurs, assume user doesn't exist
	}

	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
		String query = "SELECT role FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getString("role"); // Return the role if user exists
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null; // If no user exists or an error occurs
	}

	// Retrieves multiple roles from user from database using their Username.
	public List<String> getUserRoles(String userName) {
		List<String> roles = new ArrayList<>();
		String query = "SELECT role FROM UserRoles WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String role = rs.getString("role");
				roles.add(role);

			}
			return roles;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return roles;
	}

	// Generates a new invitation code, associates it with an email and expiration,
	// and inserts it into the database.
	public String generateInvitationCode(String email, Timestamp expiration) {
		if (!isValidEmail(email)) {
			throw new IllegalArgumentException("Invalid email format");
		}
		String code = UUID.randomUUID().toString().substring(0, 6); // 6-char code
		String query = "INSERT INTO InvitationCodes (code, email, expiration) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.setString(2, email);
			pstmt.setTimestamp(3, expiration);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}

	// Validates email format (simple regex)
	public static boolean isValidEmail(String email) {
		return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	// Validates expiration date string (yyyy-MM-dd HH:mm)
	public static boolean isValidExpiration(String expiration) {
		return expiration != null && expiration.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");
	}

	// Validates an invitation code to check if it is unused.
	// Validates an invitation code to check if it is unused and not expired.
	public boolean validateInvitationCode(String code) {
		String query = "SELECT expiration FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				if (expiration != null && expiration.after(new Timestamp(System.currentTimeMillis()))) {
					// Mark the code as used
					markInvitationCodeAsUsed(code);
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
		String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try {
			if (statement != null)
				statement.close();
		} catch (SQLException se2) {
			se2.printStackTrace();
		}
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	// Method to get all users
	public List<User> getAllUsers() throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT * FROM cse360users";
		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				User user = new User(rs.getString("userName"), rs.getString("password"), rs.getString("name"),
						rs.getString("email"), rs.getString("role") // we can add other fields as needed here
				);
				users.add(user);
			}
		}
		return users;
	}

	// Method to delete a user
	public boolean deleteUser(String userName, String currentAdmin) throws SQLException {
		// Prevent admin from deleting themselves
		if (userName.equals(currentAdmin)) {
			return false;
		}

		String query = "DELETE FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			return pstmt.executeUpdate() > 0;
		}
	}

	// Method to update user roles
	public boolean updateUserRole(String userName, String newRole, String currentAdmin) throws SQLException {
		// Prevent an admin from removing their own admin role
		if (userName.equals(currentAdmin) && !newRole.contains("admin")) {
			// Check if there's at least one other admin
			if (countAdmins() <= 1) {
				return false;
			}
		}

		String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRole);
			pstmt.setString(2, userName);
			return pstmt.executeUpdate() > 0;
		}
	}

	private int countAdmins() throws SQLException {
		String query = "SELECT COUNT(*) FROM cse360users WHERE role = 'admin'";
		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}

	public boolean updateUserRoles(String userName, List<String> newRoles, String currentAdmin) throws SQLException {
		// Prevent an admin from removing their own admin role if they're the last admin
		if (userName.equals(currentAdmin) && !newRoles.contains("admin")) {
			if (countAdmins() <= 1) {
				return false;
			}
		}

		// Use transaction to ensure consistency
		connection.setAutoCommit(false);
		try {
			// Delete all existing roles for this user
			String deleteQuery = "DELETE FROM UserRoles WHERE userName = ?";
			try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
				deleteStmt.setString(1, userName);
				deleteStmt.executeUpdate();
			}

			// Insert the new roles
			String insertQuery = "INSERT INTO UserRoles (userName, role) VALUES (?, ?)";
			try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
				for (String role : newRoles) {
					insertStmt.setString(1, userName);
					insertStmt.setString(2, role);
					insertStmt.executeUpdate();
					
				}
			}

			// Update the primary role in cse360users (use the first role)
			String updateQuery = "UPDATE cse360users SET role = ? WHERE userName = ?";
			try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
				updateStmt.setString(1, newRoles.isEmpty() ? null : newRoles.get(0));
				updateStmt.setString(2, userName);
				updateStmt.executeUpdate();		
				
			}
			
			System.out.println("roles being assigned: " + newRoles);
			if(newRoles.stream().anyMatch(r -> r.trim().equalsIgnoreCase("Reviewer"))) {
				int userId = getUserIdByUsername(userName);
				System.out.print("User ID for profile creation: " + userId);
				if(!reviewerProfileExists(userId)) {
					
					createReviewerProfile(userId,userName);
					System.out.println("Created reviewer profile for: " + userName);
					
				}else {
					System.out.println("Reviewer profile exists");
				}
			}

			connection.commit();
			return true;
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	// ADDED HW2
	// Question-related methods
	/**
	 * Insert a question into the database.
	 * @param question Question to insert
	 * @return true if insertion succeeded
	 * @throws SQLException on DB error
	 */
	public boolean insertQuestion(Question question) throws SQLException {
		String query = "INSERT INTO questions (questionId, title, content, author, createdAt, followUpOf, isResolved) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, question.getQuestionId());
			pstmt.setString(2, question.getTitle());
			pstmt.setString(3, question.getContent());
			pstmt.setString(4, question.getAuthor());
			pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
			pstmt.setObject(6, question.getFollowUpOf());
			pstmt.setBoolean(7, question.getIsResolved());

			int rowsAffected = pstmt.executeUpdate();

			// Insert tags
			if (rowsAffected > 0) {
				insertQuestionTags(question);
			}

			return rowsAffected > 0;
		}
	}

	private void insertQuestionTags(Question question) throws SQLException {
		String deleteQuery = "DELETE FROM question_tags WHERE questionId = ?";
		try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
			deleteStmt.setString(1, question.getQuestionId());
			deleteStmt.executeUpdate();
		}

		String insertQuery = "INSERT INTO question_tags (questionId, tag) VALUES (?, ?)";
		try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
			for (String tag : question.getTags()) {
				insertStmt.setString(1, question.getQuestionId());
				insertStmt.setString(2, tag);
				insertStmt.executeUpdate();
			}
		}
	}

	/**
	 * Update an existing question record.
	 * @param question Updated question object
	 * @return true if update affected a row
	 * @throws SQLException on DB error
	 */
	public boolean updateQuestion(Question question) throws SQLException {
		String query = "UPDATE questions SET title = ?, content = ?, followUpOf = ? WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, question.getTitle());
			pstmt.setString(2, question.getContent());
			pstmt.setObject(3, question.getFollowUpOf());
			pstmt.setString(4, question.getQuestionId());

			int rowsAffected = pstmt.executeUpdate();

			// Update tags
			if (rowsAffected > 0) {
				insertQuestionTags(question);
			}

			return rowsAffected > 0;
		}
	}

	/**
	 * Delete a question and its tags/answers by id.
	 * @param questionId id of the question to delete
	 * @return true if deletion succeeded
	 * @throws SQLException on DB error
	 */
	public boolean deleteQuestion(String questionId) throws SQLException {
		String deleteAnswersQuery = "DELETE FROM answers WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswersQuery)) {
			pstmt.setString(1, questionId);
			pstmt.executeUpdate();
		}

		String deleteTagsQuery = "DELETE FROM question_tags WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteTagsQuery)) {
			pstmt.setString(1, questionId);
			pstmt.executeUpdate();
		}

		String deleteQuestionQuery = "DELETE FROM questions WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteQuestionQuery)) {
			pstmt.setString(1, questionId);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Retrieve all questions ordered by creation time (desc).
	 * @return list of Question
	 * @throws SQLException on DB error
	 */
	public List<Question> getAllQuestions() throws SQLException {
		List<Question> questions = new ArrayList<>();
		String query = "SELECT * FROM questions ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Question question = new Question(rs.getString("questionId"), rs.getString("title"),
						rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt"), // Use toString()
						rs.getString("followUpOf"));

				question.setIsResolved(rs.getBoolean("isResolved"));

				// Load tags for this question
				loadQuestionTags(question);
				questions.add(question);
			}
		}
		return questions;
	}

	/**
	 * Search questions by title (case-insensitive substring).
	 * @param keyword substring to search for
	 * @return list of matching Question
	 * @throws SQLException on DB error
	 */
	public List<Question> searchQuestionsByTitle(String keyword) throws SQLException {
		List<Question> questions = new ArrayList<>();
		String query = "SELECT * FROM questions WHERE LOWER(title) LIKE LOWER(?) ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, "%" + keyword + "%");

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Question question = new Question(rs.getString("questionId"), rs.getString("title"),
							rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt") // Use
																											// toString()
					);
					question.setIsResolved(rs.getBoolean("isResolved"));
					loadQuestionTags(question);
					questions.add(question);
				}
			}
		}
		return questions;
	}

	/**
	 * Search questions by exact author username.
	 * @param author author userName to filter by
	 * @return list of matching Question
	 * @throws SQLException on DB error
	 */
	public List<Question> searchQuestionsByAuthor(String author) throws SQLException {
		List<Question> questions = new ArrayList<>();
		String query = "SELECT * FROM questions WHERE author = ? ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, author);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Question question = new Question(rs.getString("questionId"), rs.getString("title"),
							rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt"));
					question.setIsResolved(rs.getBoolean("isResolved"));
					loadQuestionTags(question);
					questions.add(question);
				}
			}
		}
		return questions;
	}

	/**
	 * Search questions by content (case-insensitive substring).
	 * @param keyword substring to search in content
	 * @return list of matching Question
	 * @throws SQLException on DB error
	 */
	public List<Question> searchQuestionsByContent(String keyword) throws SQLException {
		List<Question> questions = new ArrayList<>();
		String query = "SELECT * FROM questions WHERE LOWER(content) LIKE LOWER(?) ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, "%" + keyword + "%");

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Question question = new Question(rs.getString("questionId"), rs.getString("title"),
							rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt"));
					question.setIsResolved(rs.getBoolean("isResolved"));
					loadQuestionTags(question);
					questions.add(question);
				}
			}
		}
		return questions;
	}

	/** Load tags for a question from question_tags table. @param question question to populate tags */
	private void loadQuestionTags(Question question) throws SQLException {
		String query = "SELECT tag FROM question_tags WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, question.getQuestionId());

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					question.addTag(rs.getString("tag"));
				}
			}
		}
	}

	// Answer-related methods
	/**
	 * Insert an answer into the database.
	 * @param answer Answer to insert
	 * @return true if insertion succeeded
	 * @throws SQLException on DB error
	 */
	public boolean insertAnswer(Answer answer) throws SQLException {
		if (connection == null || connection.isClosed()) {
	        connectToDatabase();
	    }
		String query = "INSERT INTO answers (answerId, questionId, content, author, createdAt, isAccepted, isRead) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, answer.getAnswerId());
			pstmt.setString(2, answer.getQuestionId());
			pstmt.setString(3, answer.getContent());
			pstmt.setString(4, answer.getAuthor());
			pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
			pstmt.setBoolean(6, answer.getIsAccepted());
			pstmt.setBoolean(7, false);

			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Update an existing answer.
	 * @param answer Updated answer object
	 * @return true if update succeeded
	 * @throws SQLException on DB error
	 */
	public boolean updateAnswer(Answer answer) throws SQLException {
		String query = "UPDATE answers SET content = ?, isAccepted = ? WHERE answerId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, answer.getContent());
			pstmt.setBoolean(2, answer.getIsAccepted());
			pstmt.setString(3, answer.getAnswerId());

			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Delete an answer by id.
	 * @param answerId id of the answer to delete
	 * @return true if deletion succeeded
	 * @throws SQLException on DB error
	 */
	public boolean deleteAnswer(String answerId) throws SQLException {
		String query = "DELETE FROM answers WHERE answerId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, answerId);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Get all answers ordered by creation time (desc).
	 * @return list of Answer
	 * @throws SQLException on DB error
	 */
	public List<Answer> getAllAnswers() throws SQLException {
		List<Answer> answers = new ArrayList<>();
		String query = "SELECT * FROM answers ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Answer answer = new Answer(rs.getString("answerId"), rs.getString("questionId"),
						rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt").toString(),
						rs.getBoolean("isAccepted"));
				answer.setIsRead(rs.getBoolean("isRead"));
				answers.add(answer);
			}
		}
		return answers;
	}

	/**
	 * Get all answers for a specific question.
	 * @param questionId id of the question
	 * @return list of Answer
	 * @throws SQLException on DB error
	 */
	public List<Answer> getAnswersForQuestion(String questionId) throws SQLException {
		List<Answer> answers = new ArrayList<>();
		String query = "SELECT * FROM answers WHERE questionId = ? ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Answer answer = new Answer(rs.getString("answerId"), rs.getString("questionId"),
							rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt").toString(),
							rs.getBoolean("isAccepted"));
					answer.setIsRead(rs.getBoolean("isRead"));
					answers.add(answer);
				}
			}
		}
		return answers;
	}

	/**
	 * Search answers by content substring (case-insensitive).
	 * @param keyword substring to search for
	 * @return list of matching Answer
	 * @throws SQLException on DB error
	 */
	public List<Answer> searchAnswersByContent(String keyword) throws SQLException {
		List<Answer> answers = new ArrayList<>();
		String query = "SELECT * FROM answers WHERE LOWER(content) LIKE LOWER(?) ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, "%" + keyword + "%");

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Answer answer = new Answer(rs.getString("answerId"), rs.getString("questionId"),
							rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt").toString(),
							rs.getBoolean("isAccepted"));
					answer.setIsRead(rs.getBoolean("isRead"));
					answers.add(answer);
				}
			}
		}
		return answers;
	}

	// Count the unread answers
	/**
	 * Count unread answers for a given question and author.
	 * @param questionId question id
	 * @param author question author userName
	 * @return count of unread answers
	 * @throws SQLException on DB error
	 */
	public int countUnreadAnswers(String questionId, String author) throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM answers " + "WHERE questionId = ? AND isRead = FALSE "
				+ "AND questionId IN (SELECT questionId FROM questions WHERE author = ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, author);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				return rs.getInt("count");
		}
		return 0;
	}

	// Mark answers as read
	/**
	 * Mark answers as read for a question and its author.
	 * @param questionId question id
	 * @param author question author userName
	 * @throws SQLException on DB error
	 */
	public void markAnswersAsRead(String questionId, String author) throws SQLException {
		String query = "UPDATE answers SET isRead = TRUE " + "WHERE questionId = ? AND questionId IN "
				+ "(SELECT questionId FROM questions WHERE author = ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, author);
			pstmt.executeUpdate();
		}
	}

	// Utility method to check if a question exists
	/**
	 * Check whether a question exists by id.
	 * @param questionId id to check
	 * @return true if exists
	 * @throws SQLException on DB error
	 */
	public boolean doesQuestionExist(String questionId) throws SQLException {
		String query = "SELECT COUNT(*) FROM questions WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		}
	}

    // Private messaging methods
    /**
     * Insert a private message (used for feedback or question messages).
     * @param questionId question id or marker (e.g. REVFB:ID)
     * @param fromUser sender userName
     * @param toUser recipient userName
     * @param content message body
     * @return true if insertion succeeded
     * @throws SQLException on DB error
     */
    public boolean insertPrivateMessage(String questionId, String fromUser, String toUser, String content)
	    throws SQLException {
		String query = "INSERT INTO PrivateMessages (questionId, fromUser, toUser, content) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, fromUser);
			pstmt.setString(3, toUser);
			pstmt.setString(4, content);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Get unread private message count for a question and user.
	 * @param questionId id/marker
	 * @param userName recipient userName
	 * @return count of unread messages
	 * @throws SQLException on DB error
	 */
	public int getUnreadCountForQuestion(String questionId, String userName) throws SQLException {
		String query = "SELECT COUNT(*) FROM PrivateMessages WHERE questionId = ? AND toUser = ? AND isRead = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, userName);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		}
		return 0;
	}

	/**
	 * Retrieve messages for a question visible to a user (either sender or recipient).
	 * @param questionId id/marker
	 * @param forUser userName viewing the messages
	 * @return list of PrivateMessage
	 * @throws SQLException on DB error
	 */
	public List<PrivateMessage> getMessagesForQuestion(String questionId, String forUser) throws SQLException {
		List<PrivateMessage> messages = new ArrayList<>();
		String query = "SELECT * FROM PrivateMessages WHERE questionId = ? AND (toUser = ? OR fromUser = ?) ORDER BY createdAt ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, forUser);
			pstmt.setString(3, forUser);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					PrivateMessage msg = new PrivateMessage(rs.getInt("id"), rs.getString("questionId"),
							rs.getString("fromUser"), rs.getString("toUser"), rs.getString("content"),
							rs.getTimestamp("createdAt"), rs.getBoolean("isRead"));
					messages.add(msg);
				}
			}
		}
		return messages;
	}

	/**
	 * Mark private messages as read for a question and user.
	 * @param questionId id/marker
	 * @param userName recipient userName
	 * @return true if any rows were updated
	 * @throws SQLException on DB error
	 */
	public boolean markMessagesRead(String questionId, String userName) throws SQLException {
		String query = "UPDATE PrivateMessages SET isRead = TRUE WHERE questionId = ? AND toUser = ? AND isRead = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, userName);
			return pstmt.executeUpdate() > 0;
		}
	}

	// Updates the question to resolved
	/**
	 * Update the resolved flag for a question.
	 * @param questionId id of the question
	 * @param isResolved new resolved state
	 * @return true if update affected a row
	 * @throws SQLException on DB error
	 */
	public boolean updateQuestionResolved(String questionId, boolean isResolved) throws SQLException {
		String query = "UPDATE questions SET isResolved = ? WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setBoolean(1, isResolved);
			pstmt.setString(2, questionId);
			return pstmt.executeUpdate() > 0;
		}
	}

	// Checks if the question is unresolved
	/**
	 * Retrieve unresolved questions.
	 * @return list of unresolved Question
	 * @throws SQLException on DB error
	 */
	public List<Question> getUnresolvedQuestions() throws SQLException {
		List<Question> questions = new ArrayList<>();
		String query = "SELECT * FROM questions WHERE isResolved = FALSE ORDER BY createdAt DESC";

		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Question question = new Question(rs.getString("questionId"), rs.getString("title"),
						rs.getString("content"), rs.getString("author"), rs.getTimestamp("createdAt"));

				question.setIsResolved(rs.getBoolean("isResolved"));

				loadQuestionTags(question);
				questions.add(question);
			}
		}
		return questions;
	}

	// Student submits a review role request
	public void submitReviewerRequest(String studentUserName) throws SQLException {
		String checkQuery = "SELECT * FROM reviewer_requests WHERE studentUserName = ? AND status = 'Pending'";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, studentUserName);
			try (ResultSet rs = checkStmt.executeQuery()) {
				if (rs.next()) {
					throw new SQLException("You already have a pending request.");
				}
			}
		}

		String insertQuery = "INSERT INTO reviewer_requests (requestId, studentUserName, status) VALUES (?, ?, 'Pending')";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
			pstmt.setString(1, java.util.UUID.randomUUID().toString());
			pstmt.setString(2, studentUserName);
			pstmt.executeUpdate();
		}
	}

	// Instructor retrieves all pending requests
	public List<User> getPendingReviewerRequests() throws SQLException {
		List<User> requests = new ArrayList<>();
		String query = "SELECT u.userName, u.password, u.email, u.name, u.role " + "FROM cse360users AS u "
				+ "INNER JOIN reviewer_requests AS r " + "WHERE r.status = 'Pending'";
		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				requests.add(new User(

						rs.getString("userName"), rs.getString("password"), rs.getString("email"), rs.getString("name"),
						rs.getString("role")));
			}
		}
		return requests;
	}

	// Instructor approves or rejects a reviewer request
	public boolean processReviewerRequest(String studentUserName, boolean approve, String instructorUserName)
			throws SQLException {

		if (connection == null || connection.isClosed()) {
			connectToDatabase();
		}

		// Approve request: add 'Reviewer' role to student
		if (approve) {
			List<String> roles = getUserRoles(studentUserName);
			if (!roles.contains("reviewer")) {
				roles.add("reviewer");
				updateUserRoles(studentUserName, roles, instructorUserName); // pass instructor username
			}
		}

		// Update reviewer_requests table status
		String status = approve ? "Approved" : "Rejected";
		String query = "UPDATE reviewer_requests SET status = ? WHERE studentUserName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, status);
			pstmt.setString(2, studentUserName);
			return pstmt.executeUpdate() > 0;
		}
	}

	// Insert a new review into the database
	public boolean insertReview(Review review) throws SQLException {
		if (connection == null || connection.isClosed()) {
			connectToDatabase();
		}

		String query = "INSERT INTO answer_reviews (reviewId, answerId, reviewerUserName, reviewContent, parentReviewID) "
				+ "VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, review.getReviewId());
			pstmt.setString(2, review.getAnswerId());
			pstmt.setString(3, review.getReviewer());
			pstmt.setString(4, review.getContent());
			pstmt.setString(5, review.getParentReviewID());
			return pstmt.executeUpdate() > 0;
		}
	}

	public List<Review> getReviewsForAnswer(String answerId) throws SQLException {
		if (connection == null || connection.isClosed()) {
			connectToDatabase();
		}

		List<Review> reviews = new ArrayList<>();
		String sql = "SELECT reviewId, answerId, reviewerUserName, reviewContent, createdAt, parentReviewID "
				+ "FROM answer_reviews WHERE answerId = ? ORDER BY createdAt ASC";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, answerId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Review review = new Review(
							rs.getString("reviewId"), 
							rs.getString("answerId"),
							rs.getString("reviewerUserName"), 
							rs.getString("reviewContent"),
							rs.getTimestamp("createdAt"),
							rs.getString("parentReviewID"));
					reviews.add(review);
				}
			}
		}
		return reviews;
	}

	// TestCases Helpers to run tests
	// Allow tests / other classes to access the underlying JDBC connection
	public Connection getConnection() {
		return this.connection;
	}

	// Convenience helper for tests to run simple update/delete SQL (cleanup)
	public void executeUpdate(String sql) throws SQLException {
		if (connection == null || connection.isClosed()) {
			connectToDatabase();
		}
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}

	// Optional helper to insert a user with primitives (uses your register
	// internally)
	public boolean insertUserDirect(int userId, String userName, String password, String email, String name,
			String role) {
		try {
			application.User user = new application.User(userName, password, email, name, role);
			// ensure role list contains the role (User constructor already does)
			register(user);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Store a reviewer profile in {@code reviewer_profiles} table 
	 * @param userId 
	 * @param userName
	 * @throws SQLException if database error occurs 
	 */

	public void createReviewerProfile(int userId, String userName) throws SQLException {
		String query = "INSERT INTO reviewer_profiles (reviewer_id, name, experience) VALUES (?,?,?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, "");
			pstmt.executeUpdate();
		}
	}
	/**
	 * retrieves all reviewer profiles 
	 * @return a list of {@link ReviewerProfile} objects 
	 * @throws SQLException if database error occurs 
	 */

	public List<ReviewerProfile> getAllReviewerProfiles() throws SQLException {
		List<ReviewerProfile> profiles = new ArrayList<>();
		String query = "SELECT r.reviewer_id,r.name,r.experience FROM reviewer_profiles r";

		try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				int reviewerId = rs.getInt("reviewer_id");
				String name = rs.getString("name");
				String experience = rs.getString("experience");

				List<Review> reviews = getReviewsByReviewer(name);
				List<Feedback> feedback = getFeedbackByReviewer(name);
				profiles.add(new ReviewerProfile(reviewerId, name, experience, reviews,feedback));

			}
		}
		return profiles;

	}
	
	/**
	 * retrieves all reviews associated with a specific reviewer 
	 * @param reviewerUserName
	 * @return a list of {@link Review) objects 
	 * @throws SQLException if database error occurs 
	 */
	public List<Review> getReviewsByReviewer(String reviewerUserName) throws SQLException {
	    if (connection == null || connection.isClosed()) connectToDatabase();
	    List<Review> reviews = new ArrayList<>();
	    String query = "SELECT * FROM answer_reviews WHERE reviewerUserName = ? ORDER BY createdAt DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, reviewerUserName);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Review r = new Review(
	                    rs.getString("reviewId"),
	                    rs.getString("answerId"),
	                    reviewerUserName,
	                    rs.getString("reviewContent"),
	                    rs.getTimestamp("createdAt"),
	                    rs.getString("parentReviewId")
	                );
	                // Fetch feedback count for this specific review
	                r.setFeedbackCount(getFeedbackCountForReview(r.getReviewId()));
	                reviews.add(r);
	            }
	        }
	    }
	    return reviews;
	}
	
	/**
     * Counts the number of private feedback messages associated with a specific review.
     * <p>
     * Feedback messages for a review are identified by having a {@code questionId}
     * starting with "REV:" followed by the review ID.
     *
     * @param reviewId The unique ID of the review.
     * @return The count of feedback messages for the specified review.
     * @throws SQLException If a database access error occurs.
     */
	public int getFeedbackCountForReview(String reviewId) throws SQLException { //added by JA
	    String query = "SELECT COUNT(*) FROM PrivateMessages WHERE questionId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, "REV:" + reviewId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) return rs.getInt(1);
	        }
	    }
	    return 0;
	}
	
	/**
     * Deletes a review from the database based on its review ID.
     *
     * @param reviewId The unique ID of the review to delete.
     * @return true if the review was successfully deleted, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
	public boolean deleteReview(String reviewId) throws SQLException { //added by JA
	    if (connection == null || connection.isClosed()) connectToDatabase();
	    String query = "DELETE FROM answer_reviews WHERE reviewId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, reviewId);
	        return pstmt.executeUpdate() > 0;
	    }
	}
	
	/**
	 * Retrieves a specific reviewer's profile 
	 * @param reviewerId
	 * @return a {@link ReviewerProfile}  for the given reviewer
	 * @throws SQLException if database error occurs 
	 */
	public ReviewerProfile getReviewerProfileById(int reviewerId) throws SQLException {
		String query = "SELECT * FROM reviewer_profiles WHERE reviewer_id = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setInt(1,  reviewerId);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				String name = rs.getString("name");
				String experience = rs.getString("experience");
				
				List<Review> reviews = getReviewsByReviewer(name);
				List<Feedback>feedback = getFeedbackByReviewer(name);
				return new ReviewerProfile(reviewerId , name, experience,reviews,feedback);
			}
			
		}
		return null;
	}
	/**
	 * Retrieves all feedback associated with a specific reviewer 
	 * @param reviewerUser
	 * @return a list of {@link Feedback} objects
	 * @throws SQLException if database error occurs 
	 */

	public List<Feedback> getFeedbackByReviewer(String reviewerUser) throws SQLException {
		List<Feedback> feedback = new ArrayList<>();
		String query = "SELECT * FROM PrivateMessages WHERE toUser = ? AND questionId LIKE 'REVFB:%'";
		try(PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setString(1, reviewerUser);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String feedbackId = String.valueOf(rs.getInt("id"));
				String reviewId = rs.getString("questionId");
				String studentUser = rs.getString("fromUser");
				String feedbackContent = rs.getString("content");
				Timestamp createdAt = rs.getTimestamp("createdAt");
				
				int rating = 0;
				feedback.add(new Feedback(feedbackId,reviewId,studentUser,rating,feedbackContent,createdAt));
			}
		}
		
		return feedback;
	}
	/**
	 * update a reviewer's experience field
	 * @param reviewerId 
	 * @param newExperience
	 * @throws SQLException if database error occurs 
	 */

	public void updateReviewerExperience(int reviewerId, String newExperience) throws SQLException {
		String query = "UPDATE reviewer_profiles SET experience = ?  WHERE reviewer_id = ? ";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newExperience);
			pstmt.setInt(2, reviewerId);
			pstmt.executeUpdate();
		}

	}
	/**
	 * retrieve user id for a user 
	 * @param username
	 * @return user's id 
	 * @throws SQLException if database error occurs 
	 */
	public int getUserIdByUsername(String username) throws SQLException {
		String query = "SELECT id FROM cse360users WHERE userName = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setString(1,  username);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
			}
		}
		return -1;
	}
	/**
	 * checks if a profile exists for reviewer 
	 * @param reviewerId 
	 * @return boolean value (true if user exists)
	 * @throws SQLException if database error occurs 
	 */
	public boolean reviewerProfileExists(int reviewerId) throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM reviewer_profiles WHERE reviewer_id = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setInt(1,  reviewerId);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return rs.getInt("count") > 0;
			}
		}
		return false;
	}

    /**
     * Returns all trusted reviewers and their weights for a student
     * @param studentUserName The student whose trust list is being retrieved.
     * @return A map of reviewerUserName to weight.
     */
    public Map<String, Double> getTrustedReviewers(String studentUserName) throws SQLException {
        if(connection == null || connection.isClosed()) connectToDatabase();
        Map<String, Double> trusted = new HashMap<>();

        String sql = "SELECT reviewerUserName, weight FROM trusted_reviewers WHERE studentUserName = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, studentUserName);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    trusted.put(rs.getString("reviewerUserName"), rs.getDouble("weight"));
                }
            }
        }
        return trusted;
    }

    /**
     * Inserts or updates a trusted reviewer entry.
     * @param studentUserName The student assigning trust.
     * @param reviewerUserName The reviewer being trusted.
     * @param weight The weight assigned to the reviewer.
     */
    public boolean updateTrustedReviewer(String studentUserName, String reviewerUserName, double weight) throws SQLException {
        if(connection == null || connection.isClosed()) connectToDatabase();

        String merge = "MERGE INTO trusted_reviewers (studentUserName, reviewerUserName, weight)"
                + "KEY (studentUserName, reviewerUserName, weight) VALUES (?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(merge)){
            ps.setString(1, studentUserName);
            ps.setString(2, reviewerUserName);
            ps.setDouble(3, weight);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Removes a reviewer from a student's trusted list.
     * @param studentUserName The student removing trust.
     * @param reviewerUserName The reviewer is no longer trusted.
     */
    public boolean removeTrustedReviewer(String studentUserName, String reviewerUserName) throws SQLException {
        if(connection == null || connection.isClosed()) connectToDatabase();

        String sql = "DELETE FROM trusted_reviewers WHERE studentUserName = ? AND reviewerUserName = ?";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentUserName);
            ps.setString(2, reviewerUserName);
            return ps.executeUpdate() > 0;
        }
    }
    
    //Used for Staff
    /**
     * Get all private messages for staff review
     * @return list of all PrivateMessage objects
     * @throws SQLException if database error occurs
     */
    public List<PrivateMessage> getAllPrivateMessages() throws SQLException {
        List<PrivateMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM PrivateMessages ORDER BY createdAt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                PrivateMessage msg = new PrivateMessage(
                    rs.getInt("id"),
                    rs.getString("questionId"),
                    rs.getString("fromUser"),
                    rs.getString("toUser"),
                    rs.getString("content"),
                    rs.getTimestamp("createdAt"),
                    rs.getBoolean("isRead")
                );
                messages.add(msg);
            }
        }
        return messages;
    }

    /**
     * Insert a content flag into the database
     * @param contentType type of content (question, answer, message, review)
     * @param contentId ID of the flagged content
     * @param flaggedBy username of staff member flagging
     * @param reason reason for flagging
     * @return true if insertion succeeded
     * @throws SQLException if database error occurs
     */
    public boolean flagContent(String contentType, String contentId, String flaggedBy, String reason) throws SQLException {
        String query = "INSERT INTO content_flags (contentType, contentId, flaggedBy, reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, contentType);
            pstmt.setString(2, contentId);
            pstmt.setString(3, flaggedBy);
            pstmt.setString(4, reason);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all flagged content
     * @return list of ContentFlag objects
     * @throws SQLException if database error occurs
     */
    public List<ContentFlag> getAllFlags() throws SQLException {
        List<ContentFlag> flags = new ArrayList<>();
        String query = "SELECT * FROM content_flags ORDER BY createdAt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                ContentFlag flag = new ContentFlag(
                    rs.getInt("flagId"),
                    rs.getString("contentType"),
                    rs.getString("contentId"),
                    rs.getString("flaggedBy"),
                    rs.getString("reason"),
                    rs.getString("status"),
                    rs.getTimestamp("createdAt")
                );
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * Update flag status
     * @param flagId ID of the flag
     * @param newStatus new status (Reviewed, Resolved)
     * @return true if update succeeded
     * @throws SQLException if database error occurs
     */
    public boolean updateFlagStatus(int flagId, String newStatus) throws SQLException {
        String query = "UPDATE content_flags SET status = ? WHERE flagId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, flagId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get content statistics for staff reports
     * @return Map with various statistics
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getContentStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        // Count questions
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM questions")) {
            if (rs.next()) stats.put("totalQuestions", rs.getInt("count"));
        }
        
        // Count answers
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM answers")) {
            if (rs.next()) stats.put("totalAnswers", rs.getInt("count"));
        }
        
        // Count messages
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM PrivateMessages")) {
            if (rs.next()) stats.put("totalMessages", rs.getInt("count"));
        }
        
        // Count reviews
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM answer_reviews")) {
            if (rs.next()) stats.put("totalReviews", rs.getInt("count"));
        }
        
        // Count unresolved questions
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM questions WHERE isResolved = FALSE")) {
            if (rs.next()) stats.put("unresolvedQuestions", rs.getInt("count"));
        }
        
        // Count pending flags
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM content_flags WHERE status = 'Pending'")) {
            if (rs.next()) stats.put("pendingFlags", rs.getInt("count"));
        }
        
        // Count users by role
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT role, COUNT(*) as count FROM cse360users GROUP BY role")) {
            while (rs.next()) {
                stats.put("users_" + rs.getString("role"), rs.getInt("count"));
            }
        }
        
        return stats;
    }

    /**
     * Get most active users
     * @param limit number of users to return
     * @return list of username and activity count pairs
     * @throws SQLException if database error occurs
     */
    public List<Map<String, Object>> getMostActiveUsers(int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        String query = "SELECT author as userName, COUNT(*) as activityCount " +
                       "FROM (SELECT author FROM questions " +
                       "UNION ALL SELECT author FROM answers) " +
                       "GROUP BY author ORDER BY activityCount DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> userActivity = new HashMap<>();
                    userActivity.put("userName", rs.getString("userName"));
                    userActivity.put("activityCount", rs.getInt("activityCount"));
                    results.add(userActivity);
                }
            }
        }
        return results;
    }
}



