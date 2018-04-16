package main.java.liasd.asadera.textModeling.smoothings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

import java.util.TreeMap;

public class GoodTuring extends Smoothing {

	private TreeMap<NGram, Double> distrib;
	private int window;
	private HashMap<Integer, Double> Nr;
	private double total_observed;

	public GoodTuring(int window, double delta, int vocab_card, ArrayList<SentenceModel> sentences,
			Index<WordIndex> index) {
		super(sentences, vocab_card, index);
		this.window = window;

		this.buildDistrib();
		this.buildNr();
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
			}
		}
	}

	private void buildNr() {
		this.Nr = new HashMap<Integer, Double>();
		double curr_nb_occ;
		double max_occ = 0.;
		this.total_observed = 0;
		this.Nr = new HashMap<Integer, Double>();

		for (Entry<NGram, Double> entry : this.distrib.entrySet()) {
			curr_nb_occ = entry.getValue();
			if (curr_nb_occ >= max_occ) {
				max_occ = curr_nb_occ;
			}
			if (this.Nr.containsKey((int) curr_nb_occ)) {
				this.Nr.put((int) curr_nb_occ, this.Nr.get((int) curr_nb_occ) + 1);
			} else {
				this.Nr.put((int) curr_nb_occ, 1.);
			}
		}

		System.out.println("Nr : ");

		for (int i = 1; i <= max_occ + 1; i++) {
			if (this.Nr.containsKey(i)) {
				this.Nr.put(i, this.Nr.get(i) + 1);
				// System.out.println(i+"|" + this.Nr.get(i));
				this.total_observed += this.Nr.get(i);
			} else {
				this.Nr.put(i, 1.);
				// System.out.println(i+"|" + this.Nr.get(i));
				this.total_observed++;
			}
		}

	}

	@Override
	public double getSmoothedProb(NGram ng) {
		if (!this.distrib.containsKey(ng)) {
			double prob = this.Nr.get(1) / this.total_observed;
			// System.out.println(prob);
			return prob;
		}

		double nb_occ = this.distrib.get(ng);
		// System.out.println(nb_occ);
		double prob = (nb_occ + 1) * (this.Nr.get((int) nb_occ + 1) / this.total_observed) / this.total_observed;
		// System.out.println(prob);
		return prob;

	}

}
