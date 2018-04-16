package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer;

import java.util.List;
import java.util.Map;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.QueryBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.ReinforcementLearning;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Query;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;
import main.java.liasd.asadera.tools.vector.ToolsVector;
import main.java.liasd.asadera.tools.wordFilters.StopWordsFilter;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;

public class AdvancedFeatures extends Featurer
		implements IndexBasedIn<NGram>, QueryBasedIn, SentenceCaracteristicBasedIn {

	private Index<NGram> index;
	private Query query;
	private Map<SentenceModel, Object> sentenceCaracteristic;
	private int maxLength;
	private WordFilter filter;
	private SimilarityMetric sim;

	public AdvancedFeatures(ReinforcementLearning rl) throws SupportADNException {
		super(rl);

		listParameterIn.add(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParameterizedType(null, double[].class, QueryBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public void init(int maxLength) throws Exception {
		filter = new StopWordsFilter();
		this.maxLength = maxLength;

		sim = SimilarityMetric.instanciateSentenceSimilarity("JaccardSimilarity");
	}

	@Override
	public double[] getFeatures(List<SentenceModel> summary) throws Exception {
		double[] features = instanciateVector();
		if (summary.isEmpty())
			return features;
		double[] redundancy = new double[index.size()];
		int length = 0;
		int nbChar = 0;
		int nbUpperChar = 0;
		int nbStopWords = 0;
		// for (SentenceModel sen : summary) {
		// for (WordModel wordModel : sen.getListWordModel()) {
		// //TODO add count nb upper char
		// nbChar += wordModel.getmLemma().length();
		// if (!filter.passFilter(wordModel))
		// nbStopWords++;
		// }
		// for (WordIndex word : sen) {
		// features[8] +=
		// word.getTfCorpus(sen.getText().getParentCorpus().getiD())*word.getIdf(index.getNbDocument());
		// redundancy[word.getiD()]++;
		// }
		// length += sen.getNbMot();
		// features[5] += 1.0/(double)sen.getPosition();
		// }
		// features[0] = length;
		// features[1] = length/summary.size(); //Avg #token in summary
		// features[2] = nbChar;
		// features[3] = nbChar/length;
		// features[4] = nbUpperChar/length;
		// features[5] /= summary.size(); //Avg abs pos
		// features[6] = nbStopWords;
		// features[7] = nbStopWords/length;
		// features[9] = features[8]/length;
		// for (int i=0; i<index.size(); i++)
		// features[10] += Math.max(redundancy[i] - 1, 0);
		// features[11] = length/maxLength;
		//
		// double[] sumVector = new double[index.size()];
		// for (SentenceModel sen : summary)
		// sumVector = ToolsVector.somme(sumVector,
		// (double[])sentenceCaracteristic.get(sen));
		// features[12] = sim.computeSimilarity((double[])query.getQuery(), sumVector);
		return features;
	}

	@Override
	public double[] instanciateVector() {
		return new double[12];
	}

	@Override
	public void setIndex(Index<NGram> index) {
		this.index = index;
	}

	@Override
	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		this.sentenceCaracteristic = senSim;
	}

}
