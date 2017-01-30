package model.task.scoringMethod.ILP;

import java.util.ArrayList;

import model.task.process.ILP.NGram;

public interface BiGramListBasedIn {

	public void setBiGramWeights(ArrayList<Double> bigram_weights);
	public void setBiGramsInSentence(ArrayList<ArrayList<Integer>> bigrams_in_sentence);
	public void setBiGrams(ArrayList<NGram> bigrams);
}
