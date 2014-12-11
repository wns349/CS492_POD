package cs492.pod.crf;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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

  private final File output;
  private final String drugName;

  public GenOutput(File output, String drugName) {
    this.output = output;
    this.drugName = drugName;
  }

  @Override
  public void run() {
    try {
      startProcess(this.output, this.drugName);

      // FileSplit fs = new FileSplit(this.output);
      // fs.start();
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

      // User Trustworthy
      double userTrustworthy = getUserTrustworthy(con, authorId);

      // Affective Features
      getAffectiveFeatures(con, sb, authorId, docId, userTrustworthy);

      // Stylistic Features
      getStylisticFeatures(con, sb, authorId, docId, userTrustworthy);

      // User Features
      getUserFeatures(con, sb, authorId, userTrustworthy);

      String outputLine = sb.toString().trim();
      for (int i = 0; i < frequency; i++) {
        pw.println(outputLine);
      }
      logger.info("------ {} {} {}", authorId, docId, outputLine);
    }

    rsSideEffects.close();
    pstmtSelectSideEffects.close();

    pw.close();
  }

  private Map<Integer, Double> userTrustworthy = null;

  private double getUserTrustworthy(Connection con, int authorId)
      throws Exception {

    if (userTrustworthy == null) {
      userTrustworthy = new HashMap<Integer, Double>();
      PreparedStatement pstmt = con
          .prepareStatement("select author_id, sum(IF(is_more_common='Y',1,0)) / count(*) as T from specific_drug_side_effects group by author_id");
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        userTrustworthy.put(rs.getInt("author_id"), rs.getDouble("T"));
      }
      rs.close();
      pstmt.close();
    }

    if (userTrustworthy.containsKey(authorId)) {
      return userTrustworthy.get(authorId);
    } else {
      return 0;
    }
  }

  private void getAffectiveFeatures(Connection con, StringBuilder sb,
      int authorId, int docId, double userTrustworthy) throws SQLException {
    logger.info("Affective Features {} {}", authorId, docId);
    // Affective Features
    Map<String, Double> normMap = new HashMap<String, Double>();
    normMap.put("admiration",  0.0135);
    normMap.put("affection", 0.5000);
    normMap.put("aggravation", 0.0043);
    normMap.put("aggression",  0.0351);
    normMap.put("alarm", 0.0500);
    normMap.put("alienation",  0.0303);
    normMap.put("ambiguous", 0.3333);
    normMap.put("ambiguous-agitation", 0.0526);
    normMap.put("ambiguous-expectation", 0.1111);
    normMap.put("ambiguous-hope",  0.3333);
    normMap.put("amorousness", 0.0400);
    normMap.put("amour-propre",  0.0417);
    normMap.put("anger", 1.0000);
    normMap.put("angst", 0.0053);
    normMap.put("animosity", 0.0052);
    normMap.put("annoyance", 0.2500);
    normMap.put("anticipation",  1.0000);
    normMap.put("antipathy", 0.0833);
    normMap.put("anxiety", 0.3333);
    normMap.put("anxiousness", 0.3333);
    normMap.put("apathy",  0.0047);
    normMap.put("apprehension",  0.1667);
    normMap.put("approval",  0.0645);
    normMap.put("astonishment",  0.2000);
    normMap.put("attachment",  1.0000);
    normMap.put("awe", 0.0667);
    normMap.put("bang",  0.3333);
    normMap.put("belligerence",  0.0048);
    normMap.put("blue-devils", 0.0323);
    normMap.put("brotherhood", 0.0164);
    normMap.put("calmness",  0.5000);
    normMap.put("captivation", 0.0476);
    normMap.put("cheerfulness",  0.0667);
    normMap.put("chill", 0.4000);
    normMap.put("closeness", 0.1429);
    normMap.put("comfortableness", 0.0297);
    normMap.put("commiseration", 0.1667);
    normMap.put("compassion",  0.3333);
    normMap.put("compatibility", 0.0103);
    normMap.put("confidence",  0.0714);
    normMap.put("confusion", 0.1111);
    normMap.put("contempt",  0.0476);
    normMap.put("contentment", 0.0088);
    normMap.put("coolness",  0.5000);
    normMap.put("covetousness",  0.0120);
    normMap.put("creeps",  0.0400);
    normMap.put("cynicism",  0.1250);
    normMap.put("dander",  0.0556);
    normMap.put("daze",  0.0556);
    normMap.put("defeatism", 0.0435);
    normMap.put("depression",  0.3333);
    normMap.put("despair", 0.0556);
    normMap.put("despondency", 0.0042);
    normMap.put("discomfiture",  0.1000);
    normMap.put("disgust", 1.0000);
    normMap.put("distance",  0.0333);
    normMap.put("distress",  0.1667);
    normMap.put("downheartedness", 0.3333);
    normMap.put("dysphoria", 0.0333);
    normMap.put("eagerness", 1.0000);
    normMap.put("earnestness", 0.5000);
    normMap.put("easiness",  0.2000);
    normMap.put("ego", 0.0110);
    normMap.put("elation", 0.0588);
    normMap.put("embarrassment", 0.0526);
    normMap.put("emotionlessness", 0.0909);
    normMap.put("empathy", 0.0042);
    normMap.put("encouragement", 0.0455);
    normMap.put("enthusiasm",  0.0400);
    normMap.put("enthusiasm-ardor",  0.0194);
    normMap.put("envy",  0.0048);
    normMap.put("euphoria",  0.0049);
    normMap.put("exhilaration",  1.0000);
    normMap.put("fear",  1.0000);
    normMap.put("fearlessness",  0.0476);
    normMap.put("fever", 0.0769);
    normMap.put("fidget",  0.2000);
    normMap.put("fit", 0.1250);
    normMap.put("fondness",  0.0206);
    normMap.put("forgiveness", 0.1000);
    normMap.put("forlornness", 0.0242);
    normMap.put("friendliness",  0.0217);
    normMap.put("frisson", 0.4000);
    normMap.put("frustration", 0.0566);
    normMap.put("fulfillment", 0.0476);
    normMap.put("fury",  0.5000);
    normMap.put("gladness",  0.2500);
    normMap.put("gloom", 1.0000);
    normMap.put("gratitude", 0.0645);
    normMap.put("gravity", 0.1000);
    normMap.put("grief", 0.0235);
    normMap.put("grudge",  0.0077);
    normMap.put("guilt", 0.0426);
    normMap.put("happiness", 0.5000);
    normMap.put("hate",  0.3333);
    normMap.put("hilarity",  0.0435);
    normMap.put("hope",  0.5000);
    normMap.put("hopefulness", 0.3333);
    normMap.put("hopelessness",  0.0217);
    normMap.put("horror",  0.2500);
    normMap.put("huffiness", 0.1429);
    normMap.put("humility",  0.0455);
    normMap.put("hysteria",  0.1000);
    normMap.put("impatience",  0.0286);
    normMap.put("indifference",  0.0909);
    normMap.put("infuriation", 0.0047);
    normMap.put("insecurity",  0.0208);
    normMap.put("irascibility",  0.0250);
    normMap.put("isolation", 0.0192);
    normMap.put("jealousy",  0.1111);
    normMap.put("jitteriness", 0.0556);
    normMap.put("jollity", 0.5000);
    normMap.put("joy", 1.0000);
    normMap.put("joy-pride", 0.0179);
    normMap.put("levity-gaiety", 0.5000);
    normMap.put("like",  1.0000);
    normMap.put("liking",  0.2000);
    normMap.put("lividity",  0.0043);
    normMap.put("lost-sorrow", 1.0000);
    normMap.put("love",  1.0000);
    normMap.put("lovingness",  0.0400);
    normMap.put("maleficence", 0.0235);
    normMap.put("mercifulness",  0.0139);
    normMap.put("misery",  0.2500);
    normMap.put("nausea",  0.1429);
    normMap.put("negative-concern",  0.2500);
    normMap.put("negative-fear", 0.2500);
    normMap.put("oppression",  0.5000);
    normMap.put("panic", 0.3333);
    normMap.put("peace", 0.5000);
    normMap.put("pensiveness", 0.0132);
    normMap.put("pessimism", 0.0263);
    normMap.put("pique", 0.1429);
    normMap.put("placidity", 0.1667);
    normMap.put("positive-concern",  0.1053);
    normMap.put("positive-hope", 0.3333);
    normMap.put("preference",  0.1000);
    normMap.put("protectiveness",  0.0400);
    normMap.put("puppy-love",  0.0833);
    normMap.put("regard",  0.1429);
    normMap.put("regret-sorrow", 1.0000);
    normMap.put("rejoicing", 0.1111);
    normMap.put("repentance",  0.2500);
    normMap.put("resentment",  0.3333);
    normMap.put("resignation", 0.0196);
    normMap.put("reverence", 0.2500);
    normMap.put("sadness", 1.0000);
    normMap.put("sanguinity",  0.0084);
    normMap.put("satisfaction",  0.0556);
    normMap.put("satisfaction-pride",  0.0132);
    normMap.put("scare", 0.2500);
    normMap.put("self-esteem", 0.0303);
    normMap.put("self-pity", 0.0047);
    normMap.put("sensation", 0.0370);
    normMap.put("shadow",  0.0345);
    normMap.put("shame", 0.1429);
    normMap.put("shamefacedness",  0.0341);
    normMap.put("shyness", 0.0435);
    normMap.put("softheartedness", 0.0294);
    normMap.put("stir",  0.2000);
    normMap.put("stupefaction",  0.3333);
    normMap.put("surprise",  0.5000);
    normMap.put("sympathy",  0.1111);
    normMap.put("tenderness",  0.2500);
    normMap.put("thing", 0.0435);
    normMap.put("timidity",  0.0417);
    normMap.put("tranquillity",  0.2000);
    normMap.put("trepidation", 0.2500);
    normMap.put("triumph", 0.0256);
    normMap.put("umbrage", 0.0087);
    normMap.put("weakness",  0.0625);
    normMap.put("weepiness", 0.0041);
    normMap.put("weight",  0.5000);
    normMap.put("withdrawal",  0.0333);
    normMap.put("woe", 0.1667);
    normMap.put("wonder",  0.5000);
    normMap.put("world-weariness", 0.0294);

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

      Double normFactor = normMap.get(type);
      af.put(type,
          String.format("%.5f", (frequency / length) / normFactor * userTrustworthy));
    }
    rsAffective.close();
    pstmtAffective.close();

    // Append to string
    for (AffectiveFeatureType feature : features) {
      String val = af.get(feature.name().replace('_', '-'));
      sb.append(val).append(SEP);
    }
  }

  private void getStylisticFeatures(Connection con, StringBuilder sb,
      int authorId, int docId, double userTrustworthy) throws SQLException {
    logger.info("Stylistic Features {} {}", authorId, docId);

    Map<String, Double> normMap = new HashMap<String, Double>();
    normMap.put("Adjectives", 1.0000);
    normMap.put("Adverbs", 1.0000);
    normMap.put("Common Nouns", 1.0000);
    normMap.put("Conditionals", 0.2000);
    normMap.put("Contrasting Conjunctions", 0.2000);
    normMap.put("Definitie Determiner", 1.0000);
    normMap.put("First Person", 1.0000);
    normMap.put("Following Conjunctions", 0.3333);
    normMap.put("Indefinite Determiner", 1.0000);
    normMap.put("Inferential Conjunctions", 0.4000);
    normMap.put("Neg", 1.0000);
    normMap.put("Proper Nouns", 1.0000);
    normMap.put("QS", 1.0000);
    normMap.put("Second Person", 0.5000);
    normMap.put("Strong Modals", 0.4000);
    normMap.put("Third Person", 0.5000);
    normMap.put("Weak Modals", 0.3333);

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

      Double normFactor = normMap.get(type);
      sf.put(type, String.format("%.5f", (value / length) / normFactor
          * userTrustworthy));
    }
    rsStylistic.close();
    pstmtStylistic.close();

    // Append to string
    for (StylisticFeatureType feature : features) {
      String val = sf.get(feature.name().replace('_', ' '));
      sb.append(val).append(SEP);
    }
  }

  private void getUserFeatures(Connection con, StringBuilder sb, int authorId,
      double userTrustworthy) throws SQLException {
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
      double v = Double.parseDouble(value);

      uf.put(type, String.format("%.5f", v * userTrustworthy));
    }
    rsUser.close();
    pstmtUser.close();

    // Append to string
    for (UserFeatureType feature : features) {
      String val = uf.get(feature.name());
      sb.append(val).append(SEP);
    }
  }

  public static void main(String[] args) throws Exception {
    String outputPath = "./out/";
    String[] drugNames = { "Flagyl", "Ibuprofen", "Metformin", "Prilosec",
        "Tirosint", "Xanax" };

    // Ibuprofen Xanax Flagyl

    for (String drugName : drugNames) {
      GenOutput go = new GenOutput(new File(outputPath + drugName + ".txt"),
          drugName);
      go.start();
    }
  }
}
