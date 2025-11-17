
package application;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */	
	private final DatabaseHelper databaseHelper;
	private final User currentAdmin;
	
	public AdminHomePage(DatabaseHelper databaseHelper, User currentAdmin) {
		this.databaseHelper = databaseHelper;
		this.currentAdmin = currentAdmin;
	}
	
    public void show(Stage primaryStage) {
    	VBox layout = new VBox(10);
    	
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // label to display the welcome message for the admin
	    Label adminLabel = new Label("Hello, Admin!");
	    
	    adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    TableView<User> userTable = createUserTable();
	    populateUserTable(userTable);
	    
	    HBox buttonBox = new HBox(10);
	    Button refreshButton = new Button("Refresh");
	    Button deleteButton = new Button("Delete User");
	    Button editRoleButton = new Button("Edit Role");

	    Button backButton = new Button("Back");
	    Button tempPasswordButton = new Button("Set Temp Password");

	    
	    refreshButton.setOnAction(e -> populateUserTable(userTable));
	    deleteButton.setOnAction(e -> deleteSelectedUser(userTable));
	    editRoleButton.setOnAction(e -> editRoleOfSelectedUser(userTable));
	    backButton.setOnAction(e -> {
	    	new WelcomeLoginPage(databaseHelper).show(primaryStage,  currentAdmin);
	    });
	    tempPasswordButton.setOnAction(e -> {
	    	new TempPasswordPage().show(databaseHelper, primaryStage, currentAdmin);
	    });
	    
	    buttonBox.getChildren().addAll(refreshButton, deleteButton, editRoleButton, tempPasswordButton, backButton);
	    buttonBox.setAlignment(Pos.CENTER);

	    layout.getChildren().addAll(adminLabel, userTable, buttonBox);
	    Scene adminScene = new Scene(layout, 800, 600);

	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
    }
    
    // Implementation for creating table columns
    private TableView<User> createUserTable() {
    	TableView<User> table = new TableView<>();
    	
    	TableColumn<User, String> userNameCol = new TableColumn<>("Username");
    	userNameCol.setCellValueFactory(cellData -> cellData.getValue().userNameProperty());
    	userNameCol.setMinWidth(150);
    	
    	TableColumn<User, String> roleCol = new TableColumn<>("Roles");
        roleCol.setCellValueFactory(cellData -> cellData.getValue().allRolesProperty());
        roleCol.setMinWidth(200);
    	// We can add more columns as needed here
    	
    	table.getColumns().addAll(userNameCol, roleCol);
    	table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    	return table;
    }
    
    private void populateUserTable(TableView<User> table) {
    	try {
            List<User> usersList = databaseHelper.getAllUsers();
            System.out.println("Number of users retrieved: " + usersList.size());
            
            ObservableList<User> users = FXCollections.observableArrayList(usersList);
            table.setItems(users);
            
            // Debug: Print users to console
            System.out.println("Users in database:");
            for (User user : users) {
                System.out.println("Username: " + user.getUserName() + ", Role: " + user.getRole());
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteSelectedUser(TableView<User> table) {
    	User selectedUser = table.getSelectionModel().getSelectedItem();
    	if (selectedUser == null) {
    		showErrorAlert("Selection Error", "Please select a user to delete.");
    		return;
    	}
    	
    	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    	confirmation.setTitle("Confirm Deletion");
    	confirmation.setHeaderText("Delete User");
    	confirmation.setContentText("Are you sure you want to delete user: " + selectedUser.getUserName() + "?");
    	
    	Optional<ButtonType> result = confirmation.showAndWait();
    	if (result.isPresent() && result.get() == ButtonType.OK) {
    		try {
    			if (databaseHelper.deleteUser(selectedUser.getUserName(), currentAdmin.getUserName())) {
    				showInfoAlert("Success", "User deleted successfully.");
    				populateUserTable(table);
    			} else {
    				showErrorAlert("Error", "Failed to delete user. You cannot delete yourself or the last admin.");
    			}
    		} catch (SQLException e) {
    			showErrorAlert("Database Error", "Failed to delete user: " + e.getMessage());
    		}
    	}
    }
    
    private void editRoleOfSelectedUser(TableView<User> table) {
        User selectedUser = table.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("Selection Error", "Please select a user to edit.");
            return;
        }
        
        
        // Create a dialog for role editing
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Edit User Roles");
        dialog.setHeaderText("Editing roles for: " + selectedUser.getUserName());
        
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        
        // Create the role selection UI
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 20, 20, 20));
        
        
        // ListView for current roles
        ListView<String> rolesListView = new ListView<>();
        ObservableList<String> currentRoles = FXCollections.observableArrayList(selectedUser.getRoles());
        rolesListView.setItems(currentRoles);
        rolesListView.setPrefHeight(150);
        
        
        // ComboBox for available roles
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("admin", "user", "student", "instructor", "staff", "reviewer");
        
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Role");
        Button removeButton = new Button("Remove Selected");
        
        addButton.setOnAction(e -> {
            String newRole = roleComboBox.getValue();
            if (newRole != null && !currentRoles.contains(newRole)) {
                currentRoles.add(newRole); // add array to contain all possible roles JA
            }
        });
        
        removeButton.setOnAction(e -> {
            String selectedRole = rolesListView.getSelectionModel().getSelectedItem();
            if (selectedRole != null) {
                // Prevent admin from removing their own admin role if they're the last admin
                if (selectedUser.getUserName().equals(currentAdmin.getUserName()) && 
                    selectedRole.equals("admin") && 
                    countAdminRoles(currentRoles) <= 1) {
                    showErrorAlert("Error", "Cannot remove your admin role as you're the only admin.");
                    return;
                }
                currentRoles.remove(selectedRole);
            }
        });
        
        buttonBox.getChildren().addAll(roleComboBox, addButton, removeButton);
        
        content.getChildren().addAll(new Label("Current Roles:"), rolesListView, buttonBox);
        dialog.getDialogPane().setContent(content);
        
        // Convert the result to a list of roles when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return currentRoles;
            }
            return null;
        });
        
        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(newRoles -> {
            try {
                // Update the user's roles in the database
                if (databaseHelper.updateUserRoles(selectedUser.getUserName(), newRoles, currentAdmin.getUserName())) {
                    // Update the local user object
                    selectedUser.getRoles().clear();
                    selectedUser.getRoles().addAll(newRoles);
                    
                    showInfoAlert("Success", "User roles updated successfully.");
                    populateUserTable(table);
                } else {
                    showErrorAlert("Error", "Failed to update roles.");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update roles: " + e.getMessage());
            }
        });
    }

    // Helper method to count admin roles
    private int countAdminRoles(List<String> roles) {
        int count = 0;
        for (String role : roles) {
            if ("admin".equals(role)) {
                count++;
            }
        }
        return count;
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }    
    
}