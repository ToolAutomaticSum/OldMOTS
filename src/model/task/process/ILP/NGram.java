package model.task.process.ILP;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

public class NGram implements Comparable<NGram>{
	private ArrayList<Integer> grams;
	
	public NGram()
	{
		this.grams = new ArrayList <Integer> ();
	}
	
	public NGram (NGram ng)
	{
		this.grams = new ArrayList <Integer> ();
		this.grams.addAll(ng.grams);
	}
	
	public void addGram(Integer indexKey)
	{
		this.grams.add(indexKey);
	}
	
	public ArrayList <Integer> getGrams()
	{
		return this.grams;
	}

	@Override
	public int compareTo(NGram ngram) {
		if ( ngram.grams.size() < this.grams.size() )
			return 1;
		if ( this.grams.size() < ngram.grams.size() )
			return -1;
		for ( int i = 0; i < this.grams.size(); i ++ )
		{
			if ( ! ngram.grams.get(i).equals (this.grams.get(i)))
				return this.grams.get(i).compareTo(ngram.grams.get(i));
		}
		return 0;
	}
	
	@Override
	public boolean equals (Object o)
	{
		if (o.getClass() != this.getClass())
			return false;
		
		NGram ngram = (NGram) o;
		if ( ngram.grams.size() != this.grams.size() )
			return false;
		for ( int i = 0; i < this.grams.size(); i ++ )
		{
			if ( ! ngram.grams.get(i).equals (this.grams.get(i)))
				return false;
		}
		return true;
			
//		return false;	
	}
	
	public void printNGram (Map<Integer, String> hashMapWord)
	{
		for (Integer i : this.grams)
		{
			System.out.println(hashMapWord.get(i));		
		}		
	}
	
	
	public static void main (String[] args)
	{
		/*NGram n1 = new NGram();
		n1.addGram(1);
		n1.addGram(5);
		n1.addGram(2);
		n1.addGram(-3);*/
		
		
		ArrayList <Integer> al = new ArrayList <Integer>();
		al.add(1);
		al.add(3);
		al.add(1);
		TreeSet <Integer> ts = new TreeSet <Integer> (al);
		
		for (Integer i : ts)
		{
			System.out.println("i : "+i);
		}
		
		
	}
	
	public void removeLastGram()
	{
		if (this.grams.size() == 0)
			return;
		this.grams.remove(this.grams.size()-1);
	}
	
	public void removeFirstGram()
	{
		if (this.grams.size() == 0)
			return;
		this.grams.remove(0);
	}
	
	public String toString()
	{
		String s = "";
		
		for (Integer i : this.grams)
		{
			s+= i+" | ";
		}
		s+= "\n";
		return s;
	}
	
	@Override
    public int hashCode() {
		int code = 0;
		int pow = 1;
		
		for (Integer i : this.grams)
		{
			code += Math.pow((double)i, (double)pow);
			i++;
		}
		return code;
	}
	
	
}
