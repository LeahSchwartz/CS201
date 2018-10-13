import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * General trie/priority queue algorithm for implementing Autocompletor
 * 
 * @author Austin Lu
 * @author Jeff Forbes
 */
public class TrieAutocomplete implements Autocompletor {

	/**
	 * Root of entire trie
	 */
	protected Node myRoot;

	/**
	 * Constructor method for TrieAutocomplete. Should initialize the trie rooted at
	 * myRoot, as well as add all nodes necessary to represent the words in terms.
	 * 
	 * @param terms
	 *            - The words we will autocomplete from
	 * @param weights
	 *            - Their weights, such that terms[i] has weight weights[i].
	 * @throws NullPointerException
	 *             if either argument is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different length
	 */
	public TrieAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null"); //exception if args are null
		}
		if (terms.length != weights.length) {
			throw new IllegalArgumentException("terms and weights are not the same lengh"); //exception if terms and lengths are different lengths
		}
		Set<String> wordsDups = new HashSet<String>(Arrays.asList(terms));
		if (wordsDups.size() != terms.length) {
			throw new IllegalArgumentException("terms contains duplicates"); //exception if duplicate terms
		}
		// Represent the root as a dummy/placeholder node
		myRoot = new Node('-', null, 0);

		for (int i = 0; i < terms.length; i++) {
			add(terms[i], weights[i]);
		}
	}

	/**
	 * Add the word with given weight to the trie. If word already exists in the
	 * trie, no new nodes should be created, but the weight of word should be
	 * updated.
	 * 
	 * In adding a word, this method should do the following: Create any necessary
	 * intermediate nodes if they do not exist. Update the subtreeMaxWeight of all
	 * nodes in the path from root to the node representing word. Set the value of
	 * myWord, myWeight, isWord, and mySubtreeMaxWeight of the node corresponding to
	 * the added word to the correct values
	 * 
	 * @throws a
	 *             NullPointerException if word is null
	 * @throws an
	 *             IllegalArgumentException if weight is negative.
	 */
	private void add(String word, double weight) {
		if (word == null) {
			throw new NullPointerException("one of these arguments is null"); //exception if an arg is null
		}
		if (weight < 0) {
			throw new IllegalArgumentException("weight is negative"); //exception if weight is negative 
		}
		
		Node current = myRoot; 
		for (int k = 0; k < word.length(); k++) { //loop through chars in word
			char ch = word.charAt(k);
			if (current.children.get(ch) == null) { //char is not there
				current.children.put(ch, new Node(ch, current, weight)); //add node for char
			}
			current.mySubtreeMaxWeight = Math.max(weight, current.mySubtreeMaxWeight); //set mySubtreeMaxWeight to max or new weight (biggest)
			current = current.children.get(ch); //next node
			if (k + 1 == word.length()) { //last letter in word
				current.isWord = true; //set fields
				current.myWeight = weight;
				current.myWord = word;
			}
		}
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the k
	 * words in the trie with the largest weight which match the given prefix, in
	 * descending weight order. If less than k words exist matching the given prefix
	 * (including if no words exist), then the array instead contains all those
	 * words. e.g. If terms is {air:3, bat:2, bell:4, boy:1}, then topKMatches("b",
	 * 2) should return {"bell", "bat"}, but topKMatches("a", 2) should return
	 * {"air"}
	 * 
	 * @param prefix
	 *            - A prefix which all returned words must start with
	 * @param k
	 *            - The (maximum) number of words to be returned
	 * @return An Iterable of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k such
	 *         words exist, return all those words. If no such words exist, return
	 *         an empty Iterable
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		if (prefix == null) {
			throw new NullPointerException("No prefix given"); //exception if no prefix given
		}
		if (k <= 0) {
			return new LinkedList<String>();
		}
		Node current = myRoot; //start with root
		LinkedList<String> ret = new LinkedList<String>(); //new list
		
		for (int i = 0; i < prefix.length(); i++) { //loop through characters in prefix
			char letter = prefix.charAt(i);
			if (!current.children.containsKey(letter)){ //if character not in map return empty list
				return ret;
			}
			current = current.children.get(letter); //node is last letter of prefix
		}
		
		PriorityQueue<Node> nodePQ = new PriorityQueue<Node>(new Node.ReverseSubtreeMaxWeightComparator()); //queue for all terms
		PriorityQueue<Term> tPQ = new PriorityQueue<Term>(k, new Term.WeightOrder()); //selective queue
		nodePQ.add(current);
		while (nodePQ.size() > 0) {
			if (tPQ.size() >= k && tPQ.peek().getWeight() > nodePQ.peek().mySubtreeMaxWeight) { //no bigger weights to be had and queue is full
				break;
			}
			Node top = nodePQ.peek();
			nodePQ.remove(); //take out next item
			if (top.isWord) { //must be word
				tPQ.add(new Term(top.getWord(), top.getWeight())); //add to selective queue
			}
			if (tPQ.size() > k){ //too big
				tPQ.remove();	//removes smallest
			}

			for(Node n : top.children.values()) { //explore children
				nodePQ.add(n);
			}
		}
		int num = Math.min(k, tPQ.size()); //can only be as big as the queue
		for (int l = 0; l < num; l++){
			ret.addFirst(tPQ.remove().getWord()); //add for return
		}
		return ret;
	}

	/**
	 * Given a prefix, returns the largest-weight word in the trie starting with
	 * that prefix.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from with the largest weight starting with prefix, or an
	 *         empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 */
	public String topMatch(String prefix) {
		if (prefix == null) {
			throw new NullPointerException("No prefix given"); //exception if no prefix given
		}
		Node current = myRoot; //start with root
		for (int k = 0; k < prefix.length(); k++) { //for letter in prefix
			if (!current.children.containsKey(prefix.charAt(k))) { //no such letter
				return "";
			}
			current = current.children.get(prefix.charAt(k)); //go to that letter
		}
		while (current.mySubtreeMaxWeight != current.myWeight) { //until node has max subtree weight
			for (Node no : current.children.values()) { //check each child node
				if (current.mySubtreeMaxWeight == no.mySubtreeMaxWeight) { //pick node with max weight
					current = no;
				}
			}

		}
		return current.getWord();
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary, return
	 * 0.0
	 */
	public double weightOf(String term) {
		Node current = myRoot;
		for (int j = 0; j < term.length(); j++) {
			if (!current.children.containsKey(term.charAt(j))){ //character does not exist
				return 0.0;
			}
			current = current.children.get(term.charAt(j)); //move to next character
		}
		return current.getWeight();
	}
}
