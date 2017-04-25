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
	protected int nbMot;
	protected ArrayList<NGram> listNGram;

	protected int iD;
	protected double score;
	protected TextModel text;
	
	public SentenceModel() {
		super();
	}
	
	public SentenceModel(String sen) {
		super();
		sentence = sen;
	}
	
	public SentenceModel(String sen, int iD, TextModel text) {
		super();
		sentence = sen;
		this.iD = iD;
		this.text = text;
	}

	/**
	 * 
	 * @return Sentence as a list of lemme without stopword
	 */
	public String getSentence() {
		String txt = "";
		for (WordModel w : this) {
			if (!w.isStopWord())
				txt += w.toString() + " ";
			else
				txt += "%%" + w.toString() + " ";
		}
		return txt;
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

	/**
	 * @return full sentence
	 */
	@Override
	public String toString() {
		return sentence;
	}
	
	public static String listSentenceModelToString (List<SentenceModel> list) {
		String str = "";
		Iterator<SentenceModel> it = list.iterator();
		while (it.hasNext())
			str += it.next().toString() + "\n";
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
	
	private void getListNGrams(int n, Index index) {
		ArrayList <NGram> ngrams_list = new ArrayList<NGram> () ;
		WordModel u;
		
		if (n == 1) {
			for (WordModel u1 : this) {
				NGram ng = new NGram();
				if (!u1.isStopWord()) {
					WordIndex w = index.get(u1.getmLemma());
					if (w != null) {
						WordIndex uIndex = index.get(u1.getmLemma()); 
						ng.addGram(uIndex);
						ngrams_list.add(ng);
					}
				}
			}
		}
		else {
			for (int i = 0; i < this.size() - n + 1; i++)
			{
				boolean cond = false;
				boolean stopWord = false; //Un stopWord par Ngram
				NGram ng = new NGram ();
				//System.out.println("Sentence size : "+this.unitesLexWVides.size());
				for (int j = i; j < i + n; j++)
				{
					//System.out.println("j : "+j);
					u = this.get(j);
	
					if ((!stopWord && !u.isStopWord()) || (!stopWord && u.isStopWord()) || (stopWord && !u.isStopWord())) {
						cond = true;
						WordIndex w = index.get(u.getmLemma());
						if (w != null)
							ng.addGram(w);
						else {
							System.out.println("BREAK!!!" + u.getmLemma());
							cond = false;
							break;
						}
						if (u.isStopWord())
							stopWord = true;
					} else
						cond = false;
				}
				if (cond)
					ngrams_list.add(ng);
				//else
					//System.out.println("Filtrée !");
			}
		}
		listNGram = ngrams_list;
		
	}

	public ArrayList<NGram> getNGrams(int n, Index index) {
		if(listNGram == null)
			getListNGrams(n, index);
		return listNGram;
	}
	
	public double getPosScore() {
		if (text.size() > 1)
			return (double)(text.size() - 1 - text.indexOf(this)) / (double)(text.size() - 1);
		else
			return 1;
	}
	
	public int getPosition() {
		return (text.indexOf(this)+1);
	}
	
	public TextModel getText() {
		return text;
	}

	public void setText(TextModel text) {
		this.text = text;
	}

	public int getLength() {
		int n = 0;
		for (WordModel w : this) {
			if (!w.isStopWord())
				n++;
		}
		return n;
	}

	public int getNbMot() {
		return nbMot;
	}

	public void setNbMot(int nbMot) {
		this.nbMot = nbMot;
	}
	
	@Override
	public int size() {
		return super.size();
	}
}
