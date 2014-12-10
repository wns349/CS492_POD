package cs492.pod.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.feature.UserFeature.OnSelectUsersHandler;
import cs492.pod.feature.UserSentenceFeature.OnAuthorDocReviewHandler;
import cs492.pod.parser.schema.AffectiveFeature;
import cs492.pod.parser.schema.AuthorDetail;
import cs492.pod.parser.schema.AuthorDocReview;
import cs492.pod.parser.schema.AuthorDocSymptom;
import cs492.pod.parser.schema.SpecificDrugSideEffect;
import cs492.pod.statement.SideEffectStatement.SpecificDrugSideEffectSelectHandler;

public class DBClient {
  private static final String DB_NAME = "POD";
  private static final String DB_URL = "143.248.49.97";
  private static final int DB_PORT = 3306;
  private static final String DB_USERNAME = "cs492";
  private static final String DB_PASSWORD = "aliceoh";

  private final Logger logger = LogManager.getLogger(DBClient.class
      .getSimpleName());

  private static DBClient instance = null;

  private Connection connect = null;

  public static synchronized DBClient getInstance() {
    if (instance == null) {
      instance = new DBClient();
    }
    return instance;
  }

  private DBClient() {

  }

  public void connect() throws Exception {
    connect(DB_URL, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD);
  }

  public void connect(String host, int port, String dbname, String username,
      String password) throws Exception {
    if (connect != null && !connect.isClosed()) {
      throw new Exception("Already connected.");
    }

    Class.forName("com.mysql.jdbc.Driver");

    String jdbcPath = String.format(
        "jdbc:mysql://%s:%d/%s?user=%s&password=%s", host, port, dbname,
        username, password);

    connect = DriverManager.getConnection(jdbcPath);
    connect.setAutoCommit(false);
  }

  public void terminate() {
    if (connect != null) {
      try {
        connect.close();
      } catch (SQLException e) {
        e.printStackTrace();
        logger.error(e);
      }
    }
  }

