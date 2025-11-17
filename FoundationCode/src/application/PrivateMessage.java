package application;

import java.sql.Timestamp;

/**
 * Represents a private message between users about a question.
 */
public class PrivateMessage {
    private final int id;
    private final String questionId;
    private final String fromUser;
    private final String toUser;
    private final String content;
    private final Timestamp createdAt;
    private final boolean isRead;

    /**
     * Creates a PrivateMessage instance.
     * @param id unique message id
     * @param questionId associated question id
     * @param fromUser sender's username
     * @param toUser recipient's username
     * @param content message body
     * @param createdAt timestamp of creation
     * @param isRead read status
     */
    public PrivateMessage(int id, String questionId, String fromUser, String toUser, String content, Timestamp createdAt, boolean isRead) {
        this.id = id;
        this.questionId = questionId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }
    /** get PM id
     * @return id
     */
    public int getId() { return id; }

    /** get PM questionId
     * @return questionId
     */
    public String getQuestionId() { return questionId; }

    /** get PM fromUser
     * @return fromUser
     */
    public String getFromUser() { return fromUser; }

    /** get PM toUser
     * @return toUser
     */
    public String getToUser() { return toUser; }

    /** get PM content
     * @return content
     */
    public String getContent() { return content; }

    /** get PM createdAt
     * @return createdAt
     */
    public Timestamp getCreatedAt() { return createdAt; }

    /** get PM isRead
     * @return isRead
     */
    public boolean isRead() { return isRead; }
}
