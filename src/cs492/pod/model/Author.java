package cs492.pod.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Author {
  private int authorId;
  private String gender;
  private String location;
  private int postCount;
  private String membershipType;
  private int questionCount;
  private int replyCount;
  private int thankCount;

  public Author(ResultSet rs) throws SQLException {
    setAuthorId(rs.getInt("author_id"));
    setGender(rs.getString("gender"));
    setLocation(rs.getString("location"));
    setPostCount(rs.getInt("post_count"));
    setMembershipType(rs.getString("membership_type"));
    setQuestionCount(rs.getInt("question_count"));
    setReplyCount(rs.getInt("reply_count"));
    setThankCount(rs.getInt("thank_count"));
  }

  @Override
  public String toString() {
    return getAuthorId() + "\t" + getGender() + "\t" + getLocation() + "\t"
        + getPostCount() + "\t" + getMembershipType() + "\t"
        + getQuestionCount() + "\t" + getReplyCount() + "\t" + getThankCount();
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
