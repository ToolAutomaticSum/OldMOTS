package model.task.process.caracteristicBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.task.process.AbstractProcess;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;

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
	
	public abstract void processCaracteristics(List<Corpus> listCorpus);
	
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
