package galaxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static galaxy.Place.pl;


/** The state of a Galaxies Puzzle.  Each cell, cell edge, and intersection of
 *  edges has coordinates (x, y). For cells, x and y are positive and odd.
 *  For intersections, x and y are even.  For horizontal edges, x is odd and
 *  y is even.  For vertical edges, x is even and y is odd.  On a board
 *  with w columns and h rows of cells, (0, 0) indicates the bottom left
 *  corner of the board, and (2w, 2h) indicates the upper right corner.
 *  If (x, y) are the coordinates of a cell, then (x-1, y) is its left edge,
 *  (x+1, y) its right edge, (x, y-1) its bottom edge, and (x, y+1) its
 *  top edge.  The four cells (x, y), (x+2, y), (x, y+2), and (x+2, y+2)
 *  meet at intersection (x+1, y+1).  Cells contain nonnegative integer
 *  values, or "marks". A cell containing 0 is said to be unmarked.
 *  @author Abhinav Anand
 */

class Model {

    /** The default number of squares on a side of the board. */
    static final int DEFAULT_SIZE = 7;
    /** The grid itself. */
    private int[][] board;
    /** Num cols. */
    private int _cols;
    /** Num rows. */
    private int _rows;
    /** 2D array holding boundaries. */
    private boolean[][] boundaries;
    /** 2D array holding centers. */
    private boolean[][] centers;
    /** 2D array holding marks. */
    private int[][] marks;
    /** List of centers. */
    private List<Place> centerslist;



