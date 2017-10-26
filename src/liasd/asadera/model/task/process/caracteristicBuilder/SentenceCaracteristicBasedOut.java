package liasd.asadera.model.task.process.caracteristicBuilder;

import java.util.Map;

import liasd.asadera.textModeling.SentenceModel;

public interface SentenceCaracteristicBasedOut {
	public Map<SentenceModel, Object> getVectorCaracterisic();
}
