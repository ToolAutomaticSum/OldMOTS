package liasd.asadera.model.task.process.selectionMethod.genetic.geneticScorers;

import java.util.ArrayList;
import java.util.HashMap;

import liasd.asadera.model.task.process.selectionMethod.genetic.GeneticIndividual;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.InvertedIndex;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public class PosScorer extends GeneticIndividualScorer{

	public PosScorer(HashMap <GeneticIndividualScorer, Double> scorers, ArrayList<SentenceModel> ss, Corpus corpus, InvertedIndex<WordIndex> invertedIndex, Index<WordIndex> index, Double divWeight, Double delta, Double firstSentenceConceptsFactor, Integer window, Double fsc_factor) {
		super(null, null, null, null, null, null, null, null, null, null);
	}
	
	@Override
	public double computeScore(GeneticIndividual gi) {
		// TODO Auto-generated method stub
		double score = 0.;
		
		
		for (SentenceModel p : gi.getGenes())
		{
			score += p.getPosScore();
			//System.out.println(score);
		}
		score /= (double)gi.getGenes().size();
		//System.out.println("final score : "+score);
		return score;
	}

}
