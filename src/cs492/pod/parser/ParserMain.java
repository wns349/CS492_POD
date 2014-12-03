package cs492.pod.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;

public class ParserMain {
 
  private static final Logger logger = LogManager.getLogger(ParserMain.class
      .getSimpleName());

  public static void main(String[] args) throws Exception {
    logger.info("Parser");

    logger.info("Loading DB");
    

    logger.info("AffectiveFeaturesParser");
    String affectiveFeaturesPath = "D:\\Downloads\\peopleondrugs\\Affective-Features.tsv";
    AffectiveFeaturesParser affectiveFeaturesParser = new AffectiveFeaturesParser(
        affectiveFeaturesPath);
    affectiveFeaturesParser.parse();

  }
}
