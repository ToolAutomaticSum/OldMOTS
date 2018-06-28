package main.java.liasd.asadera.model.task.process.caracteristicBuilder.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.ListClusterBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.ListClusterBasedOut;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.cluster.Cluster;
import main.java.liasd.asadera.tools.Pair;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class PoBOC_Clustering extends AbstractCaracteristicBuilder
		implements SentenceCaracteristicBasedIn, ListClusterBasedOut {

	protected Map<SentenceModel, Object> sentenceCaracteristic;

	private List<Cluster> listCluster = new ArrayList<Cluster>();
	private List<List<Integer>> listPole = new ArrayList<List<Integer>>();
	private List<Integer> availableSentences;
	private Set<Integer> sentenceInPoles;
	private SimilarityMetric sim;

	private List<SentenceModel> listSentence;
	private int n;
	private double[][] u;
	private double[][] matSim;
	private double[][] graphSim;
	private int[] degree;

	public PoBOC_Clustering(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterOut.add(new ParameterizedType(Cluster.class, List.class, ListClusterBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		PoBOC_Clustering p = new PoBOC_Clustering(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(/* this, */ similarityMethod);
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) {
		listSentence = new ArrayList<SentenceModel>();
		sentenceInPoles = new TreeSet<Integer>();
		availableSentences = new ArrayList<Integer>();

		for (Corpus c : listCorpus)
			listSentence.addAll(c.getAllSentence());
		for (int i = 0; i < listSentence.size(); i++)
			availableSentences.add(i);
		try {
			generateSimilarityMatrix();
			generateSimilarityGraph();
			buildPoles();

			buildMemberShipU();
			for (int j = 0; j < n; j++)
				assign(j);

			for (List<Integer> list : listPole) {
				String t = "";
				for (Integer i : list)
					t += i + "\t";
				System.out.println(t);
			}
			for (int i = 0; i < listPole.size(); i++) {
				Cluster clust = new Cluster(i);
				for (int j = 0; j < listPole.get(i).size(); j++)
					clust.add(listSentence.get(listPole.get(i).get(j)));
				listCluster.add(clust);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void finish() {
		listCluster.clear();
		listPole.clear();
		matSim = null;
		graphSim = null;
		u = null;
		degree = null;
		availableSentences.clear();
		sentenceInPoles.clear();
	}

	private void generateSimilarityMatrix() throws Exception {
		n = listSentence.size();
		matSim = new double[n][n];

		double max = 0;
		double min = 10000;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j)
					matSim[i][j] = 1.0;
				else
					matSim[i][j] = sim.computeSimilarity(sentenceCaracteristic, listSentence.get(i),
							listSentence.get(j));
				if (matSim[i][j] > max)
					max = matSim[i][j];
				else if (matSim[i][j] < min)
					min = matSim[i][j];
			}
		}
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				if (matSim[i][j] != 1.0) {
					matSim[i][j] -= (max + min) / 2;
					matSim[i][j] /= (max - min) / 2;
				}
			}
	}

	private void generateSimilarityGraph() {
		degree = new int[n];
		graphSim = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double seuil = getMaxMeanSim(i, j);
				if (matSim[i][j] < seuil)
					graphSim[i][j] = 0;
				else
					graphSim[i][j] = 1;
			}
		}
		for (int i = 0; i < n; i++) {
			int sum = -1;
			for (int j = 0; j < n; j++) {
				if (graphSim[i][j] > 0)
					sum++;
			}
			degree[i] = sum;
		}
	}

	private void buildPoles() {
		int x = -1;
		double min = 1;
		for (int i = 0; i < n; i++) {
			double value = 0;
			if (degree[i] > 0) {
				for (int j = 0; j < n; j++) {
					if (graphSim[i][j] != 0)
						value += matSim[i][j];
				}
				value /= n;
				if (value < min) {
					min = value;
					x = i;
				}
			}
		}
		List<Integer> pole = new ArrayList<Integer>();
		pole.add(x);
		buildPoleClique(pole);
		listPole.add(pole);
		sentenceInPoles.addAll(pole);
		availableSentences.removeAll(pole);
		while (sentenceInPoles.size() < n) {
			System.out.println(sentenceInPoles.size());
			addPole();
		}
	}

	private void buildPoleClique(List<Integer> pole) {
		List<Pair<Integer, Double>> neighbours;
		while (!(neighbours = buildPoleNeighborhood(pole)).isEmpty())
			pole.add(neighbours.get(0).getKey());
	}

	private List<Pair<Integer, Double>> buildPoleNeighborhood(List<Integer> pole) {
		int poleSize = pole.size();
		List<Pair<Integer, Double>> neighbours = new ArrayList<Pair<Integer, Double>>();
		for (int i = 0; i < availableSentences.size(); i++) {
			if (!pole.contains(availableSentences.get(i))) {
				boolean neighbour = true;
				int j = 0;
				while (neighbour && j < poleSize) {
					if (graphSim[availableSentences.get(i)][pole.get(j)] == 0)
						neighbour = false;
					j++;
				}
				if (neighbour)
					neighbours.add(new Pair<Integer, Double>(availableSentences.get(i), matSim[i][pole.get(j - 1)]));
			}
		}

		Collections.sort(neighbours);
		return neighbours;
	}

	private double addPole() {
		int k = listPole.size() + 1;
		int x = -1;
		double min = 1;
		for (int i = 0; i < availableSentences.size(); i++) {
			double value = 0;
			if (degree[i] > 0) {
				for (int m = 0; m < k - 1; m++) {
					int poleSize = listPole.get(m).size();
					for (int j = 0; j < poleSize; j++) {
						value += matSim[availableSentences.get(i)][listPole.get(m).get(j)];
					}
					value /= poleSize;
				}
				value /= k - 1;
				if (value < min) {
					min = value;
					x = availableSentences.get(i);
				}
			}
		}
		if (!(x < 0)) {
			List<Integer> pole = new ArrayList<Integer>();
			pole.add(x);
			buildPoleClique(pole);
			listPole.add(pole);
			sentenceInPoles.addAll(pole);
			availableSentences.removeAll(pole);
		}
		return min;
	}

	private void buildMemberShipU() {
		u = new double[listPole.size()][n];
		int l = listPole.size();
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < n; j++) {
				double value = 0;
				for (int k : listPole.get(i))
					value += matSim[j][k];
				value /= listPole.get(i).size();
				u[i][j] = value;
			}
		}
	}

	private int[] getSimilarPoles(int j) {
		List<Pair<Integer, Double>> listScorePole = new ArrayList<Pair<Integer, Double>>();
		for (int i = 0; i < listPole.size(); i++)
			listScorePole.add(new Pair<Integer, Double>(i, u[i][j]));
		Collections.sort(listScorePole);
		int[] similarPoles = new int[listPole.size()];
		for (int i = 0; i < listPole.size(); i++)
			similarPoles[i] = listScorePole.get(i).getKey();
		return similarPoles;
	}

	private void assign(int j) {
		int l = listPole.size();
		List<Integer> pole;
		int[] similarPoles = getSimilarPoles(j);
		boolean assign = true;
		int k = 0;
		while (assign && k < l) {
			pole = listPole.get(similarPoles[k]);
			if (!pole.contains(j)) {
				if (k == 0)
					pole.add(j);
				else if (k == l - 1) {
					if (u[similarPoles[k]][j] >= u[similarPoles[k - 1]][j] / 2)
						pole.add(j);
					else
						assign = false;
				} else {
					double test = (u[similarPoles[k - 1]][j] + u[similarPoles[k + 1]][j]) / 2;
					double value = u[similarPoles[k]][j];
					if (value >= test)
						pole.add(j);
					else
						assign = false;
				}
			}
			k++;
		}
	}

	private double getMaxMeanSim(int i, int j) {
		double valueI = 0;
		double valueJ = 0;
		for (int k = 0; k < n; k++) {
			valueI += matSim[i][k];
			valueJ += matSim[j][k];
		}
		valueI /= n;
		valueJ /= n;
		return Math.max(valueI, valueJ);
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}

	@Override
	public List<Cluster> getListCluster() {
		return listCluster;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(Cluster.class, List.class, ListClusterBasedIn.class));
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((ListClusterBasedIn) compMethod).setListCluster(listCluster);
	}
}
