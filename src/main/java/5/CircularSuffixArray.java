/**
 * To efficiently implement the key component in the Burrows-Wheeler transform, you will use a fundamental data
 * structure known as the circular suffix array, which describes the abstraction of a sorted array of the N circular
 * suffixes of a string of length N.
 *
 * @author Kevin Crosby
 */
public class CircularSuffixArray {
  private static final int R = 256;   // extend ASCII alphabet size
  private final int[] indices;
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
    indices = new int[n];
    for (int i = 0; i < n; ++i) {
      indices[i] = i;
    }
    sort(indices);
  }

  private char charAt(final int i, final int index) {
    int j = index + i;
    if (j >= n) j -= n;
    return string.charAt(j);
  }

  // LSD radix sort
  private void sort(int[] a) {
    int[] aux = new int[n];

    for (int d = n - 1; d >= 0; d--) {
      // sort by key-indexed counting on dth character

      // compute frequency counts
      int[] count = new int[R + 1];
      for (int i = 0; i < n; i++)
        count[charAt(d, a[i]) + 1]++;

      // compute cumulates
      for (int r = 0; r < R; r++)
        count[r + 1] += count[r];

      // move data
      for (int i = 0; i < n; i++)
        aux[count[charAt(d, a[i])]++] = a[i];

      // copy back
      System.arraycopy(aux, 0, a, 0, n);
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
    return indices[i];
  }

  /**
   * Unit testing of the methods (optional).
   */
  public static void main(String[] args) {
    //String string = "ABRACADABRA!";
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
