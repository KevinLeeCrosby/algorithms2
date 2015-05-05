/**
 * To efficiently implement the key component in the Burrows-Wheeler transform, you will use a fundamental data
 * structure known as the circular suffix array, which describes the abstraction of a sorted array of the N circular
 * suffixes of a string of length N.
 *
 * @author Kevin Crosby
 */
public class CircularSuffixArray {
  private static final int R = 256;   // extend ASCII alphabet size
  private static final int CUTOFF = 15;   // cutoff to insertion sort
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
    int j = (index + i) % n;
    return string.charAt(j);
  }

  // MSD radix sort
  private void sort(final int[] a) {
    int[] aux = new int[n];
    sort(a, 0, n - 1, 0, aux);
  }

  // sort from a[lo] to a[hi], starting at the dth character
  private void sort(final int[] a, final int lo, final int hi, final int d, final int[] aux) {

    // cutoff to insertion sort for small subarrays
    if (hi <= lo + CUTOFF) {
      insertion(a, lo, hi, d);
      return;
    }

    // compute frequency counts
    int[] count = new int[R + 2];
    for (int i = lo; i <= hi; i++) {
      count[charAt(d, a[i]) + 2]++;
    }

    // transform counts to indices
    for (int r = 0; r < R + 1; r++)
      count[r + 1] += count[r];

    // distribute
    for (int i = lo; i <= hi; i++) {
      aux[count[charAt(d, a[i]) + 1]++] = a[i];
    }

    // copy back
    System.arraycopy(aux, 0, a, lo, hi + 1 - lo);

    // recursively sort for each character
    for (int r = 0; r < R; r++)
      sort(a, lo + count[r], lo + count[r + 1] - 1, d + 1, aux);
  }

  // insertion sort a[lo..hi], starting at dth character
  private void insertion(final int[] a, final int lo, final int hi, final int d) {
    for (int i = lo; i <= hi; i++)
      for (int j = i; j > lo && less(a[j], a[j - 1], d); j--)
        exch(a, j, j - 1);
  }

  // exchange a[i] and a[j]
  private void exch(final int[] a, final int i, final int j) {
    int temp = a[i];
    a[i] = a[j];
    a[j] = temp;
  }

  // is v less than w, starting at character d
  private boolean less(final int v, final int w, final int d) {
    // assert v.substring(0, d).equals(w.substring(0, d));
    for (int i = d; i < n; i++) {
      if (charAt(i, v) < charAt(i, w)) return true;
      if (charAt(i, v) > charAt(i, w)) return false;
    }
    return false;
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
