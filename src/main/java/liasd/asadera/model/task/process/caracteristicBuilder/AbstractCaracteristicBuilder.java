package main.java.liasd.asadera.model.task.process.caracteristicBuilder;

import java.util.HashMap;
import java.util.List;

import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;

public abstract class AbstractCaracteristicBuilder extends ParameterizedMethod {

	protected AbstractProcess currentProcess;

	public AbstractCaracteristicBuilder(int id) throws SupportADNException {
		super(id);
	}

	public abstract AbstractCaracteristicBuilder makeCopy() throws Exception;

	protected void initCopy(AbstractCaracteristicBuilder p) {
		p.setCurrentProcess(currentProcess);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}

	public void initADN() throws Exception {
	}

	public abstract void processCaracteristics(List<Corpus> listCorpus) throws Exception;

	public abstract void finish();/*
									 * { sentenceCaracteristic.clear(); }
									 */

	public void setCurrentProcess(AbstractProcess p) {
		currentProcess = p;
	}

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	/*
	 * @Override public Map<SentenceModel, T> getVectorCaracterisic() { return
	 * sentenceCaracteristic; }
	 */
}
