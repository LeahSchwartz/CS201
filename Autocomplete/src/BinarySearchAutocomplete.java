import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 
 * Using a sorted array of Term objects, this implementation uses binary search
 * to find the top term(s).
 * 
 * @author Austin Lu, adapted from Kevin Wayne
 * @author Jeff Forbes
 */
public class BinarySearchAutocomplete implements Autocompletor {

	Term[] myTerms;

	/**
	 * Given arrays of words and weights, initialize myTerms to a corresponding
	 * array of Terms sorted lexicographically.
	 * 
	 * This constructor is written for you, but you may make modifications to it.
	 * 
	 * @param terms
	 *            - A list of words to form terms from
	 * @param weights
	 *            - A corresponding list of weights, such that terms[i] has
	 *            weight[i].
	 * @return a BinarySearchAutocomplete whose myTerms object has myTerms[i] = a
	 *         Term with word terms[i] and weight weights[i].
	 * @throws a
	 *             NullPointerException if either argument passed in is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different length
	 */
	public BinarySearchAutocomplete(String[] terms, double[] weights) {
		Set<String> wordsDups = new HashSet<String>(Arrays.asList(terms));
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null"); //exception if args are null
		}
		if (terms.length != weights.length) {
			throw new IllegalArgumentException("terms length is not equal to weights length"); //exception if weights and terms are not same length
		}
		if (wordsDups.size() != terms.length) {
			throw new IllegalArgumentException("terms contains duplicates"); //exception if duplicate terms
		}
		for (double w : weights) {
			if (w < 0) {
				throw new IllegalArgumentException("weights contains a negative number"); //exception if weights negative
			}
		}
		myTerms = new Term[terms.length];

		for (int i = 0; i < terms.length; i++) {
			myTerms[i] = new Term(terms[i], weights[i]);
		}

		Arrays.sort(myTerms);
	}

	/**
	 * Uses binary search to find the index of the first Term in the passed in array
	 * which is considered equivalent by a comparator to the given key. This method
	 * should not call comparator.compare() more than 1+log n times, where n is the
	 * size of a.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the values
	 *            in a and the key.
	 * @return The first index i for which comparator considers a[i] and key as
	 *         being equal. If no such index exists, return -1 instead.
	 */
	public static int firstIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if (a.length == 0) { //no elements in array
			return -1;
		}
		int low = -1;
		int high = a.length-1;
		int mid = (low + high) / 2; //num halfway between low and high
		while (low + 1 != high) { //stop when low is one from high
			mid = (low + high) / 2;  //recalculate mid each time
			if (comparator.compare(key,a[mid]) <= 0) { //move high bound if key in lower half (inclusive)
				high = mid;
			}
			else if (comparator.compare(key,a[mid]) > 0) { //move low bound if key in upper half (exclusive)
				low = mid;
			}
		}
		if (comparator.compare(key,a[high]) == 0) { //test if high is the index of correct term
			return high;
		}

		return -1; //no term
	}

	/**
	 * The same as firstIndexOf, but instead finding the index of the last Term.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the values
	 *            in a and the key.
	 * @return The last index i for which comparator considers a[i] and key as being
	 *         equal. If no such index exists, return -1 instead.
	 */
	public static int lastIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if (a.length == 0) { //no elements in array
			return -1;
		}
		int low = 0;
		int high = a.length;
		int mid = (low + high) / 2; //num halfway between low and high
		while (low + 1 != high) { //stop when low is one from high
			mid = (low + high) / 2; //recalculate mid each time
			if (comparator.compare(key,a[mid]) < 0) { //move high bound if key in lower half (exclusive)
				high = mid;
			}
			else if (comparator.compare(key,a[mid]) >= 0) { //move low bound if key in upper half (inclusive)
				low = mid;
			}
		}
		if (comparator.compare(key,a[low]) == 0) { //test if low is the index of correct term
			return low;
		}

		return -1; //no term
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the k
	 * words in myTerms with the largest weight which match the given prefix, in
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
	 * @return An array of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k such
	 *         words exist, return an array containing all those words If no such
	 *         words exist, reutrn an empty array
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		if (k <= 0) {
			return new LinkedList<String>();
		}
		if (prefix == null) {
			throw new NullPointerException("No prefix given"); //exception if no prefix given 
		}
		PriorityQueue<Term> wordy = new PriorityQueue<Term>(k, new Term.WeightOrder());
		LinkedList<String> ret = new LinkedList<String>();
		Comparator<Term> comparator = new Term.PrefixOrder(prefix.length());
		Term p = new Term(prefix, 0);
		int first = firstIndexOf(myTerms, p, comparator); //index of first matching term
		int last = lastIndexOf(myTerms, p, comparator); //index of last matching term
		if (first < 0 || last < 0 || k <= 0) { //no matching term
			return ret;
		}
		for (int i = first; i <= last; i++) { //all matching terms from first to last 
			if (wordy.size() < k) { //pQueue not yet full
				wordy.add(myTerms[i]); //add term
			}
			else if (wordy.peek().getWeight() < myTerms[i].getWeight()){ //pQueue is full, must test if new weight is greater than current lowest
				wordy.remove(); //remove lowest
				wordy.add(myTerms[i]); //add term
			}
		}
		int num = Math.min(wordy.size(), k); 
		for (int j = 0; j < num; j++) { //for either as many things in wordy or size k, whichever is smaller
			ret.addFirst(wordy.remove().getWord()); //add words of terms to return list
			}
		return ret;
	}

	/**
	 * Given a prefix, returns the largest-weight word in myTerms starting with that
	 * prefix. e.g. for {air:3, bat:2, bell:4, boy:1}, topMatch("b") would return
	 * "bell". If no such word exists, return an empty String.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from myTerms with the largest weight starting with prefix,
	 *         or an empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 * 
	 */
	public String topMatch(String prefix) {
		if (prefix == null) {
			throw new NullPointerException("No prefix given"); //exception if no prefix given
		}
		Term p = new Term(prefix, 0);
		Comparator<Term> comparator = new Term.PrefixOrder(prefix.length());
		int first = firstIndexOf(myTerms, p, comparator); //index of first matching term
		int last = lastIndexOf(myTerms, p, comparator); //index of last matching term
		if (first < 0 || last < 0) { //no term
			return "";
		}
		double atLeast = 0.0;
		String greatWord = "";
		for (int i = first; i <= last; i++) { //loop from first matching term to last
			double weight = myTerms[i].getWeight(); //weight of given term
			if (weight > atLeast) { //new greatest if bigger than current weight
				atLeast = weight; //replace weight
				greatWord = myTerms[i].getWord(); //replace word
			}
		}
		return greatWord;
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary, return
	 * 0.0
	 */
	public double weightOf(String term) {
		Term t = new Term(term, 0); //create term from given string 
		Comparator<Term> comp = new Term.PrefixOrder(term.length());
		int num = firstIndexOf(myTerms, t, comp); //index of first matching term
		if (num < 0) { //index means no term
			return 0.0;
		}
		return myTerms[num].getWeight(); //weight of matching term
	}
}
