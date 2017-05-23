package model.task.process.indexBuilder;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;

public interface IndexBasedOut<T extends WordIndex> {
	public Index<T> getIndex();
}
