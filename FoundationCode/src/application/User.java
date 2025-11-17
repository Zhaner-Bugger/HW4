package application;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private final StringProperty userName;      // User's userName
    private final StringProperty password;
    private final StringProperty role;
    private List<String> roles = new ArrayList<>();
    private final StringProperty email;
    private final StringProperty name;  // User's real name
    private String activeRole; // Being used to keep track of active user role
    private int userId;
    // In-memory list of trusted reviewer ids for this user (student)
    private final List<Integer> trustedReviewerIds = new ArrayList<>();

    // Constructor to initialize a new User object with userName, password, and role.
    public User(String userName, String password, String email, String name, String role) {
    	this.userName = new SimpleStringProperty(userName);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        if(role != null && !role.isEmpty()) {
            this.roles.add(role);
            this.activeRole = role;
             }
        this.email = new SimpleStringProperty(email);
        this.name = new SimpleStringProperty(name);
    }
    public void setUserId(int userId) { this.userId = userId; }
    
    // Sets the role of the user.
    public void setRole(String role) {
    	this.role.set(role);;
    }

    public void addRole(String role) {
    	if(role != null && !role.isEmpty() && !roles.contains(role)) {
    		roles.add(role);
    	}
    }
    public String getRole() {
    	return roles.isEmpty() ? null : roles.get(0);
    }
    public List<String> getRoles(){
    	return roles;
    }
    
    // Get current User role
    public String getActiveRole() {
        return activeRole != null ? activeRole : getRole();
    }
    
    // Sets User's role
    public void setActiveRole(String activeRole) {
        if (activeRole != null && roles.contains(activeRole)) {
            this.activeRole = activeRole;
        }
    }
    public int getUserId() { return userId; }
    public String getUserName() { return userName.get(); }
    public String getPassword() { return password.get(); }
    public String getEmail() { return email.get(); }
    public String getUserInfoName() { return name.get(); }
    
    public StringProperty userNameProperty() {return userName; }
    public StringProperty roleProperty() { 
    	return new SimpleStringProperty(getRole());
    }
    
    public StringProperty allRolesProperty() {
    	String rolesString = String.join(", ", roles);
    	return new SimpleStringProperty(rolesString);
    }
    /**
     * Add a reviewer id to this user's trusted reviewers list.
     * This is kept in-memory for now; persistence can be added to DatabaseHelper later.
     * @param reviewerId id of the reviewer to trust
     * @return true if added, false if already present
     */
    public boolean addTrustedReviewer(int reviewerId) {
        if (!trustedReviewerIds.contains(reviewerId)) {
            trustedReviewerIds.add(reviewerId);
            return true;
        }
        return false;
    }

    /**
     * Remove a reviewer id from the trusted list.
     * @param reviewerId reviewer id to remove
     * @return true if removed, false if not present
     */
    public boolean removeTrustedReviewer(int reviewerId) {
        return trustedReviewerIds.remove((Integer) reviewerId);
    }

    /**
     * Return the list of trusted reviewer ids for this user.
     * @return unmodifiable list of reviewer ids
     */
    public List<Integer> getTrustedReviewerIds() {
        return java.util.Collections.unmodifiableList(trustedReviewerIds);
    }
    
}
