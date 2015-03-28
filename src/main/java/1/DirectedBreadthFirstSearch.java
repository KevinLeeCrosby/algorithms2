/**
 * @author Kevin Crosby
 */
public class DirectedBreadthFirstSearch {
  private static final int INFINITY = Integer.MAX_VALUE;
  private boolean[] marked;
  private int[] edgeTo, distTo;

  /**
   * Computes the shortest path from <tt>source</tt> and every other vertex in graph <tt>G</tt>.
   *
   * @param G      Digraph to search.
   * @param source Source vertex to start from.
   */
  public DirectedBreadthFirstSearch(final Digraph G, final int source) {
    initialize(G);
    bfs(G, source);
  }

  /**
   * Computes the shortest path from any one of the source vertices in <tt>sources</tt> to every other vertex in graph
   * <tt>G</tt>.
   *
   * @param G       Digraph to search.
   * @param sources Source vertices to start from.
   */
  public DirectedBreadthFirstSearch(final Digraph G, final Iterable<Integer> sources) {
    initialize(G);
    bfs(G, sources);
  }

  private void initialize(final Digraph G) {
    int V = G.V();
    marked = new boolean[V];
    edgeTo = new int[V];
    distTo = new int[V];
    for (int v = 0; v < V; v++) {
      edgeTo[v] = INFINITY;
      distTo[v] = INFINITY;
    }
  }

  // BFS from single source
  private void bfs(final Digraph G, final int source) {
    Queue<Integer> sources = new Queue<>();
    sources.enqueue(source);
    bfs(G, sources);
  }

  // BFS from multiple sources
  private void bfs(final Digraph G, final Iterable<Integer> sources) {
    Queue<Integer> q = new Queue<>();
    for (int source : sources) {
      marked[source] = true;
      distTo[source] = 0;
      q.enqueue(source);
    }
    while (!q.isEmpty()) {
      int v = q.dequeue();
      for (int w : G.adj(v)) {
        if (!marked[w]) {
          marked[w] = true;
          edgeTo[w] = v;
          distTo[w] = distTo[v] + 1;
          q.enqueue(w);
        }
      }
    }
  }

  /**
   * Is there a directed path from the source <tt>source</tt> (or sources) to vertex <tt>destination</tt>?
   *
   * @param destination Vertex to check.
   * @return True if there is a directed path, false otherwise.
   */
  public boolean hasPathTo(final int destination) {
    return marked[destination];
  }

  /**
   * Returns the number of edges in a shortest path from the source <tt>source</tt> (or sources) to vertex
   * <tt>destination<tt>?
   *
   * @param destination Vertex to get distance to.
   * @return Number of edges in a shortest path.
   */
  public int distTo(final int destination) {
    return distTo[destination];
  }

  /**
   * Returns a shortest path from <tt>source</tt> (or sources) to <tt>destination</tt>, or <tt>null</tt> if no such
   * path.
   *
   * @param destination Vertex to get path to.
   * @return Sequence of vertices on a shortest path, as an Iterable.
   */
  public Iterable<Integer> pathTo(final int destination) {
    if (!hasPathTo(destination)) { return null; }
    Stack<Integer> path = new Stack<>();
    int x;
    for (x = destination; distTo[x] != 0; x = edgeTo[x]) { path.push(x); }
    path.push(x);
    return path;
  }
}
