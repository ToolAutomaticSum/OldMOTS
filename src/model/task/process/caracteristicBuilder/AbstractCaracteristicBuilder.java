package model.task.process.caracteristicBuilder;

import java.util.HashMap;
import java.util.Map;

import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.tempProcess.AbstractProcess;
import optimize.SupportADNException;
import textModeling.SentenceModel;

public abstract class AbstractCaracteristicBuilder<T> extends ParametrizedMethod implements SentenceCaracteristicBasedOut<T> {

	protected AbstractProcess currentProcess;
	protected Map<SentenceModel, T> sentenceCaracteristic;
	
	public AbstractCaracteristicBuilder(int id) throws SupportADNException {
		super(id);
	}

	public abstract AbstractCaracteristicBuilder<T> makeCopy() throws Exception;
	
	protected void initCopy(AbstractCaracteristicBuilder<T> p) {
		p.setCurrentProcess(currentProcess);
		//p.setIndex(index);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;
	
	public abstract void processCaracteristics();
	
	public void setCurrentProcess(AbstractProcess p) {
		currentProcess = p;
	}
	
	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	@Override
	public Map<SentenceModel, T> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
}
