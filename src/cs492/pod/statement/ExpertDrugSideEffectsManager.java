package cs492.pod.statement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.model.DrugCategory;
import cs492.pod.model.Severity;
import cs492.pod.util.StringUtil;

public class ExpertDrugSideEffectsManager {
  private final Logger logger = LogManager
      .getLogger(ExpertDrugSideEffectsManager.class.getSimpleName());

  private static ExpertDrugSideEffectsManager instance = null;

  private final String[][] result = new String[DrugCategory.values().length][Severity
      .values().length];

  public synchronized static ExpertDrugSideEffectsManager getInstance() {
    if (instance == null) {
      instance = new ExpertDrugSideEffectsManager();
    }

    return instance;
  }

  public void init() throws Exception {
    String file = "D:\\Downloads\\peopleondrugs\\Expert-Drug-SideEffects.tsv";
    File f = new File(file);
    if (f == null || !f.exists() || !f.isFile()) {
      throw new Exception("File does not exist " + file);
    }

    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(f));

      String line = null;

      while ((line = br.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }

        String[] tokens = line.split("\t");
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
            String[] sideEffectTokens = sideEffects.split("#");
            for (String sideEffectToken : sideEffectTokens) {
              sideEffectToken = StringUtil.ltrim(sideEffectToken);
              sideEffectToken = StringUtil.rtrim(sideEffectToken);
              if (!sideEffectToken.trim().isEmpty()) {
                result[category.ordinal()][Severity.getSeverity(severity)
                    .ordinal()] = sideEffects;
                isAdded = true;
              }
            }
          } else {
            // logger.debug("category is null:" + drug);
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
  }

  public boolean isSideEffect(String drugName, Severity severity,
      String sideEffect) {
    DrugCategory category = DrugCategory.getDrugCategory(drugName.trim());

    if (category == null) {
      throw new RuntimeException("Category not found. " + drugName.trim());
    }

    String answer = result[category.ordinal()][severity.ordinal()];

    if (answer == null || answer.isEmpty()) {
      return false;
    } else {

      String[] tokens = sideEffect.split(" ");
      boolean isSideEffect = false;
      for (String token : tokens) {
        isSideEffect = answer.toLowerCase().trim()
            .contains(token.toLowerCase().trim());
        if (isSideEffect) {
          return true;
        }
      }
      return false;
    }
  }
}
