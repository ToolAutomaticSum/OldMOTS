package main.java.liasd.asadera.textModeling.wordIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InvertedIndex<T extends WordIndex> {

	/**
	 * iDCorpus, List of WordIndex in Corpus
	 */
	private HashMap<Integer, List<T>> corpusWordIndex;

	public HashMap<Integer, List<T>> getCorpusWordIndex() {
		return this.corpusWordIndex;
	}

	public InvertedIndex(Index<T> dico) {
		List<T> currWordList;
		this.corpusWordIndex = new HashMap<Integer, List<T>>();
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
