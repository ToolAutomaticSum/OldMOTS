package model.task.process.indexBuilder.ILP;

import java.util.Map;
import java.util.Set;

import textModeling.SentenceModel;
import textModeling.wordIndex.NGram;

public interface SentenceNGramBasedOut {
	public Map<SentenceModel, Set<NGram>> getSentenceNGramList();
}
