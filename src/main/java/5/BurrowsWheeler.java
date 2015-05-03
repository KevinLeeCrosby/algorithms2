import java.util.Arrays;

/**
 * Given a typical English text file, transform it into a text file in which sequences of the same character occur near
 * each other many times.
 *
 * @author Kevin Crosby
 */
public class BurrowsWheeler {
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
      BinaryStdOut.write(string.charAt((csa.index(i) + n - 1) % n), 8);
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
    Column[] f = new Column[n];
    for (int i = 0; i < n; ++i) {
      f[i] = new Column(string.charAt(i), i);
    }
    Arrays.sort(f);
    for (int i = 0, next = first; i < n; ++i, next = f[next].next) {
      BinaryStdOut.write(f[next].character, 8);
    }
    BinaryStdOut.close();
  }

  private static class Column implements Comparable<Column> {
    private final char character;
    private final int next;

    private Column(final char character, final int next) {
      this.character = character;
      this.next = next;
    }

    @Override
    public int compareTo(final Column that) {
      return Character.valueOf(this.character).compareTo(that.character);
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
