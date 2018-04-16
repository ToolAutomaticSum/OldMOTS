package main.java.liasd.asadera.tools.wordFilters;

import main.java.liasd.asadera.textModeling.WordModel;

public class TrueFilter extends WordFilter {

	@Override
	public boolean passFilter(WordModel w) {
		return true;
	}

}
