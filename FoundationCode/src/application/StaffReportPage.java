package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * The StaffReportPage class provides comprehensive reporting and analytics
 * functionality for staff members to monitor system health and user activity.
 * </p>
 * 
 * <p>
 * This page implements User Story 3: "As a staff member, I want to summarize 
 * trends or issues from interactions, so that I can provide feedback to 
 * instructors and other staff."
 * </p>
 * 
 * <p>
 * The page displays:
 * <ul>
 *   <li>Content statistics (counts of questions, answers, reviews, messages)</li>
 *   <li>Issue indicators (unresolved questions, pending flags)</li>
 *   <li>User activity metrics (user counts by role, most active users)</li>
 *   <li>Automated analysis and recommendations based on current trends</li>
 *   <li>Exportable report generation for sharing with instructors</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The analysis section automatically identifies potential issues such as:
 * <ul>
 *   <li>High percentage of unresolved questions</li>
 *   <li>Excessive pending flags requiring attention</li>
 *   <li>Low answer-to-question ratios</li>
 *   <li>Poor user engagement rates</li>
 * </ul>
 * </p>
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-16
 */
public class StaffReportPage {
	/** Database helper for accessing system statistics */
    private final DatabaseHelper databaseHelper;
    
    /** Currently logged-in staff user */
    private final User currentUser;
    
