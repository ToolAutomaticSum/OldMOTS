package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.HashMap;

import model.Model;
import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.InvertedIndex;


public class LengthScorer extends GeneticIndividualScorer{

	

	public LengthScorer(HashMap <GeneticIndividualScorer, Double> scorers, Corpus corpus, HashMap<Integer, String> hashMapWord, InvertedIndex invertedIndex, Dictionnary index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, null, null, null, null, null, null, null, null, null);
	}

	@Override
	public double computeScore (GeneticIndividual gi) {
		// TODO Auto-generated method stub
		double sum = 0.;
		for ( SentenceModel p : gi.getGenes() )
		{
			sum += p.size();
		}
		return sum;
	}

	
	
}