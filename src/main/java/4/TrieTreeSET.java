import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * The <tt>TrieSET</tt> class represents an ordered set of strings.
 * It supports the usual <em>add</em>, <em>contains</em>, and <em>delete</em> methods. It also provides character-based
 * methods for finding the string in the set that is the <em>longest prefix</em> of a given prefix, finding all strings
 * in the set that <em>start with</em> a given prefix, and finding all strings in the set that <em>match</em> a given
 * pattern.
 * <p>
 * This implementation uses a TreeMap trie, which was adapted from TrieSET by Robert Sedgewick and Kevin Wayne.
 * The <em>add</em>, <em>contains</em>, <em>delete</em>, and <em>longest prefix</em> methods take time proportional to
 * the length of the key (in the worst case). Construction takes constant time.
 * <p>
 * For additional documentation, see
 * <a href="http://algs4.cs.princeton.edu/52trie">Section 5.2</a> of
 * <i>Algorithms in Java, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Kevin Crosby
 * @see TrieSET by Robert Sedgewick and Kevin Wayne
 */
public class TrieTreeSET implements Iterable<String> {
  private Node root;      // root of trie
  private int N;          // number of keys in trie

  // TreeMap trie node
  private static class Node {
    private Map<Character, Node> next = new TreeMap<>();
    private boolean isString;
  }

  /**
   * Initializes an empty set of strings.
   */
  public TrieTreeSET() {
  }

  /**
   * Does the set contain the given key?
   *
   * @param key the key
   * @return <tt>true</tt> if the set contains <tt>key</tt> and <tt>false</tt> otherwise
   * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
   */
  public boolean contains(String key) {
    Node x = get(root, key, 0);
    return x != null && x.isString;
  }

  private Node get(Node x, String key, int d) {
    if (x == null) return null;
    if (d == key.length()) return x;
    char c = key.charAt(d);
    return get(x.next.get(c), key, d + 1);
  }

  /**
   * Adds the key to the set if it is not already present.
   *
   * @param key the key to add
   * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
   */
  public void add(String key) {
    root = add(root, key, 0);
  }

  private Node add(Node x, String key, int d) {
    if (x == null) x = new Node();
    if (d == key.length()) {
      if (!x.isString) N++;
      x.isString = true;
    } else {
      char c = key.charAt(d);
      x.next.put(c, add(x.next.get(c), key, d + 1));
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
   * To iterate over all of the keys in a set named <tt>set</tt>,
   * use the foreach notation: <tt>for (Key key : set)</tt>.
   *
   * @return an iterator to all of the keys in the set
   */
  public Iterator<String> iterator() {
    return keysWithPrefix("").iterator();
  }

  /**
   * Returns all of the keys in the set that start with <tt>prefix</tt>.
   *
   * @param prefix the prefix
   * @return all of the keys in the set that start with <tt>prefix</tt>, as an iterable
   */
  public Iterable<String> keysWithPrefix(String prefix) {
    Queue<String> results = new Queue<>();
    Node x = get(root, prefix, 0);
    collect(x, new StringBuilder(prefix), results);
    return results;
  }

  private void collect(Node x, StringBuilder prefix, Queue<String> results) {
    if (x == null) return;
    if (x.isString) results.enqueue(prefix.toString());
    for (char c : x.next.keySet()) {
      prefix.append(c);
      collect(x.next.get(c), prefix, results);
      prefix.deleteCharAt(prefix.length() - 1);
    }
  }

  /**
   * Returns all of the keys in the set that match <tt>pattern</tt>,
   * where . symbol is treated as a wildcard character.
   *
   * @param pattern the pattern
   * @return all of the keys in the set that match <tt>pattern</tt>,
   * as an iterable, where . is treated as a wildcard character.
   */
  public Iterable<String> keysThatMatch(String pattern) {
    Queue<String> results = new Queue<>();
    StringBuilder prefix = new StringBuilder();
    collect(root, prefix, pattern, results);
    return results;
  }

  private void collect(Node x, StringBuilder prefix, String pattern, Queue<String> results) {
    if (x == null) return;
    int d = prefix.length();
    if (d == pattern.length() && x.isString)
      results.enqueue(prefix.toString());
    if (d == pattern.length())
      return;
    char c = pattern.charAt(d);
    if (c == '.') {
      for (char ch : x.next.keySet()) {
        prefix.append(ch);
        collect(x.next.get(ch), prefix, pattern, results);
        prefix.deleteCharAt(prefix.length() - 1);
      }
    } else {
      prefix.append(c);
      collect(x.next.get(c), prefix, pattern, results);
      prefix.deleteCharAt(prefix.length() - 1);
    }
  }

  /**
   * Returns the string in the set that is the longest prefix of <tt>query</tt>,
   * or <tt>null</tt>, if no such string.
   *
   * @param query the query string
   * @return the string in the set that is the longest prefix of <tt>query</tt>,
   * or <tt>null</tt> if no such string
   * @throws NullPointerException if <tt>query</tt> is <tt>null</tt>
   */
  public String longestPrefixOf(String query) {
    int length = longestPrefixOf(root, query, 0, -1);
    if (length == -1) return null;
    return query.substring(0, length);
  }

  // returns the length of the longest string key in the subtrie
  // rooted at x that is a prefix of the query string,
  // assuming the first d character match and we have already
  // found a prefix match of length length
  private int longestPrefixOf(Node x, String query, int d, int length) {
    if (x == null) return length;
    if (x.isString) length = d;
    if (d == query.length()) return length;
    char c = query.charAt(d);
    return longestPrefixOf(x.next.get(c), query, d + 1, length);
  }

  /**
   * Removes the key from the set if the key is present.
   *
   * @param key the key
   * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
   */
  public void delete(String key) {
    root = delete(root, key, 0);
  }

  private Node delete(Node x, String key, int d) {
    if (x == null) return null;
    if (d == key.length()) {
      if (x.isString) N--;
      x.isString = false;
    } else {
      char c = key.charAt(d);
      Node node = delete(x.next.get(c), key, d + 1);
      if (node == null) {
        x.next.remove(c);
      } else {
        x.next.put(c, node);
      }
    }

    // remove subtrie rooted at x if it is completely empty
    if (x.isString) return x;
    if (!x.next.isEmpty()) return x.next.values().iterator().next();
    return null;
  }


  /**
   * Unit tests the <tt>TrieTreeSET</tt> data type.
   */
  public static void main(String[] args) {
    TrieTreeSET set = new TrieTreeSET();
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
