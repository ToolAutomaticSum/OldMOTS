package liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;

import liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.InvertedIndex;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class CosineScorer extends GeneticIndividualScorer{

	public CosineScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> dictionnary, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, invertedIndex, dictionnary, null, null, null, null, null);
	}


	@Override
	public double computeScore(GeneticIndividual gi) {	
		ArrayList<WordIndex> listWordIndex = invertedIndex.getCorpusWordIndex().get(cd.getiD());
		ArrayList<WordIndex> giIndexKeys = new ArrayList<WordIndex> ();
		HashMap<Integer, Double> giFrequencies = new HashMap<Integer, Double> ();
		this.computeGiIndexKeysAndFrequencies (giIndexKeys, giFrequencies, gi);
		return this.cosineSimilarity (giIndexKeys, giFrequencies, listWordIndex);
	}
	
	/**
	 * populate giIndexKeys and giFrequencies with indexKeys from this.index and frequencies from the scored individual
	 * @param giIndexKeys a TreeSet that will be populated with genetic individual indexKeys
	 * @param giFrequencies the HashMap linking indexKeys and the genetic individual frequencies
	 * @param gi the scored genetic individual
	 */
	public void computeGiIndexKeysAndFrequencies(ArrayList<WordIndex> giIndexKeys, HashMap<Integer, Double> giFrequencies, GeneticIndividual gi)
	{
		WordIndex uIndexKey = null;
		try {
			for (SentenceModel p : gi.getGenes()) {
				for (WordModel u : p) {
					if (!u.isStopWord()) {
						uIndexKey = index.get(u.getmLemma());
						if (giIndexKeys.contains(uIndexKey)) {
							giFrequencies.put(uIndexKey.getiD(), giFrequencies.get(uIndexKey.getiD()) + 1.);
						}
						else {
							giIndexKeys.add(uIndexKey);
							giFrequencies.put(uIndexKey.getiD(), 1.);
						}
					}
				}
			}
		}
		catch (NullPointerException e) {
			System.err.println(e + " : " + uIndexKey);
		}
		//if (giIndexKeys.size()==0) {
			//int i = 0;
			//i++;
		//}
	}
	
	public double cosineSimilarity (ArrayList<WordIndex> giIndexKeys, HashMap<Integer, Double> giFrequencies, ArrayList<WordIndex> clustIndexKeys)
	{

		double sumCommon = 0.;
		double sumGi = 0.;
		double sumClust = 0.;
		/*
		 * giIndexKeys is supposed to be a subset of clustIndexKeys, so no need to get commonIndexKeys !!!
		 */
		WordIndex cti = null;
		try {
			for (WordIndex indexKey : giIndexKeys)
			{
				cti = (WordIndex) indexKey;
				sumCommon += cti.getTf() * giFrequencies.get(cti.getiD()) * Math.pow(cti.getIdf(), 2.);
				sumGi += Math.pow (giFrequencies.get(cti.getiD()) * cti.getIdf(), 2.);
			}
			
			for (WordIndex indexKey : clustIndexKeys)
			{
				cti = (WordIndex) indexKey;
				sumClust += Math.pow (cti.getTf() * cti.getIdf(), 2.);
			}
			if (sumClust == 0 || sumGi == 0)
			{
				System.out.println("Error : giIndexKeys size="+giIndexKeys.size()+" | giFrequencies size="+giFrequencies.size()+
						" | sumGi="+sumGi+" | clustIndexKeys = "+clustIndexKeys.size());
				//System.out.println("***************** D�but Phrases ****************");
				for (WordIndex i : giIndexKeys)
				{
					cti = (WordIndex) i;
					System.out.print (i+ " | "+cti.getIdf()+" | "+giFrequencies.get(i)+" ** ");
					
				}
				//System.out.println("\n****************** Fin Phrases *****************");
				return 0;
			}
				
			
			double sim = sumCommon / (Math.sqrt(sumGi) * Math.sqrt(sumClust));
			if (sim > 1)
				return 1.;
			return sim;
			
		}
		catch (Exception hmke) {
			System.err.println(hmke + " : " + cti);
			hmke.printStackTrace();
		}
				
		return 0;
	}

	
	
	
}
