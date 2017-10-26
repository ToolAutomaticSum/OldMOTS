package liasd.asadera.model.task.process.caracteristicBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import liasd.asadera.model.task.process.AbstractProcess;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;

public abstract class AbstractCaracteristicBuilder extends ParametrizedMethod /*implements SentenceCaracteristicBasedOut<T> */{

	protected AbstractProcess currentProcess;
	//protected Map<SentenceModel, T> sentenceCaracteristic;
	
	public AbstractCaracteristicBuilder(int id) throws SupportADNException {
		super(id);
		
		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterOut = new ArrayList<ParametrizedType>();
	}

	public abstract AbstractCaracteristicBuilder makeCopy() throws Exception;
	
	protected void initCopy(AbstractCaracteristicBuilder p) {
		p.setCurrentProcess(currentProcess);
		//p.setIndex(index);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;
	
	public abstract void processCaracteristics(List<Corpus> listCorpus) throws Exception;
	
	public abstract void finish();/* {
		sentenceCaracteristic.clear();
	}*/
	
	public void setCurrentProcess(AbstractProcess p) {
		currentProcess = p;
	}
	
	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	/*@Override
	public Map<SentenceModel, T> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}*/
}
