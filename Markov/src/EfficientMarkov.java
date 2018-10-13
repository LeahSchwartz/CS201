import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class EfficientMarkov extends MarkovModel {
	Map<String,ArrayList<String>> myMap; //initializes myMap
	
	EfficientMarkov(int order){
		super(order);
		myMap = new HashMap<String, ArrayList<String>>(); 
	}
	
	@Override
	public void setTraining(String text){ //sets text and adds values to myMap
		myText = text;
		myMap.clear();
		for (int j = 0; j < text.length() - myOrder + 1; j++) {//need to get end of text char
			String textKey = text.substring(j, j + myOrder);//takes three char String
			if (!myMap.containsKey(textKey)){ //checks if substring in myMap
				myMap.put(textKey, new ArrayList<String>()); //adds substring as key
			}
			if (j < text.length() - myOrder) { //has more characters after (not end of text)
				myMap.get(textKey).add(text.substring(j + myOrder, j + myOrder + 1));	
			} else { //case for end of text  
				myMap.get(textKey).add(PSEUDO_EOS);
			}
		}
	}
	
	@Override
	public ArrayList<String> getFollows(String key){ //returns value from myMap given key
		if (myMap.containsKey(key)) {
			ArrayList<String> returnValue = myMap.get(key);
			return returnValue;
		} else { // myMap does not contain key
			 throw new NoSuchElementException(key);
		}
	}

	public static void main(String[] args) {
		
		

	}

}
