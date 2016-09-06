package structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Sequences {
	
	protected ArrayList<Sequence> m_Sequences;
	
	public Sequences(ArrayList<Sequence> seqDB) {

		m_Sequences = new ArrayList<Sequence>(seqDB.size());
		for(int i = 0; i < seqDB.size(); i ++){
			m_Sequences.add(seqDB.get(i));
		}
	}
	
	public Sequences(int capacity) {
	    
		if (capacity < 0) {
	      capacity = 0;
	    }
		m_Sequences = new ArrayList<Sequence>(capacity);
	}
	
	public Sequences(Sequences dataset) {

		m_Sequences = new ArrayList<Sequence>();

		dataset.copySequences(0, this, dataset.numSequences());
	}
	
	public Sequences(){
		
		m_Sequences = new ArrayList<Sequence>();
	}
	
    public void loadSequences(String file) throws Exception {

    	File f = new File(file);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        
        String id = br.readLine();
        
        while(id != null){

            String sequence = br.readLine();
            String label = br.readLine();

            Sequence current = new Sequence(id, sequence, label);

            m_Sequences.add(current);
            
            id = br.readLine();
        }
    }
    
	public Sequence sequence(int index) {

		return (Sequence)m_Sequences.get(index);
	}
	
	public void add(Sequence sequence) {

		m_Sequences.add(sequence);
	}
	
	public void delete() {
	    
		m_Sequences = new ArrayList<Sequence>();
	}
	
	public void delete(int index) {
	    
		m_Sequences.remove(index);
	}
	  
	public int numSequences() {

		return m_Sequences.size();
	}
	
	public int size() {

		return m_Sequences.size();
	}
	
	public void randomize(Random random) {

		for (int j = numSequences() - 1; j > 0; j--)
			swap(j, random.nextInt(j+1));
	}
	
	public void swap(int first, int second){
	    
		Object[] m_Objects = new Object[m_Sequences.size()];
		for (int k = 0; k < m_Objects.length;k ++){
			m_Objects[k] = m_Sequences.get(k);
		}
		  
		Object help = m_Objects[first];
		m_Objects[first] = m_Objects[second];
		m_Objects[second] = help;
		  
		this.delete();
		for(int k = 0; k < m_Objects.length;k ++){
			m_Sequences.add((Sequence)m_Objects[k]);
		}
	}
	
	public void stratify(int numFolds) {
	    
		if (numFolds <= 0) {
			throw new IllegalArgumentException("Number of folds must be greater than 1");
		}
	    // sort by class
	    int index = 1;
	    while (index < numSequences()) {
	    	Sequence sequence1 = sequence(index - 1);
	    	for (int j = index; j < numSequences(); j++) {
	    		Sequence sequence2 = sequence(j);
	    		if (sequence1.getLabel().equals(sequence2.getLabel())) {
	    			swap(index,j);
	    			index++;
	    		}
	    	}
	    	index++;
	    }
	    stratStep(numFolds);
	}
	
	protected void stratStep (int numFolds){
	    
		ArrayList<Sequence> newList = new ArrayList<Sequence>(m_Sequences.size());
		int start = 0, j;

		// create stratified batch
		while (newList.size() < numSequences()) {
			j = start;
			while (j < numSequences()) {
				newList.add(sequence(j));
				j = j + numFolds;
			}
			start++;
		}
		m_Sequences = newList;
	}
	
	protected void copySequences(int from, /*@non_null@*/ Sequences dest, int num) {
	    
		for (int i = 0; i < num; i++) {
			dest.add(sequence(from + i));
		}
	}
	
	public Sequences testCV(int numFolds, int numFold) {

		int numInstForFold, first, offset;
		Sequences test;
	    
		if (numFolds < 2) {
			throw new IllegalArgumentException("Number of folds must be at least 2!");
		}
		if (numFolds > numSequences()) {
			throw new IllegalArgumentException("Can't have more folds than instances!");
		}
		numInstForFold = numSequences() / numFolds;
		if (numFold < numSequences() % numFolds){
			numInstForFold++;
			offset = numFold;
		}else
			offset = numSequences() % numFolds;
		test = new Sequences(numInstForFold);
		first = numFold * (numSequences() / numFolds) + offset;
		copySequences(first, test, numInstForFold);
		return test;
	}
	  
	public int numWithClass(String label){
		int count = 0;
		  
		for (int i = 0; i < numSequences();i ++){
			if(sequence(i).getLabel().equals(label))
				count ++;
		}
		return count;
	}
	
	public Sequences trainCV(int numFolds, int numFold) {

		int numInstForFold, first, offset;
		Sequences train;
		 
		if (numFolds < 2) {
			throw new IllegalArgumentException("Number of folds must be at least 2!");
		}
		if (numFolds > numSequences()) {
			throw new IllegalArgumentException("Can't have more folds than instances!");
		}
		numInstForFold = numSequences() / numFolds;
		if (numFold < numSequences() % numFolds) {
			numInstForFold++;
		    offset = numFold;
		}else
		    offset = numSequences() % numFolds;
		train = new Sequences(numSequences() - numInstForFold);
		first = numFold * (numSequences() / numFolds) + offset;
		copySequences(0, train, first);
		copySequences(first + numInstForFold, train,
			numSequences() - first - numInstForFold);

		return train;
	}
	
	public Sequences trainCV(int numFolds, int numFold, Random random) {

		Sequences train = trainCV(numFolds, numFold);
		train.randomize(random);
		return train;
	}
	 
	public void print(){
		for(int i = 0; i < numSequences(); i ++){
			System.out.println(sequence(i).toString()+"\n");
		}
	}
	
	public void print(String output) throws Exception{
		
		File outFile = new File(output);
		FileWriter outFileWriter = new FileWriter(outFile);
		
		for(int i = 0; i < numSequences(); i ++){
			outFileWriter.write(sequence(i).toString()+"\n");
		}
		outFileWriter.close();
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < numSequences(); i ++){
			sb.append(sequence(i).toString()+"\n");
		}
		return sb.toString();
	}
	
    public HashSet<String> getIDs(){
    	HashSet<String> ids = new HashSet<String>();
    	
    	Sequence seq;
    	for (int i = 0; i < numSequences();i ++) {
    		seq = m_Sequences.get(i);
    		String id = seq.getID();
    		if (!ids.contains(id)){
    			ids.add(id);
    		}
    	}
    	return ids;
    }
	  
	public static void main(String[] args){
			
	}
}
