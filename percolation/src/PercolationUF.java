import java.util.Arrays;

/**
 * Simulate a system to see its Percolation Threshold, but use a UnionFind
 * implementation to determine whether simulation occurs. The main idea is that
 * initially all cells of a simulated grid are each part of their own set so
 * that there will be n^2 sets in an nxn simulated grid. Finding an open cell
 * will connect the cell being marked to its neighbors --- this means that the
 * set in which the open cell is 'found' will be unioned with the sets of each
 * neighboring cell. The union/find implementation supports the 'find' and
 * 'union' typical of UF algorithms.
 * <P>
 * 
 * @author Owen Astrachan
 * @author Jeff Forbes
 * @author Leah Schwartz
 *
 */

public class PercolationUF implements IPercolate {
	private final int OUT_BOUNDS = -1;
	private boolean[][] myGrid; //arrays of arrays initialized to false
	private IUnionFind myFinder; //IUnionFind object
	private int openSites; //open cells
	private final int VTOP; //top of model
	private final int VBOTTOM; //bottom of model
	private int mySize; //num of rows and cols
	

	/**
	 * Constructs a Percolation object for a nxn grid that that creates
	 * a IUnionFind object to determine whether cells are full
	 */
	public PercolationUF(int n, IUnionFind finder) { //constructs PercolationUF object with size rows and size columns 
		int size = n;
		if (size <= 0) {
			throw new IndexOutOfBoundsException("size " + size + " is out of bounds"); 
		}
		myGrid = new boolean[size][size]; //size by size grid
		myFinder = finder;
		myFinder.initialize(size * size + 2); //N*N + 1
		VBOTTOM = size * size; //index won't be used by other cell
		VTOP = size * size + 1; //index won't be used by other cell
		mySize = size;
		openSites = 0;
		for (boolean[] row : myGrid) { //cells all false to start with
			Arrays.fill(row, false);
		}
	}

	/**
	 * Return an index that uniquely identifies (row,col), typically an index
	 * based on row-major ordering of cells in a two-dimensional grid. However,
	 * if (row,col) is out-of-bounds, return OUT_BOUNDS.
	 */
	private int getIndex(int row, int col) { //returns index number given cell row and col
				if (!inBounds(row, col)) {
					return OUT_BOUNDS;
				}
				int myID = mySize * row + col; //index is size of row * row + column number
				return myID;
			}

	public void open(int row, int col) { //sets unopened cell to open (true), adds one to openSites, updates 
		if (!inBounds(row, col)) {
			throw new IndexOutOfBoundsException("Index " + row + ", " + col + " is out of bounds"); 
		}
		
		if (myGrid[row][col] != false){
			return;
		}
		openSites += 1;
		myGrid[row][col] = true; //opened
		updateOnOpen(row, col);
	}
	
	private void updateOnOpen(int row, int col){ //When cell is opened, checks & unions it with top, bottom, or neighbor cell if in bounds
		if (!inBounds(row, col)) {
			throw new IndexOutOfBoundsException("Index " + row + ", " + col + " is out of bounds"); 
		}
		int thisIndex = getIndex(row,col);
		if (row == 0) {
			myFinder.union(thisIndex, VTOP);
		}
		if (row == mySize - 1) {
			myFinder.union(thisIndex, VBOTTOM);
		}
		if (inBounds(row - 1, col) && isOpen(row - 1, col)){
			myFinder.union(thisIndex, getIndex(row - 1, col));
		}
		if (inBounds(row + 1, col) && isOpen(row + 1, col)) {
			myFinder.union(thisIndex, getIndex(row + 1, col));
		}
		if (inBounds(row, col - 1) && isOpen(row, col - 1)) {
			myFinder.union(thisIndex, getIndex(row, col - 1));
		}
		if (inBounds(row, col + 1) && isOpen(row, col + 1)) {
			myFinder.union(thisIndex, getIndex(row, col + 1));
		}
	}
	
	public boolean isOpen(int i, int j) { //checks if cell is open, meaning value would be true
				if (!inBounds(i,j)) {
					throw new IndexOutOfBoundsException("Index " + i + ", " + j + " is out of bounds"); 
				}
				return myGrid[i][j] != false;
			}
			
	public boolean isFull(int i, int j) { //checks if cell is in same set as VTOP
		if (!inBounds(i, j)) {
			throw new IndexOutOfBoundsException("Index " + i + ", " + j + " is out of bounds"); 
		}
		 return myFinder.connected(getIndex(i,j), VTOP);	
	}
	
	
	public int numberOfOpenSites() { //number of opened sites
		return openSites;
	}

	public boolean percolates() { //returns true only if top connects to bottom
				if (myFinder.connected(VBOTTOM, VTOP)) {
					return true;
				}
				return false;
			}


	private boolean inBounds(int row, int col) { //checks bounds for a cell
		if (row < 0 || row >= myGrid.length) return false;
		if (col < 0 || col >= myGrid[0].length) return false;
		return true;
	}
}

