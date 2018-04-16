package main.java.liasd.asadera.model.task.process.indexBuilder.ILP;

import java.util.Map;
import java.util.Set;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;

public interface SentenceNGramBasedIn {
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences);
}
