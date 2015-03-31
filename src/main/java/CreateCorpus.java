import java.io.*;
import java.util.*;

/**
 * @author kalle
 * @since 2015-03-27 12:52
 */
public class CreateCorpus {

  public static void main(String[] args) throws Exception {

    cleanClassCorpus(new File("src/main/resources/flicknamn.txt"), new File("data/flicknamn.txt"));
    cleanClassCorpus(new File("src/main/resources/pojknamn.txt"), new File("data/pojknamn.txt"));



  }





  public static void cleanClassCorpus(File input, File output) throws Exception {

    Set<String> names = new HashSet<String>();

    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
    String line;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("#") || line.isEmpty()) {
        continue;
      }
      names.add(line.toLowerCase());
    }
    br.close();

    List<String> orderedNames = new ArrayList<String>(names);
    Collections.sort(orderedNames);

    Writer out = new OutputStreamWriter(new FileOutputStream(output), "UTF8");
    for (String name : orderedNames) {
      out.write(name);
      out.write("\n");
    }
    out.close();


  }

}
