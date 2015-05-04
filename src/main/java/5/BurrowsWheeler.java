/**
 * Given a typical English text file, transform it into a text file in which sequences of the same character occur near
 * each other many times.
 *
 * @author Kevin Crosby
 */
public class BurrowsWheeler {
  private static final int R = 256; // extended ASCII

  /**
   * Apply Burrows-Wheeler encoding, reading from standard input and writing to standard output.
   */
  public static void encode() {
    String string = BinaryStdIn.readString();
    CircularSuffixArray csa = new CircularSuffixArray(string);
    int n = csa.length();
    for (int i = 0; i < n; ++i) {
      if (csa.index(i) == 0) {
        BinaryStdOut.write(i);
        break;
      }
    }
    for (int i = 0; i < n; ++i) {
      int j = csa.index(i) + n - 1;
      if (j >= n) j -= n;
      BinaryStdOut.write(string.charAt(j), 8);
    }
    BinaryStdOut.close();
  }

  /**
   * Apply Burrows-Wheeler decoding, reading from standard input and writing to standard output.
   */
  public static void decode() {
    int first = BinaryStdIn.readInt();
    String string = BinaryStdIn.readString();
    int n = string.length();
    int[] a = new int[n];
    for (int i = 0; i < n; ++i) {
      a[i] = i;
    }
    sort(string, a);
    for (int i = 0, next = first; i < n; ++i, next = a[next]) {
      BinaryStdOut.write(string.charAt(a[next]), 8);
    }
    BinaryStdOut.close();
  }

  // LSD radix sort
  private static void sort(final String string, final int[] a) {
    final int N = a.length;
    int[] aux = new int[N];

    // compute frequency counts
    int[] count = new int[R + 1];
    for (final int element : a) {
      count[string.charAt(element) + 1]++;
    }

    // compute cumulates
    for (int r = 0; r < R; ++r) {
      count[r + 1] += count[r];
    }

    // move data
    for (final int element : a) {
      aux[count[string.charAt(element)]++] = element;
    }

    // copy back
    System.arraycopy(aux, 0, a, 0, N);
  }

  /**
   * If args[0] is '-', apply Burrows-Wheeler encoding.
   * If args[0] is '+', apply Burrows-Wheeler decoding.
   */
  public static void main(String[] args) {
    switch (args[0]) {
      case "-":
        encode();
        break;
      case "+":
        decode();
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }
}
