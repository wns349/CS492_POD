package cs492.pod.crf;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.model.AffectiveFeatureType;
import cs492.pod.model.StylisticFeatureType;
import cs492.pod.model.UserFeatureType;

public class GenOutput extends Thread {
  private final Logger logger = LogManager.getLogger(GenOutput.class
      .getSimpleName());

  private final String SEP = "\t";
  
  private final File output ;
  private final String drugName;
  
  public GenOutput(File output, String drugName){
    this.output = output;
    this.drugName = drugName;
  }
  
  @Override
  public void run(){
    try {
      startProcess(this.output, this.drugName);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e);
    }
  }

  public void startProcess(File output, final String drugName) throws Exception {
    logger.info("GenOutput");

    if (output.exists()) {
      throw new Exception("Output file already exists.");
    }

    PrintWriter pw = new PrintWriter(output);

    // Connect
    DBClient.getInstance().connect();
    Connection con = DBClient.getInstance().getConnection();

    PreparedStatement pstmtSelectSideEffects = con
        .prepareStatement("SELECT * FROM specific_drug_side_effects WHERE type=?");
    pstmtSelectSideEffects.setString(1, drugName);
    ResultSet rsSideEffects = pstmtSelectSideEffects.executeQuery();

    while (rsSideEffects.next()) {
      StringBuilder sb = new StringBuilder();
      int authorId = rsSideEffects.getInt("author_id");
      int docId = rsSideEffects.getInt("doc_id");
      int frequency = rsSideEffects.getInt("frequency");
      String symptom = rsSideEffects.getString("symptom").trim();
      String isMoreCommon = rsSideEffects.getString("is_more_common").trim();
      if (symptom.isEmpty()) {
        continue;
      }

      // Add basic info
      sb.append(isMoreCommon).append(SEP);

      // Affective Features
      getAffectiveFeatures(con, sb, authorId, docId);

      // Stylistic Features
      getStylisticFeatures(con, sb, authorId, docId);

      // User Features
      getUserFeatures(con, sb, authorId);


      String outputLine = sb.toString().trim();
      for(int i=0 ; i < frequency; i++){
        pw.println(outputLine);
      }
      logger.info("------ {} {} {}", authorId, docId, outputLine);
    }

    rsSideEffects.close();
    pstmtSelectSideEffects.close();

    pw.close();
  }

  private void getAffectiveFeatures(Connection con, StringBuilder sb,
      int authorId, int docId) throws SQLException {
    logger.info("Affective Features {} {}", authorId, docId);
    // Affective Features
    AffectiveFeatureType[] features = AffectiveFeatureType.values();
    PreparedStatement pstmtAffective = con
        .prepareStatement("SELECT * FROM affective_feature_values WHERE author_id = ? AND doc_id = ?");

    Map<String, String> af = new HashMap<String, String>();
    for (AffectiveFeatureType feature : features) {
      af.put(feature.name().replace('_', '-'), "0.0");
    }
    
    pstmtAffective.setInt(1, authorId);
    pstmtAffective.setInt(2, docId);
    ResultSet rsAffective = pstmtAffective.executeQuery();

    while(rsAffective.next()){
      String type = rsAffective.getString("type");
      double frequency = rsAffective.getInt("frequency");
      double length = rsAffective.getInt("length");
      
      af.put(type, String.format("%.5f", (frequency / length)));
    }
    rsAffective.close();
    pstmtAffective.close();
    
    // Append to string
    for(AffectiveFeatureType feature: features){
      String val = af.get(feature.name().replace('_', '-'));
      sb.append(feature.name()+"=");
      sb.append(val).append(SEP);
    }
  }

  private void getStylisticFeatures(Connection con, StringBuilder sb,
      int authorId, int docId) throws SQLException {
    logger.info("Stylistic Features {} {}", authorId, docId);
    // Stylistic Features
    StylisticFeatureType[] features = StylisticFeatureType.values();
    PreparedStatement pstmtStylistic = con
        .prepareStatement("SELECT * FROM stylistic_feature_values WHERE author_id = ? AND doc_id = ?");

    Map<String, String> sf = new HashMap<String, String>();
    for (StylisticFeatureType feature : features) {
      sf.put(feature.name().replace('_', ' '), "0.0");
    }
    
    pstmtStylistic.setInt(1, authorId);
    pstmtStylistic.setInt(2, docId);
    ResultSet rsStylistic = pstmtStylistic.executeQuery();

    while(rsStylistic.next()){
      String type = rsStylistic.getString("type");
      double value = rsStylistic.getInt("value");
      double length = rsStylistic.getInt("length");
      
      sf.put(type, String.format("%.5f", (value / length)));
    }
    rsStylistic.close();
    pstmtStylistic.close();
    
    // Append to string
    for(StylisticFeatureType feature: features){
      String val = sf.get(feature.name().replace('_', ' '));
      sb.append(feature.name()+"=");
      sb.append(val).append(SEP);
    }
  }

  private void getUserFeatures(Connection con, StringBuilder sb, int authorId)
      throws SQLException {
    logger.info("User Features {}", authorId);
    // User Features
    UserFeatureType[] features = UserFeatureType.values();
    PreparedStatement pstmtUser = con
        .prepareStatement("SELECT * FROM author_feature_values WHERE author_id = ? ");

    Map<String, String> uf = new HashMap<String, String>();
    for (UserFeatureType feature : features) {
      uf.put(feature.name(), "0.0");
    }
    
    pstmtUser.setInt(1, authorId);
    ResultSet rsUser = pstmtUser.executeQuery();

    while(rsUser.next()){
      String type = rsUser.getString("type");
      String value = rsUser.getString("value").replace(' ', '_');
      
      uf.put(type, value);
    }
    rsUser.close();
    pstmtUser.close();
    
    // Append to string
    for(UserFeatureType feature: features){
      String val = uf.get(feature.name());
      sb.append(feature.name()+"=");
      sb.append(val).append(SEP);
    }
  }

  public static void main(String[] args) throws Exception {
    String outputPath = "./out/";
    String[] drugNames = { "Flagyl", "Ibuprofen", "Metformin", "Prilosec",
        "Tirosint", "Xanax" };
    
    //Ibuprofen Xanax Flagyl
    
    for(String drugName : drugNames){
      GenOutput go = new GenOutput(new File(outputPath + drugName + ".txt"), drugName);
      go.start();
    }
  }
}
