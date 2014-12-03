package cs492.pod.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.parser.schema.AffectiveFeature;

public class AffectiveFeaturesParser implements Parser {
  private final Logger logger = LogManager
      .getLogger(AffectiveFeaturesParser.class.getSimpleName());

  private final String filePath;

  public AffectiveFeaturesParser(String filePath) {
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
          String[] words = tokens[1].trim().split(",");
          AffectiveFeature affectiveFeature = new AffectiveFeature(
              tokens[0].trim(), words);
          
          logger.debug(affectiveFeature.toString());
          DBClient.getInstance().addAffectiveFeature(affectiveFeature);
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
    }
  }

}
