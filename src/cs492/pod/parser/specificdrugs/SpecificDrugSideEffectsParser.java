package cs492.pod.parser.specificdrugs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs492.pod.db.DBClient;
import cs492.pod.parser.Parser;
import cs492.pod.parser.schema.SpecificDrugSideEffect;

public class SpecificDrugSideEffectsParser extends Thread implements Parser {
  private final Logger logger = LogManager
      .getLogger(SpecificDrugSideEffectsParser.class.getSimpleName());

  private final String filePath;

  private final String type;

  public SpecificDrugSideEffectsParser(String filePath) {
    super(filePath);

    this.filePath = filePath;

    if (filePath.contains("Xanax"))
      this.type = "Xanax";
    else if (filePath.contains("Ibuprofen"))
      this.type = "Ibuprofen";
    else if (filePath.contains("Prilosec"))
      this.type = "Prilosec";
    else if (filePath.contains("Metformin"))
      this.type = "Metformin";
    else if (filePath.contains("Tirosint"))
      this.type = "Tirosint";
    else if (filePath.contains("Flagyl"))
      this.type = "Flagyl";
    else
      throw new RuntimeException("HUH?");
  }

  @Override
  public void run() {
    try {
      parse();
    } catch (Exception e) {
      logger.error(e);
      e.printStackTrace();
    }

    logger.info(">>>>>>>>>>>> DONE >>>> {} ", getName());
  }

  @Override
  public void parse() throws Exception {
    File dataFile = new File(this.filePath);
    if (dataFile == null || !dataFile.exists() || !dataFile.isFile()) {
      throw new Exception("File does not exist. path=" + this.filePath);
    }

    BufferedReader br = null;

    try {
      DBClient.getInstance().connect();
      br = new BufferedReader(new FileReader(dataFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (!line.isEmpty()) {
          String[] tokens = line.split("\t");

          int authorId = Integer.parseInt(tokens[0].trim());
          int docId = Integer.parseInt(tokens[1].trim());

          String sideEffects = "";
          if (tokens.length > 2) {
            sideEffects = tokens[2].trim();
            sideEffects = sideEffects.substring(sideEffects.indexOf('{') + 1,
                sideEffects.indexOf('}'));

            String[] sideEffectTokens = sideEffects.split(",");
            for (String sideEffectToken : sideEffectTokens) {
              sideEffectToken = sideEffectToken.trim();
              String symptom = sideEffectToken.substring(0,
                  sideEffectToken.indexOf('='));
              int frequency = Integer.parseInt(sideEffectToken.substring(
                  sideEffectToken.indexOf('=') + 1, sideEffectToken.length()));

              SpecificDrugSideEffect v = new SpecificDrugSideEffect(authorId,
                  docId, symptom, frequency, type);

              logger.info(v);
              DBClient.getInstance().addSpecificDrugSideEffect(v);
            }
          } else {
            SpecificDrugSideEffect v = new SpecificDrugSideEffect(authorId,
                docId, "", 0, type);
            logger.info(v);
            DBClient.getInstance().addSpecificDrugSideEffect(v);
          }

          // logger.info("{} {} {}", authorId, docId, sideEffects);
        }
      }

    } finally {
      if (br != null) {
        br.close();
      }
    }
  }

  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
      Map<K, V> map, final boolean isAscending) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        if (isAscending) {
          return (o1.getValue()).compareTo(o2.getValue());
        } else {
          return (o2.getValue()).compareTo(o1.getValue());
        }
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
    String[] files = {
        "D:\\Downloads\\peopleondrugs\\specificdrugs\\Author-Ibuprofen-SideEffects.tsv",
        "D:\\Downloads\\peopleondrugs\\specificdrugs\\Author-Prilosec-SideEffects.tsv",
        "D:\\Downloads\\peopleondrugs\\specificdrugs\\Author-Metformin-SideEffects.tsv",
        "D:\\Downloads\\peopleondrugs\\specificdrugs\\Author-Tirosint-SideEffects.tsv",
        "D:\\Downloads\\peopleondrugs\\specificdrugs\\Author-Flagyl-SideEffects.tsv",
        "D:\\Downloads\\peopleondrugs\\specificdrugs\\Author-Xanax-SideEffects.tsv" };

    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < files.length; i++) {
      threads.add(new SpecificDrugSideEffectsParser(files[i]));
    }

    for (Thread t : threads) {
      t.start();
    }
  }
}
