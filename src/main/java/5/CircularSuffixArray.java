import java.util.Arrays;

/**
 * To efficiently implement the key component in the Burrows-Wheeler transform, you will use a fundamental data
 * structure known as the circular suffix array, which describes the abstraction of a sorted array of the N circular
 * suffixes of a string of length N.
 *
 * @author Kevin Crosby
 */
public class CircularSuffixArray {
  private final Indices[] indices;
  private final String string;
  private final int n;

  /**
   * Circular suffix array of s.
   *
   * @param string Suffix array.
   */
  public CircularSuffixArray(final String string) {
    if (string == null) throw new NullPointerException();
    this.string = string;
    n = string.length();
    indices = new Indices[n];
    for (int i = 0; i < n; ++i) {
      indices[i] = new Indices(i);
    }
    Arrays.sort(indices);
  }

  private class Indices implements Comparable<Indices> {
    private final int index;

    private Indices(final int index) {
      this.index = index;
    }

    private char charAt(final int i) {
      int j = index + i;
      if (j >= n) j -= n;
      return string.charAt(j);
    }

    @Override
    public int compareTo(final Indices that) {
      if (this == that) return 0;
      for (int i = 0; i < n; ++i) {
        if (this.charAt(i) < that.charAt(i)) return -1;
        if (this.charAt(i) > that.charAt(i)) return +1;
      }
      return 0;
    }
  }

  /**
   * Length of s.
   *
   * @return Length of s.
   */
  public int length() {
    return n;
  }

  /**
   * Returns index of ith sorted suffix.
   *
   * @param i Rank of sorted suffix.
   * @return Index of ith sorted suffix.
   */
  public int index(final int i) {
    if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
    return indices[i].index;
  }

  /**
   * Unit testing of the methods (optional).
   */
  public static void main(String[] args) {
    String string = "CADABRA!ABRA";
    CircularSuffixArray csa = new CircularSuffixArray(string);
    for (int i = 0; i < csa.length(); ++i) {
      int index = csa.index(i);
      String original = string.substring(i) + string.substring(0, i);
      String sorted = string.substring(index) + string.substring(0, index);
      System.out.println(String.format("%3d\t%s\t%s\t%3d", i, original, sorted, index));
    }
  }
}
