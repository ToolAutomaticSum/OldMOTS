package liasd.asadera.model.task.process.scoringMethod.graphBased;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.optimize.parameter.Parameter;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.graphBased.GraphSentenceBased;
import liasd.asadera.textModeling.graphBased.NodeGraphSentenceBased;
import liasd.asadera.tools.PairSentenceScore;
import liasd.asadera.tools.sentenceSimilarity.SentenceSimilarityMetric;
import liasd.asadera.tools.vector.ToolsVector;

public class LexRank extends AbstractScoringMethod implements SentenceCaracteristicBasedIn {

	public static enum LexRank_Parameter {
		DumpingParameter("DumpingParameter"),
		//Epsilon("Epsilon"),
		GraphThreshold("GraphThreshold");

		private String name;

		private LexRank_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	private SentenceSimilarityMetric sim;
	/**
	 * SentenceCaracteristicBased
	 */
	private Map<SentenceModel, Object> sentenceCaracteristic;
	/**
	 * Dans ADN
	 */
	private double dumpingParameter = 0.85;
	/**
	 * Constant
	 */
	private double epsilon = 0.00001;
	/**
	 * Dans ADN
	 */
	private double graphThreshold = 0;
	/**
	 * Construit dans init
	 */
	private GraphSentenceBased graph;
	
	
	public LexRank(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("DumpingParameter", Double.class);
		//supportADN.put("Epsilon", Double.class);
		supportADN.put("GraphThreshold", Double.class);
		
		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
	
	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		LexRank p = new LexRank(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.DumpingParameter.getName(), Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.DumpingParameter.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.DumpingParameter.getName()).setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.DumpingParameter.getName()).setMinValue(0.0);
		//getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.Epsilon.getName(), 0.0001));
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.GraphThreshold.getName(), Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.GraphThreshold.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName()).setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName()).setMinValue(0.0);
	
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(this, similarityMethod);
	}
	
	private void init() throws Exception {
		
		dumpingParameter = getCurrentProcess().getADN().getParameterValue(Double.class, LexRank_Parameter.DumpingParameter.getName());
		epsilon = 0.00001;	///getCurrentProcess().getADN().getParameterValue(Double.class, LexRank_Parameter.Epsilon.getName());
		graphThreshold = getCurrentProcess().getADN().getParameterValue(Double.class, LexRank_Parameter.GraphThreshold.getName());
		
		graph = new GraphSentenceBased(graphThreshold, sentenceCaracteristic, sim);
	
		graph.generateGraph();
		//System.out.println(graph);
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		init();
		if (graph != null) {
			double[][] tempMat = new double[graph.size()][graph.size()];
			double[][] matAdj = graph.getMatAdj();
			int[] degree = graph.getDegree();

			for (int j=0;j<graph.size();j++) { //phrases courantes
				for (int k=0;k<graph.size();k++) { //phrases adjencete
					tempMat[j][k] = matAdj[j][k]/degree[k];
				}
			}
			double[] result = LexRank.computeLexRankScore(dumpingParameter, tempMat, graph.size(), epsilon);

			double max = 0.0;
			for (NodeGraphSentenceBased n : graph) {
				if (result[n.getIdNode()] > max)
					max = result[n.getIdNode()];
				sentencesScores.add(new PairSentenceScore(n.getCurrentSentence(), result[n.getIdNode()]));
			}
			for (PairSentenceScore p : sentencesScores)
				p.setScore(p.getScore()/max);
		}
 
		Collections.sort(sentencesScores);
	}
	
	public static double[] computeLexRankScore(double dampingFactor, double[][] matAdj, int matSize, double epsilon) throws Exception {
		double[] ptprec = new double[matSize];
		double[] pt = new double[matSize];
		double normeDiff;
		for (int i = 0; i<matSize; i++)
			pt[i] = 1.0 / (double)matSize;
		for (int i = 0; i<matSize; i++)
			for (int j = 0; j<matSize; j++)
				matAdj[i][j] = dampingFactor / (double)matSize + (1-dampingFactor)*matAdj[i][j];
		//ToolsVector.transposeMatrix(matAdj);
		//int n = 0;
		do {
			for (int i = 0; i<matSize; i++)
				ptprec[i] = pt[i];
			//Parcours des phrases (phrase courante i)
			for (int i = 0; i<matSize; i++) {
				pt[i] = 0;
				for (int j = 0; j<matSize; j++) { //parcours des phrases adjacentes j
					if (i!=j)
						pt[i] += matAdj[i][j] * ptprec[j];
				}
			}
			//n++;
			normeDiff = ToolsVector.norme(ToolsVector.soustraction(pt, ptprec));
			//System.out.println(normeDiff);
		} while (normeDiff > epsilon);
		//System.out.println("Itération " + n);
		return pt;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;		
	}
}
