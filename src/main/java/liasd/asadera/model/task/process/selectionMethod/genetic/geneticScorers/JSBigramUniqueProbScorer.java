package main.java.liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import main.java.liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.smoothings.DirichletUniqueProbSmoothing;
import main.java.liasd.asadera.textModeling.smoothings.Smoothing;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.InvertedIndex;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class JSBigramUniqueProbScorer extends GeneticIndividualScorer {

	private TreeMap<NGram, Double> sourceDistribution;
	private TreeMap<NGram, Double> sourceOccurences;
	private TreeMap<NGram, Integer> firstSentencesConcepts;
	private int nbBiGramsInSource;
	private Smoothing smoothing;
	private HashMap<SentenceModel, ArrayList<NGram>> ngrams_in_sentences;

	public JSBigramUniqueProbScorer(HashMap<GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss,
			Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight,
			Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, null, index, null, delta, firstSentenceConceptsFactor, null, null);
	}

	@Override
	public void init() {
		this.computeNGrams_in_sentences();

		this.computeSourceDistribution();
	}

	public void computeNGrams_in_sentences() {
		ngrams_in_sentences = new HashMap<SentenceModel, ArrayList<NGram>>();

		for (SentenceModel p : ss) {
			ArrayList<NGram> list = new ArrayList<NGram>();
			for (WordIndex wi : p.getListWordIndex(2))
				list.add((NGram) wi);
			ngrams_in_sentences.put(p, list);
		}
	}

	/**
	 * Compute the occurences and distribution for the source documents
	 */
	private void computeSourceDistribution() {
		this.sourceDistribution = new TreeMap<NGram, Double>();
		this.sourceOccurences = new TreeMap<NGram, Double>();
		this.firstSentencesConcepts = new TreeMap<NGram, Integer>();
		this.nbBiGramsInSource = 0;
		double modified_nbBiGramsInSource = 0.;
		for (SentenceModel p : this.ngrams_in_sentences.keySet()) {
			ArrayList<NGram> curr_ngrams_list = this.ngrams_in_sentences.get(p);
			for (NGram ng : curr_ngrams_list) {
				if (this.sourceOccurences.containsKey(ng))
					this.sourceOccurences.put(ng, this.sourceOccurences.get(ng) + 1.);
				else {
					this.sourceOccurences.put(ng, 1.);
				}
				this.nbBiGramsInSource++;

				if (p.getText().indexOf(p) == 1) {
					this.firstSentencesConcepts.put(ng, 1);
				}
			}
		}

		for (NGram ng : this.firstSentencesConcepts.keySet()) {
			double d = this.sourceOccurences.get(ng);
			modified_nbBiGramsInSource += this.firstSentenceConceptsFactor * d;
			this.sourceOccurences.put(ng, d + this.firstSentenceConceptsFactor * d);
		}

		modified_nbBiGramsInSource += this.nbBiGramsInSource;

		for (NGram ng : this.sourceOccurences.keySet()) {
			this.sourceDistribution.put(ng, (double) this.sourceOccurences.get(ng) / modified_nbBiGramsInSource);
		}
	}

	private double jensenShanonDivergence(GeneticIndividual gi, TreeMap<NGram, Double> summDistrib)// , int
																									// summNbTokens)
	{
		double divergence = 0;
		double divider;
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

		return 1 - divergence / 2.;
	}

	@Override
	public double computeScore(GeneticIndividual gi) {
		TreeMap<NGram, Double> summDistrib = new TreeMap<NGram, Double>();
		this.smoothing = new DirichletUniqueProbSmoothing(2, this.delta, summDistrib.size(), gi.getGenes(), this.index,
				this.sourceDistribution, this.firstSentencesConcepts, this.firstSentenceConceptsFactor);
	
		double jsd = this.jensenShanonDivergence(gi, summDistrib);

		return jsd;
	}
}