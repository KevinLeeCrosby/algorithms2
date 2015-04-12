import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given the standings in a sports division at some point during the season, determine which teams have been
 * mathematically eliminated from winning their division.
 *
 * In the baseball elimination problem, there is a division consisting of N teams. At some point during the season,
 * team i has w[i] wins, l[i] losses, r[i] remaining games, and g[i][j] games left to play against team j. A team is
 * mathematically eliminated if it cannot possibly finish the season in (or tied for) first place. The goal is to
 * determine exactly which teams are mathematically eliminated. For simplicity, we assume that no games end in a tie
 * (as is the case in Major League Baseball) and that there are no rainouts (i.e., every scheduled game is played).
 *
 * @author Kevin Crosby
 */
public class BaseballElimination {
  private static final double INFINITY = Double.POSITIVE_INFINITY;

  private int n, v, e, s, t, p;
  private final String[] teams;                        // given an index, return a team name
  private final Map<String, Integer> teamMap;          // given a team name, return an index
  private final Map<String, FordFulkerson> maxFlowMap; // given a team name, return the FordFulkerson max flow

  private final int[] w, l, r;                // wins, losses, remaining games
  private final int[][] g;                    // games left to play against other teams


  /**
   * Create a baseball division from given filename.
   *
   * @param filename Filename to load teams from.
   */
  public BaseballElimination(final String filename) {
    In in = new In(filename);
    n = in.readInt();               // number of teams
    v = 2 + n * (n - 1) / 2;        // number of flow network vertices
    e = (n - 1) * (3 * n - 4) / 2;  // number of flow network edges
    s = v - 2; // source index
    t = v - 1; // sink index
    p = n - 1; // start of pair indices [p,s)

    teams = new String[n];         // index to teams
    teamMap = new HashMap<>(n);    // teams to index
    maxFlowMap = new HashMap<>(n); // teams to FordFulkerson max flow

    w = new int[n];    // wins
    l = new int[n];    // losses
    r = new int[n];    // remaining games
    g = new int[n][n]; // games left to play against other teams

    for (int i = 0; i < n; ++i) {
      teams[i] = in.readString();
      teamMap.put(teams[i], i);
      w[i] = in.readInt();
      l[i] = in.readInt();
      r[i] = in.readInt();
      for (int j = 0; j < n; ++j) {
        g[i][j] = in.readInt();
      }
    }
  }

  /**
   * Number of teams.
   *
   * @return Number of teams.
   */
  public int numberOfTeams() {
    return n;
  }

  /**
   * All teams.
   *
   * @return Iterable of all teams.
   */
  public Iterable<String> teams() {
    return Arrays.asList(teams);
  }

  /**
   * Number of wins for given team.
   *
   * @param team Team to evaluate.
   * @return Number of wins for team.
   */
  public int wins(final String team) {
    if (!teamMap.containsKey(team)) {
      throw new IllegalArgumentException();
    }
    return w[teamMap.get(team)];
  }

  /**
   * Number of losses for given team.
   *
   * @param team Team to evaluate.
   * @return Number of losses for team.
   */
  public int losses(final String team) {
    if (!teamMap.containsKey(team)) {
      throw new IllegalArgumentException();
    }
    return l[teamMap.get(team)];
  }

  /**
   * Number of remaining games for given team.
   *
   * @param team Team to evaluate.
   * @return Number of remaining games for team.
   */
  public int remaining(final String team) {
    if (!teamMap.containsKey(team)) {
      throw new IllegalArgumentException();
    }
    return r[teamMap.get(team)];
  }

  /**
   * Number of remaining games between team1 and team2.
   *
   * @param team1 First team to evaluate.
   * @param team2 Second team to evaluate.
   * @return Number of remaining games between teams.
   */
  public int against(final String team1, final String team2) {
    if (!teamMap.containsKey(team1) || !teamMap.containsKey(team2)) {
      throw new IllegalArgumentException();
    }
    return g[teamMap.get(team1)][teamMap.get(team2)];
  }

