package main.java.liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;
import main.java.liasd.asadera.view.CommandView;

public class MMR extends AbstractSelectionMethod implements SentenceCaracteristicBasedIn, ScoreBasedIn {

	private static Logger logger = LoggerFactory.getLogger(MMR.class);

	public static enum MMR_Parameter {
		Lambda("Lambda");

		private String name;

		private MMR_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private double lambda;
	private SimilarityMetric sim;

	private boolean nbCharSizeOrNbSentenceSize;
	private int maxSummLength;
	private int nbSentenceInSummary;
	private ArrayList<SentenceModel> summary;
	private int actualSummaryLength;

	private Map<SentenceModel, Double> sentencesScores;
	private Map<SentenceModel, Object> sentenceCaracteristic;

	private Map<SentenceModel, Double> sentencesBaseScores;
	private Map<SentenceModel, Double> sentencesMMRScores;

	public MMR(int id) throws SupportADNException {
		super(id);

		supportADN.put("Lambda", Double.class);

		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(Double.class, Map.class, ScoreBasedIn.class));
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		MMR p = new MMR(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(MMR_Parameter.Lambda.getName(),
				Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Lambda"))));
		getCurrentProcess().getADN().getParameter(Double.class, MMR_Parameter.Lambda.getName()).setMaxValue(1.0);
		getCurrentProcess().getADN().getParameter(Double.class, MMR_Parameter.Lambda.getName()).setMinValue(0.5);

		nbCharSizeOrNbSentenceSize = Boolean
				.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "CharLimitBoolean"));
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));

		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);

		if (nbCharSizeOrNbSentenceSize)
			maxSummLength = size;
		else
			nbSentenceInSummary = size;
	}

	private void init() throws Exception {
		lambda = getCurrentProcess().getADN().getParameterValue(Double.class, MMR_Parameter.Lambda.getName());

		sentencesBaseScores = new HashMap<SentenceModel, Double>(sentencesScores);
		sentencesMMRScores = new HashMap<SentenceModel, Double>();
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		init();

		this.summary = new ArrayList<SentenceModel>();
		this.actualSummaryLength = 0;

		while (this.selectNextSentence()) {
		}

		return this.summary;
	}

	private void removeTooLongSentences() {
		Map<SentenceModel, Double> sentencesBaseScores_new = new HashMap<SentenceModel, Double>(sentencesBaseScores);
		for (SentenceModel p : sentencesBaseScores.keySet()) {
			if (p.getNbMot() + actualSummaryLength > maxSummLength) {
				sentencesBaseScores_new.remove(p);
			}
		}
		sentencesBaseScores = new HashMap<SentenceModel, Double>(sentencesBaseScores_new);
	}

	/**
	 * 
	 * @return true if a sentence is selected, false if no more sentence can be
	 *         selected
	 * @throws Exception
	 */
	private boolean selectNextSentence() throws Exception {
		if (nbCharSizeOrNbSentenceSize)
			this.removeTooLongSentences();
		else if (!nbCharSizeOrNbSentenceSize && (summary.size() == nbSentenceInSummary))
			return false;

		if (!this.sentencesBaseScores.isEmpty()) {
			this.scoreAllSentences();
			Double scoreMax = Double.NEGATIVE_INFINITY;
			SentenceModel pMax = null;
			for (Entry<SentenceModel, Double> e : this.sentencesMMRScores.entrySet()) {
				if (e.getValue().compareTo(scoreMax) > 0) {
					scoreMax = e.getValue();
					pMax = e.getKey();
				}
			}

			this.summary.add(pMax);
			this.sentencesBaseScores.remove(pMax);

			if (nbCharSizeOrNbSentenceSize)
				this.actualSummaryLength += pMax.getNbMot();
			else
				this.actualSummaryLength++;
			return true;
		}
		return false;
	}

	private void scoreAllSentences() throws Exception {
		this.sentencesMMRScores = new HashMap<SentenceModel, Double>();
		for (SentenceModel p : this.sentencesBaseScores.keySet()) {
			this.sentencesMMRScores.put(p, this.getMMRScore(p));
		}
	}

	private Double getMMRScore(SentenceModel p) throws Exception {
		double maxSim = 0.;
		for (SentenceModel p1 : this.summary) {
			double valSim;

			if (sentenceCaracteristic.get(p) == null)
				logger.error("Error");
			if ((valSim = sim.computeSimilarity(sentenceCaracteristic, p1, p)) >= maxSim) {
				maxSim = valSim;
			}
		}
		double score = this.lambda * this.sentencesScores.get(p) - (1. - this.lambda) * maxSim;

		return score;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

	@Override
	public void setScore(Map<SentenceModel, Double> score) {
		this.sentencesScores = score;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}
