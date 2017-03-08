package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.NGram;

public class SimpleNGramScorer extends GeneticIndividualScorer{

	private TreeMap <NGram, Double> nGram_weights;
	
	
	public SimpleNGramScorer(HashMap <GeneticIndividualScorer, Double> scorers, Corpus corpus, InvertedIndex invertedIndex, Index index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, corpus, null, index, null, null, null, window, fsc_factor);
		this.constructNGramWeights ();
	}
	
	
	private void constructNGramWeights()
	{
		this.nGram_weights = new TreeMap<NGram, Double> ();
		
		for (TextModel doc : this.cd)
		{
			TreeSet <NGram> curr_doc_nGram = new TreeSet <NGram>();
			TreeSet<NGram> fsc_set = new TreeSet<NGram> ();
			for (ParagraphModel para : doc) {
				for (SentenceModel p : para)
				{
					TreeSet <NGram> curr_phrase_ngram_list = new TreeSet <NGram> (p.getNGrams(this.window, this.index));
					
					if (para.indexOf(p) == 1)
					{
						for (NGram ng : curr_phrase_ngram_list)//ArrayList => TreeMap doc, only one instance of a bigram per document
						{
							if (this.nGram_weights.containsKey(ng))//If ngram already in ngram_weights, add 1.
							{
								this.nGram_weights.put(ng, this.nGram_weights.get(ng) + (1. + this.fsc_factor));
							}
							else //Else put it with 1 value
							{
								this.nGram_weights.put(ng, 1.);
							}
						}
					}
					else
						curr_doc_nGram.addAll(curr_phrase_ngram_list);
				}
			}
			for (NGram ng : curr_doc_nGram)
			{
				if (this.nGram_weights.containsKey(ng))//If ngram already in ngram_weights, add 1.
				{
					this.nGram_weights.put(ng, this.nGram_weights.get(ng) + 1);
				}
				else //Else put it with 1 value
				{
					this.nGram_weights.put(ng, 1.);
				}
			}
			
		}
		
		//Upweight first sentences ngrams
		/*for (NGram ng : fsc_set)
		{
			this.nGram_weights.put(ng, this.nGram_weights.get(ng) * (1. + this.fsc_factor));
		}*/
		
	}
	
	@Override
	public double computeScore(GeneticIndividual gi) {		
		TreeSet <NGram> gi_ngrams = new TreeSet <NGram> ();
		
		for (SentenceModel p : gi.getGenes())
		{
			ArrayList<NGram> curr_sentence_ngram_list = p.getNGrams(this.window, this.index);
			gi_ngrams.addAll(curr_sentence_ngram_list);
		}
		
		int score = 0;
		
		for (NGram ng : gi_ngrams)
		{
			score += this.nGram_weights.get(ng);
			/*double curr_ngram_score = 1.;
			for (Integer indexKey : ng.getGrams())
			{
				double d = this.index.getContTfIdf(indexKey).getIdf();
				curr_ngram_score *= d;
			}
			score += curr_ngram_score * this.nGram_weights.get(ng);*/
		}
		
		
		return score;
	}

	
	
	
	
	
}
