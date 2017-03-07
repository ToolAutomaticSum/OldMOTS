package textModeling.wordIndex;

import java.util.HashMap;

public class Dictionnary extends HashMap<String, WordIndex> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4164985351782026778L;
	
	private int nbDoc;
	/**
	 * Tf et Idf par documents : Key = idDoc
	 */
	private HashMap<Integer, Integer> docNbWord = new HashMap<Integer, Integer>();
	
	public Dictionnary() {
		super();
	}

	public Dictionnary(int nbDoc) {
		super();
		this.nbDoc = nbDoc;
	}

	public int getNbDocument() {
		return nbDoc;
	}

	public void setNbDocument(int nbDoc) {
		this.nbDoc = nbDoc;
	}

	public HashMap<Integer, Integer> getDocNbWord() {
		return docNbWord;
	}
}