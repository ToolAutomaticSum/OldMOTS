package textModeling.wordIndex;

import java.util.HashMap;

public class Dictionnary extends HashMap<String, WordIndex> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4164985351782026778L;
	
	int nbSentence;

	public Dictionnary() {
		super();
	}

	public Dictionnary(int nbSentence) {
		super();
		this.nbSentence = nbSentence;
	}

	public int getNbSentence() {
		return nbSentence;
	}

	public void setNbSentence(int nbSentence) {
		this.nbSentence = nbSentence;
	}
}
