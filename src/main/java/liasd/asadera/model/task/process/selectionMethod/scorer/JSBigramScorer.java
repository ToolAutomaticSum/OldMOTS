package main.java.liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.process.indexBuilder.ListSentenceBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.textModeling.smoothings.DirichletSmoothing;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class JSBigramScorer extends Scorer implements ListSentenceBasedIn {

	private static Logger logger = LoggerFactory.getLogger(JSBigramScorer.class);

	private List<SentenceModel> listSen;
	private Map<WordIndex, Double> sourceDistribution;
	private Map<WordIndex, Integer> firstSentencesConcepts;
	private SimilarityMetric sim;
	private int nbBiGramsInSource;
	private double firstSentenceConceptsFactor;
	private double delta;
	private double[] corpusDistri;

	public JSBigramScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParameterizedType(SentenceModel.class, List.class, ListSentenceBasedIn.class));
	}

	@Override
	public void init() throws Exception {
		delta = Double.parseDouble(method.getCurrentProcess().getModel().getProcessOption(id, "Delta"));
		firstSentenceConceptsFactor = Double
				.parseDouble(method.getCurrentProcess().getModel().getProcessOption(id, "Fsc"));

		String similarityMethod = method.getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);

		computeSourceDistribution();
	}

	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.size() != 0) {
			if (summary.getScore() != 0)
				return summary.getScore();
			else {
				DirichletSmoothing smoothing = new DirichletSmoothing(2, delta, sourceDistribution.size(), summary,
						sourceDistribution, firstSentencesConcepts, firstSentenceConceptsFactor);
				double[] distri = smoothing.getSmoothedDistrib();
				double jsd = sim.computeSimilarity(distri, corpusDistri);
				summary.setScore(jsd);
				return jsd;
			}
		} else
			return 0;
	}

	private void computeSourceDistribution() {
		sourceDistribution = new TreeMap<WordIndex, Double>();
		Map<WordIndex, Double> sourceOccurences = new TreeMap<WordIndex, Double>();
		firstSentencesConcepts = new TreeMap<WordIndex, Integer>();
		nbBiGramsInSource = 0;
		double modified_nbBiGramsInSource = 0.;

		for (SentenceModel p : listSen) {
			List<WordIndex> curr_ngrams_list = p;
			nbBiGramsInSource += curr_ngrams_list.size();
			for (WordIndex ng : curr_ngrams_list) {
				if (!sourceOccurences.containsKey(ng))
					sourceOccurences.put(ng, 1.);
				else
					sourceOccurences.put(ng, sourceOccurences.get(ng) + 1.);

				if (p.getPosScore() == 1) {
					if (firstSentencesConcepts.containsKey(ng))
						firstSentencesConcepts.put(ng, firstSentencesConcepts.get(ng) + 1);
					else
						firstSentencesConcepts.put(ng, 1);
				}
			}
		}

		for (WordIndex ng : firstSentencesConcepts.keySet()) {
			double d = sourceOccurences.get(ng);
			modified_nbBiGramsInSource += firstSentenceConceptsFactor * d;
			sourceOccurences.put(ng, d + firstSentenceConceptsFactor * d);
		}

		modified_nbBiGramsInSource += nbBiGramsInSource;

		corpusDistri = new double[sourceOccurences.size()];
		int i = 0;
		logger.trace(
				"Number of ngrams after filtering : " + nbBiGramsInSource + " | " + modified_nbBiGramsInSource);
		for (WordIndex ng : sourceOccurences.keySet()) {
			sourceDistribution.put(ng, (double) sourceOccurences.get(ng) / modified_nbBiGramsInSource);
			corpusDistri[i] = sourceDistribution.get(ng);
			i++;
		}
	}

	@Override
	public void setListSentence(List<SentenceModel> listSen) {
		this.listSen = listSen;
	}
}
