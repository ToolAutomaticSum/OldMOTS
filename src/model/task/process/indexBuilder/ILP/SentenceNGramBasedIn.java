package model.task.process.indexBuilder.ILP;

import java.util.List;
import java.util.Map;

import textModeling.SentenceModel;
import textModeling.wordIndex.NGram;

public interface SentenceNGramBasedIn {
	public void setSentenceNGram(Map<SentenceModel, List<NGram>> ngrams_in_sentences);
}
