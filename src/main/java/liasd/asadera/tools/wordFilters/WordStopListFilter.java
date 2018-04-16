package main.java.liasd.asadera.tools.wordFilters;

import main.java.liasd.asadera.textModeling.StopList;
import main.java.liasd.asadera.textModeling.WordModel;

public class WordStopListFilter extends WordFilter {

	private StopList stoplist;

	public WordStopListFilter(String stoplist_file) {
		stoplist = new StopList(stoplist_file);
	}

	@Override
	public boolean passFilter(WordModel w) {
		if (this.stoplist.contains(w.getWord()))
			return false;
		else if (this.stoplist.contains(w.getmLemma()))
			return false;
		else
			return true;
	}
}
