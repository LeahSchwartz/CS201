import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class EfficientWordMarkov extends WordMarkovModel{
	Map<WordGram,ArrayList<String>> myMap;

	
	EfficientWordMarkov(int order){
		super(order);
		myMap = new HashMap<WordGram, ArrayList<String>>();
	}
	@Override
	public void setTraining(String text) {
		//clears map, adds new WordGram keys with ArrayList of values
		myWords = text.split("\\s+");
		myMap.clear();
		for(int j = 0; j < myWords.length - myOrder + 1; j ++) {
			WordGram w = new WordGram(myWords, j, myOrder);
			if (!myMap.containsKey(w)) { //checks if key is present
				myMap.put(w, new ArrayList<String>()); //empty ArrayList as value
			}
			if (j < myWords.length - myOrder) {
				myMap.get(w).add(myWords[j + myOrder]); //adds next word to ArrayList
			} else {
				myMap.get(w).add(PSEUDO_EOS); //end of text
			}
		}
		//System.out.println("Map size: " + myMap.size());
	}
	@Override
	public ArrayList<String> getFollows(WordGram key){
		//returns values in ArrayList value
		if (myMap.containsKey(key)) {
			ArrayList<String> returnValue = myMap.get(key);
			return returnValue;
		} else { // myMap does not contain key
			 String keyString = key.toString();
			 throw new NoSuchElementException(keyString);
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
	}

}
