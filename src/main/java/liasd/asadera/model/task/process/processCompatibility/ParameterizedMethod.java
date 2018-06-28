package main.java.liasd.asadera.model.task.process.processCompatibility;

import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.optimize.Individu;
import main.java.liasd.asadera.optimize.SupportADNException;

public abstract class ParameterizedMethod extends Individu {

	protected List<ParameterizedType> listParameterIn;
	protected List<ParameterizedType> listParameterOut;
	protected List<ParameterizedMethod> listSubMethod;

	public ParameterizedMethod(int id) throws SupportADNException {
		super(id);
		
		listParameterIn = new ArrayList<ParameterizedType>();
		listParameterOut = new ArrayList<ParameterizedType>();
		listSubMethod = new ArrayList<ParameterizedMethod>();
	}

	public ParameterizedMethod(int id, List<ParameterizedType> in, List<ParameterizedType> out)
			throws SupportADNException {
		super(id);
		
		listParameterIn = in;
		listParameterOut = out;
		listSubMethod = new ArrayList<ParameterizedMethod>();
	}

	public List<ParameterizedType> getParameterTypeIn() {
		return listParameterIn;
	}

	public List<ParameterizedType> getParameterTypeOut() {
		return listParameterOut;
	}

	public List<ParameterizedMethod> getSubMethod() {
		return listSubMethod;
	}

	/**
	 * @param compatibleMethod
	 * @return
	 */
	public abstract boolean isOutCompatible(ParameterizedMethod compatibleMethod);

	public abstract void setCompatibility(ParameterizedMethod compMethod);
}
