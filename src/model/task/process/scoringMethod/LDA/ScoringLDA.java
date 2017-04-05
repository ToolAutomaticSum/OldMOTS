package model.task.process.scoringMethod.LDA;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedIn;
import model.task.process.VectorCaracteristicBasedOut;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordVector;
import tools.PairSentenceScore;
import tools.sentenceSimilarity.SentenceSimilarityMetric;

public class ScoringLDA extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {
	
	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	protected int K; //nb Topic
	
	protected ArrayList<PairSentenceScore> sentencesScores;
	private SentenceSimilarityMetric sim;

	public ScoringLDA(int id) throws Exception {
		super(id);
	}
	
	@Override
	public void initADN() throws Exception {
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}
	
	@Override
	public void init(AbstractProcess currentProcess, Index index)
			throws Exception {
		super.init(currentProcess, index);

		if (index.values().iterator().next().getClass() != WordVector.class)
			throw new Exception("Dictionnary need WordLDA !");
		K = ((WordVector) index.get(1)).getDimension();
	}
	
	@Override
	public void computeScores() throws Exception {
		double[] averageVector = new double[K];
		
		int n = 0; //nbWord
		Iterator<TextModel> textIt = getCurrentProcess().getCorpusToSummarize().iterator();
		while (textIt.hasNext()) {			
			TextModel textModel = textIt.next();
			Iterator<SentenceModel> sentenceIt = textModel.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				Iterator<WordModel> wordIt = sentenceModel.iterator();
				while(wordIt.hasNext()) {
					WordModel word = wordIt.next();
					if (!word.isStopWord()) {
						for (int k = 0; k<K;k++) {
							WordVector wLDA = (WordVector) index.get(word.getmLemma());
							averageVector[k] += wLDA.getWordVector()[k];
						}
						n++;
					}
				}
			}
		}
		
		for (int k = 0; k<K;k++) {
			averageVector[k] /= n;
		}
		
		sentencesScores = new ArrayList<PairSentenceScore>();
		
		//int i = 0; //Sentence variable
		
		textIt = getCurrentProcess().getCorpusToSummarize().iterator();
		while (textIt.hasNext()) {			
			TextModel textModel = textIt.next();
			Iterator<SentenceModel> sentenceIt = textModel.iterator();
			while (sentenceIt.hasNext()) {
				SentenceModel sentenceModel = sentenceIt.next();
				sentenceModel.setScore(sim.computeSimilarity(sentenceCaracteristic.get(sentenceModel), averageVector)); //Ajout du score � la phrase
				sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
			}
		}
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
