package application;

/**
 * Validator for answer content used by UI forms.
 */
public class AnswerValidator {
    
    public static String validateAnswer(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Answer content cannot be empty";
        }
        if (content.length() > 10000) {
            return "Answer content too long (max 10000 characters)";
        }
        return ""; // No errors
    }
}