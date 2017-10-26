package liasd.asadera.model.task.process.indexBuilder.ILP;

import java.util.Map;
import java.util.Set;

import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.NGram;

public interface SentenceNGramBasedOut {
	public Map<SentenceModel, Set<NGram>> getSentenceNGramList();
}
