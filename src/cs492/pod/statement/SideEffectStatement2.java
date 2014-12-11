package cs492.pod.statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;

public class SideEffectStatement2 extends Thread {
  private final Logger logger = LogManager.getLogger(SideEffectStatement2.class
      .getSimpleName());

  private final String drugName;

  public SideEffectStatement2(String drugName) {
    super(drugName);
    this.drugName = drugName;

  }

  @Override
  public void run() {
    try {
      doProcess();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e);
    }

    logger.info(">>>>>{} DONE", getName());
  }

  public void doProcess() throws Exception {
    DBClient.getInstance().connect();
    logger.info("DBClient connection established.");

    Connection con = DBClient.getInstance().getConnection();

    PreparedStatement pstmt = con
        .prepareStatement("SELECT * FROM expert_drug_side_effects WHERE "
            + "drug_family = ? AND frequency = 'More Common'");
    pstmt.setString(1, drugName);

    ResultSet rs = pstmt.executeQuery();

    String sideEffectStemmed = "";
    String sideEffectSyn = "";
    while (rs.next()) {
      sideEffectStemmed += rs.getString("side_effects_stemmed");
      sideEffectSyn += rs.getString("side_effects_synset");
    }

    rs.close();
    pstmt.close();

    PreparedStatement pstmtUpdate = con
        .prepareStatement("UPDATE pod.specific_drug_side_effects SET is_more_common=? WHERE author_id=? AND doc_id = ? AND symptom=?");

    pstmt = con
        .prepareStatement("SELECT * FROM pod.specific_drug_side_effects WHERE type=?");
    pstmt.setString(1, drugName);
    rs = pstmt.executeQuery();
    while (rs.next()) {
      int authorId = rs.getInt("author_id");
      int docId = rs.getInt("doc_id");
      String symptom = rs.getString("symptom");
      String symptomStem = rs.getString("symptom_stem");
      String symptomSynonym = rs.getString("symptom_syn");

      if(symptomStem == null){
        symptomStem = "";
      }
      
      if(symptomSynonym == null){
        symptomSynonym = "";
      }
      
      // Stem
      String[] stems = symptomStem.split(" ");
      boolean isMoreCommon = false;
      for (String stem : stems) {
        if (sideEffectStemmed.contains(stem)) {
          isMoreCommon = true;
          break;
        }
      }
      
      // Synonym
//      if(!isMoreCommon){
//        String[] syns = symptomSynonym.split(" ");
//        for(String syn : syns){
//          if(sideEffectSyn.contains(syn)){
//            isMoreCommon = true;
//            break;
//          }
//        }
//      }
      
      pstmtUpdate.setString(1, isMoreCommon ? "Y" : "N");
      pstmtUpdate.setInt(2, authorId);
      pstmtUpdate.setInt(3, docId);
      pstmtUpdate.setString(4, symptom);
      pstmtUpdate.addBatch();

    }

    pstmtUpdate.executeBatch();

    rs.close();
    pstmt.close();
    pstmtUpdate.close();

    con.commit();
  }

  public static void main(String[] args) throws Exception {
    String[] drugNames = { "Flagyl", "Ibuprofen", "Metformin", "Prilosec",
        "Tirosint", "Xanax" };

    // String[] drugNames = {"Tirosint"};

    Thread[] threads = new Thread[drugNames.length];
    for (int i = 0; i < drugNames.length; i++) {
      threads[i] = new SideEffectStatement2(drugNames[i]);
      threads[i].start();
    }
  }
}
