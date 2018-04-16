package main.java.liasd.asadera.model.task.process.indexBuilder.ILP;

import java.util.Map;
import java.util.Set;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;

public interface SentenceNGramBasedOut {
	public Map<SentenceModel, Set<NGram>> getSentenceNGramList();
}
