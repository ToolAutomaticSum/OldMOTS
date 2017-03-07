package textModeling.wordIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class InvertedIndex {
	
	//List of indexKey of every Cluster
	private HashMap <Integer, ArrayList<WordIndex>> clustWordIndex;
	
	public HashMap <Integer, ArrayList<WordIndex>> getClustWordIndex()
	{
		return this.clustWordIndex;
	}
	
	
	public InvertedIndex (Dictionnary dico)
	{
		ArrayList<WordIndex> currWordList;
		this.clustWordIndex = new HashMap <Integer, ArrayList<WordIndex>>();
		for ( WordIndex indexKey : dico.values())
		{
			WordTF_IDF cti = (WordTF_IDF) indexKey;
			/*for (Integer clustId : cti.getOccurence().keySet())
			{*/
				if (this.clustWordIndex.containsKey(0))
				{
					currWordList = this.clustWordIndex.get(0);
					currWordList.add(indexKey);
				}
				else
				{
					currWordList = new ArrayList<WordIndex>();
					currWordList.add(indexKey);
					this.clustWordIndex.put(0, currWordList);
				}
			//}
		}
	}
	
}
