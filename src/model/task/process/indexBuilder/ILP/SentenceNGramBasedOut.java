package model.task.process.indexBuilder.ILP;

import java.util.List;
import java.util.Map;

import textModeling.SentenceModel;
import textModeling.wordIndex.NGram;

public interface SentenceNGramBasedOut {
	public Map<SentenceModel, List<NGram>> getSentenceNGramList();
}
