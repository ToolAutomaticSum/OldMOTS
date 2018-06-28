package main.java.liasd.asadera.model.task.process.caracteristicBuilder.hLDA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedOut;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.hLDA.HierarchicalLDA.NCRPNode;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.Randoms;

public class HLDA extends AbstractCaracteristicBuilder
		implements IndexBasedIn<WordIndex>, SentenceCaracteristicBasedOut, HldaCaracteristicBasedOut {

	private Randoms randoms;

	private int numLevels;
	private double alpha; // smoothing on topic distributions
	private double gamma; // "imaginary" customers at the next, as yet unused table
	private double eta; // smoothing on word distributions

	private int nbIteration = 100;
	private HierarchicalLDA hlda;
	private Index<WordIndex> indexWord;

	protected Map<SentenceModel, Object> sentenceCaracteristic;
	protected Map<SentenceModel, double[]> sentenceLevelDistribution;
	protected Map<NCRPNode, double[]> topicWordDistribution;

	public static enum Inference_HLDA_Parameter {
		gamma("Gamma"), eta("Eta"), numLevels("NumLevels"), alpha("Alpha");

		private String name;

		private Inference_HLDA_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public HLDA(int id) throws SupportADNException {
		super(id);
		
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("NumLevels", Integer.class);
		supportADN.put("Alpha", Double.class);
		supportADN.put("Gamma", Double.class);
		supportADN.put("Eta", Double.class);

		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParameterizedType(NCRPNode[].class, Map.class, SentenceCaracteristicBasedOut.class));
		listParameterOut
				.add(new ParameterizedType(double[].class, SentenceModel.class, HldaCaracteristicBasedOut.class));
		listParameterOut.add(new ParameterizedType(double[].class, NCRPNode.class, HldaCaracteristicBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		HLDA p = new HLDA(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		sentenceCaracteristic = new HashMap<SentenceModel, Object>();
		sentenceLevelDistribution = new HashMap<SentenceModel, double[]>();
		topicWordDistribution = new HashMap<NCRPNode, double[]>();

		hlda = new HierarchicalLDA();

		int tempNumLevels = Integer
				.parseInt(getModel().getProcessOption(id, Inference_HLDA_Parameter.numLevels.getName()));
		getCurrentProcess().getADN()
				.putParameter(new Parameter<Integer>(Inference_HLDA_Parameter.numLevels.getName(), tempNumLevels));
		getCurrentProcess().getADN().getParameter(Integer.class, Inference_HLDA_Parameter.numLevels.getName())
				.setMaxValue(2 * tempNumLevels);
		getCurrentProcess().getADN().getParameter(Integer.class, Inference_HLDA_Parameter.numLevels.getName())
				.setMinValue(3);
		double tempAlpha = Double
				.parseDouble(getModel().getProcessOption(id, Inference_HLDA_Parameter.alpha.getName()));
		getCurrentProcess().getADN()
				.putParameter(new Parameter<Double>(Inference_HLDA_Parameter.alpha.getName(), tempAlpha));
		getCurrentProcess().getADN().getParameter(Double.class, Inference_HLDA_Parameter.alpha.getName())
				.setMaxValue(2 * tempAlpha);
		getCurrentProcess().getADN().getParameter(Double.class, Inference_HLDA_Parameter.alpha.getName())
				.setMinValue(1.0);
		double tempGamma = Double
				.parseDouble(getModel().getProcessOption(id, Inference_HLDA_Parameter.gamma.getName()));
		getCurrentProcess().getADN()
				.putParameter(new Parameter<Double>(Inference_HLDA_Parameter.gamma.getName(), tempGamma));
		getCurrentProcess().getADN().getParameter(Double.class, Inference_HLDA_Parameter.gamma.getName())
				.setMaxValue(2 * tempGamma);
		getCurrentProcess().getADN().getParameter(Double.class, Inference_HLDA_Parameter.gamma.getName())
				.setMinValue(0.01);
		double tempEta = Double.parseDouble(getModel().getProcessOption(id, Inference_HLDA_Parameter.eta.getName()));
		getCurrentProcess().getADN()
				.putParameter(new Parameter<Double>(Inference_HLDA_Parameter.eta.getName(), tempEta));
		getCurrentProcess().getADN().getParameter(Double.class, Inference_HLDA_Parameter.eta.getName())
				.setMaxValue(2 * tempEta);
		getCurrentProcess().getADN().getParameter(Double.class, Inference_HLDA_Parameter.eta.getName())
				.setMinValue(0.03);

		nbIteration = Integer.parseInt(getModel().getProcessOption(id, "NbIteration"));
		randoms = new Randoms();
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		numLevels = getCurrentProcess().getADN().getParameterValue(Integer.class,
				Inference_HLDA_Parameter.numLevels.getName());
		alpha = getCurrentProcess().getADN().getParameterValue(Double.class, Inference_HLDA_Parameter.alpha.getName());
		gamma = getCurrentProcess().getADN().getParameterValue(Double.class, Inference_HLDA_Parameter.gamma.getName());
		eta = getCurrentProcess().getADN().getParameterValue(Double.class, Inference_HLDA_Parameter.eta.getName());

		List<TextModel> listDoc = new ArrayList<TextModel>();
		for (Corpus c : listCorpus)
			listDoc.addAll(c);
		hlda.setAlpha(alpha);
		hlda.setGamma(gamma);
		hlda.setEta(eta);
		hlda.initialize(listDoc, indexWord, getCurrentProcess().getFilter(), numLevels, randoms);
		hlda.estimate(nbIteration, sentenceCaracteristic);

		double[][] senLvlDistri = hlda.getSentenceLevelDistribution();
		int currSen = 0;
		int summWeigth = 0;
		for (TextModel text : listDoc)
			for (SentenceModel sen : text) {
				if (sen.getPosition() == 1) {
					for (WordIndex word : sen)
						word.setWeight(word.getWeight() + 1);
					summWeigth++;
				}
				sentenceLevelDistribution.put(sen, senLvlDistri[currSen]);
				currSen++;
			}

		for (int tokenId = 0; tokenId < indexWord.size(); tokenId++)
			indexWord.get(tokenId)
					.setWeight((indexWord.get(tokenId).getWeight() + 1) / (indexWord.size() + summWeigth));

		generateTopicsWordDistribution(topicWordDistribution, hlda.getRootNode());
	}

	public void generateTopicsWordDistribution(Map<NCRPNode, double[]> topicWordDistribution, NCRPNode node) {
		topicWordDistribution.put(node, hlda.getTopicWordDistribution(node));
		for (NCRPNode child : node.children) {
			generateTopicsWordDistribution(topicWordDistribution, child);
		}
	}

	@Override
	public void finish() {
		sentenceCaracteristic.clear();
		topicWordDistribution.clear();
		sentenceLevelDistribution.clear();
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.indexWord = index;
	}

	@Override
	public Map<SentenceModel, Object> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

	@Override
	public Map<SentenceModel, double[]> getSentenceLevelDistribution() {
		return sentenceLevelDistribution;
	}

	@Override
	public Map<NCRPNode, double[]> getTopicWordDistribution() {
		return topicWordDistribution;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return (compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(NCRPNode[].class, Map.class, SentenceCaracteristicBasedIn.class))
				&& (compatibleMethod.getParameterTypeIn().contains(
						new ParameterizedType(double[].class, SentenceModel.class, HldaCaracteristicBasedIn.class))
						|| compatibleMethod.getParameterTypeIn().contains(
								new ParameterizedType(double[].class, NCRPNode.class, HldaCaracteristicBasedIn.class))));
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((SentenceCaracteristicBasedIn) compMethod).setCaracterisics(sentenceCaracteristic);
		if (compMethod.getParameterTypeIn()
				.contains(new ParameterizedType(double[].class, SentenceModel.class, HldaCaracteristicBasedIn.class)))
			((HldaCaracteristicBasedIn) compMethod).setSentenceLevelDistribution(sentenceLevelDistribution);
		if (compMethod.getParameterTypeIn()
				.contains(new ParameterizedType(double[].class, NCRPNode.class, HldaCaracteristicBasedIn.class)))
			((HldaCaracteristicBasedIn) compMethod).setTopicWordDistribution(topicWordDistribution);
	}
}
