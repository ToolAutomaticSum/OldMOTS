package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.HashMap;
import java.util.Map.Entry;

import model.Model;
import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.InvertedIndex;

public class ScorerWeightedSum extends GeneticIndividualScorer {
	
	private HashMap <GeneticIndividualScorer, Double> scorers;
	
	public ScorerWeightedSum(HashMap <GeneticIndividualScorer, Double> scorers, Corpus corpus, HashMap<Integer, String> hashMapWord, InvertedIndex invertedIndex, Dictionnary index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(scorers, null, null, null, null, null, null, null, null, null);
	}
	
	
	@Override
	public double computeScore(GeneticIndividual gi) {
		double sum = 0;
		
		for (Entry <GeneticIndividualScorer, Double> scorer : this.scorers.entrySet())
		{
			sum += scorer.getValue() * scorer.getKey().computeScore(gi);
		}
		
		return sum;
	}

}
