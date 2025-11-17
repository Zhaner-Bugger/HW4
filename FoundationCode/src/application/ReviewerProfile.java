package application;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a reviewer profile
 */
public class ReviewerProfile {
	private final int reviewerId;
	private final StringProperty name;
	private final StringProperty experience;
	private List<Review> reviews;
	private List<Feedback> feedback; 
	
	/**
	 * Creates a reviewer profile instance 
	 * @param reviewerId unique id belonging to the reviewer
	 * @param name   reviewer's username
	 * @param experience   reviewer's experience text
	 * @param reviews    reviews made by reviewer
	 * @param feedback    feedback reviewer has received from students
	 */
		
	public ReviewerProfile(int reviewerId, String name, String experience, List<Review> reviews, List<Feedback> feedback){
		this.reviewerId = reviewerId;
		this.name = new SimpleStringProperty(name);
		this.experience = new SimpleStringProperty(experience);
		this.reviews = reviews;
		this.feedback = feedback;
		
	}
	/**
	 * @return reviewerId
	 */
	public int getReviewerId() { return reviewerId; }
	/**
	 * @return reviewer's name
	 */
	public String getName() { return name.get(); }
	/**
	 * @return reviewer's experience
	 */
	public String getExperience() { return experience.get(); }
	/**
	 * @return reviews
	 */
	public List<Review> getReviews() { return reviews; }
	/**
	 * @return feedback
	 */
	public List<Feedback> getFeedback() { return feedback;}
	
	public StringProperty nameProperty() { return name; }
	public StringProperty experienceProperty() { return experience; }

	
	
	/**
	 * 
	 * @param experience 
	 */
	public void setExperience(String experience) {
		this.experience.set(experience);
	}
	
	

}
