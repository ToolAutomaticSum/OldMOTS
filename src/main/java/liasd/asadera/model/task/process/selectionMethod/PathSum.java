package main.java.liasd.asadera.model.task.process.selectionMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.hLDA.HldaCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.hLDA.HierarchicalLDA.NCRPNode;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.Pair;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class PathSum extends AbstractSelectionMethod
		implements IndexBasedIn<WordIndex>, SentenceCaracteristicBasedIn, HldaCaracteristicBasedIn {

	protected Map<SentenceModel, Object> sentenceCaracteristic;
	protected Map<SentenceModel, double[]> sentenceLevelDistribution;
	protected Map<NCRPNode, double[]> topicWordDistribution;

	protected Index<WordIndex> index;
	protected Map<Integer, Integer> mapId_Path;
	protected Map<Integer, NCRPNode> mapPath_Node;

	protected Set<SentenceModel> availableSentence;
	private Set<SentenceModel> summary;
	protected Set<Integer> alreadySeenWord;

	protected double[] docPathDistribution;
	protected double[] sumPathDistribution;
	protected int numLevels;
	protected int nbDimension;

	private int size = 100;
	private SimilarityMetric sim;

	public PathSum(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParameterizedType(NCRPNode[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[].class, SentenceModel.class, HldaCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[].class, NCRPNode.class, HldaCaracteristicBasedIn.class));
	}

	@Override
	public AbstractSelectionMethod makeCopy() throws Exception {
		PathSum p = new PathSum(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		size = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "Size"));

		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}

	private void init() {
		numLevels = ((NCRPNode[]) sentenceCaracteristic.values().iterator().next()).length;
		nbDimension = 0;
		availableSentence = new TreeSet<SentenceModel>(sentenceCaracteristic.keySet());
		alreadySeenWord = new TreeSet<Integer>();
		summary = new TreeSet<SentenceModel>();
		mapId_Path = new HashMap<Integer, Integer>();
		mapPath_Node = new HashMap<Integer, NCRPNode>();
		int pathId = 0;
		for (NCRPNode node : topicWordDistribution.keySet())
			if (node.isLeaf()) {
				nbDimension++;
				mapId_Path.put(node.nodeID, pathId);
				mapPath_Node.put(pathId, node);
				pathId++;
			}

		docPathDistribution = new double[nbDimension];
		sumPathDistribution = new double[nbDimension];
	}

	@Override
	public List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception {
		init();
		int summarySize = 0;
		while (summarySize <= size && availableSentence.size() > 0) {
			Iterator<SentenceModel> itSen = availableSentence.iterator();
			while (itSen.hasNext()) {
				SentenceModel sent = itSen.next();
				if (summarySize + sent.getNbMot() > size)
					itSen.remove();
			}
			SentenceModel sen = selectNextSentence();
			if (sen != null) {
				summary.add(sen);
				summarySize += sen.getNbMot();
				availableSentence.remove(sen);
				for (WordIndex word : sen)
					alreadySeenWord.add(word.getiD());
			}
		}

		return new ArrayList<SentenceModel>(summary);
	}

	private SentenceModel selectNextSentence() throws Exception {
		buildPathDistribution(sentenceCaracteristic.keySet(), docPathDistribution);
		buildPathDistribution(summary, sumPathDistribution);
		List<Pair<Integer, Double>> listPath = selectNextPath(docPathDistribution, sumPathDistribution, summary.size());

		List<SentenceModel> sentencesInPath = new ArrayList<SentenceModel>();

		NCRPNode node = mapPath_Node.get(listPath.get(0).getKey());

		for (SentenceModel sen : availableSentence)
			if (((NCRPNode[]) sentenceCaracteristic.get(sen))[numLevels - 1] == node)
				sentencesInPath.add(sen);

		if (sentencesInPath.size() == 0) {
			int iPath = 1;
			while (sentencesInPath.size() == 0 && iPath < listPath.size()) {
				node = mapPath_Node.get(listPath.get(0).getKey());

				for (SentenceModel sen : availableSentence)
					if (((NCRPNode[]) sentenceCaracteristic.get(sen))[numLevels - 1] == node)
						sentencesInPath.add(sen);
				iPath++;
			}
			if (sentencesInPath.size() == 0) {
				availableSentence.clear();
				return null;
			}
		}

		NCRPNode[] path = new NCRPNode[numLevels];
		for (int level = numLevels - 1; level >= 0; level--) {
			path[level] = node;
			node = node.parent;
		}

		double[] averageThetaS = new double[numLevels];
		double[][] weightedTopicDistribution = new double[numLevels][index.size()];
		double[][] weightedWordDistribution = new double[index.size()][numLevels];
		double[][] weightedSentenceDistribution = new double[sentencesInPath.size()][numLevels];
		double priorTopic = 1.0 / topicWordDistribution.size();

		for (int level = 0; level < numLevels; level++) {
			for (SentenceModel sen : sentencesInPath)
				averageThetaS[level] += sentenceLevelDistribution.get(sen)[level];
			averageThetaS[level] /= sentencesInPath.size();
		}

		for (int level = 0; level < numLevels; level++) {
			NCRPNode topic = path[level];
			for (int tokenId = 0; tokenId < index.size(); tokenId++)
				weightedTopicDistribution[level][tokenId] += averageThetaS[level]
						* topicWordDistribution.get(topic)[tokenId];
		}

		for (int tokenId = 0; tokenId < index.size(); tokenId++)
			for (int level = 0; level < numLevels; level++)
				weightedWordDistribution[tokenId][level] = weightedTopicDistribution[level][tokenId] * priorTopic
						/ index.get(tokenId).getWeight();

		int currSen = 0;
		for (SentenceModel sen : sentencesInPath) {
			for (WordIndex word : sen) {
				int tokenId = word.getiD();
				for (int level = 0; level < numLevels; level++)
					weightedSentenceDistribution[currSen][level] += (alreadySeenWord.contains(tokenId))
							? weightedWordDistribution[tokenId][level] / 2
							: weightedWordDistribution[tokenId][level];
			}
			currSen++;
		}

		int bestSen = -1;
		double bestProba = 0;
		for (currSen = 0; currSen < sentencesInPath.size(); currSen++)
			for (int level = 0; level < numLevels; level++)
				if (weightedSentenceDistribution[currSen][level] > bestProba) {
					bestSen = currSen;
					bestProba = weightedSentenceDistribution[currSen][level];
				}

		return sentencesInPath.get(bestSen);
	}

	private List<Pair<Integer, Double>> selectNextPath(double[] p, double[] q, int qNbAlloc) throws Exception {
		List<Pair<Integer, Double>> listPath = new ArrayList<Pair<Integer, Double>>();
		for (int i = 0; i < nbDimension; i++) {
			q[i] = (qNbAlloc == 0 ? 1 : (q[i] * qNbAlloc + 1) / qNbAlloc);
			listPath.add(new Pair<Integer, Double>(i, sim.computeSimilarity(p, q)));
			q[i] = (qNbAlloc == 0 ? 0 : (q[i] * qNbAlloc - 1) / qNbAlloc);
		}
		Collections.sort(listPath);
		return listPath;
	}

	private void buildPathDistribution(Set<SentenceModel> listSentence, double[] pathDistribution) {
		int nbPath = listSentence.size();
		Arrays.fill(pathDistribution, 0.0);
		if (nbPath != 0) {
			for (SentenceModel sen : listSentence) {
				NCRPNode path = ((NCRPNode[]) sentenceCaracteristic.get(sen))[numLevels - 1];
				pathDistribution[mapId_Path.get(path.nodeID)]++;
			}
			for (int i = 0; i < pathDistribution.length; i++)
				pathDistribution[i] /= nbPath;
		}
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}

	@Override
	public void setSentenceLevelDistribution(Map<SentenceModel, double[]> sentenceLevelDistribution) {
		this.sentenceLevelDistribution = sentenceLevelDistribution;
	}

	@Override
	public void setTopicWordDistribution(Map<NCRPNode, double[]> topicWordDistribution) {
		this.topicWordDistribution = topicWordDistribution;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		sentenceCaracteristic = senSim;
	}
}
