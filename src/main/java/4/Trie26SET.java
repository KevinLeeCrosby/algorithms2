/*************************************************************************
 *  Compilation:  javac TrieSET.java
 *  Execution:    java TrieSET < words.txt
 *  Dependencies: StdIn.java
 *
 *  An set for ASCII strings, implemented  using a 26-way trie.
 *
 *  Sample client reads in a list of words from standard input and
 *  prints out each word, removing any duplicates.
 *
 *************************************************************************/

import java.util.Iterator;

/**
 * The <tt>TrieSET</tt> class represents an ordered set of strings over the ASCII alphabet.
 * It supports the usual <em>add</em>, <em>contains</em>, and <em>delete</em> methods. It also provides character-based
 * methods for finding the string in the set that is the <em>longest prefix</em> of a given prefix, finding all strings
 * in the set that <em>start with</em> a given prefix, and finding all strings in the set that <em>match</em> a given
 * pattern.
 * <p>
 * This implementation uses a 26-way trie.
 * The <em>add</em>, <em>contains</em>, <em>delete</em>, and <em>longest prefix</em> methods take time proportional to
 * the length of the key (in the worst case). Construction takes constant time.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/52trie">Section 5.2</a> of
 * <i>Algorithms in Java, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class Trie26SET implements Iterable<String> {
  private static final int R = 26;        // ASCII

  private Node root;      // root of trie
  private int N;          // number of keys in trie
  private Node lastGoodNode = null; // last good node
  private String lastGoodPrefix;    // last good prefix

  // R-way trie node
  private static class Node {
    private Node[] next = new Node[R];
    private boolean isString;
  }

  /**
   * Initializes an empty set of strings.
   */
  public Trie26SET() {
  }

  private int index(final char c) {
    return c - 'A';
  }

  /**
   * Does the set contain the given key?
   *
   * @param key the key
   * @return <tt>true</tt> if the set contains <tt>key</tt> and <tt>false</tt> otherwise
   * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
   */
  public boolean contains(final String key) {
    Node x = get(root, key, 0);
    if (x == null) return false;
    return x.isString;
  }

  private Node get(final Node x, final String key, final int d) {
    if (x == null) return null;
    if (d == key.length()) return x;
    char c = key.charAt(d);
    return get(x.next[index(c)], key, d + 1);
  }

  /**
   * Adds the key to the set if it is not already present.
   *
   * @param key the key to add
   * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
   */
  public void add(final String key) {
    root = add(root, key, 0);
  }

  private Node add(Node x, final String key, final int d) {
    if (x == null) x = new Node();
    if (d == key.length()) {
      if (!x.isString) N++;
      x.isString = true;
    } else {
      char c = key.charAt(d);
      x.next[index(c)] = add(x.next[index(c)], key, d + 1);
    }
    return x;
  }

  /**
   * Returns the number of strings in the set.
   *
   * @return the number of strings in the set
   */
  public int size() {
    return N;
  }

  /**
   * Is the set empty?
   *
   * @return <tt>true</tt> if the set is empty, and <tt>false</tt> otherwise
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns all of the keys in the set, as an iterator.
   * To iterate over all of the keys in a set named <tt>set</tt>, use the foreach notation:
   * <tt>for (Key key : set)</tt>.
   *
   * @return an iterator to all of the keys in the set
   */
  public Iterator<String> iterator() {
    return keysWithPrefix("").iterator();
  }

  /**
   * Determines if there exists a key in the set that starts with <tt>prefix</tt>.
   *
   * @param prefix the prefix
   * @return <tt>true</tt> if there exists a key that starts with <tt>prefix</tt> and <tt>false</tt> otherwise
   */
  public boolean hasKeyWithPrefix(final String prefix) {
    Node node;
    if (lastGoodNode != null) {
      if (lastGoodPrefix.startsWith(prefix)) {
        return true;
      } else if (prefix.startsWith(lastGoodPrefix)) {
        node = get(lastGoodNode, prefix, lastGoodPrefix.length());
      } else {
        node = get(root, prefix, 0);
      }
    } else {
      node = get(root, prefix, 0);
    }
    if (node != null) {
      lastGoodNode = node;
      lastGoodPrefix = prefix;
      return true;
    }
    return false;
  }

  /**
   * Returns all of the keys in the set that start with <tt>prefix</tt>.
   *
   * @param prefix the prefix
   * @return all of the keys in the set that start with <tt>prefix</tt>, as an iterable
   */
  public Iterable<String> keysWithPrefix(final String prefix) {
    Queue<String> results = new Queue<>();
    Node x = get(root, prefix, 0);
    collect(x, new StringBuilder(prefix), results);
    return results;
  }

  private void collect(final Node x, final StringBuilder prefix, final Queue<String> results) {
    if (x == null) return;
    if (x.isString) results.enqueue(prefix.toString());
    for (char c = 'A'; c <= 'Z'; c++) {
      prefix.append(c);
      collect(x.next[index(c)], prefix, results);
      prefix.deleteCharAt(prefix.length() - 1);
    }
  }

  /**
   * Returns all of the keys in the set that match <tt>pattern</tt>, where . symbol is treated as a wildcard character.
   *
   * @param pattern the pattern
   * @return all of the keys in the set that match <tt>pattern</tt>, as an iterable, where . is treated as a wildcard
   * character.
   */
  public Iterable<String> keysThatMatch(final String pattern) {
    Queue<String> results = new Queue<>();
    StringBuilder prefix = new StringBuilder();
    collect(root, prefix, pattern, results);
    return results;
  }

  private void collect(final Node x, final StringBuilder prefix, final String pattern, final Queue<String> results) {
    if (x == null) return;
    int d = prefix.length();
    if (d == pattern.length() && x.isString)
      results.enqueue(prefix.toString());
    if (d == pattern.length())
      return;
    char c = pattern.charAt(d);
    if (c == '.') {
      for (char ch = 'A'; ch <= 'Z'; ch++) {
        prefix.append(ch);
        collect(x.next[index(ch)], prefix, pattern, results);
        prefix.deleteCharAt(prefix.length() - 1);
      }
    } else {
      prefix.append(c);
      collect(x.next[index(c)], prefix, pattern, results);
      prefix.deleteCharAt(prefix.length() - 1);
    }
  }

  /**
   * Returns the string in the set that is the longest prefix of <tt>query</tt>, or <tt>null</tt>, if no such string.
   *
   * @param query the query string
   * @return the string in the set that is the longest prefix of <tt>query</tt>, or <tt>null</tt> if no such string
   * @throws NullPointerException if <tt>query</tt> is <tt>null</tt>
   */
  public String longestPrefixOf(final String query) {
    int length = longestPrefixOf(root, query, 0, -1);
    if (length == -1) return null;
    return query.substring(0, length);
  }

  // returns the length of the longest string key in the subtrie
  // rooted at x that is a prefix of the query string,
  // assuming the first d character match and we have already
  // found a prefix match of length length
  private int longestPrefixOf(final Node x, final String query, final int d, int length) {
    if (x == null) return length;
    if (x.isString) length = d;
    if (d == query.length()) return length;
    char c = query.charAt(d);
    return longestPrefixOf(x.next[index(c)], query, d + 1, length);
  }

  /**
   * Removes the key from the set if the key is present.
   *
   * @param key the key
   * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
   */
  public void delete(final String key) {
    root = delete(root, key, 0);
  }

  private Node delete(final Node x, final String key, final int d) {
    if (x == null) return null;
    if (d == key.length()) {
      if (x.isString) N--;
      x.isString = false;
    } else {
      char c = key.charAt(d);
      x.next[index(c)] = delete(x.next[index(c)], key, d + 1);
    }

    // remove subtrie rooted at x if it is completely empty
    if (x.isString) return x;
    for (char c = 'A'; c <= 'Z'; c++)
      if (x.next[index(c)] != null)
        return x;
    return null;
  }


  /**
   * Unit tests the <tt>TrieSET</tt> data type.
   */
  public static void main(String[] args) {
    Trie26SET set = new Trie26SET();
    while (!StdIn.isEmpty()) {
      String key = StdIn.readString();
      set.add(key);
    }

    // print results
    if (set.size() < 100) {
      StdOut.println("keys(\"\"):");
      for (String key : set) {
        StdOut.println(key);
      }
      StdOut.println();
    }

    StdOut.println("longestPrefixOf(\"shellsort\"):");
    StdOut.println(set.longestPrefixOf("shellsort"));
    StdOut.println();

    StdOut.println("longestPrefixOf(\"xshellsort\"):");
    StdOut.println(set.longestPrefixOf("xshellsort"));
    StdOut.println();

    StdOut.println("keysWithPrefix(\"shor\"):");
    for (String s : set.keysWithPrefix("shor"))
      StdOut.println(s);
    StdOut.println();

    StdOut.println("keysWithPrefix(\"shortening\"):");
    for (String s : set.keysWithPrefix("shortening"))
      StdOut.println(s);
    StdOut.println();

    StdOut.println("keysThatMatch(\".he.l.\"):");
    for (String s : set.keysThatMatch(".he.l."))
      StdOut.println(s);
  }
}
