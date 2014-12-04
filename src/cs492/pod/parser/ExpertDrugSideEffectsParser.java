package cs492.pod.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.model.DrugCategory;
import cs492.pod.model.Severity;
import cs492.pod.parser.schema.AffectiveFeature;

public class ExpertDrugSideEffectsParser implements Parser {
  private final Logger logger = LogManager
      .getLogger(ExpertDrugSideEffectsParser.class.getSimpleName());

  private final String filePath;

  public ExpertDrugSideEffectsParser(String filePath) {
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

          // Drug category
          String[] drugs = tokens[0].trim().split(",");
          DrugCategory drugCateogy = null;
          for (String drug : drugs) {
            drugCateogy = DrugCategory.getDrugCategory(drug.toLowerCase().trim());
            if (drugCateogy != null)
              break;
          }
          
          if(drugCateogy == null){
           // logger.debug("Drug category not found. {}", tokens[0].trim());
            continue;
          }

          // Severity
          Severity severity = Severity.getSeverity(tokens[1].trim());

          String[] sideEffects = tokens[3].trim().split("#");

          logger.info("{} {} {}", drugCateogy, severity, sideEffects.length);
        }
      }
    } finally {
      if (br != null) {
        br.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    ExpertDrugSideEffectsParser v = new ExpertDrugSideEffectsParser(
        "D:\\Downloads\\peopleondrugs\\Expert-Drug-SideEffects.tsv");
    v.parse();
  }
}
