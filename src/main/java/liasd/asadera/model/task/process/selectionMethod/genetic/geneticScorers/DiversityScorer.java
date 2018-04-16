package main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class DiversityScorer extends GeneticIndividualScorer {
	private double maxIdf;

	public DiversityScorer(HashMap<GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus,
			InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> dictionnary, Double divWeight, Double delta,
			Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, invertedIndex, dictionnary, null, null, null, null, null);
	}

	public void init() {
		/*
		 * Finds maxIdf in index for this cluster (for future normalization)
		 */
		this.maxIdf = 0;
		ArrayList<WordIndex> indexKeys = this.invertedIndex.getCorpusWordIndex().get(cd.getiD());
		for (WordIndex key : indexKeys) {
			WordIndex w = (WordIndex) (key);
			double currIdf = w.getIdf(index.getNbDocument());
			if (this.maxIdf < currIdf)
				this.maxIdf = currIdf;
		}
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		init();
		double sum = 0;
		TreeSet<Integer> giIndexKeys = new TreeSet<Integer>();
		int cpt = 0;
		for (SentenceModel p : gi.getGenes()) {
			for (WordIndex u : p) {
				// if (!u.isStopWord()) {
				int uIndexKey = u.getiD(); // index.get(u.getmLemma()).getiD();
				if (!giIndexKeys.contains(uIndexKey)) {
					giIndexKeys.add(uIndexKey);
				}
				cpt++;
				// }
			}

		}
		for (Integer indexKey : giIndexKeys) {
			WordIndex w = (WordIndex) index.get(indexKey);
			sum += (w.getIdf(index.getNbDocument()) / this.maxIdf);
		}

		if (cpt == 0)
			return 0;
		return sum / (double) cpt;

	}

}
