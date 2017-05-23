package textModeling.wordIndex;

import java.util.ArrayList;
import java.util.HashMap;

public class InvertedIndex {
	
	//List of indexKey of every Cluster
	/**
	 * iDCorpus, List of WordIndex in Corpus
	 */
	private HashMap <Integer, ArrayList<WordIndex>> corpusWordIndex;
	
	public HashMap <Integer, ArrayList<WordIndex>> getCorpusWordIndex()
	{
		return this.corpusWordIndex;
	}
	
	public InvertedIndex (Index<WordIndex> dico)
	{
		ArrayList<WordIndex> currWordList;
		this.corpusWordIndex = new HashMap <Integer, ArrayList<WordIndex>>();
		for (WordIndex indexKey : dico.values())
		{
			for (Integer corpusId : indexKey.getCorpusOccurences().keySet()) {
				if (this.corpusWordIndex.containsKey(corpusId))
				{
					currWordList = this.corpusWordIndex.get(corpusId);
					if (!currWordList.contains(indexKey))
						currWordList.add(indexKey);
				}
				else
				{
					currWordList = new ArrayList<WordIndex>();
					currWordList.add(indexKey);
					this.corpusWordIndex.put(corpusId, currWordList);
				}
			}
		}
	}
	
}
