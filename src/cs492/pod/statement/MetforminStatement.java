package cs492.pod.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.model.Severity;
import cs492.pod.statement.SideEffectStatement.SpecificDrugSideEffectSelectHandler;

@Deprecated
public class MetforminStatement {
  private final Logger logger = LogManager.getLogger(MetforminStatement.class
      .getSimpleName());

  private static final String DRUG_NAME = "Metformin";

  public void start() throws Exception {
    DBClient.getInstance().connect();
    logger.info("DBClient connection established.");

    ExpertDrugSideEffectsManager.getInstance().init();
    logger.info("ExpertDrugSideEffectsManager init done");

    DBClient.getInstance().selectSpecificDrugSideEffect(DRUG_NAME,
        new SpecificDrugSideEffectSelectHandler() {

          @Override
          public void handle(PreparedStatement pstmtUpdate, ResultSet rs)
              throws Exception {
            while (rs.next()) {
              String sideEffect = rs.getString("symptom");
              boolean isSideEffect = false;
              if (!sideEffect.trim().isEmpty()) {
                isSideEffect = ExpertDrugSideEffectsManager.getInstance()
                    .isSideEffect(DRUG_NAME, Severity.MoreCommon,
                        sideEffect.trim());
              }

              pstmtUpdate.setString(1, isSideEffect ? "Y" : "N");
              pstmtUpdate.setInt(2, rs.getInt("author_id"));
              pstmtUpdate.setInt(3, rs.getInt("doc_id"));
              pstmtUpdate.setString(4, rs.getString("symptom"));
              pstmtUpdate.setString(5, rs.getString("type"));

              pstmtUpdate.addBatch();

              logger.info("{}/{} {} {} {}", rs.getInt("author_id"),
                  rs.getInt("doc_id"), rs.getString("type"),
                  rs.getString("symptom"), isSideEffect);
            }
          }

          @Override
          public void onHandleFinish(PreparedStatement pstmtUpdate)
              throws Exception {
            if (pstmtUpdate != null) {
              pstmtUpdate.executeBatch();
              DBClient.getInstance().commit();
            }
          }

        });

  }

  public static void main(String[] args) throws Exception {
    MetforminStatement v = new MetforminStatement();

    v.start();
  }
}
