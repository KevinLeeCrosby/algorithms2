import java.awt.Color;

/**
 * Seam-carving is a content-aware image resizing technique where the image is reduced in size by one pixel of height
 * (or width) at a time. A vertical seam in an image is a path of pixels connected from the top to the bottom with one
 * pixel in each row. (A horizontal seam is a path of pixels connected from the left to the right with one pixel in
 * each column.)  Unlike standard content-agnostic resizing techniques (e.g. cropping and scaling), the most
 * interesting features (aspect ratio, set of objects present, etc.) of the image are preserved.
 *
 * @author Kevin Crosby
 */
public class SeamCarver {
  private static final double INFINITY = Double.POSITIVE_INFINITY;
  private Picture picture;
  private int W, H;
  private double[][] energies;
  private boolean padded;

  /**
   * Create a seam carver object based on the given picture.
   *
   * @param picture Picture to process.
   */
  public SeamCarver(final Picture picture) {
    this.picture = new Picture(picture);
    W = picture.width();
    H = picture.height();
    energies = new double[W][H];
    for (int w = 0; w < W; w++) {
      for (int h = 0; h < H; h++) {
        energies[w][h] = computeEnergy(w, h);
      }
    }
    padded = false;
  }

  /**
   * Current picture.
   *
   * @return Modified picture.
   */
  public Picture picture() {
    if (padded) {
      Picture p = new Picture(W, H);
      double[][] e = new double[W][H];
      for (int w = 0; w < W; w++) {
        System.arraycopy(energies[w], 0, e[w], 0, H);
        for (int h = 0; h < H; h++) {
          p.set(w, h, picture.get(w, h));
        }
      }
      picture = p;
      energies = e;
      padded = false;
    }
    return picture;
  }

  /**
   * Width of current picture.
   *
   * @return Width of modified picture.
   */
  public int width() {
    return W;
  }

  /**
   * Height of current picture.
   *
   * @return Height of modified picture.
   */
  public int height() {
    return H;
  }

  private int[] components(final Color color) {
    return new int[]{color.getRed(), color.getGreen(), color.getBlue()};
  }

  private double squareGradient(final Color v1, final Color v2) {
    int[] rgb1 = components(v1), rgb2 = components(v2);
    double d2 = 0.0;
    for (int i = 0; i < rgb1.length; i++) {
      d2 += Math.pow(rgb1[i] - rgb2[i], 2.0);
    }
    return d2;
  }

  private double computeEnergy(final int x, final int y) {
    if (x < 0 || x >= W || y < 0 || y >= H) { throw new IndexOutOfBoundsException(); }

    double energy;
    if (x == 0 || x == W - 1 || y == 0 || y == H - 1) { // check border pixels
      energy = 195075.0; // 3 * 255^2 = 195075
    } else {
      double dx2 = squareGradient(picture.get(x - 1, y), picture.get(x + 1, y));
      double dy2 = squareGradient(picture.get(x, y - 1), picture.get(x, y + 1));
      energy = dx2 + dy2;
    }
    energies[x][y] = energy;
    return energy;
  }

  /**
   * Energy of pixel at column x and row y.
   *
   * @param x Column of pixel.
   * @param y Row of pixel.
   * @return Energy of pixel.
   */
  public double energy(final int x, final int y) {
    if (x < 0 || x >= W || y < 0 || y >= H) { throw new IndexOutOfBoundsException(); }

    return energies[x][y];
  }

  /**
   * Sequence of indices for horizontal seam.
   *
   * @return Integer array of horizontal seam.
   */
  public int[] findHorizontalSeam() {
    ShortestPath shortest = new ShortestPath(false);
    return shortest.path();
  }

  /**
   * Sequence of indices for vertical seam.
   *
   * @return Integer array of vertical seam.
   */
  public int[] findVerticalSeam() {
    ShortestPath shortest = new ShortestPath(true);
    return shortest.path();
  }

