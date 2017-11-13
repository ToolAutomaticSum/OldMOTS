package liasd.asadera.tools.wordFilters;

import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class WordIdfFilter extends WordFilter {

	private double threshold;
	private Index<WordIndex> dico;
	
	public WordIdfFilter (Index<WordIndex> dico, double absoluteThreshold) throws Exception
	{
		if (dico.values().toArray()[0].getClass() == WordIndex.class) {
			this.threshold = absoluteThreshold;
			this.dico = dico;
		}
		else
			throw new Exception("WordIdfFilter need TfIdf process !");
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean passFilter(WordModel w) {
		
		if (((WordIndex)this.dico.get(w.getmLemma())).getIdf() > this.threshold)
			return true;
		return false;
	}
	
}
