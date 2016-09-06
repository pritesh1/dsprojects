package structure;

//import utilities.HashMapVector;

//import java.util.StringTokenizer;

//import types.Alphabet;

/**@author cornelia*/

 //The String sequence is already processed.


public class Sequence {

	public String id;
	public String sequence;
	public String label;
	
	public Sequence(String id, String sequence, String label){
		this.id = id;
		this.sequence = sequence;
    	this.label = label;
	}
	
	public String getID(){
		return id;
	}
	
	public String getSeq(){
		return sequence;
	}
	
	public String getLabel(){
		return label;
	}
    
    /*public HashMapVector hashMapVector () {
    	
    	HashMapVector vector = new HashMapVector();

    	//sequence = sequence.toLowerCase();
    	
		StringTokenizer tokenizer = new StringTokenizer(sequence);
		String[] unigrams = new String[tokenizer.countTokens()];
		for(int i = 0; i < unigrams.length; i ++){
			unigrams[i] = tokenizer.nextToken();
		}
		
		for(int i = 0; i < unigrams.length; i ++){
    	    String token = unigrams[i];
    	    vector.increment(token);
		}
    	return vector;
    }*/
    
    /*public HashMapVector hashMapVector(Alphabet m_Alphabet){
    	
    	HashMapVector vector = new HashMapVector();
    	
    	sequence = sequence.toLowerCase();
    			
		StringTokenizer tokenizer = new StringTokenizer(sequence);
		String[] unigrams = new String[tokenizer.countTokens()];
		for(int i = 0; i < unigrams.length; i ++){
			unigrams[i] = tokenizer.nextToken();
		}
		
		for (int i = 0; i < unigrams.length; i ++) {
						
			if (m_Alphabet.contains(unigrams[i])) {
				vector.increment(unigrams[i]);
			}
		}			

    	return vector;
    }*/
    
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(new String(id+"\n"));
		sb.append(new String(sequence+"\n"));
		sb.append(new String(label));
		return sb.toString();
	}
	
    public static void main(String[] args) {
    	
    }
    
}
