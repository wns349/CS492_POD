package cs492.pod.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.parser.schema.AuthorDocReview;

public class AuthorDocReviewParser implements Parser {
  private final static Logger logger = LogManager
      .getLogger(AuthorDocReviewParser.class.getSimpleName());

  private final String filePath;

  public AuthorDocReviewParser(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void parse() throws Exception {
    File dataFile = new File(this.filePath);
    if (dataFile == null || !dataFile.exists() || !dataFile.isFile()) {
      throw new Exception("File does not exist. path=" + this.filePath);
    }

    BufferedReader br = null;

    try {
      br = new BufferedReader(new FileReader(dataFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (!line.isEmpty()) {
          String[] tokens = line.split("\t");
          int authorId = Integer.parseInt(tokens[0].trim());
          int doctorId = Integer.parseInt(tokens[1].trim());
          String post = tokens[2].trim();

          AuthorDocReview instance = new AuthorDocReview(authorId, doctorId,
              post);

          logger.debug(instance.toString());
          DBClient.getInstance().addAuthorDocReview(instance);
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    logger.info(AuthorDocReview.class.getName());
    DBClient.getInstance().connect();

    String path = "D:\\Downloads\\peopleondrugs\\Author-Doc-Review.tsv";
    AuthorDocReviewParser parser = new AuthorDocReviewParser(path);
    parser.parse();
  }
}
