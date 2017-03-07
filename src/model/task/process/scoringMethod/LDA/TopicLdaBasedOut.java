package model.task.process.scoringMethod.LDA;

import java.util.List;

import model.task.process.VectorCaracteristicBasedOut;
import textModeling.cluster.TopicLDA;

public interface TopicLdaBasedOut extends VectorCaracteristicBasedOut {

	public List<TopicLDA> getListTopicLda();
}
