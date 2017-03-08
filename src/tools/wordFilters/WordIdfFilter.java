package tools.wordFilters;

import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class WordIdfFilter extends WordFilter {

	private double threshold;
	private Index dico;
	
	public WordIdfFilter (Index dico, double absoluteThreshold)
	{
		this.threshold = absoluteThreshold;
		this.dico = dico;
	}
	
	@Override
	public boolean passFilter(WordModel w) {
		
		if (((WordTF_IDF)this.dico.get(w.getmLemma())).getIdf() > this.threshold)
			return true;
		return false;
	}
	
}
