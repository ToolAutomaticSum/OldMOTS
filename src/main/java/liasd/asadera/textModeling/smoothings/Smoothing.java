package main.java.liasd.asadera.textModeling.smoothings;

import java.util.List;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public abstract class Smoothing {

	protected double vocab_card;
	protected List<SentenceModel> sentences;
	protected Index<WordIndex> index;
	protected int summNbTokens;

	/**
	 * 
	 * @param sentences
	 * @param vocab_card
	 *            doesn't need to be set since the beginning, can be set afterwards,
	 *            but before distribution computation
	 */
	public Smoothing(List<SentenceModel> sentences, int vocab_card, Index<WordIndex> index) {
		this.index = index;
		this.sentences = sentences;
		this.vocab_card = (double) vocab_card;
	}

	protected void setVocabCard(int vocab_card) {
		this.vocab_card = (double) vocab_card;
	}

	public abstract double getSmoothedProb(NGram ng);

	public int getSummNbTokens() {
		return this.summNbTokens;
	}
}
