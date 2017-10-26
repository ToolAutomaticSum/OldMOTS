package liasd.asadera.model.task.process.processCompatibility;

import java.util.ArrayList;
import java.util.List;

import liasd.asadera.optimize.Individu;
import liasd.asadera.optimize.SupportADNException;

public abstract class ParametrizedMethod extends Individu {
	
	protected List<ParametrizedType> listParameterIn;
	protected List<ParametrizedType> listParameterOut;
	
	public ParametrizedMethod(int id) throws SupportADNException {
		super(id);
		listParameterIn = new ArrayList<ParametrizedType>();
		listParameterOut = new ArrayList<ParametrizedType>();
	}
	
	public ParametrizedMethod(int id, List<ParametrizedType> in, List<ParametrizedType> out) throws SupportADNException {
		super(id);
		listParameterIn = in;
		listParameterOut = out;
	}

	public List<ParametrizedType> getParameterTypeIn() {
		return listParameterIn;
	}
	
	public List<ParametrizedType> getParameterTypeOut() {
		return listParameterOut;
	}

	/**
	 * Test si les paramètres en sortie de this sont compatibles avec les paramètres en entrée de compatibleMethod
	 * @param compatibleMethod
	 * @return
	 */
	public abstract boolean isOutCompatible(ParametrizedMethod compatibleMethod);
	
	public abstract void setCompatibility(ParametrizedMethod compMethod);
}
