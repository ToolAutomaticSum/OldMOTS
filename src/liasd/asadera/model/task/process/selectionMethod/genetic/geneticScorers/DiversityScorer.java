package liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.InvertedIndex;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class DiversityScorer extends GeneticIndividualScorer {
	private double maxIdf;
	
	public DiversityScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> dictionnary, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, ss, corpus, invertedIndex, dictionnary, null, null, null, null, null);		
	}

	public void init() {
		/*
		 * Finds maxIdf in index for this cluster (for future normalization)
		 */
		this.maxIdf = 0;
		ArrayList<WordIndex> indexKeys = this.invertedIndex.getCorpusWordIndex().get(cd.getiD());
		for (WordIndex key : indexKeys)
		{
			WordIndex w = (WordIndex)(key);
			double currIdf = w.getIdf();
			if (this.maxIdf < currIdf )
				this.maxIdf = currIdf;
		}
	}
	
	@Override
	/*public double computeScore(GeneticIndividual gi) {
		double sum = 0;
		int cpt = 0;
		
		for (int i = 0; i < gi.getGenes().size(); i++)
		{
			for (int j = 0; j < i; j++)
			{
				LevenshteinTfIdfDW lev = new LevenshteinTfIdfDW (gi.getGenes().get(i), gi.getGenes().get(j), index, idClust);
				sum += lev.computeSimilarity();
				cpt++;
			}
		}
		
		if (cpt == 0)
			return 1.;
		
		return sum/cpt;
	}*/
	
	public double computeScore(GeneticIndividual gi) {
		init();
		double sum = 0;
		TreeSet<Integer> giIndexKeys = new TreeSet <Integer>();
		int cpt = 0;
		for (SentenceModel p : gi.getGenes())
		{
			for (WordModel u : p)
			{
				if (!u.isStopWord()) {
					int uIndexKey = index.get(u.getmLemma()).getiD();
					if (! giIndexKeys.contains(uIndexKey))
					{
						giIndexKeys.add(uIndexKey);
					}
					cpt++;
				}
			}
		
		}
		for (Integer indexKey : giIndexKeys)
		{
			WordIndex w = (WordIndex) index.get(indexKey);
			sum += (w.getIdf() / this.maxIdf);
		}
		
		if (cpt == 0)
			return 0;
		//System.out.println(sum / (double) cpt);
		return sum / (double)cpt;
		
		
	}

	
	
}
