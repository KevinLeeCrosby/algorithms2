import java.util.HashMap;
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
  private final Trie26SET trie;

  /**
   * Initializes the data structure using the given array of strings as the dictionary.
   * (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
   *
   * @param dictionary Words in dictionary.
   */
  public BoggleSolver(final String[] dictionary) {
    trie = new Trie26SET();
    for (final String word : dictionary) {
      if (word.length() > 2) {
        trie.add(word);
      }
    }
  }

  /**
   * Returns the set of all valid words in the given Boggle board, as an Iterable.
   *
   * @param board Boggle Board to process.
   * @return Iterable of all valid words found, based on dictionary.
   */
  public Iterable<String> getAllValidWords(final BoggleBoard board) {
    DepthFirstSearch dfs = new DepthFirstSearch(board);
    return dfs.getWords();
  }

  private class DepthFirstSearch {
    private final BoggleBoard board;
    private final Map<Integer, Iterable<Integer>> adjacencies;
    private final StringBuilder letters;
    private final Trie26SET words;
    private final boolean[] visited;
    private int m, n;

    public DepthFirstSearch(final BoggleBoard board) {
      this.board = board;
      m = board.rows();
      n = board.cols();
      adjacencies = new HashMap<>();
      letters = new StringBuilder();
      visited = new boolean[m * n];
      words = new Trie26SET();
      dfs(neighbors());
    }

    private void dfs(final Iterable<Integer> dice) {
      for (int die : dice) {
        dfs(die);
      }
    }

    private void dfs(final int die) {
      letterPush(die);
      String prefix = letters.toString();
      if (trie.hasKeyWithPrefix(prefix)) {
        if (trie.contains(prefix)) {
          words.add(prefix);
        }
        for (int d : newNeighbors(die)) {
          dfs(d);
        }
      }
      letterPop(die);
    }

    private Iterable<String> getWords() {
      return words;
    }

    private void letterPush(final int die) {
      visited[die] = true;
      char letter = getLetter(die);
      letters.append(letter);
      if (letter == 'Q') {
        letters.append('U');
      }
    }

    private void letterPop(final int die) {
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

    private Iterable<Integer> newNeighbors(final int die) {
      Queue<Integer> neighbors = new Queue<>();
      for (int neighbor : neighbors(die)) {
        if (!visited[neighbor]) {
          neighbors.enqueue(neighbor);
        }
      }
      return neighbors;
    }

    private Iterable<Integer> neighbors() {
      Queue<Integer> neighbors = new Queue<>();
      for (int neighbor = 0; neighbor < m * n; ++neighbor) {
        neighbors.enqueue(neighbor);
      }
      return neighbors;
    }

    private Iterable<Integer> neighbors(final int die) {
      if (!adjacencies.containsKey(die)) {
        int r = die / n, c = die % n;
        adjacencies.put(die, neighbors(r, c));
      }
      return adjacencies.get(die);
    }

    private Iterable<Integer> neighbors(final int row, final int col) {
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
      return neighbors;
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
