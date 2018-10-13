import java.util.*;

/**
 * Compute statistics on Percolation after performing T independent experiments on an N-by-N grid.
 * Compute 95% confidence interval for the percolation threshold, and  mean and std. deviation
 * Compute and print timings
 * 
 * @author Kevin Wayne
 * @author Jeff Forbes
 * @author Josh Hug
 * @author Leah Schwartz
 */

public class PercolationStats {
	public static int RANDOM_SEED = 1234;
	public static Random ourRandom = new Random(RANDOM_SEED);
	private IUnionFind finder; //new finder object
	private double[] runs; //array to keep data from each run in
	private int num; //number of runs
	
	public PercolationStats(int N, int T){ //exception if N or T not in bounds
		if (N <= 0 || T <= 0) {
			throw new IllegalArgumentException("out of bounds");
		}
		runs = new double[T];
		for(int t = 0; t < T; t++) {
			//finder = new QuickFind();
			finder = new QuickUWPC();
			finder.initialize(N * N + 2);
			ArrayList<int[]> happyNums = new ArrayList<int[]>();
			for(int j = 0; j < N; j++) {
				for(int i = 0; i < N; i++) {
				happyNums.add(new int[]{j,i}); //add each cell with row and column
				}
			}
			Collections.shuffle(happyNums, ourRandom); //shuffle for random order
			IPercolate wetWater = new PercolationUF(N, finder);
			//IPercolate wetWater = new PercolationDFSFast(N);
			//IPercolate wetWater = new PercolationDFS(N);
			int counter = 0;
			while(!wetWater.percolates()){ //continue until system percolates
				int[] cell = happyNums.get(counter);
			    	if(!wetWater.isOpen(cell[0], cell[1])) {
			    	   	wetWater.open(cell[0], cell[1]); //open if not open already
			       }
			      counter++;	
				}
			runs[t] = (double)wetWater.numberOfOpenSites()/(N*N);	 //p* gotten by dividing opened cells by total cells
			num = T;
		}
	}
	
	public double mean() { //find mean from all runs
		return StdStats.mean(runs);
	}
	
	public double stddev(){ //find standard deviation from all runs
		return StdStats.stddev(runs);
	}
	
	public double confidenceLow() { //find confidence low interval from all runs
		return StdStats.mean(runs) - 1.96 * (StdStats.stddev(runs))/Math.sqrt(num);
	}
	
	public double confidenceHigh() { //find confidence low interval from all runs
		return StdStats.mean(runs) + 1.96 * (StdStats.stddev(runs))/Math.sqrt(num);
	}
	
	public static void main(String[] args) {
		double start =  System.nanoTime();
		PercolationStats ps = new PercolationStats(100,100);
		double end =  System.nanoTime();
		double time =  (end-start)/1e9;
		System.out.printf("mean: %1.4f, time: %1.4f\n",ps.mean(),time);
		System.out.printf("std: %1.4f, time: %1.4f\n",ps.stddev(),time);
		System.out.printf("conf: %1.4f, time: %1.4f\n",ps.confidenceHigh(),time);

	}
}
