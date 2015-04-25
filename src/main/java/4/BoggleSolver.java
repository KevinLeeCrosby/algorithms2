import java.util.Iterator;

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
  private final TST<Integer> trie;

  /**
   * Initializes the data structure using the given array of strings as the dictionary.
   * (You can assume each word in the dictionary contains only the uppercase letters A through Z.)
   *
   * @param dictionary Words in dictionary.
   */
  public BoggleSolver(final String[] dictionary) {
    int n = dictionary.length;
    trie = new TST<>();
    String[] shuffle = new String[n];
    System.arraycopy(dictionary, 0, shuffle, 0, n);
    StdRandom.shuffle(shuffle);
    for (final String word : shuffle) {
      trie.put(word, score(word));
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
    private final TST<Integer> words;
    private final BoggleBoard board;
    private final Stack<Queue<Integer>> stack;
    private final Stack<Character> letters;
    private final Stack<Integer> dice;
    private final boolean[] visited;
    private int m, n, count;
    private String word;

    public WordIterator(final BoggleBoard board) {
      this.board = board;
      m = board.rows();
      n = board.cols();
      count = 0;
      visited = new boolean[m * n];
      stack = new Stack<>();
      stack.push(neighbors());
      letters = new Stack<>();
      dice = new Stack<>();
      word = null;
      words = new TST<>();
    }

    @Override
    public boolean hasNext() {
      while (!stack.isEmpty()) {
        if (!stack.peek().isEmpty()) {
          int die = stack.peek().dequeue();
          addDie(die);
          String prefix = catenate(letters);
          boolean prune = !trie.keysWithPrefix(prefix).iterator().hasNext();
          if (!prune) {
            int score = trie.get(prefix);
            if (score > 0 && !words.contains(prefix)) {
              word = prefix;
              words.put(word, count++);
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
      letters.push(getLetter(die));
      visited[die] = true;
    }

    private void removeDie() {
      int die = dice.pop();
      letters.pop();
      visited[die] = false;
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
      int r = die / n, c = die % n;
      return neighbors(r, c);
    }

    private Queue<Integer> neighbors(final int row, final int col) {
      Queue<Integer> neighbors = new Queue<>();
      for (int dr = -1; dr <= +1; ++dr) {
        int r = row + dr;
        if (r < 0 || r >= m) { continue; }
        for (int dc = -1; dc <= +1; ++dc) {
          int c = col + dc;
          if (c < 0 || c >= n || (dr == 0 && dc == 0)) { continue; }
          int neighbor = r * n + c;
          neighbors.enqueue(neighbor);
        }
      }
      return neighbors;
    }

    private String catenate(final Iterable<Character> characters) {
      StringBuilder sb = new StringBuilder();
      boolean isStack = characters instanceof Stack;
      for (char letter : characters) {
        if (!isStack) {
          sb.append(letter);
        }
        if (letter == 'Q') {
          sb.append('U');
        }
        if (isStack) {
          sb.append(letter);
        }
      }
      if (isStack) {
        return sb.reverse().toString();
      }
      return sb.toString();
    }
  }

  private int score(final String word) {
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

  /**
   * Returns the score of the given word if it is in the dictionary, zero otherwise.
   * (You can assume the word contains only the uppercase letters A through Z.)
   *
   * @param word Word to score.
   * @return Score of word.
   */
  public int scoreOf(final String word) {
    Integer score = trie.get(word);
    if (score == null) {
      return 0;
    }
    return score;
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