    /**
     * Constructs a new StaffReportPage.
     * 
     * @param databaseHelper the database helper for statistics access
     * @param currentUser the currently logged-in staff user
     */
    public StaffReportPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    /**
     * Displays the staff report page in the provided stage.
     * Creates a scrollable view with statistics, trends, and analysis sections.
     * 
     * @param primaryStage the stage to display the report page
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: top-center;");
        
        Label titleLabel = new Label("System Reports & Trends");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Statistics section
        VBox statsBox = createStatisticsSection();
        
        // Trends section
        VBox trendsBox = createTrendsSection();
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button refreshButton = new Button("Refresh Data");
        Button exportButton = new Button("Export Report");
        Button backButton = new Button("Back");
        
        refreshButton.setOnAction(e -> show(primaryStage)); // Reload page
        exportButton.setOnAction(e -> exportReport());
        backButton.setOnAction(e -> {
            new StaffHomePage(databaseHelper, currentUser).show(primaryStage);
        });
        
        buttonBox.getChildren().addAll(refreshButton, exportButton, backButton);
        
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(10));
        contentBox.getChildren().addAll(statsBox, trendsBox);
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        
        layout.getChildren().addAll(titleLabel, scrollPane, buttonBox);
        
        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Reports");
    }
    
    /**
     * Creates the statistics section showing system-wide content counts and metrics.
     * Includes total counts for questions, answers, reviews, messages, and user statistics.
     * Highlights issues such as unresolved questions and pending flags.
     * 
     * @return a VBox containing the statistics section
     */
    private VBox createStatisticsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("Content Statistics");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(10));
        
        try {
            Map<String, Integer> stats = databaseHelper.getContentStatistics();
            
            int row = 0;
            
            // Total content counts
            addStatRow(statsGrid, row++, "Total Questions:", String.valueOf(stats.getOrDefault("totalQuestions", 0)));
            addStatRow(statsGrid, row++, "Total Answers:", String.valueOf(stats.getOrDefault("totalAnswers", 0)));
            addStatRow(statsGrid, row++, "Total Reviews:", String.valueOf(stats.getOrDefault("totalReviews", 0)));
            addStatRow(statsGrid, row++, "Total Messages:", String.valueOf(stats.getOrDefault("totalMessages", 0)));
            
            row++; // Add spacing
            
            // Issue indicators
            addStatRow(statsGrid, row++, "Unresolved Questions:", 
                      String.valueOf(stats.getOrDefault("unresolvedQuestions", 0)), true);
            addStatRow(statsGrid, row++, "Pending Flags:", 
                      String.valueOf(stats.getOrDefault("pendingFlags", 0)), true);
            
            row++; // Add spacing
            
            // User statistics
            addStatRow(statsGrid, row++, "Total Users (admin):", 
                      String.valueOf(stats.getOrDefault("users_admin", 0)));
            addStatRow(statsGrid, row++, "Total Users (student):", 
                      String.valueOf(stats.getOrDefault("users_student", 0)));
            addStatRow(statsGrid, row++, "Total Users (instructor):", 
                      String.valueOf(stats.getOrDefault("users_instructor", 0)));
            addStatRow(statsGrid, row++, "Total Users (reviewer):", 
                      String.valueOf(stats.getOrDefault("users_reviewer", 0)));
            addStatRow(statsGrid, row++, "Total Users (staff):", 
                      String.valueOf(stats.getOrDefault("users_staff", 0)));
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading statistics: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            statsGrid.add(errorLabel, 0, 0, 2, 1);
        }
        
        section.getChildren().addAll(sectionTitle, statsGrid);
        return section;
    }
    
    /**
     * Creates the trends section showing user activity and automated analysis.
     * Displays the most active users and provides intelligent recommendations
     * based on current system health metrics.
     * 
     * @return a VBox containing the trends and analysis section
     */
    private VBox createTrendsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label sectionTitle = new Label("User Activity Trends");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox trendsBox = new VBox(10);
        
        try {
            // Most active users
            Label activeUsersLabel = new Label("Top 10 Most Active Users:");
            activeUsersLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            List<Map<String, Object>> activeUsers = databaseHelper.getMostActiveUsers(10);
            
            GridPane activeUsersGrid = new GridPane();
            activeUsersGrid.setHgap(15);
            activeUsersGrid.setVgap(5);
            activeUsersGrid.setPadding(new Insets(5, 0, 0, 20));
            
            int row = 0;
            for (Map<String, Object> userActivity : activeUsers) {
                String userName = (String) userActivity.get("userName");
                Integer count = (Integer) userActivity.get("activityCount");
                
                Label rankLabel = new Label((row + 1) + ".");
                Label nameLabel = new Label(userName);
                Label countLabel = new Label(count + " contributions");
                
                activeUsersGrid.add(rankLabel, 0, row);
                activeUsersGrid.add(nameLabel, 1, row);
                activeUsersGrid.add(countLabel, 2, row);
                
                row++;
            }
            
            trendsBox.getChildren().addAll(activeUsersLabel, activeUsersGrid);
            
            // Summary analysis
            Label summaryLabel = new Label("\nKey Observations:");
            summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            TextArea summaryArea = new TextArea();
            summaryArea.setEditable(false);
            summaryArea.setPrefRowCount(8);
            summaryArea.setWrapText(true);
            
            StringBuilder summary = new StringBuilder();
            summary.append("=== System Health Summary ===\n\n");
            
            Map<String, Integer> stats = databaseHelper.getContentStatistics();
            
            // Analyze unresolved questions
            int unresolved = stats.getOrDefault("unresolvedQuestions", 0);
            int totalQuestions = stats.getOrDefault("totalQuestions", 0);
            double unresolvedPercent = 0.0;
            if (totalQuestions > 0) {
                unresolvedPercent = (unresolved * 100.0) / totalQuestions;
                summary.append(String.format("• %.1f%% of questions remain unresolved (%d out of %d)\n", 
                                            unresolvedPercent, unresolved, totalQuestions));
                if (unresolvedPercent > 30) {
                    summary.append("  ⚠ HIGH: Consider encouraging more answers or instructor intervention\n");
                }
            }
            
            summary.append("\n");
            
            // Analyze flagged content
            int pendingFlags = stats.getOrDefault("pendingFlags", 0);
            if (pendingFlags > 0) {
                summary.append(String.format("• %d content flags pending review\n", pendingFlags));
                if (pendingFlags > 10) {
                    summary.append("  ⚠ HIGH: Immediate attention recommended\n");
                }
            } else {
                summary.append("• No pending content flags - system looks clean\n");
            }
            
            summary.append("\n");
            
            // Analyze answer/question ratio
            int totalAnswers = stats.getOrDefault("totalAnswers", 0);
            if (totalQuestions > 0) {
                double answerRatio = (double) totalAnswers / totalQuestions;
                summary.append(String.format("• Average answers per question: %.2f\n", answerRatio));
                if (answerRatio < 1.0) {
                    summary.append("  ⚠ LOW: Many questions lack answers\n");
                } else if (answerRatio > 2.0) {
                    summary.append("  ✓ GOOD: Questions receiving multiple perspectives\n");
                }
            }
            
            summary.append("\n");
            
            // User engagement
            int totalUsers = stats.getOrDefault("users_student", 0) + 
                           stats.getOrDefault("users_instructor", 0) +
                           stats.getOrDefault("users_reviewer", 0);
            if (!activeUsers.isEmpty() && totalUsers > 0) {
                int activeCount = activeUsers.size();
                double engagementRate = (activeCount * 100.0) / totalUsers;
                summary.append(String.format("• User engagement rate: %.1f%%\n", engagementRate));
                if (engagementRate < 20) {
                    summary.append("  ⚠ LOW: Consider strategies to increase participation\n");
                }
            }
            
            summary.append("\n=== Recommendations ===\n\n");
            
            // Generate recommendations
            if (unresolvedPercent > 30) {
                summary.append("• Encourage instructors to review and answer unresolved questions\n");
            }
            if (pendingFlags > 0) {
                summary.append("• Review and address flagged content promptly\n");
            }
            if (totalAnswers < totalQuestions) {
                summary.append("• Promote answer contributions - consider incentives or recognition\n");
            }
            if (!activeUsers.isEmpty() && activeUsers.size() < 5) {
                summary.append("• Consider outreach to increase user engagement\n");
            }
            
            summaryArea.setText(summary.toString());
            
            trendsBox.getChildren().addAll(summaryLabel, summaryArea);
            
        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading trends: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            trendsBox.getChildren().add(errorLabel);
        }
        
        section.getChildren().addAll(sectionTitle, trendsBox);
        return section;
    }
    
    
    /**
     * Adds a single statistic row to the statistics grid.
     * 
     * @param grid the GridPane to add the row to
     * @param row the row index
     * @param label the label text for the statistic
     * @param value the value to display
     */
    private void addStatRow(GridPane grid, int row, String label, String value) {
        addStatRow(grid, row, label, value, false);
    }
    
    /**
     * Adds a single statistic row to the statistics grid with optional highlighting.
     * Highlighted rows are displayed in red to draw attention to potential issues.
     * 
     * @param grid the GridPane to add the row to
     * @param row the row index
     * @param label the label text for the statistic
     * @param value the value to display
     * @param highlight whether to highlight this statistic (true for issues/warnings)
     */
    private void addStatRow(GridPane grid, int row, String label, String value, boolean highlight) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");
        
        Label valueNode = new Label(value);
        if (highlight) {
            valueNode.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            valueNode.setStyle("-fx-font-size: 14px;");
        }
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
    
    /**
     * Generates and displays an exportable text report with all statistics and analysis.
     * The report can be copied and shared with instructors or other staff members.
     * Includes timestamp, content statistics, and top active users.
     */
    private void exportReport() {
        try {
            Map<String, Integer> stats = databaseHelper.getContentStatistics();
            List<Map<String, Object>> activeUsers = databaseHelper.getMostActiveUsers(10);
            
            StringBuilder report = new StringBuilder();
            report.append("========================================\n");
            report.append("     STAFF SYSTEM REPORT\n");
            report.append("     Generated: ").append(java.time.LocalDateTime.now()).append("\n");
            report.append("========================================\n\n");
            
            report.append("CONTENT STATISTICS\n");
            report.append("------------------\n");
            report.append("Total Questions: ").append(stats.getOrDefault("totalQuestions", 0)).append("\n");
            report.append("Total Answers: ").append(stats.getOrDefault("totalAnswers", 0)).append("\n");
            report.append("Total Reviews: ").append(stats.getOrDefault("totalReviews", 0)).append("\n");
            report.append("Total Messages: ").append(stats.getOrDefault("totalMessages", 0)).append("\n");
            report.append("Unresolved Questions: ").append(stats.getOrDefault("unresolvedQuestions", 0)).append("\n");
            report.append("Pending Flags: ").append(stats.getOrDefault("pendingFlags", 0)).append("\n\n");
            
            report.append("TOP ACTIVE USERS\n");
            report.append("----------------\n");
            int rank = 1;
            for (Map<String, Object> user : activeUsers) {
                report.append(rank++).append(". ")
                      .append(user.get("userName")).append(" - ")
                      .append(user.get("activityCount")).append(" contributions\n");
            }
            
            // Display in dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Report");
            alert.setHeaderText("Report Generated");
            
            TextArea reportArea = new TextArea(report.toString());
            reportArea.setEditable(false);
            reportArea.setWrapText(false);
            reportArea.setPrefRowCount(20);
            reportArea.setPrefColumnCount(60);
            
            alert.getDialogPane().setContent(reportArea);
            alert.getDialogPane().setPrefWidth(700);
            alert.setContentText("Copy the report below:");
            alert.showAndWait();
            
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setContentText("Failed to generate report: " + e.getMessage());
            alert.showAndWait();
        }
    }
}