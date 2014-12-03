package cs492.pod.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.parser.schema.AuthorDetail;

public class AuthorDetailParser implements Parser {
  private final static Logger logger = LogManager
      .getLogger(AuthorDetailParser.class.getSimpleName());

  private final String filePath;

  public AuthorDetailParser(String filePath) {
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
          String gender = tokens[1].trim();
          String location = tokens[2].trim();
          int postCount = Integer.parseInt(tokens[3].trim());
          String membershipType = tokens[4].trim();
          int questionCount = Integer.parseInt(tokens[5].trim());
          int replyCount = Integer.parseInt(tokens[6].trim());
          int thankCount = 0;
          try{
           thankCount = Integer.parseInt(tokens[7].trim());
          } catch (Exception e){
            thankCount = 0;
          }
          // CREATE TABLE `pod`.`author_detail` (
          // `author_id` INT NOT NULL,
          // `gender` VARCHAR(6) NULL,
          // `location` TEXT NULL,
          // `post_count` INT NULL,
          // `membership_type` INT NULL,
          // `question_count` INT NULL,
          // `reply_count` INT NULL,
          // `thank_count` INT NULL,
          // PRIMARY KEY (`author_id`))

          AuthorDetail instance = new AuthorDetail(authorId, gender, location,
              postCount, membershipType, questionCount, replyCount, thankCount);

          logger.debug(instance.toString());
          DBClient.getInstance().addAuthorDetail(instance);
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    logger.info(AuthorDetail.class.getName());
    DBClient.getInstance().connect();

    String path = "D:\\Downloads\\peopleondrugs\\Author-Details.tsv";
    AuthorDetailParser parser = new AuthorDetailParser(path);
    parser.parse();
  }
}
