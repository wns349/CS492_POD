package cs492.pod.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.model.DrugCategory;
import cs492.pod.model.Severity;

public class ExpertDrugSideEffectsValidator {
  private final Logger logger = LogManager
      .getLogger(ExpertDrugSideEffectsValidator.class.getSimpleName());

  private final String filePath;

  public ExpertDrugSideEffectsValidator(String filePath) {
    this.filePath = filePath;
  }

  public void start() throws Exception {
    File f = new File(this.filePath);
    if (f == null || !f.exists() || !f.isFile()) {
      throw new Exception("File does not exist. " + this.filePath);
    }

    BufferedReader br = null;
    int[][] result = new int[DrugCategory.values().length][Severity.values().length];

    try {
      br = new BufferedReader(new FileReader(f));
      String line = null;

      while ((line = br.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }

        String[] tokens = line.split("\t");
        logger.info("tokens length: " + tokens.length);
        String[] drugs = tokens[0].trim().split(",");
        String severity = tokens[1].trim();
        String sideEffects = tokens[2].trim();

        if (sideEffects
            .substring(sideEffects.indexOf('[') + 1, sideEffects.indexOf(']'))
            .trim().isEmpty()) {
          continue;
        }

        boolean isAdded = false;
        for (String drug : drugs) {
          drug = drug.trim();
          DrugCategory category = DrugCategory.getDrugCategory(drug);
          if (category != null) {
            result[category.ordinal()][Severity.getSeverity(severity).ordinal()] += sideEffects
                .split(",").length;
            isAdded = true;
          } else {
            logger.debug("category is null:" + drug);
          }

          if (isAdded) {
            break;
          }
        }
      }

    } finally {
      if (br != null) {
        br.close();
      }
    }

    for (int i = 0; i < result.length; i++) {
      for (int j = 0; j < result[i].length; j++) {
        System.out.println(DrugCategory.getByOrdinal(i) + " | "
            + Severity.getByOrdinal(j) + " = " + result[i][j]);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    final String filePath = "D:\\Downloads\\peopleondrugs\\Expert-Drug-SideEffects.tsv";
    ExpertDrugSideEffectsValidator v = new ExpertDrugSideEffectsValidator(
        filePath);
    v.start();
  }
}
