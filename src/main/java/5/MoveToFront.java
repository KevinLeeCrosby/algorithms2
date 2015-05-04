import java.util.LinkedList;

/**
 * Given a text file in which sequences of the same character occur near each other many times, convert it into a text
 * file in which certain characters appear more frequently than others.
 *
 * @author Kevin Crosby
 */
public class MoveToFront {
  private static final int R = 256; // extended ASCII

  private static LinkedList<Character> initialize() {
    LinkedList<Character> order = new LinkedList<>();
    for (char c = 0; c < R; ++c) {
      order.add(c);
    }
    return order;
  }

  /**
   * Apply move-to-front encoding, reading from standard input and writing to standard output.
   */
  public static void encode() {
    LinkedList<Character> order = initialize();
    while (!BinaryStdIn.isEmpty()) {
      char c = BinaryStdIn.readChar();
      int i = order.indexOf(c);
      BinaryStdOut.write(i, 8);
      order.remove(i);
      order.add(0, c);
    }
    BinaryStdOut.close();
  }

  /**
   * Apply move-to-front decoding, reading from standard input and writing to standard output.
   */
  public static void decode() {
    LinkedList<Character> order = initialize();
    while (!BinaryStdIn.isEmpty()) {
      int i = BinaryStdIn.readChar();
      char c = order.get(i);
      BinaryStdOut.write(c, 8);
      order.remove(i);
      order.add(0, c);
    }
    BinaryStdOut.close();
  }

  /**
   * If args[0] is '-', apply move-to-front encoding.
   * If args[0] is '+', apply move-to-front decoding.
   */
  public static void main(final String[] args) {
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
