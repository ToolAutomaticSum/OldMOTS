package liasd.asadera.textModeling.wordIndex.old.TF_IDF;

import liasd.asadera.textModeling.wordIndex.WordIndex;

public class WordTF_IDF extends WordIndex {

	protected int nbDocumentWithWordSeen;

	public WordTF_IDF(String word/* , Index<WordTF_IDF> dictionnary */) {
		super(word/* , index */);
	}

	public WordTF_IDF(String word, /* Index<WordTF_IDF> dictionnary, */ int nbDocumentWithWordSeen) {
		super(word/* , index */);
		this.nbDocumentWithWordSeen = nbDocumentWithWordSeen;
	}

	@Override
	public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen + docOccurences.size();
	}
}
