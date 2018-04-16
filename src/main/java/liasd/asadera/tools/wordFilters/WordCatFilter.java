package main.java.liasd.asadera.tools.wordFilters;

import main.java.liasd.asadera.textModeling.WordModel;

public class WordCatFilter extends WordFilter {

	public WordCatFilter() {
	}

	@Override
	public boolean passFilter(WordModel w) {
		if (w.getmPosTag().equals("DET") || w.getmPosTag().equals("PRP") || w.getmPosTag().equals("PRO"))
			return false;
		return true;
	}

}