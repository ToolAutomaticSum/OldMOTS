package main.java.liasd.asadera.model.task.process.scoringMethod.TF_IDF;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import main.java.liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class TfIdfThreshold extends AbstractScoringMethod implements ScoreBasedIn, IndexBasedIn<WordIndex> {

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

	private Index<WordIndex> index;
	private double tfidfThreshold;

	public TfIdfThreshold(int id) throws SupportADNException {
		super(id);

		supportADN.put("TfIdfThreshold", Double.class);

		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		TfIdfThreshold p = new TfIdfThreshold(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(ScoringTfIdf_Parameter.TfIdfThreshold.getName(),
				Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "TfIdfThreshold"))));
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		tfidfThreshold = getCurrentProcess().getADN().getParameterValue(Double.class,
				ScoringTfIdf_Parameter.TfIdfThreshold.getName());

		for (Corpus corpus : listCorpus) {
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					double score = 0;
					for (WordIndex w : sentenceModel) {
						double temp = w.getTfCorpus(corpus.getiD()) * w.getIdf(index.getNbDocument());
						if (temp > tfidfThreshold)
							score += temp;
						// }
					}
					sentenceModel.setScore(score);
					sentencesScore.put(sentenceModel, sentenceModel.getScore());
				}
			}
		}

		double max = 0;
		for (Entry<SentenceModel, Double> e : sentencesScore.entrySet())
			if (e.getValue() > max)
				max = e.getValue();
		for (Entry<SentenceModel, Double> e : sentencesScore.entrySet())
			e.setValue(e.getValue() / max);
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}
}
