package model.task.process.old;

import java.util.List;

import textModeling.cluster.TopicLDA;

public interface TopicLdaBasedIn extends VectorCaracteristicBasedIn {
	public void setListTopicLda(List<TopicLDA> listTopic);
}
