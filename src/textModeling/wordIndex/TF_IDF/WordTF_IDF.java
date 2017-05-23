package textModeling.wordIndex.TF_IDF;

import java.util.HashMap;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;

public class WordTF_IDF extends WordIndex {

	protected int nbDocumentWithWordSeen;
	
	public WordTF_IDF(String word, Index<WordTF_IDF> dictionnary) {
		super(word, dictionnary);
	}

	public WordTF_IDF(String word, Index<WordTF_IDF> dictionnary, int nbDocumentWithWordSeen) {
		super(word, dictionnary);
		this.nbDocumentWithWordSeen = nbDocumentWithWordSeen;
	}
	
	public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen+docOccurences.size();
	}
	
	public double getIdf() {
		/**
		 * Smooth IDF (1+log à la place de log simple) si rencontre de mot inconnu du dictionnaire
		 */
		return Math.log(index.getNbDocument()/getNbDocumentWithWordSeen());
	}
	
	public double getTf() {
		return (double)getNbOccurence();
	}
	
	public HashMap<Integer, Integer> getCorpusOccurence() {
		return corpusOccurences;
	}
}
