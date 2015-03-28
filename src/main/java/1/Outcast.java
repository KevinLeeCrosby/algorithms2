/**
 * @author Kevin Crosby
 */
public class Outcast {
  private static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
  private final WordNet wordnet;

  // constructor takes a WordNet object
  public Outcast(final WordNet wordnet) {
    this.wordnet = wordnet;
  }

  /**
   * Given an array of WordNet nouns, return an outcast.
   *
   * @param nouns Nouns to select outcast from.
   * @return Outcast among nouns.
   */
  public String outcast(final String[] nouns) {
    int length = nouns.length;
    int[] sums = new int[length];
    for (int i = 0; i < length - 1; i++) {
      String nounA = nouns[i];
      for (int j = i + 1; j < length; j++) {
        String nounB = nouns[j];
        int distance = wordnet.distance(nounA, nounB);
        sums[i] += distance;
        sums[j] += distance;
      }
    }
    int maxSum = NEGATIVE_INFINITY, index = -1;
    for (int i = 0; i < length; i++) {
      if (maxSum < sums[i]) {
        maxSum = sums[i];
        index = i;
      }
    }
    return nouns[index];
  }

  // see test client below
  public static void main(String[] args) {
    String synsets = args[0], hypernyms = args[1];
    WordNet wordnet = new WordNet(synsets, hypernyms);
    Outcast outcast = new Outcast(wordnet);
    for (int t = 2; t < args.length; t++) {
      In in = new In(args[t]);
      String[] nouns = in.readAllStrings();
      StdOut.println(args[t] + ": " + outcast.outcast(nouns));
    }
  }
}
