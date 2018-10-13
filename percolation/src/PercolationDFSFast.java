/**
 *@author Leah Schwartz
 **/

public class PercolationDFSFast extends PercolationDFS{

	public PercolationDFSFast(int n){
		super(n);
	}
	
	@Override
	protected void updateOnOpen(int row, int col) { //decide if cell is full, if true, check cells surrounding it recursively 
		if ((inBounds(row,col) && (row == 0)) || (inBounds(row - 1, col) && isFull(row - 1,col))
|| (inBounds(row + 1, col) && isFull(row + 1,col))|| (inBounds(row, col-1) && isFull(row,col - 1)) || (inBounds(row, col+1) && isFull(row,col + 1))){
			myGrid[row][col] = FULL; //cell has water in it
			dfs(row - 1, col);
			dfs(row, col - 1);
			dfs(row, col + 1);
			dfs(row + 1, col);
		}
	}
	
	@Override
	public boolean isOpen(int row, int col) { //check if cell already opened
		if (!inBounds(row,col)) {
			throw new IndexOutOfBoundsException("Index " + row + ", " + col + " is out of bounds");
		}
		return super.isOpen(row, col);
	}
	
	@Override
	public void open(int row, int col) { //open cell
		if (!inBounds(row,col)) {
			throw new IndexOutOfBoundsException("Index " + row + ", " + col + " is out of bounds");
		}
		super.open(row, col);
	}
	
	@Override
	public boolean isFull(int row, int col) { //check if cell connected to water source
		if (!inBounds(row,col)) {
			throw new IndexOutOfBoundsException("Index " + row + ", " + col + " is out of bounds");
		}
		return super.isFull(row, col);
	}
	

}