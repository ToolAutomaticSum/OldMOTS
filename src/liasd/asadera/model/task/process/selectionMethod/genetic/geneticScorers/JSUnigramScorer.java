package liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.InvertedIndex;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class JSUnigramScorer extends GeneticIndividualScorer{

	private TreeMap <Integer, Double> sourceDistribution;
	private TreeMap <Integer, Integer> sourceOccurences;
	private int nbWordsInSource;
	//private int nbWordsInIndividual;
	//private int nbMaxWords;
	
	public JSUnigramScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, invertedIndex, index, null, delta, null, null, null);
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
		WordIndex current_cti;
		double current_idf;
		int current_occ;
		this.sourceDistribution = new TreeMap <Integer, Double> ();
		this.sourceOccurences = new TreeMap <Integer, Integer> ();
		for ( WordIndex indexKey : invertedIndex.getCorpusWordIndex().get(cd.getiD()))
		{
			current_cti = (WordIndex) indexKey;
			current_idf = current_cti.getIdf(index.getNbDocument());
			
			current_occ = current_cti.getNbOccurence();
			if (current_idf != 0)
			{
				this.nbWordsInSource += current_occ;
				if (this.sourceOccurences.containsKey(indexKey.getiD()))
				{
					this.sourceOccurences.put(indexKey.getiD(), this.sourceOccurences.get(indexKey) + current_occ);
				}
				else
				{
					this.sourceOccurences.put(indexKey.getiD(), current_occ);
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
			for (WordIndex uIndexKey : sent)
			{
//				WordIndex uIndexKey = (WordIndex) index.get(u.getmLemma());
				if (uIndexKey.getIdf(index.getNbDocument()) != 0)
				{
					if (summDist.containsKey(uIndexKey.getiD()))
					{
						summDist.put(uIndexKey.getiD(), summDist.get(uIndexKey) + 1.);
						summOcc ++;
					}
					else
					{
						summDist.put(uIndexKey.getiD(), 1.);
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
