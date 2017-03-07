package textModeling.smoothings;

import java.util.ArrayList;
import java.util.TreeMap;

import textModeling.SentenceModel;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.NGram;

public class DirichletUniqueProbSmoothing extends Smoothing{

	private TreeMap <NGram, Double> distrib;
	private TreeMap <NGram, Double> corpusDistrib;
	private TreeMap <NGram, Integer> firstSentencesConcepts;
	private double delta;
	private int window;
	private double ngram_total_occs;
	private double firstSentenceConceptsFactor;
	
	public DirichletUniqueProbSmoothing (int window, double delta, int vocab_card, ArrayList<SentenceModel> sentences, 
			Dictionnary index, TreeMap <NGram, Double> corpusDistrib, TreeMap <NGram, Integer> firstSentencesConcepts,
			double firstSentenceConceptsFactor)
	{
		super (sentences, vocab_card, index);
		this.window = window;
		this.delta = delta;
		this.ngram_total_occs = 0.;
		this.corpusDistrib = corpusDistrib;
		this.firstSentencesConcepts = firstSentencesConcepts;
		this.firstSentenceConceptsFactor = firstSentenceConceptsFactor;
		
		this.buildDistrib();
		
	}
	
	private void buildDistrib()
	{
		double log2 = Math.log(2.);
		this.distrib = new TreeMap <NGram, Double> ();
		for (SentenceModel sent : this.sentences)
		{
			//ArrayList <NGram> curr_ngrams_list = sent.getBiGrams(this.index, this.filter);
			ArrayList <NGram> curr_ngrams_list = sent.getNGrams(this.window, this.index);
			for (NGram ng : curr_ngrams_list)
			{
				/*We filter the sourceDistribution upon every NGram occurrence, so we have to check if this 
				ngram belongs to the sourceDistribution if we want parallel lists*/
				//if (this.sourceDistribution.containsKey(ng))
				//{
					if (this.distrib.containsKey(ng))
					{
			//			this.distrib.put(ng, this.distrib.get(ng) + 1.);
			//			this.ngram_total_occs ++;
					}
					else
					{
						this.distrib.put(ng, 1.);
						this.ngram_total_occs ++;
					}
					
				//}
			}
		}
		
		/*this.ngram_total_occs = 0.;
		
		for (NGram ng : this.distrib.keySet())
		{
			double new_count = Math.log(this.distrib.get(ng) + 1.) / log2;
			this.distrib.put(ng, new_count);
			this.ngram_total_occs += new_count;
		}*/
		
		
		
		
		
		
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
		// TODO Auto-generated method stub
		Double dProb = this.distrib.get(ng);
		double probSource = this.corpusDistrib.get(ng);
		double divider = this.ngram_total_occs + this.delta;
		
		dProb = dProb== null ? this.delta * probSource / divider : (dProb + this.delta * probSource) / divider;
		return dProb;
	}
	

}
