package main.java.liasd.asadera.textModeling.smoothings;

import java.util.ArrayList;
import java.util.Map.Entry;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

import java.util.TreeMap;

public class Interpolation extends Smoothing {

	private ArrayList<TreeMap<NGram, Double>> probs;
	private ArrayList<Double> alphas;
	private ArrayList<SentenceModel> sentences;
	private int maxN;

	public Interpolation(int maxN, ArrayList<Double> alphas, ArrayList<SentenceModel> sentences,
			Index<WordIndex> index) {
		super(sentences, 0, index);
		this.alphas = alphas;
		this.sentences = sentences;
		this.maxN = maxN;
		this.constructProbs();
	}

	private void constructProbs() {
		this.summNbTokens = 0;
		this.probs = new ArrayList<TreeMap<NGram, Double>>();
		double summOccBiggerGram = 0.;
		for (int i = maxN - 1; i >= 0; i--) {
			TreeMap<NGram, Double> curr_distrib_n = new TreeMap<NGram, Double>();
			for (SentenceModel sent : this.sentences) {
				ArrayList<NGram> curr_ngrams_list = new ArrayList<NGram>();
				for (WordIndex wi : sent.getListWordIndex(i + 1))
					curr_ngrams_list.add((NGram) wi);

				for (NGram ng : curr_ngrams_list) {
					/*
					 * We filter the sourceDistribution upon every NGram occurrence, so we have to
					 * check if this ngram belongs to the sourceDistribution if we want parallel
					 * lists
					 */
					if (curr_distrib_n.containsKey(ng)) {
						curr_distrib_n.put(ng, curr_distrib_n.get(ng) + 1.);
					} else {
						curr_distrib_n.put(ng, 1.);
					}
					if (i == maxN - 1) {
						summOccBiggerGram++;
					}
				}

			}
			this.summNbTokens = (int) summOccBiggerGram;
			for (Entry<NGram, Double> entry : curr_distrib_n.entrySet()) {
				entry.setValue(entry.getValue() / summOccBiggerGram);
			}
			this.probs.add(0, curr_distrib_n);
		}
	}

	@Override
	public double getSmoothedProb(NGram ng) {
		double smoothed_prob = 0.;

		NGram ng_copy = new NGram(ng);

		for (int i = this.maxN - 1; i >= 0; i--) {
			if (this.probs.get(i).containsKey(ng_copy)) {
				smoothed_prob += this.alphas.get(i + 1) * this.probs.get(i).get(ng_copy);
			}
			ng_copy.removeFirstGram();
		}
		smoothed_prob += this.alphas.get(0) / this.vocab_card;
		return smoothed_prob;
	}
}
