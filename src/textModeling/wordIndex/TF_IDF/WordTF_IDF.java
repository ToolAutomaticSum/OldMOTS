package textModeling.wordIndex.TF_IDF;

import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;

public class WordTF_IDF extends WordIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2186642315849071658L;
	//protected double tf = getListWord().size();

	protected int nbSentenceWithWordSeen;
	//protected double[] listTf;
	//protected int tf;
	//protected int nbSentence;

	public WordTF_IDF(String word, Dictionnary dictionnary, int iD/*, int nbSentence*/) {
		super(word, dictionnary, iD);
		//listTf = new double[nbSentence];
		//this.nbSentence = nbSentence;
	}

	
	public WordTF_IDF(String word, Dictionnary dictionnary, int iD/*, int nbSentence*/, int nbSentenceWithWordSeen) {
		super(word, dictionnary, iD);
		this.nbSentenceWithWordSeen = nbSentenceWithWordSeen;
		//this.nbSentence = nbSentence;
	}


	/*public double getTf(int sentence) {
		return listTf[sentence]/nbWordTotal;
	}*/
	
	public void incrementSentenceWithWordSeen() {
		nbSentenceWithWordSeen++;
	}
	
	public double getIdf() {
		/*int nbWordSeenSentence = 0;
		for (int i = 0; i < listTf.length; i++) {
			if (listTf[i] > 0)
				nbWordSeenSentence++;
		}*/
		/**
		 * Smooth IDF (1+log à la place de log simple) si rencontre de mot inconnu du dictionnaire
		 */
		return Math.log10(dictionnary.getNbSentence()/nbSentenceWithWordSeen);
	}
	
	public int getTf() {
		return this.size();
	}

	public int getNbDocumentWithWordSeen() {
		return nbSentenceWithWordSeen;
	}


	public void setNbDocumentWithWordSeen(int nbSentenceWithWordSeen) {
		this.nbSentenceWithWordSeen = nbSentenceWithWordSeen;
	}


	/*public int getNbDocument() {
		return nbSentence;
	}


	public void setNbDocument(int nbSentence) {
		this.nbSentence = nbSentence;
	}*/
	
	/*public double getTfIdf(int sentence) {
		return getTf(sentence)*getIdf();
	}*/
	
	/*public void actualizeTf(int doc, int nbWord) {
		if (doc < listTf.length) {
			int countTf = 0; 
			int i = doc;
			while (i > 0) {
				countTf += listTf[i];
				i--;
			}
			listTf[doc] = getListWord().size() - countTf;
			nbWordTotal = nbWord;
		}
	}*/
}