  private FordFulkerson evaluate(final String team) {
    if (!teamMap.containsKey(team)) {
      throw new IllegalArgumentException();
    }
    if (!maxFlowMap.containsKey(team)) {
      int x = teamMap.get(team);
      FlowNetwork fn = new FlowNetwork(v);
      // connect source s to game vertices, and game vertices to team vertices
      for (int i = 0, a = i, w = p; i < n - 1; ++i) {
        if (i == x) { continue; }
        for (int j = i + 1, b = j; j < n; ++j) {
          if (j == x) { continue; }
          fn.addEdge(new FlowEdge(s, w, g[i][j]));
          fn.addEdge(new FlowEdge(w, a, INFINITY));
          fn.addEdge(new FlowEdge(w++, b++, INFINITY));
        }
        a++;
      }
      // connect team vertices to sink t
      for (int i = 0, a = i; i < n; ++i) {
        if (i == x) { continue; }
        fn.addEdge(new FlowEdge(a++, t, w[x] + r[x] - w[i]));
      }
      assert fn.E() == e : "Incorrect number of edges!";
      maxFlowMap.put(team, new FordFulkerson(fn, s, t));
    }
    return maxFlowMap.get(team);
  }

  /**
   * Is given team eliminated?
   *
   * @param team Team to evaluate.
   * @return True if team is eliminated, false otherwise.
   */
  public boolean isEliminated(final String team) {
    if (!teamMap.containsKey(team)) {
      throw new IllegalArgumentException();
    }
    int x = teamMap.get(team);

    // trivial elimination
    for (int i = 0; i < n; ++i) {
      if (x == i) { continue; }
      if (w[x] + r[x] < w[i]) { return true; }
    }

    // non-trivial elimination
    FordFulkerson ff = evaluate(team);
    for (int i = 0, a = i; i < n; ++i) {
      if (i == x) { continue; }
      if (ff.inCut(a++)) { return true; }
    }
    return false;
  }

  /**
   * Subset R of teams that eliminates given team; null if not eliminated.
   *
   * @param team Team to evaluate.
   * @return Subset of teams that eliminates given team, or null if team not eliminated.
   */
  public Iterable<String> certificateOfElimination(final String team) {
    if (!teamMap.containsKey(team)) {
      throw new IllegalArgumentException();
    }

    if (!isEliminated(team)) { return null; }

    Queue<String> subset = new Queue<>();
    int x = teamMap.get(team);

    // trivial elimination
    for (int i = 0; i < n; ++i) {
      if (x == i) { continue; }
      if (w[x] + r[x] < w[i]) {
        subset.enqueue(teams[i]);
        return subset;
      }
    }

    // non-trivial elimination
    double mu = 0.0;
    FordFulkerson ff = evaluate(team);
    List<Integer> indices = new ArrayList<>();
    for (int i = 0, a = i; i < n; ++i) {
      if (i == x) { continue; }
      if (ff.inCut(a++)) {
        subset.enqueue(teams[i]);
        indices.add(i);
        mu += w[i];
      }
    }
    int size = indices.size();
    for (int a = 0; a < size - 1; ++a) {
      for (int b = a + 1; b < size; ++b) {
        int i = indices.get(a), j = indices.get(b);
        mu += g[i][j];
      }
    }
    mu /= size;

    assert w[x] + r[x] < mu : "Invalid certificate of elimination in R!  Average smaller than max number of games!";
    assert !subset.isEmpty() : "Invalid certificate of elimination in R!  EMPTY!";
    return subset;
  }

  public static void main(String[] args) {
    BaseballElimination division = new BaseballElimination(args[0]);
    for (String team : division.teams()) {
      if (division.isEliminated(team)) {
        StdOut.print(team + " is eliminated by the subset R = { ");
        for (String t : division.certificateOfElimination(team)) {
          StdOut.print(t + " ");
        }
        StdOut.println("}");
      } else {
        StdOut.println(team + " is not eliminated");
      }
    }
  }
}
