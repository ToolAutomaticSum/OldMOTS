package model.task.process.indexBuilder;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;

public interface IndexBasedIn<T extends WordIndex> {
	public void setIndex(Index<T> index);
}
