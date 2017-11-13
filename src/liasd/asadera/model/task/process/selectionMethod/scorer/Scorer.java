package liasd.asadera.model.task.process.selectionMethod.scorer;

import java.lang.reflect.Constructor;

import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Summary;

public abstract class Scorer extends ParametrizedMethod {
	
	protected AbstractSelectionMethod method;
	
	public Scorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method.getId());
		this.method = method;
	}
	
	public abstract void init() throws Exception;
	
	public abstract double getScore(Summary summary) throws Exception;
	

	public static Scorer instanciateScorer(AbstractSelectionMethod method, /*ParametrizedMethod compatibleMethod,*/ String scorer) throws Exception {
		Class<?> cl;
		cl = Class.forName("liasd.asadera.model.task.process.selectionMethod.scorer." + scorer);
	    @SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor(AbstractSelectionMethod.class);
	    Scorer o = (Scorer) ct.newInstance(method);
//	    if (!o.isOutCompatible(compatibleMethod))
//	    	throw new RuntimeException("Error when trying compatibility between SimilaritryMetric and ParametrizedMethod.");
//	    o.setCompatibility(compatibleMethod);
	    return (Scorer) o;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
	}

}
