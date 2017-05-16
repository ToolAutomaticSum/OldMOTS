package textModeling.wordIndex;

import java.util.ArrayList;
import java.util.HashMap;

import textModeling.WordModel;

public class WordIndex extends ArrayList<WordModel> implements Comparable<WordIndex> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6192277237881669695L;
	private String word;
	private Integer iD;
	protected Index index;
	
	/**
	 * Tf et Idf par documents : Key = idCorpus
	 */
	protected HashMap<Integer, Integer> docOccurences = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> corpusOccurences = new HashMap<Integer, Integer>();

	public Integer getiD() {
		return iD;
	}

	public HashMap<Integer, Integer> getDocOccurences() {
		return docOccurences;
	}

	public HashMap<Integer, Integer> getCorpusOccurences() {
		return corpusOccurences;
	}

	public WordIndex(String word, Index index) {
		super();
		this.word = word;
		this.index = index;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Integer getId() {
		return iD;
	}

	public void setId(int iD) {
		this.iD = iD;
	}

	public Index getDictionnary() {
		return index;
	}

	public void setDictionnary(Index dictionnary) {
		this.index = dictionnary;
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
	
	public double getTfDocument(int idDoc) {
		return (double)docOccurences.get(idDoc);
	}
	
	public double getTfCorpus(int idCorpus) {
		return (double)corpusOccurences.get(idCorpus);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		WordIndex wi = (WordIndex) o;
		return iD == wi.getId() ;
	}
	
	@Override
	public String toString() {
		return word;
	}

	@Override
	public int compareTo(WordIndex o) {
		return this.getiD().compareTo(o.getId());
	}
}
