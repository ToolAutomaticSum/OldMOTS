package liasd.asadera.textModeling.smoothings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;

public class DirichletSmoothing extends Smoothing {

	private TreeMap <NGram, Double> distrib;
	private TreeMap <NGram, Double> corpusDistrib;
	private TreeMap <NGram, Integer> firstSentencesConcepts;
	private double delta;
	//private int window;
	private double ngram_total_occs;
	private double firstSentenceConceptsFactor;
	private HashMap<SentenceModel, ArrayList<NGram>> ngrams_in_sentences;
	
	public DirichletSmoothing (int window, double delta, int vocab_card, HashMap<SentenceModel, ArrayList<NGram>> ngrams_in_sentences, ArrayList<SentenceModel> sentences, 
			Index index, TreeMap <NGram, Double> corpusDistrib, TreeMap <NGram, Integer> firstSentencesConcepts2,
			double firstSentenceConceptsFactor)
	{
		super (sentences, vocab_card, index);
		//this.window = window;
		this.delta = delta;
		this.ngrams_in_sentences = ngrams_in_sentences;
		this.ngram_total_occs = 0.;
		this.corpusDistrib = corpusDistrib;
		this.firstSentencesConcepts = firstSentencesConcepts2;
		this.firstSentenceConceptsFactor = firstSentenceConceptsFactor;
		
		this.buildDistrib();
		
	}
	
	private void buildDistrib()
	{
		this.distrib = new TreeMap <NGram, Double> ();
		for (SentenceModel sent : this.sentences)
		{
			//ArrayList <NGram> curr_ngrams_list = sent.getBiGrams(this.index, this.filter);
			ArrayList <NGram> curr_ngrams_list = ngrams_in_sentences.get(sent);
			for (NGram ng : curr_ngrams_list)
			{
				/*We filter the sourceDistribution upon every NGram occurrence, so we have to check if this 
				ngram belongs to the sourceDistribution if we want parallel lists*/
				//if (this.sourceDistribution.containsKey(ng))
				//{
					if (this.distrib.containsKey(ng))
					{
						this.distrib.put(ng, this.distrib.get(ng) + 1.);
					}
					else
					{
						this.distrib.put(ng, 1.);
					}
					this.ngram_total_occs ++;
				//}
			}
		}
		
		if ( this.firstSentencesConcepts != null )
		{
			for (NGram ng : this.distrib.keySet())
			{
				if (this.firstSentencesConcepts.containsKey(ng))
				{
					double d = this.distrib.get(ng);
					this.ngram_total_occs += this.firstSentenceConceptsFactor * d;
					this.distrib.put(ng, d + this.firstSentenceConceptsFactor * d);
				}
			}
		}

	}
	
	
	@Override
	public double getSmoothedProb(NGram ng) {
		Double dProb = this.distrib.get(ng);
		double probSource = this.corpusDistrib.get(ng);
		double divider = this.ngram_total_occs + this.delta;
		
		dProb = (dProb== null) ? this.delta * probSource / divider : (dProb + this.delta * probSource) / divider;
		return dProb;
	}
	

}
