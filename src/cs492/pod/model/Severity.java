package cs492.pod.model;

public enum Severity {
  Overdose("Overdose"), MoreCommon("More Common"), LessCommon("Less Common"), Rare(
      "Rare"), NotKnown("Not Known"), ;

  private final String text;

  Severity(String s) {
    this.text = s;
  }

  public String getText() {
    return text;
  }

  public static Severity getSeverity(String s) {
    for (Severity severity : Severity.values()) {
      if (s.trim().equals(severity.getText())) {
        return severity;
      }
    }

    return null;
  }
  
  public static Severity getByOrdinal(int ordinal) {
    for (Severity severity : Severity.values()) {
      if (severity.ordinal() == ordinal) {
        return severity;
      }
    }
    return null;
  }
}
