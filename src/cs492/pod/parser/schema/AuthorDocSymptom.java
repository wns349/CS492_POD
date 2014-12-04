package cs492.pod.parser.schema;

public class AuthorDocSymptom {
  private int authorId;
  private int docId;
  private String[] symptoms;

  public AuthorDocSymptom(int authorId, int docId, String... symptoms) {
    super();
    this.authorId = authorId;
    this.docId = docId;
    this.symptoms = symptoms;
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

  public String[] getSymptoms() {
    return symptoms;
  }

  public void setSymptoms(String... symptoms) {
    this.symptoms = symptoms;
  }

}
