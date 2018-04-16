package main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class SimpleNGramScorer extends GeneticIndividualScorer {

	private TreeMap<NGram, Double> nGram_weights;

	public SimpleNGramScorer(HashMap<GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss,
			Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight,
			Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, null, index, null, null, null, window, fsc_factor);
		this.constructNGramWeights();
	}

	private void constructNGramWeights() {
		this.nGram_weights = new TreeMap<NGram, Double>();

		for (TextModel doc : this.cd) {
			TreeSet<NGram> curr_doc_nGram = new TreeSet<NGram>();
			for (SentenceModel p : doc) {
				TreeSet<NGram> curr_phrase_ngram_list = new TreeSet<NGram>();
				for (WordIndex wi : p.getListWordIndex(2))
					curr_phrase_ngram_list.add((NGram) wi);
				if (doc.indexOf(p) == 1) {
					for (NGram ng : curr_phrase_ngram_list) {
						if (this.nGram_weights.containsKey(ng)) {
							this.nGram_weights.put(ng, this.nGram_weights.get(ng) + (1. + this.fsc_factor));
						}
						else {
							this.nGram_weights.put(ng, 1.);
						}
					}
				} else
					curr_doc_nGram.addAll(curr_phrase_ngram_list);
			}
			for (NGram ng : curr_doc_nGram) {
				if (this.nGram_weights.containsKey(ng)) {
					this.nGram_weights.put(ng, this.nGram_weights.get(ng) + 1);
				}
				else {
					this.nGram_weights.put(ng, 1.);
				}
			}
		}
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		TreeSet<NGram> gi_ngrams = new TreeSet<NGram>();

		for (SentenceModel p : gi.getGenes()) {
			ArrayList<NGram> curr_sentence_ngram_list = new ArrayList<NGram>();
			for (WordIndex wi : p.getListWordIndex(2))
				curr_sentence_ngram_list.add((NGram) wi);
			gi_ngrams.addAll(curr_sentence_ngram_list);
		}

		int score = 0;

		for (NGram ng : gi_ngrams) {
			score += this.nGram_weights.get(ng);
		}

		return score;
	}

}
