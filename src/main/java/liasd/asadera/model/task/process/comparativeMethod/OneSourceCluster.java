package main.java.liasd.asadera.model.task.process.comparativeMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.ListClusterBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.cluster.Cluster;
import main.java.liasd.asadera.tools.Pair;

public class OneSourceCluster extends AbstractComparativeMethod implements ListClusterBasedIn, ScoreBasedIn {

	protected List<Cluster> listCluster;
	protected Map<SentenceModel, Double> score;

	public OneSourceCluster(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(Cluster.class, List.class, ListClusterBasedIn.class));
	}

	@Override
	public AbstractComparativeMethod makeCopy() throws Exception {
		return null;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public List<Pair<SentenceModel, String>> calculateDifference(List<Corpus> listCorpus) throws Exception {
		List<Pair<SentenceModel, String>> listInfo = new ArrayList<Pair<SentenceModel, String>>();
		List<Pair<SentenceModel, Double>> listScore = new ArrayList<Pair<SentenceModel, Double>>();
		for (Cluster clust : listCluster) {
			Iterator<SentenceModel> itSen = clust.iterator();
			boolean oneSource = true;
			Set<String> setClustLabel = new TreeSet<String>();

			while (oneSource && itSen.hasNext())
				for (String label : itSen.next().getLabels())
					oneSource = setClustLabel.add(label); // return false if label already in the set

			if (oneSource) {
				for (SentenceModel sen : clust)
					listScore.add(new Pair<SentenceModel, Double>(sen, getScore(score, sen)));
				Collections.sort(listScore);
				listInfo.add(new Pair<SentenceModel, String>(listScore.get(0).getKey(), "LACK_OF_INFORMATION"));
				listScore.clear();
			}
		}
		return listInfo;
	}

	@Override
	public void finish() {
	}

	public static double getScore(Map<SentenceModel, Double> score, SentenceModel sen) {
		double s = 0;
		boolean find = false;
		Iterator<SentenceModel> pairIt = score.keySet().iterator();
		while (!find && pairIt.hasNext()) {
			SentenceModel pair = pairIt.next();
			if (pair.equals(sen)) {
				find = true;
				s = score.get(pair);
			}
		}
		return s;
	}

	@Override
	public void setListCluster(List<Cluster> listCluster) {
		this.listCluster = listCluster;
	}

	@Override
	public void setScore(Map<SentenceModel, Double> score) {
		this.score = score;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}
}
