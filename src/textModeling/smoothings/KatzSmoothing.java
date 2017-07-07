package textModeling.smoothings;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;

public class KatzSmoothing extends Smoothing{

	private ArrayList <TreeMap <NGram, Double>> probs;
	private ArrayList <Double> alphas;
	private ArrayList <SentenceModel> sentences;
	private int maxN;
	
	public KatzSmoothing ( int maxN, ArrayList <Double> alphas, ArrayList <SentenceModel> sentences, Index<WordIndex> index, int vocab_card )
	{
		super (sentences, vocab_card, index);
		this.alphas = alphas;
		//this.index = index;
		this.sentences = sentences;
		this.maxN = maxN;
		this.constructProbs();
	}
	
	
	private void constructProbs()
	{
		this.probs = new ArrayList <TreeMap <NGram, Double>> ();
		for (int i = 0; i < this.maxN; i++)
		{
			TreeMap<NGram, Double> curr_distrib_n = new TreeMap<NGram, Double> ();
			double summOcc = 0.;
			for (SentenceModel sent : this.sentences)
			{
				ArrayList <NGram> curr_ngrams_list = sent.getNGrams(i+1, this.index, null);
			
				for (NGram ng : curr_ngrams_list)
				{
					/*We filter the sourceDistribution upon every NGram occurrence, so we have to check if this 
					ngram belongs to the sourceDistribution if we want parallel lists*/
					//if (this.sourceDistribution.containsKey(ng))
					//{
						if (curr_distrib_n.containsKey(ng))
						{
							curr_distrib_n.put(ng, curr_distrib_n.get(ng) + 1.);
						}
						else
						{
							curr_distrib_n.put(ng, 1.);
						}
						summOcc ++;
					//}
				}
				
			}
			for (Entry <NGram, Double> entry : curr_distrib_n.entrySet())
			{
				entry.setValue(entry.getValue() / summOcc );
			}
			this.probs.add(curr_distrib_n);
		}
	}
	
	
	@Override
	public double getSmoothedProb(NGram ng) {
		double smoothed_prob = 1.;
		
		NGram ng_copy = new NGram (ng);
		
		for (int i = this.maxN - 1; i >= 0; i-- )
		{
			//System.out.println("size of "+i+" : "+this.probs.get(i).size());
			if (this.probs.get(i).containsKey(ng_copy))
			{
				smoothed_prob *= this.alphas.get(i+1) * this.probs.get(i).get(ng_copy);
				//System.out.println("Contient "+smoothed_prob);
				return smoothed_prob;
			}
			else
			{
				smoothed_prob *= this.alphas.get(i + 1);
				//System.out.println("Contient pas "+smoothed_prob);
				ng_copy.removeLastGram();
			}
		}
		smoothed_prob *= this.alphas.get(0);
		//System.out.println("Contient pas "+smoothed_prob);
		return smoothed_prob ;
	}
}
