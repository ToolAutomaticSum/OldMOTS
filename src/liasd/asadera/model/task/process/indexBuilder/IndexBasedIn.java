package liasd.asadera.model.task.process.indexBuilder;

import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public interface IndexBasedIn<T extends WordIndex> {
	public void setIndex(Index<T> index);
}
