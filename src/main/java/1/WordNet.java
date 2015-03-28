import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * WordNet is a semantic lexicon for the English language that is used extensively by computational linguists and
 * cognitive scientists; for example, it was a key component in IBM's Watson. WordNet groups words into sets of
 * synonyms called synsets and describes semantic relationships between them. One such relationship is the is-a
 * relationship, which connects a hyponym (more specific synset) to a hypernym (more general synset). For example, a
 * plant organ is a hypernym of carrot and plant organ is a hypernym of plant root.
 *
 * @author Kevin Crosby
 */
public class WordNet {
  private final Set<String> nouns;
  private final List<String> idToSynset;
  private final Map<String, List<Integer>> nounToID; // multimap (not allowed to use Guava)
  private final SAP sap;

  /**
   * Constructor takes the name of the two input files.
   *
   * @param synsets   Synset file.
   * @param hypernyms Hypernym file.
   */
  public WordNet(final String synsets, final String hypernyms) { // linearithmic time or better
    if (synsets == null || hypernyms == null) { throw new NullPointerException(); }

    nouns = new TreeSet<>();
    idToSynset = new ArrayList<>();
    nounToID = new HashMap<>();
    In sin = new In(synsets);
    int V = 0;
    while (!sin.isEmpty()) {
      String[] line = sin.readLine().split(",");
      int id = Integer.parseInt(line[0]);
      String synset = line[1];
      idToSynset.add(synset);
      List<String> nouns = Arrays.asList(synset.split(" "));
      put(nouns, id); // updates nounToID
      this.nouns.addAll(nouns);
      //String gloss = line[2]; // unused
      V++;
    }

    Digraph G = new Digraph(V);
    In hin = new In(hypernyms);
    while (!hin.isEmpty()) {
      String[] line = hin.readLine().split(",");
      int hyponymID = Integer.parseInt(line[0]);
      for (int i = 1; i < line.length; i++) {
        int hypernymID = Integer.parseInt(line[i]);
        G.addEdge(hyponymID, hypernymID);
      }
    }

    // check if unique root
    int roots = 0;
    for (int v = 0; v < V; v++) {
      if (!G.adj(v).iterator().hasNext()) {
        roots++;
      }
    }
    if (roots != 1) { throw new IllegalArgumentException(); }

    sap = new SAP(G);
  }

  // helper method for multimap
  private void put(final String key, final int value) {
    if (nounToID.containsKey(key)) {
      nounToID.get(key).add(value);
    } else {
      List<Integer> collection = new ArrayList<>();
      collection.add(value);
      nounToID.put(key, collection);
    }
  }

  // helper method for multimap
  private void put(final List<String> keys, final int value) {
    for (final String key : keys) {
      put(key, value);
    }
  }

  /**
   * Returns all WordNet nouns.
   *
   * @return Iterable of nouns.
   */
  public Iterable<String> nouns() {
    return nouns;
  }

  /**
   * Is the word a WordNet noun?
   *
   * @param word Word to test.
   * @return True if word is WordNet noun, false otherwise.
   */
  public boolean isNoun(final String word) {  // logarithmic time or better
    if (word == null) { throw new NullPointerException(); }
    return nouns.contains(word);
  }

  /**
   * Distance between nounA and nounB.
   *
   * @param nounA First noun to compute distance from.
   * @param nounB Second noun to compute distance from.
   * @return Distance between noun pair.
   */
  public int distance(final String nounA, final String nounB) { // linear in size of digraph
    if (nounA == null || nounB == null) { throw new NullPointerException(); }
    if (!isNoun(nounA) || !isNoun(nounB)) { throw new IllegalArgumentException(); }
    List<Integer> idsA = nounToID.get(nounA), idsB = nounToID.get(nounB);
    return sap.length(idsA, idsB);
  }

  /**
   * A synset (second field of synsets.txt) that is the common ancestor of nounA and nounB in a shortest ancestral
   * path.
   *
   * @param nounA First noun to compute distance from.
   * @param nounB Second noun to compute distance from.
   * @return Shortest common ancestor synset of noun pair.
   */
  public String sap(final String nounA, final String nounB) { // linear in size of digraph
    if (nounA == null || nounB == null) { throw new NullPointerException(); }
    if (!isNoun(nounA) || !isNoun(nounB)) { throw new IllegalArgumentException(); }
    List<Integer> idsA = nounToID.get(nounA), idsB = nounToID.get(nounB);
    int id = sap.ancestor(idsA, idsB);
    return idToSynset.get(id);
  }

  // do unit testing of this class
  public static void main(String[] args) {
    String synsets = args[0], hypernyms = args[1];
  }
}
