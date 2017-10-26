package liasd.asadera.model.task.process.indexBuilder;

import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public interface IndexBasedOut<T extends WordIndex> {
	public Index<T> getIndex();
}
