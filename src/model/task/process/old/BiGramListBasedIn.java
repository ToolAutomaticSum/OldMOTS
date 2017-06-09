package model.task.process.scoringMethod.ILP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import textModeling.wordIndex.NGram;

public interface BiGramListBasedIn {

	public void setBiGramsIds(TreeMap<NGram, Integer> bigrams_ids);
	public void setBiGramWeights(HashMap<NGram, Double> bigram_weights);
	public void setBiGramsInSentence(ArrayList<TreeSet<NGram>> bigrams_in_sentence);
	//public void setBiGrams(ArrayList<NGram> bigrams);
}
