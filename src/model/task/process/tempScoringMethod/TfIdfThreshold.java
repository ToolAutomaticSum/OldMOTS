package model.task.process.tempScoringMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;
import tools.PairSentenceScore;

public class TfIdfThreshold extends AbstractScoringMethod implements ScoreBasedIn, IndexBasedIn<WordTF_IDF>, ScoreBasedOut {

	public static enum ScoringTfIdf_Parameter {
		TfIdfThreshold("TfIdfThreshold");

		private String name;

		private ScoringTfIdf_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	private Index<WordTF_IDF> index;
	private ArrayList<PairSentenceScore> sentencesScores;
	private double tfidfThreshold;
	
	public TfIdfThreshold(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("TfIdfThreshold", Double.class);

		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterIn.add(new ParametrizedType(WordTF_IDF.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
		listParameterOut = new ArrayList<ParametrizedType>();
		listParameterOut.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedOut.class));
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		TfIdfThreshold p = new TfIdfThreshold(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(ScoringTfIdf_Parameter.TfIdfThreshold.getName(), Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "TfIdfThreshold"))));
	
		//TODO voir Treemap pour modifier le score de la phrase si elle existe déjà dans le tableau
		if (sentencesScores == null)
			sentencesScores = new ArrayList<PairSentenceScore>();
	}

	@Override
	public void computeScores() throws Exception {
		tfidfThreshold = getCurrentProcess().getADN().getParameterValue(Double.class, ScoringTfIdf_Parameter.TfIdfThreshold.getName());

		for (TextModel textModel : getCurrentProcess().getCorpusToSummarize()) {
			for (SentenceModel sentenceModel : textModel) {
				double score = 0;
				for (WordModel word : sentenceModel) {
					if (!word.isStopWord()) {
						WordTF_IDF w = (WordTF_IDF) index.get(word.getmLemma());
						double temp = w.getTfCorpus(currentProcess.getSummarizeCorpusId())*w.getIdf();
						if (temp  > tfidfThreshold)
							score += temp;
					}
				}
				sentenceModel.setScore(score); //Ajout du score à la phrase
				sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
			}
		}
		Collections.sort(sentencesScores);
		double max = sentencesScores.get(0).getScore();
		for (PairSentenceScore pss : sentencesScores)
			pss.setScore(pss.getScore() / max);
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((ScoreBasedIn)compMethod).setScore(sentencesScores);
	}

	@Override
	public void setIndex(Index<WordTF_IDF> index) {
		this.index = index;
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}

	@Override
	public void setScore(ArrayList<PairSentenceScore> score) {
		this.sentencesScores = score;
	}

}
