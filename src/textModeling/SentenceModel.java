package textModeling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;

public class SentenceModel extends ArrayList<WordModel> implements Comparable<SentenceModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4028010227097874686L;
	protected String sentence;
	protected int iD;
	protected double score;
	//protected Caracteristic caracteristic;
	
	//protected ArrayList<WordModel> listWord = new ArrayList<WordModel>();

	private ParagraphModel paragraph;
	
	public SentenceModel() {
		super();
		//caracteristic = new Caracteristic();
	}
	
	public SentenceModel(String text) {
		super();
		sentence = text;
		//caracteristic = new Caracteristic();
	}
	
	public SentenceModel(String text, int iD, ParagraphModel paragraph) {
		super();
		sentence = text;
		this.iD = iD;
		this.paragraph = paragraph;
		//caracteristic = new Caracteristic();
	}

	public ParagraphModel getParagraph() {
		return paragraph;
	}

	public void setParagraph(ParagraphModel paragraph) {
		this.paragraph = paragraph;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	/*public ArrayList<WordModel> getListWord() {
		return listWord;
	}

	public void setListWord(ArrayList<WordModel> listWord) {
		this.listWord = listWord;
	}*/
	
	public int getiD() {
		return iD;
	}

	public void setiD(int iD) {
		this.iD = iD;
	}

	/*public Caracteristic getCaracteristic() {
		return caracteristic;
	}

	public void setCaracteristic(Caracteristic caracteristic) {
		this.caracteristic = caracteristic;
	}*/

	@Override
	public String toString() {
		return sentence;
	}
	
	public static String listSentenceModelToString (List<SentenceModel> list) {
		String str = "";
		Iterator<SentenceModel> it = list.iterator();
		while (it.hasNext())
			str += it.next().getSentence() + "\n";
		return str;
	}

	@Override
	public int compareTo(SentenceModel arg0) {
		if (this.getiD() > arg0.getiD())
			return 1;
		else if (this.getiD() < arg0.getiD())
			return -1;
		else
			return 0;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public ArrayList<NGram> getNGrams(int n, Index index) {
		ArrayList <NGram> ngrams_list = new ArrayList<NGram> () ;
		WordModel u;
		
		if (n == 1)
		{
			for (WordModel u1 : this)
			{
				
				NGram ng = new NGram();
			//	System.out.println(ng);
				if (!u1.isStopWord())
				{
				//	System.out.println(index);
					WordIndex uIndex = index.get(u1.getmLemma()); 
					ng.addGram(uIndex);
					ngrams_list.add(ng);
				}
			}
			return ngrams_list;
		}
		
		
		for (int i = 0; i < this.size() - n + 1; i++)
		{
			boolean cond = false;
			NGram ng = new NGram ();
			//System.out.println("Sentence size : "+this.unitesLexWVides.size());
			for (int j = i; j < i + n; j++)
			{
				//System.out.println("j : "+j);
				u = this.get(j);
				if (!u.isStopWord())
				{	
					cond = true;
					ng.addGram(index.get(u.getmLemma()));
				}
			}
			if (cond)
				ngrams_list.add(ng);
			//else
				//System.out.println("Filtrée !");
		}
		
		return ngrams_list;
	}
	
	public double getPosScore() {
		if (paragraph.size() > 1)
			return (double)(paragraph.size() - 1 - paragraph.indexOf(this)) / (double)(paragraph.size() - 1);
		else
			return 1;
	}
	
	public int getLength() {
		int n = 0;
		for (WordModel w : this) {
			if (!w.isStopWord())
				n++;
		}
		return n;
	}
}
