package cs492.pod.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.parser.schema.AuthorDocReview;
import cs492.pod.parser.schema.AuthorDocSymptom;
import cs492.pod.util.StringUtil;

public class AuthorDocSymptomsParser implements Parser {
  private final static Logger logger = LogManager
      .getLogger(AuthorDocSymptomsParser.class.getSimpleName());

  private final String filePath;

  public AuthorDocSymptomsParser(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void parse() throws Exception {
    File dataFile = new File(this.filePath);
    if (dataFile == null || !dataFile.exists() || !dataFile.isFile()) {
      throw new Exception("File does not exist. path=" + this.filePath);
    }

    BufferedReader br = null;
    PreparedStatement pstmt = null;

    long lineCount = 0;
    final int MAX_LINES = 10000;
    try {
      br = new BufferedReader(new FileReader(dataFile));
      String line = null;
      pstmt = DBClient.getInstance().createAuthorDocSymptom();
      while ((line = br.readLine()) != null) {
        if (!line.isEmpty()) {
          String[] tokens = line.split("\t");
          int authorId = Integer.parseInt(tokens[0].trim());
          int docId = Integer.parseInt(tokens[1].trim());
          String[] symptoms = { "" };
          if (tokens.length > 2) {
            symptoms = tokens[2].trim().split("#");
          }

          for (int i = 0; i < symptoms.length; i++) {
            symptoms[i] = StringUtil.ltrim(symptoms[i]);
            symptoms[i] = StringUtil.rtrim(symptoms[i]);
          }

          AuthorDocSymptom v = new AuthorDocSymptom(authorId, docId, symptoms);
          logger.info("{} - {}", lineCount, v);

          DBClient.getInstance().addAuthorDocSymptom(pstmt, v);
          lineCount++;

          if (lineCount % MAX_LINES == 0) {
            pstmt.executeBatch();
            pstmt.clearBatch();
            DBClient.getInstance().commit();
          }
        }
      }

      pstmt.executeBatch();
      pstmt.clearBatch();
      DBClient.getInstance().commit();
    } finally {
      if (br != null) {
        br.close();
      }

      if (pstmt != null) {
        pstmt.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    logger.info(AuthorDocReview.class.getName());
    DBClient.getInstance().connect();

    String path = args[0].trim();
    AuthorDocSymptomsParser parser = new AuthorDocSymptomsParser(path);
    parser.parse();
  }
}
