package liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.optimize.parameter.Parameter;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.tools.PairSentenceScore;
import liasd.asadera.tools.sentenceSimilarity.SentenceSimilarityMetric;

public class MMR extends AbstractSelectionMethod implements SentenceCaracteristicBasedIn, ScoreBasedIn {

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
	private SentenceSimilarityMetric sim;

	private boolean nbCharSizeOrNbSentenceSize;
	private int maxSummLength;
	private int nbSentenceInSummary;
	private ArrayList<SentenceModel> summary;
	private int actualSummaryLength;
	
	private ArrayList<PairSentenceScore> sentencesScores;
	private Map<SentenceModel, Object> sentenceCaracteristic;
	
	private HashMap<SentenceModel, Double> sentencesBaseScores;
	private HashMap<SentenceModel, Double> sentencesMMRScores;
	
	public MMR(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("Lambda", Double.class);

		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
		listParameterOut = new ArrayList<ParametrizedType>();
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		MMR p = new MMR(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(MMR_Parameter.Lambda.getName(), Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Lambda"))));
		getCurrentProcess().getADN().getParameter(Double.class, MMR_Parameter.Lambda.getName()).setMaxValue(1.0);
		getCurrentProcess().getADN().getParameter(Double.class, MMR_Parameter.Lambda.getName()).setMinValue(0.5);
	
		nbCharSizeOrNbSentenceSize = Boolean.parseBoolean(getCurrentProcess().getModel().getProcessOption(id, "CharLimitBoolean"));
		int size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(this, similarityMethod);
		
		if (nbCharSizeOrNbSentenceSize)
			this.maxSummLength = size;
		else
			this.nbSentenceInSummary = size;
	}
	
	private void init() throws Exception {
		lambda = getCurrentProcess().getADN().getParameterValue(Double.class, MMR_Parameter.Lambda.getName());
		
		this.sentencesBaseScores = new HashMap<SentenceModel, Double>();
		this.sentencesMMRScores = new HashMap<SentenceModel, Double>();
		
		Double scoreMax = Double.NEGATIVE_INFINITY;
		for (PairSentenceScore p : this.sentencesScores)
		{
			if (p.getScore().compareTo(scoreMax) > 0)
				scoreMax = p.getScore();
		}
		
		for (PairSentenceScore p : this.sentencesScores)
		{
			if (p.getScore() != 0)
				this.sentencesBaseScores.put(p.getPhrase(), p.getScore() * (1./scoreMax));
		}
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		init();
		
		this.summary = new ArrayList<SentenceModel> ();
		this.actualSummaryLength = 0;
		
		//int cnt = 0;
		while (this.selectNextSentence())
		{
			//System.out.println("Iteration " + cnt + " : " + this.sentencesBaseScores.size());
			//cnt ++;
		}
		
		return this.summary;
	}
	
	private void removeTooLongSentences()
	{
		HashMap <SentenceModel, Double> sentencesBaseScores_new = new HashMap <SentenceModel, Double> (this.sentencesBaseScores);
		for (SentenceModel p : this.sentencesBaseScores.keySet())
		{
			if (p.getNbMot() + this.actualSummaryLength > this.maxSummLength)
			{
				sentencesBaseScores_new.remove(p);
			}
		}
		this.sentencesBaseScores = new HashMap<SentenceModel, Double> (sentencesBaseScores_new);
	}
	
	/**
	 * 
	 * @return true if a sentence is selected, false if no more sentence can be selected
	 * @throws Exception 
	 */
	private boolean selectNextSentence() throws Exception
	{
		if (nbCharSizeOrNbSentenceSize)
			this.removeTooLongSentences();
		else if (!nbCharSizeOrNbSentenceSize &&(summary.size() == nbSentenceInSummary)) 
			return false;
		
		if (!this.sentencesBaseScores.isEmpty())
		{
			this.scoreAllSentences();
			Double scoreMax = Double.NEGATIVE_INFINITY;
			SentenceModel pMax = null;
			for (Entry <SentenceModel, Double> e : this.sentencesMMRScores.entrySet())
			{
				if (e.getValue().compareTo(scoreMax) > 0)
				{
					scoreMax = e.getValue();
					pMax = e.getKey();
				}
			}
			
			this.summary.add(pMax);
			//System.out.println(getMMRScore(pMax));
			this.sentencesBaseScores.remove(pMax);

			if (nbCharSizeOrNbSentenceSize)
				this.actualSummaryLength += pMax.getNbMot();
			else
				this.actualSummaryLength++;
			return true;
		}
		return false;
	}
	
	private void scoreAllSentences () throws Exception
	{
		this.sentencesMMRScores = new HashMap<SentenceModel, Double> ();
		for (SentenceModel p : this.sentencesBaseScores.keySet())
		{
			this.sentencesMMRScores.put(p, this.getMMRScore(p));
		}
	}
	
	private Double getMMRScore (SentenceModel p) throws Exception
	{
		double maxSim = 0.;
		for (SentenceModel p1 : this.summary)
		{
			double valSim;

			if(sentenceCaracteristic.get(p) == null)
				System.out.println("Prout");
			if ( (valSim = sim.computeSimilarity(sentenceCaracteristic, p1, p)) >= maxSim)
			{
				maxSim = valSim;
			}
		}
		double score = this.lambda * this.sentencesBaseScores.get(p) - (1. - this.lambda) * maxSim;
	
		return score;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
	}

	@Override
	public void setScore(ArrayList<PairSentenceScore> score) {
		this.sentencesScores = score;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}
