package model.task.process.summarizeMethod.genetic;

import java.util.ArrayList;

import model.task.process.summarizeMethod.genetic.geneticScorers.GeneticIndividualScorer;
import textModeling.SentenceModel;

public class GeneticIndividual {

	private ArrayList<SentenceModel> genes;
	private int genesTotalLength;
	private Double score;
	
	public ArrayList <SentenceModel> getGenes()
	{
		return this.genes;
	}
	
	public GeneticIndividual (GeneticIndividual gi)
	{
		this.genesTotalLength = gi.genesTotalLength;
		this.genes = new ArrayList<SentenceModel> (gi.genes);
		if (gi.score == null)
			this.score = null;
		else
			this.score = new Double(gi.score);
	}
	
	
	/**
	 * Random creation
	 * @param ss
	 * @param maxTotalLength
	 */
	public GeneticIndividual (ArrayList<SentenceModel> listSentence, int maxTotalLength)
	{
		int remainingLength;
		int randIndex;
		this.genesTotalLength = 0;
		this.genes = new ArrayList<SentenceModel>();
		this.score = null;
		
		while ( (this.genesTotalLength < maxTotalLength) && (listSentence.size() != 0) )
		{
			remainingLength = maxTotalLength - this.genesTotalLength;
			for (int i = 0; i < listSentence.size(); i++)
			{
				if (listSentence.get(i).getNbMot() > remainingLength)
				{
					listSentence.remove(i);
					i--;
				}
				else if (listSentence.get(i).getNbMot()==0) {
					listSentence.remove(i);
					i--;
				}
			}
			if (listSentence.size() == 0)
				break;
			
			randIndex = (int) (Math.random() * listSentence.size());
			this.genes.add(listSentence.get(randIndex));
			this.genesTotalLength += listSentence.get(randIndex).getNbMot();
			listSentence.remove(randIndex);
		}
	}
	
	/**
	 * Mutation
	 * @param ss
	 * @param maxTotalLength
	 * @param indiv
	 */
	public GeneticIndividual (ArrayList<SentenceModel> listSentence, int maxTotalLength, GeneticIndividual indiv, int genesMutationNb)//double mutationProb)
	{
		int geneId;
		this.genesTotalLength = indiv.genesTotalLength;
		this.genes = new ArrayList<SentenceModel>(indiv.genes);
		this.score = null;
		for (int i = 0; i < genesMutationNb; i++)
		{
			if (this.genes.size() == 0)
				break;
			geneId = (int) (Math.random() * this.genes.size());
			this.genesTotalLength -= this.genes.get(geneId).getNbMot();
			this.genes.remove(geneId);
			i--;
		}
		/*for (int i = 0; i < this.genes.size(); i++)
		{
			if (Math.random() < mutationProb)
			{
				this.genesTotalLength -= this.genes.get(i).getNbWords();
				this.genes.remove(i);
				i--;
			}
		}*/
		this.completeIndividual(listSentence, maxTotalLength);
		
	}
	
	/**
	 * Hybridation
	 * @param ss
	 * @param maxTotalLength
	 * @param parent1
	 * @param parent2
	 */
	public GeneticIndividual (ArrayList<SentenceModel> listSentence, int maxTotalLength, GeneticIndividual parent1, GeneticIndividual parent2)
	{
		this.score = null;
		ArrayList<SentenceModel> union = new ArrayList<SentenceModel>();
		
		for ( SentenceModel p : parent1.genes )
		{
			if (! parent2.genes.contains(p))
				union.add(p);
		}
		
		for ( SentenceModel p : parent2.genes )
		{
			union.add(p);
		}
		this.genes = new ArrayList<SentenceModel>();
		this.genesTotalLength = 0;
		this.randomSelection (maxTotalLength, union);
		this.completeIndividual(listSentence, maxTotalLength);
	}
	
	
	/**
	 * Completes an individual, adding sentences from ss if its actual length allows it
	 * @param ss
	 * @param maxTotalLength
	 */
	public void completeIndividual (ArrayList<SentenceModel> listSentence, int maxTotalLength)
	{
		if (this.genesTotalLength == maxTotalLength)
			return;
		ArrayList<SentenceModel> sa = new ArrayList<SentenceModel>();
		sa.addAll(listSentence);
		sa.removeAll(this.genes);
		
		this.randomSelection(maxTotalLength, sa);
	}
	
	/**
	 * Randomly selects adds sentences to genes from source until this.genesTotalLength reaches maxTotalLength 
	 * @param maxTotalLength
	 * @param source
	 */
	public void randomSelection (int maxTotalLength, ArrayList<SentenceModel> source)
	{
		int remainingLength;
		int randIndex;
		ArrayList<SentenceModel> sa = new ArrayList<SentenceModel> ();
		sa.addAll(source);
		while ( (this.genesTotalLength < maxTotalLength) && (sa.size() != 0) )
		{
			remainingLength = maxTotalLength - this.genesTotalLength;
			for (int i = 0; i < sa.size(); i++)
			{
				if (sa.get(i).getNbMot() > remainingLength)
				{
					sa.remove(i);
					i--;
				}
			}
			
			if (sa.size() == 0)
				break;
			
			randIndex = (int) (Math.random() * sa.size());
			this.genes.add(sa.get(randIndex));
			this.genesTotalLength += sa.get(randIndex).getNbMot();
			sa.remove(randIndex);
		}
	}
	
	public double getScore (GeneticIndividualScorer scorer)
	{
		if (this.score != null)
			return this.score;
		return scorer.computeScore(this);
	}
	
	@Override
	public boolean equals (Object o)
	{
		if (! o.getClass().equals(this.getClass()))
			return false;
		GeneticIndividual gi = (GeneticIndividual) o;
		
		if (this.genes.size() != gi.genes.size())
			return false;
		
		if (this.genesTotalLength != gi.genesTotalLength)
			return false;
		
		if (! this.genes.containsAll(gi.genes))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.valueOf(genes.size());
	}
}
