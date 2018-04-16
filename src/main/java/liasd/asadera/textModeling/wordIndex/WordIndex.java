package main.java.liasd.asadera.textModeling.wordIndex;

import java.util.HashMap;

public class WordIndex implements Comparable<WordIndex> {

	private int iD;
	private double weight;
	private int nbDocumentWithWordSeen;

	protected String word;
	private int nbOccurence = 0;

	protected HashMap<Integer, Integer> docOccurences = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> corpusOccurences = new HashMap<Integer, Integer>();

	public WordIndex() {
		super();
	}

	public WordIndex(String word, int nbDocumentWithWordSeen) {
		this.nbDocumentWithWordSeen = nbDocumentWithWordSeen;
	}

	public WordIndex(String word) {
		super();
		this.word = word;
	}

	public Integer getiD() {
		return iD;
	}

	public void setiD(int iD) {
		this.iD = iD;
	}

	public HashMap<Integer, Integer> getDocOccurences() {
		return docOccurences;
	}

	public HashMap<Integer, Integer> getCorpusOccurences() {
		return corpusOccurences;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void addDocumentOccurence(int idCorpus, int idDoc) {
		nbOccurence++;
		if (!corpusOccurences.containsKey(idCorpus))
			corpusOccurences.put(idCorpus, 1);
		else
			corpusOccurences.put(idCorpus, corpusOccurences.get(idCorpus) + 1);
		if (!docOccurences.containsKey(idDoc))
			docOccurences.put(idDoc, 1);
		else
			docOccurences.put(idDoc, docOccurences.get(idDoc) + 1);
	}

	public double getTfDocument(int idDoc) {
		return (double) docOccurences.get(idDoc);
	}

	public double getTfCorpus(int idCorpus) {
		return (double) corpusOccurences.get(idCorpus);
	}

	public int getNbOccurence() {
		return nbOccurence;
	}

	public double getIdf(int nbDocument) {
		return Math.log(nbDocument / getNbDocumentWithWordSeen());
	}

	public double getTf() {
		return (double) getNbOccurence();
	}

	public int getNbDocumentWithWordSeen() {
		return nbDocumentWithWordSeen + docOccurences.size();
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		WordIndex wi = (WordIndex) o;
		return iD == wi.getiD();
	}

	@Override
	public String toString() {
		return word;
	}

	@Override
	public int compareTo(WordIndex o) {
		return this.getiD().compareTo(o.getiD());
	}
}
