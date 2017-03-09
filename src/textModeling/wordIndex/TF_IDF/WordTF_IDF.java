package textModeling.wordIndex.TF_IDF;

import java.util.HashMap;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;

public class WordTF_IDF extends WordIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2186642315849071658L;

	protected int nbDocumentWithWordSeen;
	
	/**
	 * Tf et Idf par documents : Key = idCorpus
	 */
	private HashMap<Integer, Integer> docOccurences = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> corpusOccurences = new HashMap<Integer, Integer>();

	public WordTF_IDF(String word, Index dictionnary) {
		super(word, dictionnary);
	}

	public WordTF_IDF(String word, Index dictionnary, int nbDocumentWithWordSeen) {
		super(word, dictionnary);
		this.nbDocumentWithWordSeen = nbDocumentWithWordSeen;
	}
	
	public void addDocumentOccurence(int idCorpus, int idDoc) {
		if (!corpusOccurences.containsKey(idCorpus))
			corpusOccurences.put(idCorpus, 1);
		else
			corpusOccurences.put(idCorpus, corpusOccurences.get(idCorpus)+1);
		if (!docOccurences.containsKey(idDoc))
			docOccurences.put(idDoc, 1);
		else
			docOccurences.put(idDoc, docOccurences.get(idDoc)+1);
	}
	
	public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen+docOccurences.size();
	}
	
	public double getIdf() {
		/**
		 * Smooth IDF (1+log à la place de log simple) si rencontre de mot inconnu du dictionnaire
		 */
		return Math.log(dictionnary.getNbDocument()/getNbDocumentWithWordSeen());
	}
	
	public double getTfDocument(int idDoc) {
		return (double)docOccurences.get(idDoc);
	}
	
	public double getTfCorpus(int idCorpus) {
		return (double)corpusOccurences.get(idCorpus);
	}
	
	public double getTf() {
		return (double)size();
	}
	
	public HashMap<Integer, Integer> getCorpusOccurence() {
		return corpusOccurences;
	}
}
