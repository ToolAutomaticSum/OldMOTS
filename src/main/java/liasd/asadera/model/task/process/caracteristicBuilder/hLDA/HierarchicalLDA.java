package main.java.liasd.asadera.model.task.process.caracteristicBuilder.hLDA;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.Pair;
import main.java.liasd.asadera.tools.Randoms;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;

public class HierarchicalLDA {

	private List<SentenceModel> listDoc;
	private Index<WordIndex> dict;

	private NCRPNode rootNode, node;

	public NCRPNode getRootNode() {
		return rootNode;
	}

	private int numLevels;
	private int numDocuments;
	private int numTypes;

	private double alpha; // smoothing on topic distributions
	private double gamma; // "imaginary" customers at the next, as yet unused table
	private double eta; // smoothing on word distributions
	private double[] etaPerLevel;
	private double etaSum;

	private int[][] levels; // indexed <doc, token>
	private NCRPNode[] documentLeaves; // currently selected path (ie leaf node) through the NCRP tree

	private int totalNodes = 0;

	private String stateFile = "hlda.state";

	private Randoms random;

	private boolean showProgress = false;

	private int displayTopicsInterval = 10000;
	private int numWordsToDisplay = 5;

	public HierarchicalLDA() {
		alpha = 10.0;
		gamma = 1.0;
		eta = 0.25;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public void setEta(double eta) {
		this.eta = eta;
	}

	public void setStateFile(String stateFile) {
		this.stateFile = stateFile;
	}

	public void setTopicDisplay(int interval, int words) {
		displayTopicsInterval = interval;
		numWordsToDisplay = words;
	}

	/**
	 * This parameter determines whether the sampler outputs shows progress by
	 * outputting a character after every iteration.
	 */
	public void setProgressDisplay(boolean showProgress) {
		this.showProgress = showProgress;
	}

	public void initialize(List<TextModel> listText, Index<WordIndex> dict, WordFilter filter, int numLevels,
			Randoms random) {
		listDoc = new ArrayList<SentenceModel>();
		for (TextModel text : listText)
			listDoc.addAll(text);
		this.dict = dict;
		this.random = random;
		this.numLevels = numLevels;
		etaPerLevel = new double[numLevels];
		for (int level = 0; level < numLevels; level++)
			etaPerLevel[level] = eta / ((double) level + 1);

		numDocuments = listDoc.size();
		numTypes = dict.size();

		etaSum = eta * numTypes;

		// Initialize a single path

		NCRPNode[] path = new NCRPNode[numLevels];
		System.out.println("hLDA\nNumLevels : " + numLevels);
		rootNode = new NCRPNode(numTypes);

		levels = new int[numDocuments][];
		documentLeaves = new NCRPNode[numDocuments];

		// Initialize and fill the topic pointer arrays for
		// every document. Set everything to the single path that
		// we added earlier.
		for (int doc = 0; doc < numDocuments; doc++) {
			SentenceModel sen = listDoc.get(doc);
			path[0] = rootNode;
			rootNode.customers++;
			for (int level = 1; level < numLevels; level++) {
				path[level] = path[level - 1].select();
				path[level].customers++;
			}
			node = path[numLevels - 1];

			levels[doc] = new int[sen.getLength(filter)];
			documentLeaves[doc] = node;
			int token = 0;
			for (WordIndex word : sen) {
				// if (filter.passFilter(word)) {
				int type = word.getiD(); // dict.getKeyId(word.getmLemma());
				levels[doc][token] = random.nextInt(numLevels);
				node = path[levels[doc][token]];
				node.totalTokens++;
				node.typeCounts[type]++;
				token++;
				// }
			}
		}
	}

	public Map<SentenceModel, Object> estimate(int numIterations, Map<SentenceModel, Object> sentenceCaracteristic) {
		for (int iteration = 1; iteration <= numIterations; iteration++) {
			for (int doc = 0; doc < numDocuments; doc++)
				samplePath(doc, iteration);
			for (int doc = 0; doc < numDocuments; doc++)
				sampleTopics(doc);

			if (showProgress) {
				System.out.print(".");
				if (iteration % 50 == 0)
					System.out.println(" " + iteration);
			}

			if (iteration % displayTopicsInterval == 0)
				printNodes();
		}

		try {
			printState();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int doc = 0;
		int level;
		for (SentenceModel sen : listDoc) {
			node = documentLeaves[doc];
			NCRPNode[] path = new NCRPNode[numLevels];
			for (level = numLevels - 1; level >= 0; level--) {
				path[level] = node;
				node = node.parent;
			}
			sentenceCaracteristic.put(sen, path);
			doc++;
		}
		return sentenceCaracteristic;
	}

	public double[][] getSentenceLevelDistribution() {
		int doc = 0;
		double[][] levelDistribution = new double[listDoc.size()][];
		for (SentenceModel sen : listDoc) {
			levelDistribution[doc] = new double[numLevels];
			for (int token = 0; token < sen.size(); token++) {
				levelDistribution[doc][levels[doc][token]]++;
				token++;
			}

			for (int level = 0; level < numLevels; level++)
				levelDistribution[doc][level] /= sen.size();
			doc++;
		}
		return levelDistribution;
	}

	public double[] getTopicWordDistribution(NCRPNode node) {
		double[] multinomial = new double[numTypes];
		for (int type = 0; type < numTypes; type++) {
			multinomial[type] += (etaPerLevel[node.level] + node.typeCounts[type]) / (etaSum + node.totalTokens);
		}
		return multinomial;
	}

	public void samplePath(int doc, int iteration) {
		NCRPNode[] path = new NCRPNode[numLevels];

		int level, type;
		node = documentLeaves[doc];
		for (level = numLevels - 1; level >= 0; level--) {
			path[level] = node;
			node = node.parent;
		}

		documentLeaves[doc].dropPath();

		TObjectDoubleHashMap<NCRPNode> nodeWeights = new TObjectDoubleHashMap<NCRPNode>();

		// Calculate p(c_m | c_{-m})
		calculateNCRP(nodeWeights, rootNode, 0.0);

		// Add weights for p(w_m | c, w_{-m}, z)

		// The path may have no further customers and therefore
		// be unavailable, but it should still exist since we haven't
		// reset documentLeaves[doc] yet...

		TIntIntHashMap[] typeCounts = new TIntIntHashMap[numLevels];

		int[] docLevels;

		for (level = 0; level < numLevels; level++) {
			typeCounts[level] = new TIntIntHashMap();
		}

		docLevels = levels[doc];
		SentenceModel sen = listDoc.get(doc);
		// FeatureSequence fs = (FeatureSequence) instances.get(doc).getData();

		// Save the counts of every word at each level, and remove
		// counts from the current path
		int token = 0;
		for (WordIndex word : sen) {
			// if (filter.passFilter(word)) {
			level = docLevels[token];
			type = word.getiD(); // dict.getKeyId(word.getmLemma());

			if (!typeCounts[level].containsKey(type))
				typeCounts[level].put(type, 1);
			else
				typeCounts[level].increment(type);

			path[level].typeCounts[type]--;
			assert (path[level].typeCounts[type] >= 0);

			path[level].totalTokens--;
			assert (path[level].totalTokens >= 0);

			token++;
			// }
		}

		// Calculate the weight for a new path at a given level.
		double[] newTopicWeights = new double[numLevels];
		for (level = 1; level < numLevels; level++) { // Skip the root...
			int[] types = typeCounts[level].keys();
			int totalTokens = 0;

			for (int t : types) {
				for (int i = 0; i < typeCounts[level].get(t); i++) {
					newTopicWeights[level] += Math.log((etaPerLevel[level] + i) / (etaSum + totalTokens));
					totalTokens++;
				}
			}

			// if (iteration > 1) { System.out.println(newTopicWeights[level]); }
		}

		calculateWordLikelihood(nodeWeights, rootNode, 0.0, typeCounts, newTopicWeights, 0, iteration);

		NCRPNode[] nodes = nodeWeights.keys(new NCRPNode[] {});
		double[] weights = new double[nodes.length];
		double sum = 0.0;
		double max = Double.NEGATIVE_INFINITY;

		// To avoid underflow, we're using log weights and normalizing the node weights
		// so that
		// the largest weight is always 1.
		for (int i = 0; i < nodes.length; i++) {
			if (nodeWeights.get(nodes[i]) > max) {
				max = nodeWeights.get(nodes[i]);
			}
		}

		for (int i = 0; i < nodes.length; i++) {
			weights[i] = Math.exp(nodeWeights.get(nodes[i]) - max);

			/*
			 * if (iteration > 1) { if (nodes[i] == documentLeaves[doc]) {
			 * System.out.print("* "); } System.out.println(((NCRPNode) nodes[i]).level +
			 * "\t" + weights[i] + "\t" + nodeWeights.get(nodes[i])); }
			 */

			sum += weights[i];
		}

		// if (iteration > 1) {System.out.println();}

		node = nodes[random.nextDiscrete(weights, sum)];

		// If we have picked an internal node, we need to
		// add a new path.
		if (!node.isLeaf()) {
			node = node.getNewLeaf();
		}

		node.addPath();
		documentLeaves[doc] = node;

		for (level = numLevels - 1; level >= 0; level--) {
			int[] types = typeCounts[level].keys();

			for (int t : types) {
				node.typeCounts[t] += typeCounts[level].get(t);
				node.totalTokens += typeCounts[level].get(t);
			}

			node = node.parent;
		}
	}

	public void calculateNCRP(TObjectDoubleHashMap<NCRPNode> nodeWeights, NCRPNode node, double weight) {
		for (NCRPNode child : node.children) {
			calculateNCRP(nodeWeights, child, weight + Math.log((double) child.customers / (node.customers + gamma)));
		}

		nodeWeights.put(node, weight + Math.log(gamma / (node.customers + gamma)));
	}

	public void calculateWordLikelihood(TObjectDoubleHashMap<NCRPNode> nodeWeights, NCRPNode node, double weight,
			TIntIntHashMap[] typeCounts, double[] newTopicWeights, int level, int iteration) {

		// First calculate the likelihood of the words at this level, given
		// this topic.
		double nodeWeight = 0.0;
		int[] types = typeCounts[level].keys();
		int totalTokens = 0;

		// if (iteration > 1) { System.out.println(level + " " + nodeWeight); }

		for (int type : types) {
			for (int i = 0; i < typeCounts[level].get(type); i++) {
				nodeWeight += Math.log(
						(etaPerLevel[level] + node.typeCounts[type] + i) / (etaSum + node.totalTokens + totalTokens));
				totalTokens++;

				/*
				 * if (iteration > 1) { System.out.println("(" +eta + " + " +
				 * node.typeCounts[type] + " + " + i + ") /" + "(" + etaSum + " + " +
				 * node.totalTokens + " + " + totalTokens + ")" + " : " + nodeWeight); }
				 */

			}
		}

		// if (iteration > 1) { System.out.println(level + " " + nodeWeight); }

		// Propagate that weight to the child nodes

		for (NCRPNode child : node.children) {
			calculateWordLikelihood(nodeWeights, child, weight + nodeWeight, typeCounts, newTopicWeights, level + 1,
					iteration);
		}

		// Finally, if this is an internal node, add the weight of
		// a new path

		level++;
		while (level < numLevels) {
			nodeWeight += newTopicWeights[level];
			level++;
		}

		nodeWeights.adjustValue(node, nodeWeight);

	}

	/**
	 * Propagate a topic weight to a node and all its children. weight is assumed to
	 * be a log.
	 */
	public void propagateTopicWeight(TObjectDoubleHashMap<NCRPNode> nodeWeights, NCRPNode node, double weight) {
		if (!nodeWeights.containsKey(node)) {
			// calculating the NCRP prior proceeds from the
			// root down (ie following child links),
			// but adding the word-topic weights comes from
			// the bottom up, following parent links and then
			// child links. It's possible that the leaf node may have
			// been removed just prior to this round, so the current
			// node may not have an NCRP weight. If so, it's not
			// going to be sampled anyway, so ditch it.
			return;
		}

		for (NCRPNode child : node.children) {
			propagateTopicWeight(nodeWeights, child, weight);
		}

		nodeWeights.adjustValue(node, weight);
	}

	public void sampleTopics(int doc) {
		SentenceModel sen = listDoc.get(doc);
		// FeatureSequence fs = (FeatureSequence) instances.get(doc).getData();
		// int seqLen = fs.getLength();
		int[] docLevels = levels[doc];
		NCRPNode[] path = new NCRPNode[numLevels];
		NCRPNode node;
		int[] levelCounts = new int[numLevels];
		int type, level;
		double sum;

		// Get the leaf
		node = documentLeaves[doc];
		for (level = numLevels - 1; level >= 0; level--) {
			path[level] = node;
			node = node.parent;
		}

		double[] levelWeights = new double[numLevels];

		// int token = 0;
		// Initialize level counts
		// for (WordModel word : sen) {
		for (int token = 0; token < sen.size(); token++) {
			// if (filter.passFilter(word)) {
			levelCounts[docLevels[token]]++;
			token++;
			// }
		}

		int token = 0;
		for (WordIndex word : sen) {
			// if (filter.passFilter(word)) {
			type = word.getiD(); // dict.getKeyId(word.getmLemma());

			levelCounts[docLevels[token]]--;
			node = path[docLevels[token]];
			node.typeCounts[type]--;
			node.totalTokens--;

			sum = 0.0;
			for (level = 0; level < numLevels; level++) {
				levelWeights[level] = (alpha + levelCounts[level]) * (etaPerLevel[level] + path[level].typeCounts[type])
						/ (etaSum + path[level].totalTokens);
				sum += levelWeights[level];
			}
			level = random.nextDiscrete(levelWeights, sum);

			docLevels[token] = level;
			levelCounts[docLevels[token]]++;
			node = path[level];
			node.typeCounts[type]++;
			node.totalTokens++;
			token++;
			// }
		}
	}

	/**
	 * Writes the current sampling state to the file specified in
	 * <code>stateFile</code>.
	 */
	public void printState() throws IOException, FileNotFoundException {
		printState(new PrintWriter(new BufferedWriter(new FileWriter(stateFile))));
	}

	/**
	 * Write a text file describing the current sampling state.
	 */
	public void printState(PrintWriter out) throws IOException {
		int doc = 0;

		for (SentenceModel sen : listDoc) {

			// int[] docLevels = levels[doc];
			NCRPNode node;
			int /* type, token, */ level;

			StringBuffer path = new StringBuffer();

			// Start with the leaf, and build a string describing the path for this doc
			node = documentLeaves[doc];
			for (level = numLevels - 1; level >= 0; level--) {
				path.append(node.nodeID + " ");
				node = node.parent;
			}
			out.println(sen.getRawSentence() + "\t" + path);
			/*
			 * token = 0; for (WordModel word : sen) { if (filter.passFilter(word)) { type =
			 * dict.getKeyId(word.getmLemma()); level = docLevels[token];
			 * 
			 * // The "" just tells java we're not trying to add a string and an int
			 * out.println(path + "" + type + " " + word.getmLemma() + " " + level + " ");
			 * token++; } }
			 */
			doc++;
		}
	}

	public void printNodes() {
		printNode(rootNode, 0, false);
	}

	public void printNodes(boolean withWeight) {
		printNode(rootNode, 0, withWeight);
	}

	public void printNode(NCRPNode node, int indent, boolean withWeight) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			out.append("  ");
		}

		out.append(node.totalTokens + "/" + node.customers + " ");
		out.append(node.getTopWords(numWordsToDisplay, withWeight));
		System.out.println(out);

		for (NCRPNode child : node.children) {
			printNode(child, indent + 1, withWeight);
		}
	}

