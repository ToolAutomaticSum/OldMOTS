package main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class CosDivScorer extends GeneticIndividualScorer {

	private CosineScorer cosScorer;
	private DiversityScorer divScorer;

	public CosDivScorer(HashMap<GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus,
			InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta,
			Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) throws LacksOfFeatures {
		super(null, ss, corpus, invertedIndex, index, divWeight, null, null, null, null);
		this.cosScorer = new CosineScorer(null, ss, corpus, this.invertedIndex, this.index, null, null, null, null,
				null);
		this.divScorer = new DiversityScorer(null, ss, corpus, this.invertedIndex, this.index, null, null, null, null,
				null);
		if (divWeight == null)
			throw new LacksOfFeatures("Need feature DivWeight.");
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		return (1 - this.divWeight) * cosScorer.computeScore(gi) + this.divWeight * divScorer.computeScore(gi);
	}

}