  private void checkSeam(final int[] seam, final int dimension) {
    if (seam == null) { throw new NullPointerException(); }
    int previous = seam[0];
    for (int element : seam) {
      if (element < 0 || element >= dimension) { throw new IllegalArgumentException(); }
      int difference = element - previous;
      if (difference < -1 || difference > 1) { throw new IllegalArgumentException(); }
      previous = element;
    }
  }

  /**
   * Remove horizontal seam from current picture.
   *
   * @param seam Integer array of horizontal seam.
   */
  public void removeHorizontalSeam(final int[] seam) {
    if (W <= 1) { throw new IllegalArgumentException(); }
    checkSeam(seam, H);

    padded = true;
    for (int w = 0; w < W; w++) {
      int s = seam[w];
      for (int h = s + 1, r = s; h < H; h++, r++) {
        picture.set(w, r, picture.get(w, h));
        energies[w][r] = energies[w][h];
      }
      energies[w][H - 1] = INFINITY;
    }
    H--;
    for (int w = 0; w < W; w++) {
      int s = seam[w], lo = Math.max(s - 1, 0), hi = Math.min(s, H - 1);
      for (int r = lo; r <= hi; r++) {
        energies[w][r] = computeEnergy(w, r);
      }
    }
  }

  /**
   * Remove vertical seam from current picture.
   *
   * @param seam Integer array of vertical seam.
   */
  public void removeVerticalSeam(final int[] seam) {
    if (H <= 1) { throw new IllegalArgumentException(); }
    checkSeam(seam, W);

    padded = true;
    for (int h = 0; h < H; h++) {
      int s = seam[h];
      for (int w = s + 1, c = s; w < W; w++, c++) {
        picture.set(c, h, picture.get(w, h));
        energies[c][h] = energies[w][h];
      }
      energies[W - 1][h] = INFINITY;
    }
    W--;
    for (int h = 0; h < H; h++) {
      int s = seam[h], lo = Math.max(s - 1, 0), hi = Math.min(s, W - 1);
      for (int c = lo; c <= hi; c++) {
        energies[c][h] = computeEnergy(c, h);
      }
    }
  }

  private class ShortestPath {
    private final int A, B;
    private boolean isVertical;
    private int[][] edgeTo;
    private double[][] distTo;

    private ShortestPath(final boolean isVertical) {
      this.isVertical = isVertical;
      if (isVertical) {
        A = H;
        B = W;
      } else {
        A = W;
        B = H;
      }
      edgeTo = new int[A][B];
      distTo = new double[A][B];

      for (int a = 1; a < A; a++) {
        for (int b = 0; b < B; b++) {
          distTo[a][b] = INFINITY;
        }
      }
      for (int b = 0; b < B; b++) {
        distTo[0][b] = weight(0, b);
      }

      for (int a = 0; a < A - 1; a++) {
        for (int b = 0; b < B; b++) {
          for (int q : adjacent(a, b)) {
            relax(a, b, q);
          }
        }
      }
    }

    private Iterable<Integer> adjacent(final int a, final int b) {
      Queue<Integer> queue = new Queue<>();
      queue.enqueue(b);
      if (b + 1 < B) {
        queue.enqueue(b + 1);
      }
      if (b - 1 >= 0) {
        queue.enqueue(b - 1);
      }
      return queue;
    }

    private double weight(final int a, final int b) {
      if (isVertical) {
        return energies[b][a];
      } else {
        return energies[a][b];
      }
    }

    private void relax(final int a, final int b, final int q) {
      double distance = distTo[a][b] + weight(a, b);
      if (distTo[a + 1][q] > distance) {
        distTo[a + 1][q] = distance;
        edgeTo[a + 1][q] = b;
      }
    }

    private int[] path() {
      int[] path = new int[A];
      double minDistance = INFINITY;
      int s = -1;

      for (int b = 0; b < B; b++) {
        double distance = distTo[A - 1][b];
        if (minDistance > distance) {
          minDistance = distance;
          s = b;
        }
      }
      path[A - 1] = s;
      for (int a = A - 1; a > 0; a--) {
        s = edgeTo[a][s];
        path[a - 1] = s;
      }
      return path;
    }
  }
}
