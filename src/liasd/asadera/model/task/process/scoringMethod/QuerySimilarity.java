package liasd.asadera.model.task.process.scoringMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.Query;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.tools.PairSentenceScore;
import liasd.asadera.tools.sentenceSimilarity.SentenceSimilarityMetric;

public class QuerySimilarity extends AbstractScoringMethod implements QueryBasedIn, SentenceCaracteristicBasedIn {

	private Query query;
	protected Map<SentenceModel, Object> sentenceCaracteristic;
	private SentenceSimilarityMetric sim;
	
	public QuerySimilarity(int id) throws SupportADNException {
		super(id);
		
		listParameterIn.add(new ParametrizedType(null, double[].class, QueryBasedIn.class));
		listParameterIn.add(new ParametrizedType(null, double[][].class, QueryBasedIn.class));
		listParameterIn.add(new ParametrizedType(null, double[][][].class, QueryBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		QuerySimilarity p = new QuerySimilarity(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(this, similarityMethod);
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		Object queryVec = query.getQuery();
		if (queryVec.getClass() != sentenceCaracteristic.values().iterator().next().getClass())
			throw new RuntimeException("Query and sentence vector representation need to have the same number of dimension.");
		for (Corpus corpus : listCorpus) {
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					double score = sim.computeSimilarity(sentenceCaracteristic, queryVec, sentenceModel);
					sentenceModel.setScore(score); //Ajout du score � la phrase
					sentencesScores.add(new PairSentenceScore(sentenceModel, sentenceModel.getScore()));
				}
			}
		}
		Collections.sort(sentencesScores);
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}

	@Override
	public void setQuery(Query query) {
		this.query = query;
	}
}
