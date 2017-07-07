package model.task.process.old;

import java.util.List;

import textModeling.cluster.TopicLDA;

public interface TopicLdaBasedOut extends VectorCaracteristicBasedOut {

	public List<TopicLDA> getListTopicLda();
}
