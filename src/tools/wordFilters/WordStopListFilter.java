package tools.wordFilters;

import textModeling.WordModel;

public class WordStopListFilter extends WordFilter {

	
	public WordStopListFilter ()
	{}
	
	@Override
	public boolean passFilter(WordModel u) {
		
		if (!u.isStopWord())
			return true;
		return false;
	}
	
}
