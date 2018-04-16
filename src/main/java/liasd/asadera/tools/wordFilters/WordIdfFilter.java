package main.java.liasd.asadera.tools.wordFilters;

import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class WordIdfFilter extends WordFilter {

	private double threshold;
	private Index<WordIndex> index;

	public WordIdfFilter(Index<WordIndex> dico, double absoluteThreshold) throws Exception {
		if (dico.values().toArray()[0].getClass() == WordIndex.class) {
			this.threshold = absoluteThreshold;
			this.index = dico;
		} else
			throw new Exception("WordIdfFilter need TfIdf process !");
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean passFilter(WordModel w) {

		if (((WordIndex) this.index.get(w.getmLemma())).getIdf(index.getNbDocument()) > this.threshold)
			return true;
		return false;
	}

}
