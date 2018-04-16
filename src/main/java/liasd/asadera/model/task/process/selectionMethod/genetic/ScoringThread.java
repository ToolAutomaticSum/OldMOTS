package main.java.liasd.asadera.model.task.process.selectionMethod.genetic;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class ScoringThread extends Thread implements Runnable {
	private GeneticIndividual gi;
	private Map<WordIndex, Double> sourceDistribution;
	private Map<WordIndex, Integer> firstSentencesConcepts;
	private double delta;
	private double fsc;

	public ScoringThread(GeneticIndividual gi, Map<WordIndex, Double> sourceDistribution2,
			Map<WordIndex, Integer> firstSentencesConcepts2, Index<WordIndex> index, double fsc, double delta) {
		this.gi = gi;
		this.sourceDistribution = sourceDistribution2;
		this.firstSentencesConcepts = firstSentencesConcepts2;
		this.fsc = fsc;
		this.delta = delta;
	}

	public void run() {
		double js = jensenShannon();
		gi.setScore(js);
	}

	private double jensenShannon() {
		double divergence = 0.;

		TreeMap<WordIndex, Double> distribGI;
		distribGI = computeNGram_GI_distrib();

		double divider;
		double probSumm;
		double probSource;
		double log2 = Math.log(2);

		for (WordIndex ng : this.sourceDistribution.keySet()) {
			probSource = this.sourceDistribution.get(ng);

			probSumm = distribGI.get(ng);

			divider = (probSource + probSumm) / 2.;

			divergence += probSumm * Math.log(probSumm / divider) / log2;
			divergence += probSource * Math.log(probSource / divider) / log2;
		}
		return 1. - divergence / 2.;
	}

	private TreeMap<WordIndex, Double> computeNGram_GI_distrib() {
		TreeMap<WordIndex, Double> distrib = new TreeMap<WordIndex, Double>();
		double nb_bi_grams_gi = 0.;
		for (SentenceModel p : this.gi.getGenes()) {
			Set<WordIndex> curr_ngrams_list = new LinkedHashSet<WordIndex>(p);
			for (WordIndex ng : curr_ngrams_list) {
				if (distrib.containsKey(ng)) {
					distrib.put(ng, distrib.get(ng) + 1.);
				} else {
					distrib.put(ng, 1.);
				}
				nb_bi_grams_gi += 1.;
			}
		}
		for (WordIndex ng : distrib.keySet()) {
			if (this.firstSentencesConcepts.containsKey(ng)) {
				double d = distrib.get(ng);
				double dMult = d * fsc;
				nb_bi_grams_gi += dMult;
				d += dMult;
				distrib.put(ng, d);

			}
		}
		
		double divider = nb_bi_grams_gi + this.delta;

		for (WordIndex ng : this.sourceDistribution.keySet()) {
			double probSource = this.sourceDistribution.get(ng);
			Double dProb = distrib.get(ng);
			if (dProb == null)
				dProb = 0.;
			distrib.put(ng, (dProb + this.delta * probSource) / divider);
		}

		return distrib;
	}

}
