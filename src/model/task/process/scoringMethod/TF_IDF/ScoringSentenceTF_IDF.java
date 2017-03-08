package model.task.process.scoringMethod.TF_IDF;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedIn;
import model.task.process.VectorCaracteristicBasedOut;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import optimize.SupportADNException;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import tools.PairSentenceScore;

public class ScoringSentenceTF_IDF extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	protected TreeSet<PairSentenceScore> sentencesScores;
	protected double cosineThreshold;
	
	public ScoringSentenceTF_IDF(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary) throws Exception {
		super.init(currentProcess, dictionnary);
		cosineThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "CosineThreshold"));
	}
	
	@Override
	public void computeScores() throws Exception {		
		sentencesScores = new TreeSet<PairSentenceScore>();
		
		Iterator<TextModel> textIt = getCurrentProcess().getModel().getCurrentMultiCorpus().get(getCurrentProcess().getSummarizeCorpusId()).iterator();
		while (textIt.hasNext()) {			
			TextModel textModel = textIt.next();
			Iterator<ParagraphModel> paragraphIt = textModel.iterator();
			while (paragraphIt.hasNext()) {
				ParagraphModel paragraphModel = paragraphIt.next();
				Iterator<SentenceModel> sentenceIt = paragraphModel.iterator();
				while (sentenceIt.hasNext()) {
					SentenceModel sentenceModel = sentenceIt.next();
					double score = 0;
					Iterator<WordModel> wordIt = sentenceModel.iterator();
					while (wordIt.hasNext()) {
						WordModel word = wordIt.next();
						double temp = sentenceCaracteristic.get(sentenceModel)[dictionnary.get(word.getmLemma()).getId()];
						if (temp  > cosineThreshold)
							score+= temp;
					}
					sentenceModel.setScore(score); //Ajout du score à la phrase
					sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
				}
			}
		}
		System.out.println(sentencesScores);
	}

	@Override
	public TreeSet<PairSentenceScore> getScore() {
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
