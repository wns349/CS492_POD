package cs492.pod.parser.schema;

public class AuthorDocReview {
  private int authorId;
  private int docId;
  private String post;

  public AuthorDocReview(int authorId, int docId, String post) {
    super();
    this.authorId = authorId;
    this.docId = docId;
    this.post = post;
  }

  public int getAuthorId() {
    return authorId;
  }

  public void setAuthorId(int authorId) {
    this.authorId = authorId;
  }

  public int getDocId() {
    return docId;
  }

  public void setDocId(int docId) {
    this.docId = docId;
  }

  public String getPost() {
    return post;
  }

  public void setPost(String post) {
    this.post = post;
  }

}
