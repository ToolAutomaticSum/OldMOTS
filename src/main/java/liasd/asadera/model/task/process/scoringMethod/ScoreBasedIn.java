package main.java.liasd.asadera.model.task.process.scoringMethod;

import java.util.Map;

import main.java.liasd.asadera.textModeling.SentenceModel;

public interface ScoreBasedIn {

	public void setScore(Map<SentenceModel, Double> score);
}
