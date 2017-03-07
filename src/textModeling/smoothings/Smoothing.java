package textModeling.smoothings;

import java.util.ArrayList;

import textModeling.SentenceModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.NGram;

public abstract class Smoothing {

	protected double vocab_card;
	protected ArrayList<SentenceModel> sentences;
	protected Dictionnary index;
	protected int summNbTokens;
	
	/**
	 * 
	 * @param sentences
	 * @param vocab_card doesn't need to be set since the beginning, can be set afterwards, but before distribution computation
	 */
	public Smoothing (ArrayList<SentenceModel> sentences, int vocab_card, Dictionnary index)
	{
		this.index = index;
		this.sentences = sentences;
		this.vocab_card = (double) vocab_card;
	}
	
	
	protected void setVocabCard (int vocab_card)
	{
		this.vocab_card = (double) vocab_card;
 	}
	
	public abstract double getSmoothedProb (NGram ng);
	
	public int getSummNbTokens ()
	{
		return this.summNbTokens;
	}
}
