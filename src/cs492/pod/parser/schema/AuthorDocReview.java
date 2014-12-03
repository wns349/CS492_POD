package cs492.pod.parser.schema;

public class AuthorDocReview {
  private int authorId;
  private int doctorId;
  private String post;

  public AuthorDocReview(int authorId, int doctorId, String post) {
    super();
    this.authorId = authorId;
    this.doctorId = doctorId;
    this.post = post;
  }

  public int getAuthorId() {
    return authorId;
  }

  public void setAuthorId(int authorId) {
    this.authorId = authorId;
  }

  public int getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(int doctorId) {
    this.doctorId = doctorId;
  }

  public String getPost() {
    return post;
  }

  public void setPost(String post) {
    this.post = post;
  }

}
