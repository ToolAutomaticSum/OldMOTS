package main.java.liasd.asadera.model.task.process.selectionMethod;

import java.util.HashMap;
import java.util.List;

import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;

public abstract class AbstractSelectionMethod extends ParameterizedMethod {

	protected AbstractProcess currentProcess;

	public AbstractSelectionMethod(int id) throws SupportADNException {
		super(id);
	}

	public abstract AbstractSelectionMethod makeCopy() throws Exception;

	protected void initCopy(AbstractSelectionMethod p) {
		p.setCurrentProcess(currentProcess);
		if (supportADN != null)
			p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}

	public void initADN() throws Exception {
	}

	public abstract List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception;

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}

	@Override
	public abstract boolean isOutCompatible(ParameterizedMethod compatibleMethod);

	@Override
	public abstract void setCompatibility(ParameterizedMethod compMethod);
}
