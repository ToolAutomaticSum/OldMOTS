package tools.wordFilters;

import textModeling.WordModel;

public class WordCatFilter extends WordFilter {
	
	public WordCatFilter ()
	{}
	
	@Override
	public boolean passFilter(WordModel w) {
		if (w.getmPosTag().equals("DET") || w.getmPosTag().equals("PRP") || w.getmPosTag().equals("PRO"))
			return false;
		return true;
	}

}