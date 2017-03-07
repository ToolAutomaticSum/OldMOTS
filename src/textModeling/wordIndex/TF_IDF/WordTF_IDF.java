package textModeling.wordIndex.TF_IDF;

import java.util.HashMap;

import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;

public class WordTF_IDF extends WordIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2186642315849071658L;
	//protected double tf = getListWord().size();

	protected int nbDocumentWithWordSeen;
	
	/**
	 * Tf et Idf par documents : Key = idDoc
	 */
	private HashMap<Integer, Integer> occurences = new HashMap<Integer, Integer>();
	//protected double[] listTf;
	//protected int tf;
	//protected int nbSentence;

	public WordTF_IDF(String word, Dictionnary dictionnary, int iD/*, int nbSentence*/) {
		super(word, dictionnary, iD);
		//listTf = new double[nbSentence];
		//this.nbSentence = nbSentence;
	}

	public WordTF_IDF(String word, Dictionnary dictionnary, int iD, int nbDocumentWithWordSeen) {
		super(word, dictionnary, iD);
		this.nbDocumentWithWordSeen = nbDocumentWithWordSeen;
		//this.nbSentence = nbSentence;
	}

	/*public double getTf(int sentence) {
		return listTf[sentence]/nbWordTotal;
	}*/
	
	public void addDocumentOccurence(int idDoc) {
		if (!occurences.containsKey(idDoc))
			occurences.put(idDoc, 1);
		else
			occurences.put(idDoc, occurences.get(idDoc)+1);
	}
	
	public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen+occurences.size();
	}
	
	public double getIdf() {
		/**
		 * Smooth IDF (1+log à la place de log simple) si rencontre de mot inconnu du dictionnaire
		 */
		return Math.log10(dictionnary.getNbDocument()/getNbDocumentWithWordSeen());
	}
	
	public double getTf(int idDoc) {
		return (double)occurences.get(idDoc)/(double)dictionnary.getDocNbWord().get(idDoc);
	}
	
	public double getTf() {
		int nbWord = 0;
		for (int i : dictionnary.getDocNbWord().keySet()) {
			nbWord+=dictionnary.getDocNbWord().get(i);
		}
		return (double)size()/(double)nbWord;
	}
	
	public HashMap<Integer, Integer> getOccurence() {
		return occurences;
	}
	/*public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen;
	}


	public void setNbDocumentWithWordSeen(int nbDocumentWithWordSeen) {
		this.nbDocumentWithWordSeen = nbDocumentWithWordSeen;
	}*/


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
