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
    char[] characters = string.toCharArray();
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
      BinaryStdOut.write(characters[j], 8);
    }
    BinaryStdOut.close();
  }

  /**
   * Apply Burrows-Wheeler decoding, reading from standard input and writing to standard output.
   */
  public static void decode() {
    int first = BinaryStdIn.readInt();
    char[] characters = BinaryStdIn.readString().toCharArray();
    int n = characters.length;
    Column[] a = new Column[n];
    for (int i = 0; i < n; ++i) {
      a[i] = new Column(characters[i], i);
    }
    sort(a);
    for (int i = 0, next = first; i < n; ++i, next = a[next].next) {
      BinaryStdOut.write(a[next].character, 8);
    }
    BinaryStdOut.close();
  }

  private static void sort (final Column[] a) {
    final int N = a.length;
    Column[] aux = new Column[N];
    int[] count = new int[R + 1];
    for (final Column element : a) {
      count[element.character + 1]++;
    }
    for (int r = 0; r < R; ++r) {
      count[r + 1] += count[r];
    }
    for (final Column element : a) {
      aux[count[element.character]++] = element;
    }
    System.arraycopy(aux, 0, a, 0, N);
  }

  private static class Column {
    private final char character;
    private final int next;

    private Column(final char character, final int next) {
      this.character = character;
      this.next = next;
    }
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