	public class NCRPNode {
		int customers;
		ArrayList<NCRPNode> children;
		public NCRPNode parent;
		int level;

		int totalTokens;
		int[] typeCounts;

		public int nodeID;

		public NCRPNode(NCRPNode parent, int dimensions, int level) {
			customers = 0;
			this.parent = parent;
			children = new ArrayList<NCRPNode>();
			this.level = level;

			totalTokens = 0;
			typeCounts = new int[dimensions];

			nodeID = totalNodes;
			totalNodes++;
		}

		public NCRPNode(int dimensions) {
			this(null, dimensions, 0);
		}

		public NCRPNode addChild() {
			NCRPNode node = new NCRPNode(this, typeCounts.length, level + 1);
			children.add(node);
			return node;
		}

		public boolean isLeaf() {
			return level == numLevels - 1;
		}

		public NCRPNode getNewLeaf() {
			NCRPNode node = this;
			for (int l = level; l < numLevels - 1; l++) {
				node = node.addChild();
			}
			return node;
		}

		public void dropPath() {
			NCRPNode node = this;
			node.customers--;
			if (node.customers == 0) {
				node.parent.remove(node);
			}
			for (int l = 1; l < numLevels; l++) {
				node = node.parent;
				node.customers--;
				if (node.customers == 0) {
					node.parent.remove(node);
				}
			}
		}

