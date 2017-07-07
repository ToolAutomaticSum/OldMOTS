package model.task.process.comparativeMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import model.task.process.caracteristicBuilder.ListClusterBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import model.task.process.scoringMethod.ScoreBasedIn;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.cluster.Cluster;
import tools.Pair;
import tools.PairSentenceScore;

public class OneSourceCluster extends AbstractComparativeMethod implements ListClusterBasedIn, ScoreBasedIn {

	protected List<Cluster> listCluster;
	protected ArrayList<PairSentenceScore> score;
	
	public OneSourceCluster(int id) throws SupportADNException {
		super(id);
		
		listParameterIn.add(new ParametrizedType(Cluster.class, List.class, ListClusterBasedIn.class));
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
			Set<String> listClustLabel = new TreeSet<String>();
			
			while (oneSource && itSen.hasNext())
				for (String label : itSen.next().getLabels())
					oneSource = listClustLabel.add(label);
			
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
	
	public static double getScore(List<PairSentenceScore> score, SentenceModel sen) {
		double s = 0;
		boolean find = false;
		Iterator<PairSentenceScore> pairIt = score.iterator();
		while (!find && pairIt.hasNext()) {
			PairSentenceScore pair = pairIt.next();
			if (pair.getPhrase().equals(sen)) {
				find = true;
				s = pair.getScore();
			}
		}
		return s;
	}

	@Override
	public void setListCluster(List<Cluster> listCluster) {
		this.listCluster = listCluster;
	}

	@Override
	public void setScore(ArrayList<PairSentenceScore> score) {this.score = score;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
	}
}
