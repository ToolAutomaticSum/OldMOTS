package liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer;

import java.lang.reflect.Constructor;
import java.util.List;

import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.ReinforcementLearning;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.SentenceModel;

public abstract class Featurer extends ParametrizedMethod {
	
	protected ReinforcementLearning rl;
	
	public Featurer(ReinforcementLearning rl) throws SupportADNException {
		super(rl.getId());
		this.rl = rl;
	}
	
	public abstract void init(int maxLength) throws Exception;
	
	public abstract double[] getFeatures(List<SentenceModel> summary) throws Exception;
	
	public abstract double[] instanciateVector();

	public static Featurer instanciateFeaturer(ReinforcementLearning rl, /*ParametrizedMethod compatibleMethod,*/ String featurer) throws Exception {
		Class<?> cl;
		cl = Class.forName("liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer." + featurer);
	    @SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor(ReinforcementLearning.class);
	    Featurer o = (Featurer) ct.newInstance(rl);
//	    if (!o.isOutCompatible(compatibleMethod))
//	    	throw new RuntimeException("Error when trying compatibility between SimilaritryMetric and ParametrizedMethod.");
//	    o.setCompatibility(compatibleMethod);
	    return (Featurer) o;
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
	}

}
