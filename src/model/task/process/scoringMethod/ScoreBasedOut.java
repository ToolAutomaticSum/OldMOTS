package model.task.process.scoringMethod;

import java.util.TreeSet;

import tools.PairSentenceScore;

public interface ScoreBasedOut {

	public TreeSet<PairSentenceScore> getScore();
}
