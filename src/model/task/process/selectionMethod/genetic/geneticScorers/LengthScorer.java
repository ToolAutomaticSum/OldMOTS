package model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;

import model.task.process.selectionMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.WordIndex;


public class LengthScorer extends GeneticIndividualScorer{

	

	public LengthScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
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
