package model.task.process.scoringMethod.TF_IDF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import model.task.process.AbstractProcess;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import optimize.SupportADNException;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;
import tools.PairSentenceScore;

public class ScoringSentenceTF_IDF extends AbstractScoringMethod implements ScoreBasedOut {

	protected ArrayList<PairSentenceScore> sentencesScores;
	protected double tfidfThreshold;
	
	public ScoringSentenceTF_IDF(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary) throws Exception {
		super.init(currentProcess, dictionnary);
		tfidfThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "TfIdfThreshold"));
	}
	
	@Override
	public void computeScores() throws Exception {		
		sentencesScores = new ArrayList<PairSentenceScore>();
		
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
						if (!word.isStopWord()) {
							WordTF_IDF w = (WordTF_IDF) index.get(word.getmLemma());
							double temp = w.getTfCorpus(currentProcess.getSummarizeCorpusId())*w.getIdf();
							if (temp  > tfidfThreshold)
								score+= temp;
						}
					}
					sentenceModel.setScore(score); //Ajout du score � la phrase
					sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
				}
			}
		}
		Collections.sort(sentencesScores);
		//System.out.println(sentencesScores);
	}
	
	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}

}
