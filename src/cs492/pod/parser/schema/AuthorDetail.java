package cs492.pod.parser.schema;

public class AuthorDetail {
//  
//  CREATE TABLE `pod`.`author_detail` (
//      `author_id` INT NOT NULL,
//      `gender` VARCHAR(6) NULL,
//      `location` TEXT NULL,
//      `post_count` INT NULL,
//      `membership_type` INT NULL,
//      `question_count` INT NULL,
//      `reply_count` INT NULL,
//      `thank_count` INT NULL,
//      PRIMARY KEY (`author_id`))
  
  private int authorId;
  private String gender;
  private String location;
  private int postCount;
  private String membershipType;
  private int questionCount;
  private int replyCount;
  private int thankCount;
  public AuthorDetail(int authorId, String gender, String location,
      int postCount, String membershipType, int questionCount, int replyCount,
      int thankCount) {
    super();
    this.authorId = authorId;
    this.gender = gender;
    this.location = location;
    this.postCount = postCount;
    this.membershipType = membershipType;
    this.questionCount = questionCount;
    this.replyCount = replyCount;
    this.thankCount = thankCount;
  }
  public int getAuthorId() {
    return authorId;
  }
  public void setAuthorId(int authorId) {
    this.authorId = authorId;
  }
  public String getGender() {
    return gender;
  }
  public void setGender(String gender) {
    this.gender = gender;
  }
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  public int getPostCount() {
    return postCount;
  }
  public void setPostCount(int postCount) {
    this.postCount = postCount;
  }
  public String getMembershipType() {
    return membershipType;
  }
  public void setMembershipType(String membershipType) {
    this.membershipType = membershipType;
  }
  public int getQuestionCount() {
    return questionCount;
  }
  public void setQuestionCount(int questionCount) {
    this.questionCount = questionCount;
  }
  public int getReplyCount() {
    return replyCount;
  }
  public void setReplyCount(int replyCount) {
    this.replyCount = replyCount;
  }
  public int getThankCount() {
    return thankCount;
  }
  public void setThankCount(int thankCount) {
    this.thankCount = thankCount;
  }
  
}
