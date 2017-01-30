package model.task.scoringMethod.TF_IDF;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import model.task.VectorCaracteristicBasedIn;
import model.task.VectorCaracteristicBasedOut;
import model.task.process.AbstractProcess;
import model.task.scoringMethod.AbstractScoringMethod;
import model.task.scoringMethod.ScoreBasedOut;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Dictionnary;
import tools.PairSentenceScore;

public class ScoringSentenceTF_IDF extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	protected TreeSet<PairSentenceScore> sentencesScores;
	protected double cosineThreshold;
	
	public ScoringSentenceTF_IDF(int id) {
		super(id);
	}

	@Override
	public void init(AbstractProcess currentProcess, Dictionnary dictionnary, Map<Integer, String> hashMapWord) throws Exception {
		super.init(currentProcess, dictionnary, hashMapWord);
		cosineThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "CosineThreshold"));
	}
	
	@Override
	public void computeScores() throws Exception {
		//int nbLemma = getDictionnary().size();
		//double[] averageVector = new double[nbLemma];
		
		/** Récupération de TfIdfMax afin de déterminer le vecteur général des documents */
		/*double tfIdfMax = 0;		
		Iterator<WordIndex> itWord = getDictionnary().values().iterator();
		while (itWord.hasNext()) {
			WordTF_IDF w = (WordTF_IDF) itWord.next();
			double temp = w.getTf()*w.getIdf();
			if (temp > tfIdfMax)
				tfIdfMax = temp;
		}
		
		int j = 0; //Word variable
		itWord = getDictionnary().values().iterator();
		while (itWord.hasNext()) {
			WordTF_IDF w = (WordTF_IDF) itWord.next();
			double temp = w.getTf()*w.getIdf();
			if (temp >= tfIdfMax/10) {
				averageVector[j] = temp;
			}
			j++;
		}*/
		
		sentencesScores = new TreeSet<PairSentenceScore>();
		
		Iterator<TextModel> textIt = getCurrentProcess().getModel().getDocumentModels().iterator();
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
