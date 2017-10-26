package liasd.asadera.tools.wordFilters;

import liasd.asadera.textModeling.WordModel;

public class StopWordsFilter extends WordFilter {

	@Override
	public boolean passFilter(WordModel w) {
		return !w.isStopWord();
	}

}
