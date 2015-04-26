import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Boggle®.
 *
 * The Boggle game. Boggle is a word game designed by Allan Turoff and distributed by Hasbro. It involves a board made
 * up of 16 cubic dice, where each die has a letter printed on each of its sides. At the beginning of the game, the 16
 * dice are shaken and randomly distributed into a 4-by-4 tray, with only the top sides of the dice visible. The
 * players compete to accumulate points by building valid words out of the dice according to the following rules:
 *
 * A valid word must be composed by following a sequence of adjacent dice—two dice are adjacent if they are horizontal,
 * vertical, or diagonal neighbors.
 * A valid word can use each die at most once.
 * A valid word must contain at least 3 letters.
 * A valid word must be in the dictionary (which typically does not contain proper nouns).
 *
 * @author Kevin Crosby
 */
public class BoggleSolver {
  private final TrieHashSET trie;

  /**
   * Initializes the data structure using the given array of strings as the dictionary.
   * (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
   *
   * @param dictionary Words in dictionary.
   */
  public BoggleSolver(final String[] dictionary) {
    trie = new TrieHashSET();
    for (final String word : dictionary) {
      trie.add(word);
    }
  }

  /**
   * Returns the set of all valid words in the given Boggle board, as an Iterable.
   *
   * @param board Boggle Board to process.
   * @return Iterable of all valid words found, based on dictionary.
   */
  public Iterable<String> getAllValidWords(final BoggleBoard board) {
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        return new WordIterator(board);
      }
    };
  }

  /**
   * Depth-First-Search iterator over all words.
   */
  private class WordIterator implements Iterator<String> {
    private final BoggleBoard board;
    private final Stack<Queue<Integer>> stack;
    private final Stack<Integer> dice;
    private final Map<Integer, Queue<Integer>> adjacencies;
    private final StringBuilder letters;
    private final TrieSET words;
    private final boolean[] visited;
    private String word;
    private int m, n;

    public WordIterator(final BoggleBoard board) {
      this.board = board;
      m = board.rows();
      n = board.cols();
      stack = new Stack<>();
      stack.push(neighbors());
      dice = new Stack<>();
      adjacencies = new HashMap<>();
      letters = new StringBuilder();
      words = new TrieSET();
      visited = new boolean[m * n];
      word = null;
    }

    @Override
    public boolean hasNext() {
      while (!stack.isEmpty()) {
        if (!stack.peek().isEmpty()) {
          int die = stack.peek().dequeue();
          addDie(die);
          String prefix = letters.toString();
          boolean prune = !trie.hasKeyWithPrefix(prefix);
          if (!prune) {
            if (scoreOf(prefix) > 0 && !words.contains(prefix)) {
              word = prefix;
              words.add(word);
            }
            Queue<Integer> layer = newNeighbors(die);
            prune = layer.isEmpty();
            if (!prune) {
              stack.push(layer);
            }
          }
          if (prune) {
            removeDie();
          }
        } else {
          stack.pop();
          if (!stack.isEmpty()) {
            removeDie();
          }
        }
        if (word != null) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String next() {
      String nextWord = word;
      word = null;
      return nextWord;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private void addDie(final int die) {
      dice.push(die);
      visited[die] = true;
      char letter = getLetter(die);
      letters.append(getLetter(die));
      if (letter == 'Q') {
        letters.append('U');
      }
    }

    private void removeDie() {
      int die = dice.pop();
      visited[die] = false;
      int end = letters.length(), start = end - 1;
      if (getLetter(die) == 'Q') {
        --start;
      }
      letters.delete(start, end);
    }

    private char getLetter(final int die) {
      int r = die / n, c = die % n;
      return board.getLetter(r, c);
    }

    private Queue<Integer> newNeighbors(final int die) {
      Queue<Integer> neighbors = new Queue<>();
      for (int neighbor : neighbors(die)) {
        if (!visited[neighbor]) {
          neighbors.enqueue(neighbor);
        }
      }
      return neighbors;
    }

    private Queue<Integer> neighbors() {
      Queue<Integer> neighbors = new Queue<>();
      for (int neighbor = 0; neighbor < m * n; ++neighbor) {
        neighbors.enqueue(neighbor);
      }
      return neighbors;
    }

    private Queue<Integer> neighbors(final int die) {
      if (!adjacencies.containsKey(die)) {
        int r = die / n, c = die % n;
        return neighbors(r, c); // adds to adjacencies
      }
      return adjacencies.get(die);
    }

    private Queue<Integer> neighbors(final int row, final int col) {
      int die = row * n + col;
      if (!adjacencies.containsKey(die)) {
        Queue<Integer> neighbors = new Queue<>();
        for (int dr = -1; dr <= +1; ++dr) {
          int r = row + dr;
          if (r < 0 || r >= m) {
            continue;
          }
          for (int dc = -1; dc <= +1; ++dc) {
            int c = col + dc;
            if (c < 0 || c >= n || (dr == 0 && dc == 0)) {
              continue;
            }
            int neighbor = r * n + c;
            neighbors.enqueue(neighbor);
          }
        }
        adjacencies.put(die, neighbors);
      }
      return adjacencies.get(die);
    }
  }

  /**
   * Returns the score of the given word if it is in the dictionary, zero otherwise.
   * (You can assume the word contains only the uppercase letters A through Z.)
   *
   * @param word Word to score.
   * @return Score of word.
   */
  public int scoreOf(final String word) {
    if (!trie.contains(word)) {
      return 0;
    }
    switch (word.length()) {
      case 0:
      case 1:
      case 2:
        return 0;
      case 3:
      case 4:
        return 1;
      case 5:
        return 2;
      case 6:
        return 3;
      case 7:
        return 5;
      default:
        return 11;
    }
  }

  public static void main(String[] args) {
    In in = new In(args[0]);
    String[] dictionary = in.readAllStrings();
    BoggleSolver solver = new BoggleSolver(dictionary);
    BoggleBoard board = new BoggleBoard(args[1]);
    int score = 0;
    for (String word : solver.getAllValidWords(board)) {
      StdOut.println(word);
      score += solver.scoreOf(word);
    }
    StdOut.println("Score = " + score);
  }
}
