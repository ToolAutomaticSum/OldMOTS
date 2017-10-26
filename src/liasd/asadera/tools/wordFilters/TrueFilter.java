package liasd.asadera.tools.wordFilters;

import liasd.asadera.textModeling.WordModel;

public class TrueFilter extends WordFilter {

	@Override
	public boolean passFilter(WordModel w) {
		return true;
	}

}
