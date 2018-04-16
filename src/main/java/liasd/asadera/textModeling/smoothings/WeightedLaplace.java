package main.java.liasd.asadera.textModeling.smoothings;

import java.util.ArrayList;
import java.util.TreeMap;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class WeightedLaplace extends Smoothing {

	private TreeMap<NGram, Double> distrib;
	private double delta;
	private int window;
	private int ngram_total_occs;

	public WeightedLaplace(int window, double delta, int vocab_card, ArrayList<SentenceModel> sentences,
			Index<WordIndex> index) {
		super(sentences, vocab_card, index);
		this.window = window;
		this.delta = delta;
		this.ngram_total_occs = 0;

		this.buildDistrib();

	}

	private void buildDistrib() {
		this.distrib = new TreeMap<NGram, Double>();
		for (SentenceModel sent : this.sentences) {
			ArrayList<NGram> curr_ngrams_list = new ArrayList<NGram>();
			for (WordIndex wi : sent.getListWordIndex(window))
				curr_ngrams_list.add((NGram) wi);
			for (NGram ng : curr_ngrams_list) {
				/*
				 * We filter the sourceDistribution upon every NGram occurrence, so we have to
				 * check if this ngram belongs to the sourceDistribution if we want parallel
				 * lists
				 */
				if (this.distrib.containsKey(ng)) {
					this.distrib.put(ng, this.distrib.get(ng) + 1.);
				} else {
					this.distrib.put(ng, 1.);
				}
				this.ngram_total_occs++;
			}
		}

	}

	@Override
	public double getSmoothedProb(NGram ng) {
		Double dProb = this.distrib.get(ng);
		double prob;

		prob = dProb == null ? this.delta / (this.ngram_total_occs + this.vocab_card * this.delta)
				: (dProb + this.delta) / (this.ngram_total_occs + this.vocab_card * this.delta);
		return prob;
	}

}
