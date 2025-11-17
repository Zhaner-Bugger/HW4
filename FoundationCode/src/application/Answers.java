package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper providing CRUD and search operations for Answer objects.
 */
public class Answers {
    private List<Answer> answers;
    
    public Answers() {
        this.answers = new ArrayList<>();
    }
    
    // CRUD Operations
    /**
     * Adds an Answer to the in-memory collection.
     * @param answer the Answer to add
     */
    public void addAnswer(Answer answer) {
        answers.add(answer);
    }
    
    /**
     * Retrieves an Answer by its id from the collection, or null if not found.
     * @param answerId the id to look up
     * @return the Answer if found, otherwise null
     */
    public Answer getAnswer(String answerId) {
        return answers.stream()
            .filter(a -> a.getAnswerId().equals(answerId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Replaces an existing answer with the provided updated instance (matching by id).
     * @param updatedAnswer the updated Answer instance
     */
    public void updateAnswer(Answer updatedAnswer) {
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).getAnswerId().equals(updatedAnswer.getAnswerId())) {
                answers.set(i, updatedAnswer);
                return;
            }
        }
    }
    
    /**
     * Removes an answer by id from the collection.
     * @param answerId id of the answer to remove
     */
    public void deleteAnswer(String answerId) {
        answers.removeIf(a -> a.getAnswerId().equals(answerId));
    }
    
    // Search operations
    /**
     * Returns all answers for a specific question id as an Answers collection.
     * @param questionId the question id to filter by
     * @return Answers collection containing answers for the question
     */
    public Answers getAnswersForQuestion(String questionId) {
        Answers result = new Answers();
        result.answers = answers.stream()
            .filter(a -> a.getQuestionId().equals(questionId))
            .collect(Collectors.toList());
        return result;
    }
    
    /**
     * Searches answers by content substring (case-insensitive).
     * @param keyword substring to search for
     * @return Answers collection with matching answers
     */
    public Answers searchByContent(String keyword) {
        Answers result = new Answers();
        result.answers = answers.stream()
            .filter(a -> a.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return result;
    }
    
    /**
     * Returns a copy of all answers in the collection.
     * @return list copy of all answers
     */
    public List<Answer> getAllAnswers() {
        return new ArrayList<>(answers);
    }
    
    /**
     * Returns true when there are no answers stored.
     * @return true when empty
     */
    public boolean isEmpty() {
        return answers.isEmpty();
    }
    
    /**
     * Returns the number of answers in the collection.
     * @return answer count
     */
    public int size() {
        return answers.size();
    }
}