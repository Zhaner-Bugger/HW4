package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper providing CRUD and search operations for Question objects.
 */
public class Questions {
    private List<Question> questions;
    
    public Questions() {
        this.questions = new ArrayList<>();
    }
    
    // CRUD Operations
    /**
     * Adds a new Question to the in-memory collection.
     * @param question the Question to add
     */
    public void addQuestion(Question question) {
        questions.add(question);
    }
    
    /**
     * Retrieves a Question by id.
     * @param questionId id to look up
     * @return the Question if found, otherwise null
     */
    public Question getQuestion(String questionId) {
        return questions.stream()
            .filter(q -> q.getQuestionId().equals(questionId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Replaces an existing question with an updated instance (matching by id).
     * @param updatedQuestion the updated Question
     */
    public void updateQuestion(Question updatedQuestion) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getQuestionId().equals(updatedQuestion.getQuestionId())) {
                questions.set(i, updatedQuestion);
                return;
            }
        }
    }
    
    // Updated delete to remove follow up question form main question
    /**
     * Deletes a question and any follow-ups recursively.
     * @param questionId id of the question to delete
     */
    public void deleteQuestion(String questionId) {
        List<String> toDelete = new ArrayList<>();
        toDelete.add(questionId);

        // Recursively find all follow-ups
        for (int i = 0; i < toDelete.size(); i++) {
            String currentId = toDelete.get(i);
            questions.stream()
                .filter(q -> currentId.equals(q.getFollowUpOf()))
                .forEach(q -> toDelete.add(q.getQuestionId()));
        }

        questions.removeIf(q -> toDelete.contains(q.getQuestionId()));
    }

    // Search operations
    /**
     * Returns a Questions collection filtered by title substring (case-insensitive).
     * @param keyword substring to search for
     * @return Questions collection matching the title
     */
    public Questions searchByTitle(String keyword) {
        Questions result = new Questions();
        result.questions = questions.stream()
            .filter(q -> q.getTitle().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return result;
    }
    
    /**
     * Returns Questions authored by the given user.
     * @param author author userName to filter by
     * @return Questions collection authored by the user
     */
    public Questions searchByAuthor(String author) {
        Questions result = new Questions();
        result.questions = questions.stream()
            .filter(q -> q.getAuthor().equalsIgnoreCase(author))
            .collect(Collectors.toList());
        return result;
    }
    
    /**
     * Returns a copy of all questions.
     * @return list copy of all questions
     */
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    /**
     * Returns only unresolved questions as a Questions collection.
     * @return Questions collection of unresolved questions
     */
    public Questions getUnresolvedQuestions() {
    	Questions result = new Questions();
    	result.questions = questions.stream().filter(q -> !q.getIsResolved()).collect(java.util.stream.Collectors.toList());
    	return result;
    }
     
    // Helper to retrieve follow up questions
    /**
     * Retrieves follow-up questions for a given parent question id.
     * @param parentQuestionId id of the parent question
     * @return list of follow-up Question instances
     */
    public List<Question> getFollowUps(String parentQuestionId) {
        return questions.stream()
            .filter(q -> parentQuestionId.equals(q.getFollowUpOf()))
            .collect(Collectors.toList());
    }
    
    /**
     * Returns true when there are no questions stored.
     * @return true when empty
     */
    public boolean isEmpty() {
        return questions.isEmpty();
    }
    
    /**
     * Returns the number of questions in the collection.
     * @return question count
     */
    public int size() {
        return questions.size();
    }
}