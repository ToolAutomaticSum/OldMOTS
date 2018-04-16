package main.java.liasd.asadera.textModeling.smoothings;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class DirichletSmoothing extends Smoothing {

	private Map<WordIndex, Double> distrib;
	private Map<WordIndex, Double> corpusDistrib;
	private Map<WordIndex, Integer> firstSentencesConcepts;
	private double delta;
	private double ngram_total_occs;
	private double firstSentenceConceptsFactor;

	public DirichletSmoothing(int window, double delta, int vocab_card, List<SentenceModel> sentences,
			Map<WordIndex, Double> corpusDistrib, Map<WordIndex, Integer> firstSentencesConcepts2,
			double firstSentenceConceptsFactor) {
		super(sentences, vocab_card, null);
		this.delta = delta;
		this.ngram_total_occs = 0.;
		this.corpusDistrib = corpusDistrib;
		this.firstSentencesConcepts = firstSentencesConcepts2;
		this.firstSentenceConceptsFactor = firstSentenceConceptsFactor;

		buildDistrib();
	}

	private void buildDistrib() {
		distrib = new TreeMap<WordIndex, Double>();
		for (SentenceModel sent : sentences) {
			Set<WordIndex> curr_ngrams_list = new LinkedHashSet<WordIndex>(sent);
			for (WordIndex ng : curr_ngrams_list) {
				/*
				 * We filter the sourceDistribution upon every NGram occurrence, so we have to
				 * check if this ngram belongs to the sourceDistribution if we want parallel
				 * lists
				 */
				if (distrib.containsKey(ng))
					distrib.put(ng, distrib.get(ng) + 1.);
				else
					distrib.put(ng, 1.);
				ngram_total_occs++;
			}
		}

		if (this.firstSentencesConcepts != null) {
			for (WordIndex ng : distrib.keySet()) {
				if (this.firstSentencesConcepts.containsKey(ng)) {
					double d = distrib.get(ng);
					ngram_total_occs += firstSentenceConceptsFactor * d;
					distrib.put(ng, d + firstSentenceConceptsFactor * d);
				}
			}
		}
	}

	@Override
	public double getSmoothedProb(NGram ng) {
		Double dProb = distrib.get(ng);
		double probSource = corpusDistrib.get(ng);
		double divider = ngram_total_occs + delta;

		dProb = (dProb == null) ? delta * probSource / divider : (dProb + delta * probSource) / divider;
		return dProb;
	}

	public double getSmoothedProb(WordIndex ng) {
		Double dProb = distrib.get(ng);
		double probSource = corpusDistrib.get(ng);
		double divider = ngram_total_occs + delta;

		dProb = (dProb == null) ? delta * probSource / divider : (dProb + delta * probSource) / divider;
		return dProb;
	}

	public double[] getSmoothedDistrib() {
		double[] distri = new double[corpusDistrib.size()];
		int i = 0;
		for (WordIndex ng : corpusDistrib.keySet()) {
			distri[i] = getSmoothedProb(ng);
			i++;
		}
		return distri;
	}
}
