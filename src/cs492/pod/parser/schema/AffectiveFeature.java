package cs492.pod.parser.schema;

import java.util.ArrayList;
import java.util.List;

public class AffectiveFeature {
  private String affectiveCategory;
  private List<String> affectiveWordList;

  public AffectiveFeature(String affectiveCategory, String... affectiveWords) {
    this.affectiveCategory = affectiveCategory;
    this.affectiveWordList = new ArrayList<String>();
    for (String affectiveWord : affectiveWords) {
      this.affectiveWordList.add(affectiveWord.trim());
    }
  }

  public String getAffectiveCategory() {
    return affectiveCategory;
  }

  public void setAffectiveCategory(String affectiveCategory) {
    this.affectiveCategory = affectiveCategory;
  }

  public List<String> getAffectiveWordList() {
    return affectiveWordList;
  }

  public void setAffectiveWordList(List<String> affectiveWordList) {
    this.affectiveWordList = affectiveWordList;
  }
  
  public String toString(){
    StringBuilder sb = new StringBuilder();
    
    sb.append(this.affectiveCategory);
    sb.append("\t");
    for(String affectiveWord : this.affectiveWordList){
      sb.append(affectiveWord);
      sb.append("\t");
    }
    
    return sb.toString();
  }
}
