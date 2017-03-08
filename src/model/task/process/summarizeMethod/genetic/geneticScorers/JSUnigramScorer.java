package model.task.process.summarizeMethod.genetic.geneticScorers;

import java.util.HashMap;
import java.util.TreeMap;

import model.task.process.summarizeMethod.genetic.GeneticIndividual;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.WordIndex;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class JSUnigramScorer extends GeneticIndividualScorer{

	private TreeMap <Integer, Double> sourceDistribution;
	private TreeMap <Integer, Integer> sourceOccurences;
	private int nbWordsInSource;
	//private int nbWordsInIndividual;
	//private int nbMaxWords;
	
	public JSUnigramScorer(HashMap <GeneticIndividualScorer, Double> scorers, Corpus corpus, InvertedIndex invertedIndex, Index index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, corpus, invertedIndex, index, null, delta, null, null, null);
	}
	
	public void init() {
		this.computeCorpusDistribution();
	}
	
	@Override
	public double computeScore(GeneticIndividual gi) {
		init();
		TreeMap<Integer, Double> summDistrib = new TreeMap<Integer, Double> ();
		int summOcc = this.computeIndividualDistribution(gi, summDistrib);
		double jsd = this.jensenShanonDivergence (summDistrib, summOcc);
		
		return jsd;
	}
	
	public void computeCorpusDistribution ()
	{
		WordTF_IDF current_cti;
		double current_idf;
		int current_occ;
		this.sourceDistribution = new TreeMap <Integer, Double> ();
		this.sourceOccurences = new TreeMap <Integer, Integer> ();
		for ( WordIndex indexKey : invertedIndex.getCorpusWordIndex().get(cd.getiD()))
		{
			current_cti = (WordTF_IDF) indexKey;
			current_idf = current_cti.getIdf();
			
			current_occ = current_cti.size();
			if (current_idf != 0)
			{
				this.nbWordsInSource += current_occ;
				if (this.sourceOccurences.containsKey(indexKey.getId()))
				{
					this.sourceOccurences.put(indexKey.getId(), this.sourceOccurences.get(indexKey) + current_occ);
				}
				else
				{
					this.sourceOccurences.put(indexKey.getId(), current_occ);
				}
			}
		}
		
		for ( Integer indexKey : this.sourceOccurences.keySet() )
		{
			this.sourceDistribution.put(indexKey, (double)this.sourceOccurences.get(indexKey) / (double)this.nbWordsInSource);
		}
	}
	
	public int computeIndividualDistribution (GeneticIndividual gi, TreeMap <Integer, Double> summDist)
	{
		//double deltaTimesVoid;
		//TreeMap<Integer, Double> summDist = new TreeMap<Integer, Double>();
		int summOcc = 0;
		/*double probSumm = 0;
		double curr_prob;
		double divider;*/
		for (SentenceModel sent : gi.getGenes())
		{
			for (WordModel u : sent)
			{
				WordTF_IDF uIndexKey = (WordTF_IDF) index.get(u.getmLemma());
				if (uIndexKey.getIdf() != 0)
				{
					if (summDist.containsKey(uIndexKey.getId()))
					{
						summDist.put(uIndexKey.getId(), summDist.get(uIndexKey) + 1.);
						summOcc ++;
					}
					else
					{
						summDist.put(uIndexKey.getId(), 1.);
						summOcc ++;
					}

				}
			}
		}
		//Lissage des probas
		//TreeSet<Integer> sourceDistribCopy = new TreeSet <Integer>(this.sourceDistribution.keySet());
		//sourceDistribCopy.removeAll(summDist.keySet());
		/*deltaTimesVoid = sourceDistribCopy.size() * this.delta * 1.5;
		divider = summOcc + deltaTimesVoid;

		//System.out.println("sourceDistribCopy size : "+sourceDistribCopy.size());
		for (Integer indexKey : summDist.keySet())
		{
			curr_prob = summDist.get(indexKey) / summOcc;
			summDist.put(indexKey, curr_prob);
			probSumm += curr_prob;
		}
		for (Integer indexKey : sourceDistribCopy)
		{
			curr_prob = ((double)this.sourceOccurences.get(indexKey) + this.delta) / divider;
			summDist.put(indexKey, curr_prob);
			probSumm += curr_prob;
		}
		
		for (Integer indexKey : sourceDistribCopy)
		{
			summDist.put(indexKey, summDist.get(indexKey) / (double)probSumm);
		}*/
		
		
		return summOcc;
	}
	
	private double jensenShanonDivergence (TreeMap<Integer, Double> summDist, int summNbTokens)
	{
		double divergence = 0;
		
		double divider, divider1 = 1.5 * summNbTokens + this.delta;
		Double dProbSumm;
		double probSumm;
		double probSource;
		double log2 = Math.log(2);
		double sourceOp;
		double summOp;
		
		//System.out.println("summDist size : "+summDist.size());
		//System.out.println("sourceDist size : "+this.sourceDistribution.size());
		
		for (Integer indexKey : this.sourceDistribution.keySet())
		{
			probSource = this.sourceDistribution.get(indexKey);
			dProbSumm = summDist.get(indexKey);
			
			probSumm = dProbSumm == null ? this.delta / divider1 : (dProbSumm + this.delta) / divider1;
			
			
			//probSumm = dProbSumm == null ? this.delta * probSource / divider1 : (dProbSumm + this.delta * probSource) / divider1;
			
			divider = probSource + probSumm;
			sourceOp = 2 * probSource / divider;
			summOp = 2 * probSumm / divider;
			
			divergence += probSumm * Math.log(summOp) / log2;
			divergence += probSource * Math.log(sourceOp) / log2;
		}
		
		
		
		return 1. - (divergence / 2); //+ 0.1 * (double) summNbTokens / (double) this.nbMaxWords;
	}
	
}
