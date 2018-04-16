package main.java.liasd.asadera.tools;

import main.java.liasd.asadera.textModeling.SentenceModel;

public class PairSentenceScore implements Comparable<PairSentenceScore> {

	private SentenceModel phrase;
	private Double score;

	public PairSentenceScore(SentenceModel phrase, Double score) {
		this.phrase = phrase;
		this.score = score;
	}

	public SentenceModel getPhrase() {
		return this.phrase;
	}

	public Double getScore() {
		return this.score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public int compareTo(PairSentenceScore p1) {
		return p1.score.compareTo(this.score); // Score in decrescent order so first sentence is the best
	}

	public boolean equals(Object o) {
		if (!o.getClass().equals(PairSentenceScore.class))
			return false;
		PairSentenceScore p = (PairSentenceScore) o;
		return p.phrase.equals(this.phrase);
	}

	@Override
	public String toString() {
		return score + "\t" + phrase.toString() + "\n";
	}
}
