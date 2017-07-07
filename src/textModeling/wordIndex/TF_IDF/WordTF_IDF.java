package textModeling.wordIndex.TF_IDF;

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
	
	@Override
	public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen+docOccurences.size();
	}
}
