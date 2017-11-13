package liasd.asadera.model.task.process.selectionMethod;

import java.util.HashMap;
import java.util.List;

import liasd.asadera.model.task.process.AbstractProcess;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;

public abstract class AbstractSelectionMethod extends ParametrizedMethod {

	protected AbstractProcess currentProcess;

	public AbstractSelectionMethod(int id) throws SupportADNException {
		super(id);
	}

	public abstract AbstractSelectionMethod makeCopy() throws Exception;
	
	//TODO virer initCopy et mettre dans makeCopy without abstract;
	protected void initCopy(AbstractSelectionMethod p) {
		p.setCurrentProcess(currentProcess);
		if (supportADN != null)
			p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;
	
	public abstract List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception;

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
	
	@Override
	public abstract boolean isOutCompatible(ParametrizedMethod compatibleMethod);

	@Override
	public abstract void setCompatibility(ParametrizedMethod compMethod);
}
