package model.task.process.scoringMethod.TF_IDF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exception.LacksOfFeatures;
import jgibblda.Pair;
import model.task.process.VectorCaracteristicBasedIn;
import model.task.process.VectorCaracteristicBasedOut;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import optimize.SupportADNException;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.InvertedIndex;
import textModeling.wordIndex.WordIndex;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;
import tools.PairSentenceScore;

public class Centroid extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	protected double[] centroid;
	protected int nbMaxWordInCentroid;
	protected InvertedIndex invertIndex;
	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	protected ArrayList<PairSentenceScore> sentencesScores;
	
	public Centroid(int id) throws SupportADNException {
		super(id);
	}
	
	private void init() throws Exception {
		nbMaxWordInCentroid = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "NbMaxWordInCentroid"));
		
		invertIndex = new InvertedIndex(getCurrentProcess().getIndex());
	}

	@Override
	public void computeScores() throws Exception {
		init();
		calculateCentroid();
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
					//TODO filtre
					for (WordModel w : sentenceModel) {
						if (!w.isStopWord()) {
							double value= centroid[index.getKeyId(w.getmLemma())];
							score += value;
						}
					}
					sentenceModel.setScore(score); //Ajout du score � la phrase
					sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
				}
			}
		}
		Collections.sort(sentencesScores);
		System.out.println(sentencesScores.size());
		System.out.println(sentencesScores);
	}
	
	private void calculateCentroid() throws NumberFormatException, LacksOfFeatures {
		centroid = new double[index.size()];
		List<Pair> listBestWord = new ArrayList<Pair>();
		double minTfIdf = 0;
		int corpusId = getCurrentProcess().getSummarizeCorpusId();
		
		for (WordIndex wi : invertIndex.getCorpusWordIndex().get(corpusId)) {
			WordTF_IDF w = (WordTF_IDF) wi;
			double tfidf = w.getTfCorpus(corpusId)*w.getIdf();
			if (listBestWord.size() < nbMaxWordInCentroid) {
				listBestWord.add(new Pair(wi.getId(), tfidf));
				Collections.sort(listBestWord);
				minTfIdf = (double) listBestWord.get(listBestWord.size()-1).second;
			} else if (tfidf > minTfIdf) {
				listBestWord.remove(nbMaxWordInCentroid-1);
				listBestWord.add(new Pair(wi.getId(), tfidf));
				Collections.sort(listBestWord);
				minTfIdf = (double) listBestWord.get(listBestWord.size()-1).second;
			}
		}
		
		for (Pair p : listBestWord) {
			centroid[(int)p.first] = (double) p.second;
			System.out.println(index.get((int)p.first).getWord());
		}
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}

}
