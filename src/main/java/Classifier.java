import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;

/**
 * @author kalle
 * @since 2015-03-27 12:58
 */
public class Classifier {

  public static void main(String[] args) throws Exception {

    Classifier classifier = new Classifier();
    classifier.build();

    System.out.println("flickor>");

    System.out.println(classifier.classify("susanne"));
    System.out.println(classifier.classify("susanna"));
    System.out.println(classifier.classify("birgit"));
    System.out.println(classifier.classify("berit"));
    System.out.println(classifier.classify("birgitta"));
    System.out.println(classifier.classify("karoline"));
    System.out.println(classifier.classify("mariell"));
    System.out.println(classifier.classify("miriam"));
    System.out.println(classifier.classify("hedvig"));

    System.out.println("pojkar>");

    System.out.println(classifier.classify("kalle"));
    System.out.println(classifier.classify("bo"));
    System.out.println(classifier.classify("karl"));
    System.out.println(classifier.classify("jens"));
    System.out.println(classifier.classify("ludwig"));
    System.out.println(classifier.classify("alfons"));
    System.out.println(classifier.classify("ragnar"));

    System.currentTimeMillis();

  }

  private Set<String> flicknamn;
  private Set<String> pojknamn;

  private Instances trainingData;
  private weka.classifiers.Classifier classifier;

  private Map<String, Integer> attributeIndexByToken;

//  private File flicknamnFile = new File("data/flicknamn.txt");
//  private File pojknamnFile = new File("data/pojknamn.txt");

  public void build() throws Exception {

    Set<String> allTokens = new HashSet<String>();

    flicknamn = new HashSet<String>(gatherNames(getClass().getResourceAsStream("/data/processed/flicknamn.txt")));
    pojknamn = new HashSet<String>(gatherNames(getClass().getResourceAsStream("/data/processed/pojknamn.txt")));

    for (String name : flicknamn) {
      allTokens.addAll(Arrays.asList(tokenize(name)));
    }
    for (String name : pojknamn) {
      allTokens.addAll(Arrays.asList(tokenize(name)));
    }


    List<String> allTokensOrdered = new ArrayList<String>(allTokens);
    Collections.sort(allTokensOrdered);

    attributeIndexByToken = new HashMap<String, Integer>(allTokensOrdered.size());

    StringBuilder arff = new StringBuilder(49152);
    arff.append("@relation fornamnsklassifierare\n");

    for (int i = 0; i < allTokensOrdered.size(); i++) {
      String token = allTokensOrdered.get(i);
      arff.append("@attribute ").append(token).append(" numeric\n");
      attributeIndexByToken.put(token, i);
    }

    arff.append("@attribute class {flicka, pojke}\n");

    arff.append("@data\n");

    buildClassData(attributeIndexByToken, arff, flicknamn, "flicka");
    buildClassData(attributeIndexByToken, arff, pojknamn, "pojke");

    trainingData = new Instances(new StringReader(arff.toString()));
    trainingData.setClass(trainingData.attribute("class"));
    classifier = new SMO();
    classifier.buildClassifier(trainingData);


  }

  public String classify(String name) throws Exception {

    Instance instance = new Instance(trainingData.numAttributes());
    instance.setDataset(trainingData);

    String[] tokens = tokenize(name);
    List<Integer> attributeIndices = new ArrayList<Integer>(tokens.length);
    for (String token : tokens) {
      Integer index = attributeIndexByToken.get(token);
      if (index != null) {
        attributeIndices.add(index);
      }
    }
    Collections.sort(attributeIndices);

    for (Integer index : attributeIndices) {
      instance.setValue(index, 1);
    }

    double classification = classifier.classifyInstance(instance);
    return trainingData.classAttribute().value((int)classification);

  }


  private void buildClassData(Map<String, Integer> attributeIndexByToken, StringBuilder arff, Set<String> names, String className) throws Exception {
    for (String name : names) {
      String[] tokens = tokenize(name);
      List<Integer> attributeIndices = new ArrayList<Integer>(tokens.length);
      for (String token : tokens) {
        attributeIndices.add(attributeIndexByToken.get(token));
      }
      Collections.sort(attributeIndices);
      arff.append("{");
      for (Integer index : attributeIndices) {
        arff.append(String.valueOf(index)).append(" 1, ");
      }
      arff.append(attributeIndexByToken.size()).append(" \"").append(className).append("\"}\n");
    }
  }


  private List<String> gatherNames(InputStream inputStream) throws Exception {

    List<String> names = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
    String line;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("#") || line.isEmpty()) {
        continue;
      }
      names.add(line);
    }

    br.close();
    return names;
  }


  private String[] tokenize(String input) throws Exception {

    input = input.toLowerCase();

    NGramTokenFilter filter = new NGramTokenFilter(
        new KeywordTokenizer(new StringReader("^" + input + "$")),
        1, 4);

    CharTermAttribute charTermAttrib = filter.getAttribute(CharTermAttribute.class);


    filter.reset();

    Set<String> tokens = new LinkedHashSet<String>();

    while (filter.incrementToken()) {
      tokens.add(charTermAttrib.toString());
    }


    filter.close();

    return tokens.toArray(new String[tokens.size()]);

  }


}