  // -----
  public void addAffectiveFeature(AffectiveFeature af) throws Exception {
    if (af == null) {
      logger.error("AffectiveFeature is null.");
      return;
    }

    String query = "INSERT INTO affective_features (feature, word) VALUES (?, ?) ";
    PreparedStatement pstmt = null;

    try {
      pstmt = connect.prepareStatement(query);

      for (String word : af.getAffectiveWordList()) {
        pstmt.setString(1, af.getAffectiveCategory());
        pstmt.setString(2, word);

        try {
          pstmt.executeUpdate();
        } catch (Exception e) {
          if (e.getMessage().contains("Duplicate entry")) {
            logger.debug("Duplicate entry found: " + e.getMessage());
          } else {
            throw e;
          }
        }
      }

      this.connect.commit();
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
  }

  public void addAuthorDocReview(AuthorDocReview authorDocReview)
      throws Exception {
    if (authorDocReview == null) {
      logger.error("AuthorDocReview is null.");
      return;
    }

    String query = "INSERT INTO author_doc_review (author_id, doc_id, post) VALUES (?, ?, ?) ";
    PreparedStatement pstmt = null;

    try {
      pstmt = connect.prepareStatement(query);
      pstmt.setInt(1, authorDocReview.getAuthorId());
      pstmt.setInt(2, authorDocReview.getDocId());
      pstmt.setString(3, authorDocReview.getPost());

      pstmt.executeUpdate();

      this.connect.commit();
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
  }

  public void addAuthorDetail(AuthorDetail authorDetail) throws Exception {
    if (authorDetail == null) {
      logger.error("AuthorDetail is null.");
      return;
    }

    String query = "INSERT INTO author_detail (author_id, gender, location, post_count, membership_type, question_count, reply_count, thank_count) VALUES "
        + "(?, ?, ?, ?, ?, ?, ?, ?) ";
    PreparedStatement pstmt = null;

    try {
      pstmt = connect.prepareStatement(query);
      pstmt.setInt(1, authorDetail.getAuthorId());
      pstmt.setString(2, authorDetail.getGender());
      pstmt.setString(3, authorDetail.getLocation());
      pstmt.setInt(4, authorDetail.getPostCount());
      pstmt.setString(5, authorDetail.getMembershipType());
      pstmt.setInt(6, authorDetail.getQuestionCount());
      pstmt.setInt(7, authorDetail.getReplyCount());
      pstmt.setInt(8, authorDetail.getThankCount());

      pstmt.executeUpdate();

      this.connect.commit();
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
  }

  public PreparedStatement createAuthorDocSymptom() throws SQLException {

    String query = "INSERT INTO author_doc_symptoms (author_id, doc_id, symptom) VALUES "
        + "(?, ?, ?) ";

    return connect.prepareStatement(query);
  }

  public void addAuthorDocSymptom(PreparedStatement pstmt,
      AuthorDocSymptom authorDocSymptom) throws Exception {
    if (authorDocSymptom == null) {
      logger.error("AuthorDocSymptom is null.");
      return;
    }

    for (String symptom : authorDocSymptom.getSymptoms()) {
      pstmt.setInt(1, authorDocSymptom.getAuthorId());
      pstmt.setInt(2, authorDocSymptom.getDocId());
      pstmt.setString(3, symptom);

      pstmt.addBatch();
    }

  }

  public void commit() throws SQLException {
    this.connect.commit();
  }

  public Connection getConnection() {
    return this.connect;
  }

  public void addSpecificDrugSideEffect(SpecificDrugSideEffect v)
      throws Exception {
    if (v == null) {
      logger.error("SpecificDrugSideEffect is null.");
      return;
    }

    String query = "INSERT INTO specific_drug_side_effects (author_id, doc_id, symptom, frequency, type) VALUES "
        + "(?, ?, ?, ?, ?) ";
    PreparedStatement pstmt = null;

    try {
      pstmt = connect.prepareStatement(query);
      pstmt.setInt(1, v.getAuthorId());
      pstmt.setInt(2, v.getDocId());
      pstmt.setString(3, v.getSymptom());
      pstmt.setInt(4, v.getFrequency());
      pstmt.setString(5, v.getType());

      pstmt.executeUpdate();

      this.connect.commit();
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
  }

  public PreparedStatement createSpecificDrugSideEffectUpdateMoreCommon()
      throws Exception {
    String query = "UPDATE specific_drug_side_effects SET is_more_common = ? "
        + "WHERE author_id = ? AND doc_id = ? AND symptom = ? AND type = ?";
    return connect.prepareStatement(query);
  }

  public void selectSpecificDrugSideEffect(String drugName,
      SpecificDrugSideEffectSelectHandler handler) throws Exception {
    String query = "SELECT * FROM specific_drug_side_effects WHERE type = ?";
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    PreparedStatement pstmtUpdate = createSpecificDrugSideEffectUpdateMoreCommon();
    try {
      pstmt = connect.prepareStatement(query);
      pstmt.setString(1, drugName);

      rs = pstmt.executeQuery();

      if (handler != null) {
        handler.handle(pstmtUpdate, rs);
        handler.onHandleFinish(pstmtUpdate);
      }

    } finally {

      if (rs != null && !rs.isClosed()) {
        rs.close();
      }

      if (pstmt != null && !pstmt.isClosed()) {
        pstmt.close();
      }

      if (pstmtUpdate != null && !pstmtUpdate.isClosed()) {
        pstmtUpdate.close();
      }
    }

  }

  public void selectUsers(OnSelectUsersHandler handler) throws Exception {
    String query = "SELECT * FROM author_detail";
    String queryInsert = "INSERT INTO author_feature_values (author_id, type, value) VALUES (?, ?, ?)";
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    PreparedStatement pstmtInsert = null;
    try {
      pstmt = connect.prepareStatement(query);
      pstmtInsert = connect.prepareStatement(queryInsert);
      rs = pstmt.executeQuery();

      if (handler != null) {
        handler.handle(pstmtInsert, rs);
      }

    } finally {

      if (rs != null && !rs.isClosed()) {
        rs.close();
      }

      if (pstmt != null && !pstmt.isClosed()) {
        pstmt.close();
      }

      if (pstmtInsert != null && !pstmtInsert.isClosed()) {
        pstmtInsert.close();
      }
    }
  }

  public void selectAuthorDocReview(OnAuthorDocReviewHandler handler)
      throws Exception {
    String query = "SELECT * FROM author_doc_review WHERE author_id = ?";
    String queryAuthorDetail = "SELECT author_id FROM author_detail";
    String queryInsert = "INSERT INTO author_feature_values (author_id, type, value) VALUES (?, ?, ?)";
    PreparedStatement pstmt = null;
    PreparedStatement pstmtAuthorDetail = null;
    ResultSet rs = null;
    ResultSet rsAuthorDetail = null;
    PreparedStatement pstmtInsert = null;
    try {
      pstmt = connect.prepareStatement(query);
      pstmtInsert = connect.prepareStatement(queryInsert);
      pstmtAuthorDetail = connect.prepareStatement(queryAuthorDetail);

      rsAuthorDetail = pstmtAuthorDetail.executeQuery();

      while (rsAuthorDetail.next()) {
        int authorId = rsAuthorDetail.getInt("author_id");
        pstmt.setInt(1, authorId);
        rs = pstmt.executeQuery();

        if (handler != null) {
          handler.handle(rs);
        }

        rs.close();
      }

      if (handler != null) {
        handler.handleFinish(pstmtInsert);
      }
    } finally {

      if (rs != null && !rs.isClosed()) {
        rs.close();
      }
      if (rsAuthorDetail != null && !rsAuthorDetail.isClosed()) {
        rsAuthorDetail.close();
      }

      if (pstmt != null && !pstmt.isClosed()) {
        pstmt.close();
      }

      if (pstmtAuthorDetail != null && !pstmtAuthorDetail.isClosed()) {
        pstmtAuthorDetail.close();
      }

      if (pstmtInsert != null && !pstmtInsert.isClosed()) {
        pstmtInsert.close();
      }
    }
  }
}
