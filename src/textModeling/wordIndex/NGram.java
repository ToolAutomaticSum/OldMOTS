package textModeling.wordIndex;

import java.util.ArrayList;

public class NGram extends ArrayList<WordIndex> implements Comparable<NGram>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4188336154082203140L;

	public NGram()
	{
		super();
	}
	
	public NGram (NGram ng)
	{
		super(ng);
	}
	
	public void addGram(WordIndex indexKey)
	{
		add(indexKey);
	}
	
	public ArrayList<WordIndex> getGrams()
	{
		return this;
	}

	@Override
	public int compareTo(NGram ngram) {
		if ( ngram.size() < this.size() )
			return 1;
		if ( this.size() < ngram.size() )
			return -1;
		if (this.equals(ngram))
			return 0;
		else if (this.hashCode() > ngram.hashCode())
			return 1;
		else
			return -1;
	}
	
	@Override
	public boolean equals (Object o)
	{
		if (o.getClass() != this.getClass())
			return false;
		NGram ngram = (NGram) o;
		if (ngram.size() != this.size() )
			return false;
		if (ngram.hashCode() == this.hashCode())
			return true;
		else
			return false;
	}
	
	public void printNGram ()
	{
		for (WordIndex i : this)
		{
			System.out.print(" | "+i);
		}
	}
	
	public void removeLastGram()
	{
		if (this.size() == 0)
			return;
		this.remove(this.size()-1);
	}
	
	public void removeFirstGram()
	{
		if (this.size() == 0)
			return;
		this.remove(0);
	}
	
	@Override
	public String toString() {
		return super.toString() + "\n" + hashCode();
	}
	
/*	public String toString()
	{
		String s = "";
		
		for (WordIndex i : this)
		{
			s+= i.toString() +" | ";
		}
		s+= "\n";
		return s;
	}*/
	
	@Override
    public int hashCode() {
		int code = 0;
		int i = 1;
		for (WordIndex w : this)
		{
			code += Math.pow(w.getWord().hashCode(), (double)i);
		}
		return code;
	}
	
	
}
