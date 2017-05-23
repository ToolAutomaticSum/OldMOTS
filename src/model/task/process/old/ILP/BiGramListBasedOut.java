package model.task.process.old.ILP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import textModeling.wordIndex.NGram;

public interface BiGramListBasedOut {

	public TreeMap<NGram, Integer> getBiGramsIds();

	public HashMap<NGram, Double> getBiGramWeights();

	public ArrayList<TreeSet<NGram>> getBiGramsInSentence();
	// public ArrayList<NGram> getBiGrams();
}
