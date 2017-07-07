package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;

public class ScorerWeightedSum extends GeneticIndividualScorer {
	
	private HashMap <GeneticIndividualScorer, Double> scorers;
	
	public ScorerWeightedSum(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex invertedIndex, Index index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(scorers, ss, corpus, null, null, null, null, null, null, null);
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