package textModeling.wordIndex;

import java.util.ArrayList;
import java.util.HashMap;

import textModeling.WordModel;

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
	
	public InvertedIndex (Index dico)
	{
		ArrayList<WordIndex> currWordList;
		this.corpusWordIndex = new HashMap <Integer, ArrayList<WordIndex>>();
		for (WordIndex indexKey : dico.values())
		{
			for (WordModel wm : indexKey)
			{
				int corpusId = wm.getSentence().getParagraph().getText().getParentCorpus().getiD();
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
