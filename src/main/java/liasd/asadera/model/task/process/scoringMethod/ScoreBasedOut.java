package main.java.liasd.asadera.model.task.process.scoringMethod;

import java.util.Map;

import main.java.liasd.asadera.textModeling.SentenceModel;

public interface ScoreBasedOut {

	public Map<SentenceModel, Double> getScore();
}
