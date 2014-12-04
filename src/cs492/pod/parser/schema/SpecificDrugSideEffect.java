package cs492.pod.parser.schema;

public class SpecificDrugSideEffect {
  private int authorId;
  private int docId;
  private String symptom;
  private int frequency;

  private String type;
  
  public SpecificDrugSideEffect(int authorId, int docId, String symptom,
      int frequency, String type) {
    super();
    this.authorId = authorId;
    this.docId = docId;
    this.symptom = symptom;
    this.frequency = frequency;
    this.setType(type);
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

  public String getSymptom() {
    return symptom;
  }

  public void setSymptom(String symptom) {
    this.symptom = symptom;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
