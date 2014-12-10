package cs492.pod.feature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.model.Author;
import cs492.pod.model.UserFeatureType;

public class UserFeature {
  private final Logger logger = LogManager.getLogger(UserFeature.class
      .getSimpleName());

  public void start() throws Exception {

    DBClient.getInstance().connect();

    logger.info("UserFeature");

    DBClient.getInstance().selectUsers(new OnSelectUsersHandler() {

      @Override
      public void handle(PreparedStatement pstmtInsert, ResultSet rs)
          throws Exception {

        int pstmtInsertCount = 0;

        while (rs.next()) {
          Author author = new Author(rs);

          // Gender
          pstmtInsertCount += insertGender(pstmtInsert, author);

          // QRRatio
          pstmtInsertCount += insertQRRatio(pstmtInsert, author);

          // AvgThankPerReply
          pstmtInsertCount += insertAvgThankPerReply(pstmtInsert, author);

          // MembershipType
          pstmtInsertCount += insertMembershipType(pstmtInsert, author);

          logger.debug(author);

          if (pstmtInsertCount > 50000) {
            pstmtInsert.executeBatch();
            pstmtInsert.clearBatch();
            pstmtInsertCount = 0;
          }
        }

        pstmtInsert.executeBatch();
        pstmtInsert.clearBatch();

        DBClient.getInstance().commit();
      }

      private int insertMembershipType(PreparedStatement pstmtInsert,
          Author author) throws Exception {
        pstmtInsert.setInt(1, author.getAuthorId());
        pstmtInsert.setString(2, UserFeatureType.MembershipType.name());
        pstmtInsert.setString(3, author.getMembershipType());
        pstmtInsert.addBatch();

        return 1;
      }

      private int insertAvgThankPerReply(PreparedStatement pstmtInsert,
          Author author) throws Exception {
        double t = (double) author.getThankCount();
        double r = (double) author.getReplyCount();

        String ans = "";
        if (r == 0) {
          ans = "NaN";
        } else {
          ans = String.format("%.5f", (t / r));
        }

        pstmtInsert.setInt(1, author.getAuthorId());
        pstmtInsert.setString(2, UserFeatureType.AvgThankPerReply.name());
        pstmtInsert.setString(3, ans);
        pstmtInsert.addBatch();

        return 1;
      }

      private int insertQRRatio(PreparedStatement pstmtInsert, Author author)
          throws Exception {
        double q = (double) author.getQuestionCount();
        double r = (double) author.getReplyCount();

        String ans = "";
        if (r == 0) {
          ans = "NaN";
        } else {
          ans = String.format("%.5f", (q / r));
        }

        pstmtInsert.setInt(1, author.getAuthorId());
        pstmtInsert.setString(2, UserFeatureType.QRRatio.name());
        pstmtInsert.setString(3, ans);
        pstmtInsert.addBatch();

        return 1;
      }

      private int insertGender(PreparedStatement pstmtInsert, Author author)
          throws Exception {
        pstmtInsert.setInt(1, author.getAuthorId());
        pstmtInsert.setString(2, UserFeatureType.Gender.name());
        pstmtInsert.setString(3, author.getGender());

        pstmtInsert.addBatch();
        return 1;
      }
    });
  }

  public static void main(String[] args) throws Exception {
    UserFeature uf = new UserFeature();
    uf.start();
  }

  public interface OnSelectUsersHandler {
    public void handle(PreparedStatement pstmtInsert, ResultSet rs)
        throws Exception;
  }
}
