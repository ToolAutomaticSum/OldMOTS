package liasd.asadera.model.task.process.indexBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import liasd.asadera.model.task.process.AbstractProcess;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;

public abstract class AbstractIndexBuilder<T extends WordIndex> extends ParametrizedMethod implements IndexBasedOut<T> {

	private AbstractProcess currentProcess;
	protected final Index<T> index;
	
	public AbstractIndexBuilder(int id) throws SupportADNException {
		super(id);
		index = new Index<T>();
	}
	
	public AbstractIndexBuilder(int id, List<ParametrizedType> in, List<ParametrizedType> out) throws SupportADNException {
		super(id, in, out);
		index = new Index<T>();
		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterOut = new ArrayList<ParametrizedType>();
	}

	public abstract AbstractIndexBuilder<T> makeCopy() throws Exception;
	
	protected void initCopy(AbstractIndexBuilder<T> p) {
		p.setCurrentProcess(currentProcess);
		//p.setIndex(index);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;

	public abstract void processIndex(List<Corpus> listCorpus) throws Exception;
	
	public void finish() {
		index.clear();
	}
	
	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}

	public final Index<T> getIndex() {
		if (index == null)
			throw new NullPointerException("Index is null, use processIndex() first.");
		return index;
	}
}