    /** Initializes an empty puzzle board of size DEFAULT_SIZE x DEFAULT_SIZE,
     *  with a boundary around the periphery.
     *  This happens when there are no arguments passed into Model().*/
    Model() {
        init(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /** Initializes an empty puzzle board of size COLS x ROWS, with a boundary
     *  around the periphery. */
    Model(int cols, int rows) {
        init(cols, rows);
    }

    /** Initializes a copy of MODEL. */
    Model(Model model) {
        copy(model);
    }

    /** Copies MODEL into new instance variable. */
    void copy(Model model) {
        this._cols = model._cols;
        this._rows = model._rows;
        this.board = new int[2 * this._cols + 1 ][2 * this._rows + 1];
        this.centerslist = new ArrayList<>();
        for (Place x : model.centerslist) {
            this.centerslist.add(x);
        }
        this.boundaries = new boolean[2 * this._cols + 1][2 * this._rows + 1];
        for (int i = 0; i < 2 * _cols + 1; i++) {
            for (int j = 0; j < 2 * _cols + 1; j++) {
                if (model.boundaries[i][j]) {
                    this.boundaries[i][j] = true;
                }
            }
        }
        this.centers = new boolean[2 * this._cols + 1][2 * this._rows + 1];
        for (int i = 0; i < 2 * _cols + 1; i++) {
            for (int j = 0; j < 2 * _cols + 1; j++) {
                if (model.centers[i][j]) {
                    this.centers[i][j] = true;
                }
            }
        }
        this.marks = new int[2 * this._cols + 1][2 * this._rows + 1];
        for (int i = 0; i < 2 * _cols + 1; i++) {
            for (int j = 0; j < 2 * _cols + 1; j++) {
                if (model.marks[i][j] != 0) {
                    this.marks[i][j] = model.marks[i][j];
                }
            }
        }
    }

    /** Sets the puzzle board size to COLS x ROWS, and clears it. */
    void init(int cols, int rows) {
        _cols = cols;
        _rows = rows;
        board = new int[xlim()][ylim()];
        boundaries = new boolean[xlim()][ylim()];
        centers = new boolean[xlim()][ylim()];
        marks = new int[xlim()][ylim()];
        centerslist = new ArrayList<>();
        for (int i = 1; i < xlim(); i += 2) {
            boundaries[i][0] = true;
            boundaries[i][ylim() - 1] = true;
        }
        for (int i = 1; i < ylim(); i += 2) {
            boundaries[0][i] = true;
            boundaries[xlim() - 1][i] = true;
        }
    }


    /** Clears the board (removes centers, boundaries that are not on the
     *  periphery, and marked cells) without resizing. */
    void clear() {
        init(cols(), rows());
    }

    /** Returns the number of columns of cells in the board. */
    int cols() {
        return xlim() / 2;
    }

    /** Returns the number of rows of cells in the board. */
    int rows() {
        return ylim() / 2;
    }

    /** Returns the number of vertical edges and cells in a row. */
    int xlim() {
        return _cols * 2 + 1;
    }

    /** Returns the number of horizontal edges and cells in a column. */
    int ylim() {
        return _rows * 2 + 1;
    }

    /** Returns true iff (X, Y) is a valid cell. */
    boolean isCell(int x, int y) {
        return 0 <= x && x < xlim() && 0 <= y && y < ylim()
                && x % 2 == 1 && y % 2 == 1;
    }

    /** Returns true iff P is a valid cell. */
    boolean isCell(Place p) {
        return isCell(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a valid edge. */
    boolean isEdge(int x, int y) {
        return 0 <= x && x < xlim() && 0 <= y && y < ylim() && x % 2 != y % 2;
    }

    /** Returns true iff P is a valid edge. */
    boolean isEdge(Place p) {
        return isEdge(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a vertical edge. */
    boolean isVert(int x, int y) {
        return isEdge(x, y) && x % 2 == 0;
    }

    /** Returns true iff P is a vertical edge. */
    boolean isVert(Place p) {
        return isVert(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a horizontal edge. */
    boolean isHoriz(int x, int y) {
        return isEdge(x, y) && y % 2 == 0;
    }

    /** Returns true iff P is a horizontal edge. */
    boolean isHoriz(Place p) {
        return isHoriz(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a valid intersection. */
    boolean isIntersection(int x, int y) {
        return x % 2 == 0 && y % 2 == 0
                && x >= 0 && y >= 0 && x < xlim() && y < ylim();
    }

    /** Returns true iff P is a valid intersection. */
    boolean isIntersection(Place p) {
        return isIntersection(p.x, p.y);
    }

    /** Returns true iff (X, Y) is a center. */
    boolean isCenter(int x, int y) {
        return isCenter(pl(x, y));
    }

    /** Returns true iff P is a center. */
    boolean isCenter(Place p) {
        if (centers[p.x][p.y]) {
            return true;
        }
        return false;
    }

    /** Returns true iff (X, Y) is a boundary. */
    boolean isBoundary(int x, int y) {
        /** Returns true iff (X, Y) on perimeter **/
        if (boundaries[x][y]) {
            return true;
        } else if (!isEdge(x, y)) {
            return false;
        }
        return false;
    }

    /** Returns true iff P is a boundary. */
    boolean isBoundary(Place p) {
        return isBoundary(p.x, p.y);
    }

    /** Returns true iff the puzzle board is solved, given the centers and
     *  boundaries that are currently on the board. */
    boolean solved() {
        int total;
        total = 0;
        for (Place c : centers()) {
            HashSet<Place> r = findGalaxy(c);
            if (r == null) {
                return false;
            } else {
                total += r.size();
            }
        }
        return total == rows() * cols();
    }

    /** Finds cells reachable from CELL and adds them to REGION.  Specifically,
     *  it finds cells that are reachable using only vertical and horizontal
     *  moves starting from CELL that do not cross any boundaries and
     *  do not touch any cells that were initially in REGION. Requires
     *  that CELL is a valid cell. */
    private void accreteRegion(Place cell, HashSet<Place> region) {
        assert isCell(cell);
        if (region.contains(cell)) {
            return;
        }
        region.add(cell);
        for (int i = 0; i < 4; i += 1) {
            int dx = (i % 2) * (2 * (i / 2) - 1),
                    dy = ((i + 1) % 2) * (2 * (i / 2) - 1);
            if (!isBoundary(cell.move(dx, dy))) {
                accreteRegion(cell.move(2 * dx, 2 * dy), region);
            }
        }
    }

    /** Returns true iff REGION is a correctly formed galaxy. A correctly formed
     *  galaxy has the following characteristics:
     *      - is symmetric about CENTER,
     *      - contains no interior boundaries, and
     *      - contains no other centers.
     * Assumes that REGION is connected. */
    private boolean isGalaxy(Place center, HashSet<Place> region) {
        for (Place cell : region) {
            if (!region.contains(opposing(center, cell))) {
                return false;
            }
            if (centers[cell.x][cell.y] && center != cell) {
                return false;
            }

            for (int i = 0; i < 4; i += 1) {
                int dx = (i % 2) * (2 * (i / 2) - 1),
                        dy = ((i + 1) % 2) * (2 * (i / 2) - 1);
                Place boundary = cell.move(dx, dy),
                        nextCell = cell.move(2 * dx, 2 * dy);

                if (isBoundary(boundary) && region.contains(nextCell)) {
                    return false;
                }
            }
            for (int i = 0; i < 4; i += 1) {
                int dx = 2 * (i / 2) - 1, dy = 2 * (i % 2) - 1;
                Place intersection = cell.move(dx, dy);
                if (center != cell && isCenter(cell)
                        || intersection != center
                        && isCenter(intersection)) {
                    return false;
                }
            }
        }
        return true;
    }


    /** Returns the galaxy containing CENTER that has the following
     *  characteristics:
     *      - encloses CENTER completely,
     *      - is symmetric about CENTER,
     *      - is connected, --> maxUnmarkedRegion
     *      - contains no stray boundary edges, and --> isGalaxy
     *      - contains no other centers aside from CENTER. --> isGalaxy
     *  Otherwise, returns null. Requires that CENTER is not on the
     *  periphery. */
    HashSet<Place> findGalaxy(Place center) {
        HashSet<Place> galaxy = new HashSet<>();
        if (isCell(center)) {
            accreteRegion(center, galaxy);
        } else if (isHoriz(center)) {
            accreteRegion(center.move(0, 1), galaxy);
        } else if (isVert(center)) {
            accreteRegion(center.move(1, 0), galaxy);
        } else if (isIntersection(center)) {
            accreteRegion(center.move(1, 1), galaxy);
        }

        if (isGalaxy(center, galaxy)) {
            return galaxy;
        } else {
            return null;
        }
    }

    /** Returns the largest, unmarked region around CENTER with the
     *  following characteristics:
     *      - contains all cells touching CENTER,
     *      - consists only of unmarked cells,
     *      - is symmetric about CENTER, and
     *      - is contiguous.
     *  The method ignores boundaries and other centers on the current board.
     *  If there is no such region, returns the empty set. */
    Set<Place> maxUnmarkedRegion(Place center) {
        HashSet<Place> region = new HashSet<>();
        region.addAll(unmarkedContaining(center));

        int total;
        int change = 100;

        while (change > 0) {
            total = region.size();
            region.addAll(unmarkedSymAdjacent(center, new ArrayList<>(region)));
            change = region.size() - total;
            markAll(region, 1);
        }
        markAll(region, 0);
        return region;
    }

    /** Marks all properly formed galaxies with value V. Unmarks all cells that
     *  are not contained in any of these galaxies. Requires that V is greater
     *  than or equal to 0. */
    void markGalaxies(int v) {
        assert v >= 0;
        markAll(0);
        for (Place c : centers()) {
            HashSet<Place> region = findGalaxy(c);
            if (region != null) {
                markAll(region, v);
            }
        }
    }

    /** Toggles the presence of a boundary at the edge (X, Y). That is, negates
     *  the value of isBoundary(X, Y) (from true to false or vice-versa).
     *  Requires that (X, Y) is an edge. */
    void toggleBoundary(int x, int y) {
        if (x < xlim() && y < ylim() && (isEdge(x, y))) {
            if (boundaries[x][y]) {
                boundaries[x][y] = false;
            } else {
                boundaries[x][y] = true;
            }
        }
    }

    /** Places a center at (X, Y). Requires that X and Y are within bounds of
     *  the board. */
    void placeCenter(int x, int y) {
        placeCenter(pl(x, y));
    }

    /** Places center at P. */
    void placeCenter(Place p) {
        centers[p.x][p.y] = true;
        centerslist.add(p);

    }

    /** Returns the current mark on cell (X, Y), or -1 if (X, Y) is not a valid
     *  cell address. */
    int mark(int x, int y) {
        if (!isCell(x, y) || x < 0 || y < 0 || x > xlim() || y > ylim()) {
            return -1;
        }
        return marks[x][y];
    }

    /** Returns the current mark on cell P, or -1 if P is not a valid cell
     *  address. */
    int mark(Place p) {
        return mark(p.x, p.y);
    }

    /** Marks the cell at (X, Y) with value V. Requires that V must be greater
     *  than or equal to 0, and that (X, Y) is a valid cell address. */
    void mark(int x, int y, int v) {
        if (!isCell(x, y)) {
            throw new IllegalArgumentException("bad cell coordinates");
        }
        if (v < 0) {
            throw new IllegalArgumentException("bad mark value");
        }
        marks[x][y] = v;
    }

    /** Marks the cell at P with value V. Requires that V must be greater
     *  than or equal to 0, and that P is a valid cell address. */
    void mark(Place p, int v) {
        mark(p.x, p.y, v);
    }

    /** Sets the marks of all cells in CELLS to V. Requires that V must be
     *  greater than or equal to 0. */
    void markAll(Collection<Place> cells, int v) {
        assert v >= 0;
        for (Place cell:  cells)  {
            mark(cell, v);
        }
    }

    /** Sets the marks of all cells to V. Requires that V must be greater than
     *  or equal to 0. */
    void markAll(int v) {
        assert v >= 0;
        for (int i = 0; i < xlim(); i++) {
            for (int j = 0; j < ylim(); j++) {
                if (isCell(i, j)) {
                    marks[i][j] = v;
                }
            }
        }
    }

    /** Returns the position of the cell that is opposite P using P0 as the
     *  center, or null if that is not a valid cell address. */
    Place opposing(Place p0, Place p) {
        int dx = p0.x - p.x;
        int dy = p0.y - p.y;
        int newx = p0.x + dx;
        int newy = p0.y + dy;
        if ((p0.x < -1
                || p0.y < -1
                || p.x < -1
                || p.y < -1)
                || newx < 0
                || newy < 0) {
            return null;
        }
        if (!isCell(pl(newx, newy))) {
            return null;
        }
        return pl(newx, newy);
    }

    /** Returns a list of all cells "containing" PLACE if all of the cells are
     *  unmarked. A cell, c, "contains" PLACE if
     *      - c is PLACE itself,
     *      - PLACE is a corner of c, or
     *      - PLACE is an edge of c.
     *  Otherwise, returns an empty list. */
    List<Place> unmarkedContaining(Place place) {
        if (isCell(place)) {
            if (marks[place.x][place.y] == 0) {
                return asList(place);
            }
        } else if (isVert(place)) {
            if (place.x >= 0 && place.x < xlim()) {
                List<Place> unmarked =  asList(place.move(-1, 0),
                        place.move(1, 0));
                for (Place cell: unmarked) {
                    if (marks[cell.x][cell.y] != 0) {
                        return Collections.emptyList();
                    }
                }
                return unmarked;
            }
        } else if (isHoriz(place)) {
            if (place.y >= 0 && place.y < ylim()) {
                List<Place> unmarked = asList(place.move(0, -1),
                        place.move(0, 1));
                for (Place cell: unmarked) {
                    if (marks[cell.x][cell.y] != 0) {
                        return Collections.emptyList();
                    }
                }
                return unmarked;
            }
        } else {
            if (isIntersection(place) && place.x >= 0 && place.y >= 0) {
                List<Place> unmarked =  asList(place.move(1, -1),
                        place.move(1, 1),
                    place.move(-1, 1), place.move(-1, -1));
                for (Place cell : unmarked) {
                    if (marks[cell.x][cell.y] != 0) {
                        return Collections.emptyList();
                    }
                }
                return unmarked;
            }
        }
        return Collections.emptyList();
    }

    /** Returns a list of all cells, c, such that:
     *      - c is unmarked,
     *      - The opposite cell from c relative to CENTER exists and
     *        is unmarked, and
     *      - c is vertically or horizontally adjacent to a cell in REGION.
     *  CENTER and all cells in REGION must be valid cell positions.
     *  Each cell appears at most once in the resulting list. */
    List<Place> unmarkedSymAdjacent(Place center, List<Place> region) {
        ArrayList<Place> result = new ArrayList<>();
        for (Place r : region) {
            assert isCell(r);
            for (int i = 0; i < 4; i += 1) {
                int dx = (i % 2) * (2 * (i / 2) - 1),
                        dy = ((i + 1) % 2) * (2 * (i / 2) - 1);
                Place p = r.move(2 * dx, 2 * dy);
                Place opp = opposing(center, p);
                if (p.x >= 0 && p.y >= 0 && p.x < xlim() && p.y < ylim()) {
                    if (opp != null && marks[p.x][p.y] == 0
                            && marks[opp.x][opp.y] == 0) {
                        result.add(p);
                    }
                }
            }
        }
        return result;
    }

    /** Returns an unmodifiable view of the list of all centers. */
    List<Place> centers() {
        return Collections.unmodifiableList(centerslist);
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        int w = xlim(), h = ylim();
        for (int y = h - 1; y >= 0; y -= 1) {
            for (int x = 0; x < w; x += 1) {
                boolean cent = isCenter(x, y);
                boolean bound = isBoundary(x, y);
                if (isIntersection(x, y)) {
                    out.format(cent ? "o" : " ");
                } else if (isCell(x, y)) {
                    if (cent) {
                        out.format(mark(x, y) > 0 ? "O" : "o");
                    } else {
                        out.format(mark(x, y) > 0 ? "*" : " ");
                    }
                } else if (y % 2 == 0) {
                    if (cent) {
                        out.format(bound ? "O" : "o");
                    } else {
                        out.format(bound ? "=" : "-");
                    }
                } else if (cent) {
                    out.format(bound ? "O" : "o");
                } else {
                    out.format(bound ? "I" : "|");
                }
            }
            out.format("%n");
        }
        return out.toString();
    }

}
