package main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class ScorerWeightedSum extends GeneticIndividualScorer {

	private HashMap<GeneticIndividualScorer, Double> scorers;

	public ScorerWeightedSum(HashMap<GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss,
			Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight,
			Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(scorers, ss, corpus, null, null, null, null, null, null, null);
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		double sum = 0;

		for (Entry<GeneticIndividualScorer, Double> scorer : this.scorers.entrySet()) {
			sum += scorer.getValue() * scorer.getKey().computeScore(gi);
		}

		return sum;
	}

}
