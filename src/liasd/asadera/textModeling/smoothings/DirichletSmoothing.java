package liasd.asadera.textModeling.smoothings;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.NGram;

public class DirichletSmoothing extends Smoothing {

	private Map <NGram, Double> distrib;
	private Map <NGram, Double> corpusDistrib;
	private Map <NGram, Integer> firstSentencesConcepts;
	private double delta;
	//private int window;
	private double ngram_total_occs;
	private double firstSentenceConceptsFactor;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	
	public DirichletSmoothing(int window, double delta, int vocab_card, Map<SentenceModel, Set<NGram>> ngrams_in_sentences, List<SentenceModel> sentences, 
			Map <NGram, Double> corpusDistrib, Map <NGram, Integer> firstSentencesConcepts2,
			double firstSentenceConceptsFactor) {
		super(sentences, vocab_card, null);
		//this.window = window;
		this.delta = delta;
		this.ngrams_in_sentences = ngrams_in_sentences;
		this.ngram_total_occs = 0.;
		this.corpusDistrib = corpusDistrib;
		this.firstSentencesConcepts = firstSentencesConcepts2;
		this.firstSentenceConceptsFactor = firstSentenceConceptsFactor;
		
		buildDistrib();
	}
	
	private void buildDistrib() {
		distrib = new TreeMap <NGram, Double> ();
		for (SentenceModel sent : sentences) {
			//ArrayList <NGram> curr_ngrams_list = sent.getBiGrams(this.index, this.filter);
			Set <NGram> curr_ngrams_list = ngrams_in_sentences.get(sent);
			for (NGram ng : curr_ngrams_list) {
				/*We filter the sourceDistribution upon every NGram occurrence, so we have to check if this 
				ngram belongs to the sourceDistribution if we want parallel lists*/
				//if (this.sourceDistribution.containsKey(ng))
				//{
					if (distrib.containsKey(ng))
						distrib.put(ng, distrib.get(ng) + 1.);
					else
						distrib.put(ng, 1.);
					ngram_total_occs++;
				//}
			}
		}
		
		if (this.firstSentencesConcepts != null ) {
			for (NGram ng : distrib.keySet()) {
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
		
		dProb = (dProb== null) ? delta * probSource / divider : (dProb + delta * probSource) / divider;
		return dProb;
	}
	
	public double[] getSmoothedDistrib() {
		double[] distri = new double[corpusDistrib.size()];
		int i = 0;
		for (NGram ng : corpusDistrib.keySet()) {
			distri[i] = getSmoothedProb(ng);	
			i++;
		}
		return distri;
	}
}
