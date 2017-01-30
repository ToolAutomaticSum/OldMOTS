package model.task.scoringMethod.LDA;

import java.util.List;

import model.task.VectorCaracteristicBasedOut;
import textModeling.cluster.TopicLDA;

public interface TopicLdaBasedOut extends VectorCaracteristicBasedOut {

	public List<TopicLDA> getListTopicLda();
}
