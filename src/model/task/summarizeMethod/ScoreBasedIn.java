package model.task.summarizeMethod;

import java.util.TreeSet;

import tools.PairSentenceScore;

public interface ScoreBasedIn {

	public void setScore(TreeSet<PairSentenceScore> score);
}
