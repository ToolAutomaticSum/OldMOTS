package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.NGram;

/**
 * 
 * @author aurelien
 * Computes a diversity score based on the number of different ngrams divided by the total number of ngrams
 */
public class SimpleNGramDiversity extends GeneticIndividualScorer{
	
	public SimpleNGramDiversity(HashMap <GeneticIndividualScorer, Double> scorers, Corpus corpus, InvertedIndex invertedIndex, Index index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, corpus, null, index, null, null, null, window, null);
	}
	
	@Override
	public double computeScore(GeneticIndividual gi) {
		ArrayList<NGram> curr_ngram_list;
		TreeSet<NGram> total_ngram_set = new TreeSet<NGram> ();
		int total_number_of_ngrams = 0;
		
		for (SentenceModel p : gi.getGenes())
		{
			curr_ngram_list = p.getNGrams(this.window, this.index);
			total_number_of_ngrams += curr_ngram_list.size();
			total_ngram_set.addAll(curr_ngram_list);
		}
		
		return (double)total_ngram_set.size() / (double) total_number_of_ngrams;
	}

	
	
	
	
	
}
