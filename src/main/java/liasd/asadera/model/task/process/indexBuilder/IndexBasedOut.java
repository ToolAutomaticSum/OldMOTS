package main.java.liasd.asadera.model.task.process.indexBuilder;

import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public interface IndexBasedOut<T extends WordIndex> {
	public Index<T> getIndex();
}
