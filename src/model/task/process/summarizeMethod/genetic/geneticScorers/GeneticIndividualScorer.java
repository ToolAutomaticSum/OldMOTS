package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.HashMap;

import model.Model;
import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.InvertedIndex;

public abstract class GeneticIndividualScorer {
	protected HashMap<Integer, String> hashMapWord;
	protected InvertedIndex invertedIndex;
	protected Dictionnary index;
	protected Double divWeight;
	protected Corpus cd;
	protected Double delta;
	protected Double firstSentenceConceptsFactor;
	protected HashMap <GeneticIndividualScorer, Double> scorers;
	protected Integer window;
	protected Double fsc_factor;
	
	public GeneticIndividualScorer(HashMap <GeneticIndividualScorer, Double> scorers, Corpus corpus, HashMap<Integer, String> hashMapWord, InvertedIndex invertedIndex, Dictionnary index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		this.index = index;
		this.invertedIndex = invertedIndex;
		this.hashMapWord = hashMapWord;
		this.divWeight = divWeight;
		this.delta = delta;
		this.cd = corpus;
		this.firstSentenceConceptsFactor = firstSentenceConceptsFactor;
		if (scorers != null)
			this.scorers = new HashMap <GeneticIndividualScorer, Double> (scorers);
		this.window = window;
		this.fsc_factor = fsc_factor;
	}
	
	public abstract double computeScore(GeneticIndividual gi);
}