		public void remove(NCRPNode node) {
			children.remove(node);
		}

		public void addPath() {
			NCRPNode node = this;
			node.customers++;
			for (int l = 1; l < numLevels; l++) {
				node = node.parent;
				node.customers++;
			}
		}

		public NCRPNode selectExisting() {
			double[] weights = new double[children.size()];

			int i = 0;
			for (NCRPNode child : children) {
				weights[i] = (double) child.customers / (gamma + customers);
				i++;
			}

			int choice = random.nextDiscrete(weights);
			return children.get(choice);
		}

		public NCRPNode select() {
			double[] weights = new double[children.size() + 1];

			weights[0] = gamma / (gamma + customers);

			int i = 1;
			for (NCRPNode child : children) {
				weights[i] = (double) child.customers / (gamma + customers);
				i++;
			}

			int choice = random.nextDiscrete(weights);
			if (choice == 0) {
				return (addChild());
			} else {
				return children.get(choice - 1);
			}
		}

		public String getTopWords(int numWords, boolean withWeight) {
			@SuppressWarnings("rawtypes")
			Pair[] sortedTypes = new Pair[numTypes];

			for (int type = 0; type < numTypes; type++) {
				sortedTypes[type] = new Pair<Integer, Integer>(type, typeCounts[type]);
			}
			Arrays.sort(sortedTypes);

			StringBuffer out = new StringBuffer();
			for (int i = 0; i < numWords; i++) {
				if (withWeight) {
					out.append(dict.get(sortedTypes[i].getKey()) + ":" + sortedTypes[i].getValue() + " ");
				} else
					out.append(dict.get(sortedTypes[i].getKey()) + " ");
			}
			return out.toString();
		}

	}
}
