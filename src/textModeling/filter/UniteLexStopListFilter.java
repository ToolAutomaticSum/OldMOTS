package textModeling.filter;

import textModeling.StopList;
import textModeling.WordModel;

public class UniteLexStopListFilter extends UniteLexFilter {

	private StopList stoplist;
	
	public UniteLexStopListFilter (String stoplist_file)
	{
		this.stoplist = new StopList(stoplist_file);
	}
	
	@Override
	public boolean passFilter(WordModel u) {
		if (!this.stoplist.contains(u.getmLemma()) )
			return true;
		return false;
	}
	
}
