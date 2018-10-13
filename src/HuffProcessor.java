import java.util.PriorityQueue;

/**
 * Interface that all compression suites must implement. That is they must be
 * able to compress a file and also reverse/decompress that process.
 * 
 * @author Brian Lavallee
 * @since 5 November 2015
 * @author Owen Atrachan
 * @since December 1, 2016
 */
public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); // or 256
	public static final int PSEUDO_EOF = ALPH_SIZE;
	// 0-9, 10: a, b, c, d, 13: e, 15:f
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE = HUFF_NUMBER | 1;
	public static final int HUFF_COUNTS = HUFF_NUMBER | 2;

	public enum Header {
		TREE_HEADER, COUNT_HEADER
	};

	public Header myHeader = Header.TREE_HEADER;

	private int[] readForCounts(BitInputStream in) { //creating array with freqs of chars
		int[] counts = new int[256]; //arrays with space for all values
		int val = 0;
		int charCount = 0;
		while (true) { //will read bits of a char until end of file character
			val = in.readBits(BITS_PER_WORD); 
			charCount++;
			if (val != -1) { //til end of file
				counts[val] += 1; //add 1 for each char of that type
			}
			else { //end of file
				break;
			}
		}
		System.out.println("file length:");
		System.out.println(charCount - 1);
		return counts;
	}
	
	private HuffNode makeTreeFromCounts(int[] counts) {
		PriorityQueue<HuffNode> pq = new PriorityQueue<>(); 
		// call pq.add(new HuffNode(...)) for every 8-bit
		// value that occurs one or more times, including PSEUDO_EOF!!
		// these values/counts are in the array of counts
		for (int j = 0; j < counts.length; j++) { //each character with freq
			if (counts[j] > 0) { //occurs in text
				HuffNode h = new HuffNode(j, counts[j], null, null); //make node with char number and freq
				pq.add(h); //add to pq
			}
			
		HuffNode PSEUDO = new HuffNode(PSEUDO_EOF, 1, null, null); //node for end of text character
		pq.add(PSEUDO);
		}
		
		while (pq.size() > 1) { //pq is not empty
		    HuffNode left = pq.remove(); //remove first smallest
		    HuffNode right = pq.remove(); //remove second smallest
		    HuffNode t = new HuffNode(-1,
		                 left.weight() + right.weight(), //add weights together
		                 left,right); //make originals left and right subtree
		    pq.add(t);
		}
		HuffNode root = pq.remove(); 
		return root;
	}
	
	private String[] makeCodingsFromTree(HuffNode root) { //return String array with paths 
		String[] paths = new String[257];
		recursiveHelper(root, "", paths); //recursive method to fill paths array
		return paths;
	}
	
	private void recursiveHelper(HuffNode root, String s, String[] paths) { //recursive method to find paths
		if (root == null) { //root is null
			return;
		}
		if (root.right() == null && root.left() == null) { //no children means leaf, path is complete
			paths[root.value()] = s; //set array value to current path
			return;
		}
		recursiveHelper(root.left(), s + 0, paths); //add a 0 and go left
		recursiveHelper(root.right(), s + 1, paths); //add a 1 and go right
	}
	
	private void writeHeader(HuffNode root, BitOutputStream out) {
		out.writeBits(BITS_PER_INT,HUFF_TREE); //write in magic number
		writeTree(root, out); //call recursive method to write tree
		
	}
	
	private void writeTree(HuffNode root, BitOutputStream out) { //recursive method to write the tree
		if (root == null) { //base case
			return;
		}
		if (root.value() == -1) { //internal place holder node
			out.writeBits(1,0); //write a 0
		}
		else {
			out.writeBits(1,1); //write a 1 to signal upcoming character 
			out.writeBits(BITS_PER_WORD + 1,root.value()); //write 9 bits for a character
		}
		writeTree(root.left(), out); //keep traversing
		writeTree(root.right(), out); //keep traversing
	}
	
	private void writeCompressedBits(BitInputStream in, String[] paths, BitOutputStream out) { //write file using paths
		int val = 0;
		while (true) {
			val = in.readBits(BITS_PER_WORD); //read each character
			if (val != -1) { //file is not finished
			String encode = paths[val]; //string of path
			out.writeBits(encode.length(), Integer.parseInt(encode,2)); //write path to file in bits
			}
			else { //file is finished
				break;
			}
		}
		out.writeBits(paths[PSEUDO_EOF].length(), Integer.parseInt(paths[PSEUDO_EOF],2)); //write end of file path
	}

	
	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){
		double cStart =  System.nanoTime();
		int[] counts = readForCounts(in); //create freq array
		int counter = 0;
		for (int l = 0; l < counts.length; l++) {
			if (counts[l] != 0) {
				counter += 1;
			}
		}
		//System.out.println("Alph size is:");
		//System.out.println(counter);
		HuffNode root = makeTreeFromCounts(counts); //build tree
		String[] paths = makeCodingsFromTree(root); //create paths array
		writeHeader(root, out); //write magic number and tree in start of file
		in.reset(); //reset file reading
		writeCompressedBits(in, paths, out); //write new compressed file using paths
		double cEnd =  System.nanoTime();
		//System.out.println("time to compress:");
		double cTime =  (cEnd-cStart)/1e9;
		//System.out.println(cTime);
	}

	private HuffNode readTreeHeader(BitInputStream in) { //rebuild tree using the header
		// do a pre-order traversal of the tree (self left right)
		// return it...
		int val = in.readBits(1);
		if (val == 0) { //signals internal node
			HuffNode n = new HuffNode(-1, 0 , readTreeHeader(in), readTreeHeader(in)); //build node with value -1, children are recursively created
			return n;
		}
		if (val == 1) { //signals character node
			int newVal = in.readBits(BITS_PER_WORD + 1); //character is value of node
			HuffNode h = new HuffNode(newVal, 0, null, null); //build leaf node with character as value
			return h; //return because leaf node
		}
		return null;
	}
	
	private void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) { //read compressed file and write to new file
		HuffNode current = root;
		while (true) {
			if (current.value() != -1) { //leaf node storing character
				if (current.value() == PSEUDO_EOF) { //end of file character
					break; //stop
				}
				out.write(current.value()); //otherwise write in character
				current = root; //reset
			} //otherwise it is an internal node
			int val = in.readBits(1);
			if (val == -1) { //no more to read
				throw new HuffException("No EOF");
			}
			if (val == 0) { //go left in tree
				current = current.left();
			}
			else if (val == 1) { //go right in tree
				current = current.right();
			}
		}

	}
	
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){
		double dStart =  System.nanoTime();
		/*
		 * 1. check if file is compressed if file has magic number
		 * 2. read magic number
		 * 3. recreate tree from header
		 */
		int magic = in.readBits(BITS_PER_INT); 
		// not a tree
		//if(magic != HUFF_TREE) {
		if(magic != HUFF_TREE && magic != HUFF_NUMBER) { //if not there, not compressed
		throw new HuffException("No magic number so file is not compressed");
		}
		HuffNode root = readTreeHeader(in); 
		readCompressedBits(root, in, out); //write decompressed file
		double dEnd =  System.nanoTime();
		System.out.println("time to decompress:");
		double dTime =  (dEnd-dStart)/1e9;
		System.out.println(dTime);
		}
		

	public void setHeader(Header header) {
		myHeader = header;
		System.out.println("header set to " + myHeader);
	}
}