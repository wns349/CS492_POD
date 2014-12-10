package cs492.pod.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileSplit extends Thread {
  private final Logger logger = LogManager.getLogger(FileSplit.class
      .getSimpleName());

  final File file;

  public FileSplit(File file) {
    super(file.getName());
    this.file = file;
  }

  @Override
  public void run() {
    try {
      this.split(0.80);
      logger.info("DONE");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void split(double ratio) throws Exception {
    BufferedReader sc = new BufferedReader(new FileReader(this.file));
    logger.info("{}", this.file.getName());
    int numLines = 0;
    String line = null;
    while ((line = sc.readLine()) != null) {
      numLines++;
    }
    sc.close();

    int lastTxt = file.getAbsolutePath().lastIndexOf(".txt");
    int lastSlash = file.getAbsolutePath().lastIndexOf("\\");
    String path = file.getAbsolutePath().substring(0, lastSlash);
    String drugName = file.getAbsolutePath().substring(lastSlash + 1, lastTxt);

    File fTrain = new File(path, drugName + "_train.txt");
    File fTest = new File(path, drugName + "_test.txt");

    PrintWriter pwTrain = new PrintWriter(fTrain);
    PrintWriter pwTest = new PrintWriter(fTest);

    sc = new BufferedReader(new FileReader(this.file));

    logger.info("{} {}", fTrain.getName(), fTest.getName());

    int trainLines = (int) (numLines * ratio);
    numLines = 0;
    while ((line = sc.readLine()) != null) {
      if (numLines <= trainLines) {
        pwTrain.println(line);
      } else {
        pwTest.println(line);
      }
      numLines++;
    }

    pwTrain.close();
    pwTest.close();
  }

  public static void main(String[] args) {
    String[] paths = { "./out/Flagyl.txt", "./out/Ibuprofen.txt",
        "./out/Metformin.txt", "./out/Prilosec.txt", "./out/Tirosint.txt",
        "./out/Xanax.txt" };

    for (String p : paths) {
      FileSplit fs = new FileSplit(new File(p));
      fs.start();
    }
  }
}
