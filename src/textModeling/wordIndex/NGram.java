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
		for ( int i = 0; i < this.size(); i ++ )
		{
			if (!ngram.get(i).equals (this.get(i)))
				return this.get(i).getId().compareTo(ngram.get(i).getId());
		}
		return 0;
		
		
	}
	
	@Override
	public boolean equals (Object o)
	{
		if (o.getClass() != this.getClass())
			return false;
		
		NGram ngram = (NGram) o;
		if ( ngram.size() != this.size() )
			return false;
		/*if (this.grams.containsAll(ngram.grams))
			return true;*/
		for ( int i = 0; i < this.size(); i ++ )
		{
			if ( ! ngram.get(i).getId().equals (this.get(i).getId()))
				return false;
		}
		return true;
			
//		return false;	
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
	
	public String toString()
	{
		String s = "";
		
		for (WordIndex i : this)
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
		
		for (WordIndex i : this)
		{
			code += Math.pow((double)i.getId(), (double)pow);
		}
		return code;
	}
	
	
}
