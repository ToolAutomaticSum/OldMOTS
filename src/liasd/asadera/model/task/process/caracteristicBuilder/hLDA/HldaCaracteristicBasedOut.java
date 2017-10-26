package liasd.asadera.model.task.process.caracteristicBuilder.hLDA;

import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.hLDA.HierarchicalLDA.NCRPNode;
import liasd.asadera.textModeling.SentenceModel;

public interface HldaCaracteristicBasedOut {
	
	public Map<SentenceModel, double[]> getSentenceLevelDistribution();
	
	public Map<NCRPNode, double[]> getTopicWordDistribution();
}
