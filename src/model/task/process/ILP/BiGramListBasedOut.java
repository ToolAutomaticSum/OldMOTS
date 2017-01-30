package model.task.process.ILP;

import java.util.ArrayList;

public interface BiGramListBasedOut {

	public ArrayList<Double> getBiGramWeights();
	public ArrayList<ArrayList<Integer>> getBiGramsInSentence();
	public ArrayList<NGram> getBiGrams();
}
