package main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.smoothings.Interpolation;
import main.java.liasd.asadera.textModeling.smoothings.Smoothing;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class JSInterpolation extends GeneticIndividualScorer {

	private TreeMap<NGram, Double> sourceDistribution;
	private TreeMap<NGram, Integer> sourceOccurences;
	private int nbBiGramsInSource;
	private Smoothing smoothing;

	public JSInterpolation(HashMap<GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus,
			InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta,
			Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, invertedIndex, index, null, delta, null, null, null);
	}

	public void init() {
		this.ss = new ArrayList<SentenceModel>();
		this.computeSourceDistribution();
	}

	private void computeSourceDistribution() {
		this.sourceDistribution = new TreeMap<NGram, Double>();
		this.sourceOccurences = new TreeMap<NGram, Integer>();
		this.nbBiGramsInSource = 0;
		for (TextModel doc : this.cd) {
			for (SentenceModel p : doc) {
				ArrayList<NGram> curr_ngrams_list = new ArrayList<NGram>();
				for (WordIndex wi : p.getListWordIndex(2))
					curr_ngrams_list.add((NGram) wi);
				for (NGram ng : curr_ngrams_list) {
					if (this.sourceOccurences.containsKey(ng))
						this.sourceOccurences.put(ng, this.sourceOccurences.get(ng) + 1);
					else
						this.sourceOccurences.put(ng, 1);
					this.nbBiGramsInSource++;
				}
				this.ss.add(p);
			}
		}
		System.out.println(" Numbre of bigrams : " + this.nbBiGramsInSource);
		System.out.println(" Numbre of bigrams after filtering : " + this.nbBiGramsInSource);
		for (NGram ng : this.sourceOccurences.keySet()) {
			this.sourceDistribution.put(ng, (double) this.sourceOccurences.get(ng) / (double) this.nbBiGramsInSource);
		}

	}

	private double jensenShanonDivergence(GeneticIndividual gi, TreeMap<NGram, Double> summDistrib, int summNbTokens) {
		double divergence = 0;
		double divider = summNbTokens + this.delta;
		
		double probSumm;
		double probSource;
		double log2 = Math.log(2);
		double sourceOp;
		double summOp;

		for (NGram ng : this.sourceDistribution.keySet()) {
			probSource = this.sourceDistribution.get(ng);

			probSumm = this.smoothing.getSmoothedProb(ng);

			divider = probSource + probSumm;
			sourceOp = 2 * probSource / divider;
			summOp = 2 * probSumm / divider;

			divergence += probSumm * Math.log(summOp) / log2;
			divergence += probSource * Math.log(sourceOp) / log2;
		}

		return 1 - divergence / 2;
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		init();

		TreeMap<NGram, Double> summDistrib = new TreeMap<NGram, Double>();

		ArrayList<Double> alphas = new ArrayList<Double>();
		alphas.add(0.000001);
		alphas.add(0.998);
		alphas.add(0.001999);

		this.smoothing = new Interpolation(2, alphas, gi.getGenes(), this.index);

		double jsd = this.jensenShanonDivergence(gi, summDistrib, this.smoothing.getSummNbTokens());

		return jsd;
	}

}
