package model.task.process.scoringMethod.TF_IDF;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.processCompatibility.ParametrizedType;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedIn;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;
import tools.PairSentenceScore;

public class TfIdfThreshold extends AbstractScoringMethod implements ScoreBasedIn, IndexBasedIn<WordTF_IDF> {

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
	private double tfidfThreshold;
	
	public TfIdfThreshold(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("TfIdfThreshold", Double.class);

		listParameterIn.add(new ParametrizedType(WordTF_IDF.class, Index.class, IndexBasedIn.class));
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
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		tfidfThreshold = getCurrentProcess().getADN().getParameterValue(Double.class, ScoringTfIdf_Parameter.TfIdfThreshold.getName());

		for (Corpus corpus : listCorpus) {
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					double score = 0;
					for (WordModel word : sentenceModel) {
						if (!word.isStopWord()) {
							WordTF_IDF w = index.get(word.getmLemma());
							double temp = w.getTfCorpus(corpus.getiD())*w.getIdf();
							if (temp  > tfidfThreshold)
								score += temp;
						}
					}
					sentenceModel.setScore(score); //Ajout du score à la phrase
					sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
				}
			}
		}
		Collections.sort(sentencesScores);
		double max = sentencesScores.get(0).getScore();
		for (PairSentenceScore pss : sentencesScores)
			pss.setScore(pss.getScore() / max);
	}

	@Override
	public void setIndex(Index<WordTF_IDF> index) {
		this.index = index;
	}
}
