package main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer;

import java.lang.reflect.Constructor;
import java.util.List;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.ReinforcementLearning;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;

public abstract class Featurer extends ParameterizedMethod {

	protected ReinforcementLearning rl;

	public Featurer(ReinforcementLearning rl) throws SupportADNException {
		super(rl.getId());
		this.rl = rl;
	}

	public abstract void init(int maxLength) throws Exception;

	public abstract double[] getFeatures(List<SentenceModel> summary) throws Exception;

	public abstract double[] instanciateVector();

	public static Featurer instanciateFeaturer(ReinforcementLearning rl,
			/* ParametrizedMethod compatibleMethod, */ String featurer) throws Exception {
		Class<?> cl;
		cl = Class
				.forName("main.java.liasd.asadera.model.task.process.selectionMethod.reinforcementLearning.featurer." + featurer);
		@SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor(ReinforcementLearning.class);
		Featurer o = (Featurer) ct.newInstance(rl);
		return (Featurer) o;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
	}

}
