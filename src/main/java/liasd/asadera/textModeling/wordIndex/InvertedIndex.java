package main.java.liasd.asadera.textModeling.wordIndex;

import java.util.ArrayList;
import java.util.HashMap;

public class InvertedIndex<T extends WordIndex> {

	// List of indexKey of every Cluster
	/**
	 * iDCorpus, List of WordIndex in Corpus
	 */
	private HashMap<Integer, ArrayList<T>> corpusWordIndex;

	public HashMap<Integer, ArrayList<T>> getCorpusWordIndex() {
		return this.corpusWordIndex;
	}

	public InvertedIndex(Index<T> dico) {
		ArrayList<T> currWordList;
		this.corpusWordIndex = new HashMap<Integer, ArrayList<T>>();
		for (T indexKey : dico.values()) {
			for (Integer corpusId : indexKey.getCorpusOccurences().keySet()) {
				if (this.corpusWordIndex.containsKey(corpusId)) {
					currWordList = this.corpusWordIndex.get(corpusId);
					if (!currWordList.contains(indexKey))
						currWordList.add(indexKey);
				} else {
					currWordList = new ArrayList<T>();
					currWordList.add(indexKey);
					this.corpusWordIndex.put(corpusId, currWordList);
				}
			}
		}
	}

}
