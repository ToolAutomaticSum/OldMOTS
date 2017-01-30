package model.task.summarizeMethod;

import java.util.List;

import model.task.VectorCaracteristicBasedIn;
import textModeling.cluster.TopicLDA;

public interface TopicLdaBasedIn extends VectorCaracteristicBasedIn {
	public void setListTopicLda(List<TopicLDA> listTopic);
}
