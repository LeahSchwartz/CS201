
public class WordGram implements Comparable<WordGram>{
	
	private int myHash;
	private String[] myWords;
	
	public WordGram(String[] words, int index, int size) {
		//creates WordGram object using array words, int index for first word, and int size
		myHash = -1; 
		myWords = new String[size];
		int k = 0;
		for(int j = index; j < size + index; j++) {
			myWords[k] = words[j]; //adds words from word array
			k ++;
		}	
	}
	
	@Override
	public int hashCode() { //unique hashCodes for items
		if (myHash == -1) {
			int hash = 0;
			for(int k = 0; k < myWords.length; k++) {
				hash += myWords[k].hashCode() * (k + 1) * 13;
				hash *= 13; //multiplies by prime number
			}
		myHash = hash;
		}
		return myHash;
	}
	
	@Override
	public String toString() { 
		//returns WordGram words as String
		String myWordStr = String.join(" ", myWords);
		return myWordStr;
	}
	
	@Override
	public boolean equals(Object other) {
		//checks if WordGrams are equivalent
		if (other == null || ! (other instanceof WordGram)) {
			return false;
		}
		WordGram wg = (WordGram) other; //cast other to a WordGram called wg
		if (this.myWords.length == wg.myWords.length){ //checks if WordGrams are same length
			for (int j = 0; j < myWords.length; j++) {
				if (!this.myWords[j].equals(wg.myWords[j])) { //checks if each word is the same
					return false; //returns false if a word is different 
				}
			}
		}
		return true;
	}
	
	@Override
	public int compareTo(WordGram wg) { 
		//returns pos num if WordGram longer, neg if shorter, 0 if equal
		if (wg.myWords.length < this.myWords.length) { //this array is longer
			return 5;
		}
		if (wg.myWords.length > this.myWords.length) { //this array is longer
			return -5;
		}
		if (wg.myWords.length == this.myWords.length) { //arrays are same length
			for (int j = 0; j < this.myWords.length; j ++) {
				if (! wg.myWords[j].equals(this.myWords[j])) { //not the same word
					return this.myWords[j].compareTo(wg.myWords[j]); //compare the words in WordGram
				}
			}
		}
		return 0; //arrays are the same 
	}
	
	public int length() {
		//returns length of myWords
		return myWords.length;
	}
	
	public WordGram shiftAdd(String last) { //shifts words over and adds word last to the end
		String[] shiftedWords = new String[myWords.length];
		shiftedWords[shiftedWords.length - 1] = last; //last element is last
		for(int j = 1; j < myWords.length; j++) { //adds myWords elements starting at index 1
			shiftedWords[j - 1] = myWords[j]; //element must be one place behind in new array
		}
		WordGram shifted = new WordGram(shiftedWords, 0, shiftedWords.length); //creates wordGram objet
		return shifted;
	}
	
}
