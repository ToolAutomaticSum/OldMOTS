package model.task.process.scoringMethod.LDA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.task.process.AbstractProcess;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.LDA.WordLDA;
import tools.PairSentenceScore;
import tools.vector.ToolsVector;

public class ScoringSentenceLDA extends AbstractScoringMethod implements LdaBasedIn, ScoreBasedOut {

	static {
		supportADN = new HashMap<String, Class<?>>();
	}
	
	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	//protected double[] averageVector;
	protected int K; //nb Topic
	protected double[][] theta; //Document/Topic score
	protected int nbSentence;
	
	protected ArrayList<PairSentenceScore> sentencesScores;

	public ScoringSentenceLDA(int id) throws Exception {
		super(id);
	}
	
	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary)
			throws Exception {
		super.init(currentProcess, dictionnary);
		
		if (dictionnary.values().iterator().next().getClass() != WordLDA.class)
			throw new Exception("Dictionnary need WordLDA !");
	}
	
	@Override
	public void computeScores() throws Exception {
		double[] averageVector = new double[K];
		
		int nbText = getCurrentProcess().getModel().getCurrentMultiCorpus().get(getCurrentProcess().getSummarizeCorpusId()).size();
		for (int i = 0; i<K;i++) {
			for (int j=0;j<nbText;j++) {
				averageVector[i]+=theta[j][i];
			}
			averageVector[i]/=nbText;
		}
		
		sentencesScores = new ArrayList<PairSentenceScore>();
		
		//int i = 0; //Sentence variable
		
		Iterator<TextModel> textIt = getCurrentProcess().getModel().getCurrentMultiCorpus().get(getCurrentProcess().getSummarizeCorpusId()).iterator();
		while (textIt.hasNext()) {			
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					sentenceModel.setScore(ToolsVector.cosineSimilarity(sentenceCaracteristic.get(sentenceModel), averageVector)); //Ajout du score à la phrase
					sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
					//i++;
				}
			}
		}
		System.out.println(sentencesScores);
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}

	public void setTheta(double[][] theta) {
		this.theta = theta;
	}
	public void setK(int K) {
		this.K = K;
	}
	public void setNbSentence(int nbSentence) {
		this.nbSentence = nbSentence;
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}
