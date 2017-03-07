package model.task.process.ILP;

import java.util.ArrayList;

import textModeling.wordIndex.NGram;

public interface BiGramListBasedOut {

	public ArrayList<Double> getBiGramWeights();
	public ArrayList<ArrayList<Integer>> getBiGramsInSentence();
	public ArrayList<NGram> getBiGrams();
}
