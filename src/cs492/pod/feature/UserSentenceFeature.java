package cs492.pod.feature;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.feature.UserFeature.FeatureType;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class UserSentenceFeature {
  private final Logger logger = LogManager.getLogger(UserSentenceFeature.class
      .getSimpleName());

  private final Map<Integer, Bean> map = new HashMap<Integer, Bean>(150000);

  private void start() throws Exception {
    logger.info("UserSentenceFeature");
    DBClient.getInstance().connect();

    DBClient.getInstance().selectAuthorDocReview(
        new OnAuthorDocReviewHandler() {

          @Override
          public void handle(ResultSet rs) throws Exception {
            while (rs.next()) {
              int authorId = rs.getInt("author_id");
              int docId = rs.getInt("doc_id");
              String post = rs.getString("post");

              Bean userBean = null;
              if (!map.containsKey(authorId)) {
                userBean = new Bean();
                map.put(authorId, userBean);
              } else {
                userBean = map.get(authorId);
              }

              DocumentPreprocessor dp = new DocumentPreprocessor(
                  new StringReader(post));
              int sentences = 0;
              for (List sentence : dp) {
                sentences++;
                userBean.incrementWords(sentence.size());
              }

              userBean.incrementSentence(sentences);
              userBean.incrementPost();
              logger.debug(authorId + " " + docId + " " + userBean);
            }
          }

          @Override
          public void handleFinish(PreparedStatement pstmtInsert)
              throws Exception {
            int batchCount = 0;
            for (Map.Entry<Integer, Bean> entry : map.entrySet()) {
              int authorId = entry.getKey();
              Bean bean = entry.getValue();
              String avgWordsPerSentence = bean.avgWordsPerSentence();
              String avgSentencesPerPost = bean.avgSentencesPerPost();
              
              logger.debug("{} {} {} {}", authorId, bean, avgWordsPerSentence,
                  avgSentencesPerPost);

              pstmtInsert.setInt(1, authorId);
              pstmtInsert.setString(2, FeatureType.AvgWordsPerSentence.name());
              pstmtInsert.setString(3, avgWordsPerSentence);
              pstmtInsert.addBatch();

              pstmtInsert.setInt(1, authorId);
              pstmtInsert.setString(2, FeatureType.AvgSentencesPerPost.name());
              pstmtInsert.setString(3, avgSentencesPerPost);
              pstmtInsert.addBatch();

              batchCount += 2;

              if (batchCount > 50000) {
                pstmtInsert.executeBatch();
                pstmtInsert.clearBatch();
                batchCount = 0;
              }

            }
            pstmtInsert.executeBatch();
            pstmtInsert.clearBatch();
            batchCount = 0;

            DBClient.getInstance().commit();
          }
        });
  }

  public static void main(String[] args) throws Exception {
    UserSentenceFeature usf = new UserSentenceFeature();
    usf.start();
  }

  class Bean {
    public double sentences = 0;
    public double words = 0;
    public double posts = 0;

    public String avgSentencesPerPost() throws Exception {
      try {
        return String.format("%.5f", (sentences / posts));
      } catch (Exception e) {
        return "NaN";
      }
    }

    public String avgWordsPerSentence() {
      try {
        return String.format("%.5f", (words / sentences));
      } catch (Exception e) {
        return "NaN";
      }
    }

    public void incrementSentence(int num) {
      sentences += num;
    }

    public void incrementWords(int word) {
      words += word;
    }

    public void incrementPost() {
      posts++;
    }

    @Override
    public String toString() {
      return "S=" + sentences + " W=" + words + " P=" + posts;
    }
  }

  public interface OnAuthorDocReviewHandler {
    public void handle(ResultSet rs) throws Exception;

    public void handleFinish(PreparedStatement pstmtInsert) throws Exception;
  }
}
