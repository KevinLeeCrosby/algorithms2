/**
 * An ancestral path between two vertices v and w in a digraph is a directed path from v to a common ancestor x,
 * together with a directed path from w to the same ancestor x. A shortest ancestral path is an ancestral path of
 * minimum total length.
 *
 * @author Kevin Crosby
 */
public class SAP {
  private static final int INFINITY = Integer.MAX_VALUE;
  private final Digraph G;
  private final int V;

  /**
   * Constructor takes a digraph (not necessarily a DAG).
   *
   * @param G Digraph.
   */
  public SAP(final Digraph G) {
    this.G = new Digraph(G);
    this.V = G.V();
  }

  private void checkRange(final int vertex) {
    if (vertex < 0 || vertex >= V) {
      throw new IndexOutOfBoundsException();
    }
  }

  private void checkRange(final Iterable<Integer> vertices) {
    for (final int vertex : vertices) {
      checkRange(vertex);
    }
  }

  /**
   * Length of shortest ancestral path between v and w; -1 if no such path.
   *
   * @param v First vertex to compute length from.
   * @param w Second vertex to compute length from.
   * @return Shortest length between vertices.
   */
  public int length(final int v, final int w) {
    Queue<Integer> vs = new Queue<>();
    Queue<Integer> ws = new Queue<>();
    vs.enqueue(v);
    ws.enqueue(w);
    return length(vs, ws);
  }

  /**
   * A common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path.
   *
   * @param v First vertex to compute length from.
   * @param w Second vertex to compute length from.
   * @return Closest common ancestor of vertices.
   */
  public int ancestor(final int v, final int w) {
    Queue<Integer> vs = new Queue<>();
    Queue<Integer> ws = new Queue<>();
    vs.enqueue(v);
    ws.enqueue(w);
    return ancestor(vs, ws);
  }

  /**
   * Length of shortest ancestral path between any vertex in vs and any vertex in ws; -1 if no such path.
   *
   * @param vs First set of vertices to compute length from.
   * @param ws Second set vertices to compute length from.
   * @return Shortest length between sets of vertices.
   */
  public int length(final Iterable<Integer> vs, final Iterable<Integer> ws) {
    if (vs == null || ws == null) { throw new NullPointerException(); }
    checkRange(vs);
    checkRange(ws);

    DirectedBreadthFirstSearch bfsV = new DirectedBreadthFirstSearch(G, vs);
    DirectedBreadthFirstSearch bfsW = new DirectedBreadthFirstSearch(G, ws);

    int minimumLength = INFINITY;
    for (int vertex = 0; vertex < V; vertex++) {
      if (bfsV.hasPathTo(vertex) && bfsW.hasPathTo(vertex)) {
        int length = bfsV.distTo(vertex) + bfsW.distTo(vertex);
        if (minimumLength > length) {
          minimumLength = length;
        }
      }
    }
    if (minimumLength == INFINITY) { return -1; }
    return minimumLength;
  }

  /**
   * A common ancestor that participates in shortest ancestral path; -1 if no such path.
   *
   * @param vs First set of vertices to compute length from.
   * @param ws Second set vertices to compute length from.
   * @return Closest common ancestor of sets of vertices.
   */
  public int ancestor(final Iterable<Integer> vs, final Iterable<Integer> ws) {
    if (vs == null || ws == null) { throw new NullPointerException(); }
    checkRange(vs);
    checkRange(ws);

    DirectedBreadthFirstSearch bfsV = new DirectedBreadthFirstSearch(G, vs);
    DirectedBreadthFirstSearch bfsW = new DirectedBreadthFirstSearch(G, ws);

    int minimumLength = INFINITY, ancestor = -1;
    for (int vertex = 0; vertex < V; vertex++) {
      if (bfsV.hasPathTo(vertex) && bfsW.hasPathTo(vertex)) {
        int length = bfsV.distTo(vertex) + bfsW.distTo(vertex);
        if (minimumLength > length) {
          minimumLength = length;
          ancestor = vertex;
        }
      }
    }
    return ancestor;
  }

  // do unit testing of this class
  public static void main(String[] args) {
    In in = new In(args[0]);
    Digraph G = new Digraph(in);
    SAP sap = new SAP(G);
    while (!StdIn.isEmpty()) {
      int v = StdIn.readInt();
      int w = StdIn.readInt();
      int length = sap.length(v, w);
      int ancestor = sap.ancestor(v, w);
      StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
    }
  }
}
