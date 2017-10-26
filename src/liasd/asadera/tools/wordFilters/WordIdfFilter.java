package liasd.asadera.tools.wordFilters;

import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class WordIdfFilter extends WordFilter {

	private double threshold;
	private Index<WordTF_IDF> dico;
	
	public WordIdfFilter (Index<WordTF_IDF> dico, double absoluteThreshold) throws Exception
	{
		if (dico.values().toArray()[0].getClass() == WordTF_IDF.class) {
			this.threshold = absoluteThreshold;
			this.dico = dico;
		}
		else
			throw new Exception("WordIdfFilter need TfIdf process !");
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean passFilter(WordModel w) {
		
		if (((WordTF_IDF)this.dico.get(w.getmLemma())).getIdf() > this.threshold)
			return true;
		return false;
	}
	
}
