package cs492.pod.crf;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.model.AffectiveFeatureType;
import cs492.pod.model.DoubleWrapper;
import cs492.pod.model.StylisticFeatureType;
import cs492.pod.model.UserFeatureType;

public class GenOutputNew extends Thread {
  private final Logger logger = LogManager.getLogger(GenOutputNew.class
      .getSimpleName());

  private final String SEP = "\t";

  private final File output;
  private final String drugName;

  public GenOutputNew(File output, String drugName) {
    this.output = output;
    this.drugName = drugName;
  }

  @Override
  public void run() {
    try {
      startProcess(this.output, this.drugName);
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e);
    }
  }

  public void startProcess(File output, final String drugName) throws Exception {
    logger.info("GenOutputNew");

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

    Set<String> symptoms = new HashSet<String>();
    while (rsSideEffects.next()) {
      String symptom = rsSideEffects.getString("symptom");
      if (symptom.trim().isEmpty()) {
        continue;
      }
      symptoms.add(symptom);
    }
    rsSideEffects.close();
    pstmtSelectSideEffects.close();

    pstmtSelectSideEffects = con
        .prepareStatement("SELECT * FROM specific_drug_side_effects WHERE type=? AND symptom=?");
    for (String symptom : symptoms) {
      pstmtSelectSideEffects.setString(1, drugName);
      pstmtSelectSideEffects.setString(2, symptom);
      ResultSet rsSymptomSideEffects = pstmtSelectSideEffects.executeQuery();

      double pi = 1.0;
      String isMoreCommon = "N";
      while (rsSymptomSideEffects.next()) {
        int authorId = rsSideEffects.getInt("author_id");
        int docId = rsSideEffects.getInt("doc_id");
        int frequency = rsSideEffects.getInt("frequency");
        isMoreCommon = rsSideEffects.getString("is_more_common").trim();

        double sigma = 0.0;
        sigma += getAffectiveFeatures(con, authorId, docId) * frequency;
        sigma += getStylisticFeatures(con, authorId, docId) * frequency;
        sigma += getUserFeatures(con, authorId) * frequency;
        pi *= Math.exp(sigma);
      }

      pw.println(isMoreCommon + "\t" + pi);
      rsSymptomSideEffects.close();
    }

    pw.close();
  }

  private double getAffectiveFeatures(Connection con, int authorId, int docId)
      throws SQLException {
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

    while (rsAffective.next()) {
      String type = rsAffective.getString("type");
      double frequency = rsAffective.getInt("frequency");
      double length = rsAffective.getInt("length");

      af.put(type, String.format("%.5f", (frequency / length)));
    }
    rsAffective.close();
    pstmtAffective.close();

    // Append to string
    double ans = 0.0;
    for (AffectiveFeatureType feature : features) {
      String val = af.get(feature.name().replace('_', '-'));
      ans += Double.parseDouble(val.trim());
    }
    return ans;
  }

  private double getStylisticFeatures(Connection con, int authorId, int docId)
      throws SQLException {
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

    while (rsStylistic.next()) {
      String type = rsStylistic.getString("type");
      double value = rsStylistic.getInt("value");
      double length = rsStylistic.getInt("length");

      sf.put(type, String.format("%.5f", (value / length)));
    }
    rsStylistic.close();
    pstmtStylistic.close();

    // Append to string
    double ans = 0.0;
    for (StylisticFeatureType feature : features) {
      String val = sf.get(feature.name().replace('_', ' '));
      ans += Double.parseDouble(val.trim());
    }
    return ans;
  }

  private double getUserFeatures(Connection con, int authorId)
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

    while (rsUser.next()) {
      String type = rsUser.getString("type");
      String value = rsUser.getString("value").replace(' ', '_');

      uf.put(type, value);
    }
    rsUser.close();
    pstmtUser.close();

    // Append to string
    double ans = 0.0;
    for (UserFeatureType feature : features) {
      String val = uf.get(feature.name());
      ans = Double.parseDouble(val.trim());
    }
    return ans;
  }

  public static void main(String[] args) throws Exception {
    String outputPath = "./out/";
    String[] drugNames = { "Flagyl", "Ibuprofen", "Metformin", "Prilosec",
        "Tirosint", "Xanax" };

    // Ibuprofen Xanax Flagyl

    for (String drugName : drugNames) {
      GenOutputNew go = new GenOutputNew(new File(outputPath + drugName
          + ".txt"), drugName);
      go.start();
    }
  }
}
