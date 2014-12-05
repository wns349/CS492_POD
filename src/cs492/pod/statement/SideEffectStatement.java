package cs492.pod.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.model.Severity;

public class SideEffectStatement extends Thread {
  private final Logger logger = LogManager.getLogger(SideEffectStatement.class
      .getSimpleName());

  private final String drugName;

  public SideEffectStatement(String drugName) {
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

    ExpertDrugSideEffectsManager.getInstance().init();
    logger.info("ExpertDrugSideEffectsManager init done");

    DBClient.getInstance().selectSpecificDrugSideEffect(drugName,
        new SpecificDrugSideEffectSelectHandler() {

          @Override
          public void handle(PreparedStatement pstmtUpdate, ResultSet rs)
              throws Exception {
            while (rs.next()) {
              String sideEffect = rs.getString("symptom");
              boolean isSideEffect = false;
              if (!sideEffect.trim().isEmpty()) {
                isSideEffect = ExpertDrugSideEffectsManager.getInstance()
                    .isSideEffect(drugName, Severity.MoreCommon,
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
    String[] drugNames = { "Flagyl", "Ibuprofen", "Metformin", "Prilosec",
        "Tirosint", "Xanax" };

    // String[] drugNames = {"Tirosint"};

    Thread[] threads = new Thread[drugNames.length];
    for (int i = 0; i < drugNames.length; i++) {
      threads[i] = new SideEffectStatement(drugNames[i]);
      threads[i].start();
    }
  }

  public interface SpecificDrugSideEffectSelectHandler {
    public void handle(PreparedStatement pstmtUpdate, ResultSet rs)
        throws Exception;

    public void onHandleFinish(PreparedStatement pstmtUpdate) throws Exception;
  }
}
