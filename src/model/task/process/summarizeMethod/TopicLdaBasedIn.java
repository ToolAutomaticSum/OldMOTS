package model.task.process.summarizeMethod;

import java.util.List;

import model.task.process.VectorCaracteristicBasedIn;
import textModeling.cluster.TopicLDA;

public interface TopicLdaBasedIn extends VectorCaracteristicBasedIn {
	public void setListTopicLda(List<TopicLDA> listTopic);
}
