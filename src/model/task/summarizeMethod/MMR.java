package model.task.summarizeMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import model.task.VectorCaracteristicBasedIn;
import textModeling.SentenceModel;
import tools.PairSentenceScore;
import tools.Tools;
import tools.sentenceSimilarity.SentenceSimilarityMetric;

public class MMR extends AbstractSummarizeMethod implements VectorCaracteristicBasedIn, ScoreBasedIn/*, VectorQueryBasedIn*/ {

	//private double[][] similarities;
	private double lambda;
	//private double[] query;
	private SentenceSimilarityMetric sim;

	private boolean nbCharSizeOrNbSentenceSize;
	private int maxSummLength;
	private int nbSentenceInSummary;
	private ArrayList <SentenceModel> summary;
	private int actualSummaryLength;
	
	private TreeSet<PairSentenceScore> sentencesScores;
	private Map<SentenceModel,double[]> sentenceCaracteristic;
	
	private HashMap<SentenceModel, Double> sentencesBaseScores;
	private HashMap<SentenceModel, Double> sentencesMMRScores;
	
	public MMR(int id) {
		super(id);
	}
	
	public void init() throws Exception {
		nbCharSizeOrNbSentenceSize = Boolean.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "CharLimitBoolean"));
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));
		lambda = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Lambda"));
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		sim = Tools.instanciateSentenceSimilarity(similarityMethod, sentenceCaracteristic);
		
		if (nbCharSizeOrNbSentenceSize)
			this.maxSummLength = size;
		else
			this.nbSentenceInSummary = size;
		
		this.sentencesBaseScores = new HashMap<SentenceModel, Double>();
		this.sentencesMMRScores = new HashMap<SentenceModel, Double>();
		
		Double scoreMax = Double.NEGATIVE_INFINITY;
		for (PairSentenceScore p : this.sentencesScores)
		{
			if (p.getScore().compareTo(scoreMax) > 0)
				scoreMax = p.getScore();
		}
		
		for (PairSentenceScore p : this.sentencesScores)
		{
			this.sentencesBaseScores.put(p.getPhrase(), p.getScore() * (1./scoreMax));
		}
		
		
	/*	for (PairSentenceScore p : this.sentencesScores)
		{
			System.out.println(p.getScore()+" | "+p.getPhrase().getSentence());
		}
		
		for (Entry<SentenceModel, Double> e: this.sentencesBaseScores.entrySet())
		{
			System.out.println("base scores : "+e.getValue()+" | "+e.getKey().getSentence());
		}*/
	}

	@Override
	public List<SentenceModel> calculateSummary() throws Exception {
		init();
		
		this.summary = new ArrayList<SentenceModel> ();
		this.actualSummaryLength = 0;
		
		int cnt = 0;
		while (this.selectNextSentence())
		{
			System.out.println("Iteration " + cnt + " : " + this.sentencesBaseScores.size());
			cnt ++;
		}
		
		return this.summary;
	}
	
	private void removeTooLongSentences()
	{
		HashMap <SentenceModel, Double> sentencesBaseScores_new = new HashMap <SentenceModel, Double> (this.sentencesBaseScores);
		for (SentenceModel p : this.sentencesBaseScores.keySet())
		{
			if (p.size() + this.actualSummaryLength > this.maxSummLength)
			{
				sentencesBaseScores_new.remove(p);
			}
		}
		this.sentencesBaseScores = new HashMap<SentenceModel, Double> (sentencesBaseScores_new);
	}
	
	/**
	 * 
	 * @return true if a sentence is selected, false if no more sentence can be selected
	 * @throws Exception 
	 */
	private boolean selectNextSentence() throws Exception
	{
		if (nbCharSizeOrNbSentenceSize)
			this.removeTooLongSentences();
		else if (!nbCharSizeOrNbSentenceSize &&(summary.size() == nbSentenceInSummary)) 
			return false;
		
		if (!this.sentencesBaseScores.isEmpty())
		{
			this.scoreAllSentences();
			//PairSentenceScore bestPair = this.sentencesScores.first();
			Double scoreMax = Double.NEGATIVE_INFINITY;
			//System.out.println("min value : " + scoreMax);
			SentenceModel pMax = null;
			for (Entry <SentenceModel, Double> e : this.sentencesMMRScores.entrySet())
			{
				if (e.getValue().compareTo(scoreMax) > 0)
				{
					//System.out.println("Yo mama !");
					scoreMax = e.getValue();
					pMax = e.getKey();
				}
				//else
					//System.out.println("Pas Yo mamma du tout !" + e.getValue());
			}
			
			this.summary.add(pMax);
			//this.sentencesScores.remove(bestPair);
			this.sentencesBaseScores.remove(pMax);

			if (nbCharSizeOrNbSentenceSize)
				this.actualSummaryLength += pMax.size();
			else
				this.actualSummaryLength++;
			return true;
		}
		return false;
	}
	
	private void scoreAllSentences () throws Exception
	{
		this.sentencesMMRScores = new HashMap<SentenceModel, Double> ();
		for (SentenceModel p : this.sentencesBaseScores.keySet())
		{
			this.sentencesMMRScores.put(p, this.getMMRScore(p));
		}
	}
	
	/*private void scoreSentence (PairSentenceScore p)
	{
		p.setScore(this.getNewMMRScore(p.getPhrase()));
	}*/
	
	private Double getMMRScore (SentenceModel p) throws Exception
	{
		double maxSim = 0.;
		for (SentenceModel p1 : this.summary)
		{
			double valSim;
			if ( (valSim = sim.computeSimilarity(p1, p)) >= maxSim)
			{
				maxSim = valSim;
			}
		}
		double score = this.lambda * this.sentencesBaseScores.get(p) - (1. - this.lambda) * maxSim;
	
		
		//System.out.println("Score : "+score+" depuis "+this.sentencesBaseScores.get(p));
		return score;
	}

	@Override
	public void setScore(TreeSet<PairSentenceScore> score) {
		sentencesScores = score;
	}

	/*@Override
	public void setVectorQuery(double[] vectorQuery) {
		query = vectorQuery;
	}*/

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}
