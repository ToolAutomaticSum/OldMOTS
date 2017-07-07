package model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;

import model.task.process.selectionMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.WordIndex;

public abstract class GeneticIndividualScorer {
	protected InvertedIndex<WordIndex> invertedIndex;
	protected Index<WordIndex>  index;
	protected Double divWeight;
	protected Corpus cd;
	protected Double delta;
	protected Double firstSentenceConceptsFactor;
	protected HashMap <GeneticIndividualScorer, Double> scorers;
	protected Integer window;
	protected Double fsc_factor;
	protected ArrayList<SentenceModel> ss;
	
	public GeneticIndividualScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		this.ss = ss;
		this.index = index;
		this.invertedIndex = invertedIndex;
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
	
	public void computeScore(ArrayList<GeneticIndividual> population) {}

	public void init() {
		
	}
}
