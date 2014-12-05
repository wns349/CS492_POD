package cs492.pod.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class TaggerDemo {

  private TaggerDemo() {
  }

  public static void main(String[] args) throws Exception {
    String model = "./models/english-bidirectional-distsim.tagger";
    String testFile = "./test/sample-input.txt";

    MaxentTagger tagger = new MaxentTagger(model);
    List<List<HasWord>> sentences = MaxentTagger
        .tokenizeText(new BufferedReader(new FileReader(testFile)));
    for (List<HasWord> sentence : sentences) {
      List<TaggedWord> tSentence = tagger.tagSentence(sentence);
      System.out.println(Sentence.listToString(tSentence, false));
    }
  }

}
